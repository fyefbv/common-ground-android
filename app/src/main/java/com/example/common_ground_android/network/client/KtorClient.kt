package com.example.common_ground_android.network.client

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.auth.RefreshTokenRequest
import com.example.common_ground_android.network.model.response.ApiErrorResponse
import com.example.common_ground_android.network.model.response.ValidationErrorDetail
import com.example.common_ground_android.network.model.response.auth.AuthTokensResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.android.*
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
class KtorClient(private val tokenManager: TokenManager) {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun performTokenRefresh(refreshToken: String): AuthTokensResponse? {
        return try {
            httpClient.post {
                attributes.put(AuthCircuitBreaker, Unit)
                url(ApiConfig.Endpoints.AUTH_REFRESH)
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("HTTP: $message")
                }
            }
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                println("HTTP status: ${response.status.value}")
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT.inWholeMilliseconds
            connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT.inWholeMilliseconds
            socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT.inWholeMilliseconds
        }

        install(Auth) {
            bearer {
                cacheTokens = false
                loadTokens {
                    val token = runBlocking { tokenManager.getAccessTokenSync() }
                    BearerTokens(accessToken = token ?: "", refreshToken = "")
                }
                refreshTokens {
                    val refreshToken = runBlocking { tokenManager.getRefreshTokenSync() }
                    if (refreshToken == null) return@refreshTokens null
                    val response = runBlocking { this@KtorClient.performTokenRefresh(refreshToken) }
                    if (response == null) {
                        runBlocking { tokenManager.clearTokens() }
                        return@refreshTokens null
                    }
                    runBlocking { tokenManager.saveTokens(response.accessToken, response.refreshToken) }
                    BearerTokens(accessToken = response.accessToken, refreshToken = response.refreshToken)
                }
                sendWithoutRequest { request ->
                    !request.attributes.contains(AuthCircuitBreaker)
                }
            }
        }

        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, ApiConfig.DEFAULT_LANGUAGE)
        }

        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status

                if (!statusCode.isSuccess()) {
                    val errorBody = try {
                        response.bodyAsText()
                    } catch (e: Exception) {
                        "Не удалось прочитать тело ошибки"
                    }

                    val errorResponse = try {
                        json.decodeFromString<ApiErrorResponse>(errorBody)
                    } catch (e: Exception) {
                        null
                    }

                    val errorCode = errorResponse?.error?.code
                    val errorMessage = errorResponse?.error?.message ?: errorBody
                    val errorDetails = errorResponse?.error?.details

                    when (statusCode.value) {
                        in 400..499 -> throw NetworkException.ClientError(
                            status = statusCode,
                            message = errorMessage,
                            errorCode = errorCode,
                            details = errorDetails
                        )
                        in 500..599 -> throw NetworkException.ServerError(
                            status = statusCode,
                            message = errorMessage,
                            errorCode = errorCode
                        )
                        else -> throw NetworkException.UnknownError(
                            message = errorMessage
                        )
                    }
                }
            }

            handleResponseExceptionWithRequest { exception, request ->
                when (exception) {
                    is HttpRequestTimeoutException -> {
                        throw NetworkException.TimeoutError(
                            message = "Таймаут запроса",
                            cause = exception
                        )
                    }
                    is ConnectTimeoutException -> {
                        throw NetworkException.ConnectionError(
                            message = "Таймаут подключения",
                            cause = exception
                        )
                    }
                    is ClientRequestException, is ServerResponseException -> {
                        throw exception
                    }
                    else -> throw exception
                }
            }
        }
    }

    val webSocketClient = HttpClient(Android) {
        install(WebSockets) {
            pingInterval = ApiConfig.WS_PING_INTERVAL
            maxFrameSize = Long.MAX_VALUE
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("WS: $message")
                }
            }
            level = LogLevel.INFO
        }
    }

    suspend fun createRoomWebSocketUrl(roomId: String): String {
        val token = tokenManager.getAccessTokenSync()
        return "${ApiConfig.WS_BASE_URL}${ApiConfig.Endpoints.WS_ROOMS.replace("{room_id}", roomId)}?token=$token"
    }

    suspend fun createChatRouletteWebSocketUrl(sessionId: String): String {
        val token = tokenManager.getAccessTokenSync()
        return "${ApiConfig.WS_BASE_URL}${ApiConfig.Endpoints.WS_CHAT_ROULETTE.replace("{session_id}", sessionId)}?token=$token"
    }

    fun close() {
        httpClient.close()
        webSocketClient.close()
    }
}

sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
    open val errorCode: String? = null
) : Exception(message, cause) {

    data class ClientError(
        val status: HttpStatusCode,
        override val message: String,
        override val cause: Throwable? = null,
        override val errorCode: String? = null,
        val details: List<ValidationErrorDetail>? = null
    ) : NetworkException(message, cause, errorCode)

    data class ServerError(
        val status: HttpStatusCode,
        override val message: String,
        override val cause: Throwable? = null,
        override val errorCode: String? = null
    ) : NetworkException(message, cause, errorCode)

    data class TimeoutError(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    data class ConnectionError(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)
}

fun String.replacePathParams(vararg params: Pair<String, String>): String {
    var result = this
    params.forEach { (key, value) ->
        result = result.replace("{$key}", value)
    }
    return result
}