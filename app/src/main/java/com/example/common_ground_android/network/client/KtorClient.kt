package com.example.common_ground_android.network.client

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.response.ApiErrorResponse
import io.ktor.client.*
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
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

@OptIn(InternalAPI::class)
class KtorClient(private val tokenManager: TokenManager) {

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
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(ResponseObserver) {
            onResponse { response ->
                println("HTTP status: ${response.status.value}")
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT
            connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
            socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val token = runBlocking { tokenManager.getAccessTokenSync() }
                    BearerTokens(accessToken = token ?: "", refreshToken = "")
                }
                refreshTokens {
                    val refreshToken = runBlocking { tokenManager.getRefreshTokenSync() }
                    if (refreshToken != null) {
                        try {
                            // TODO: Реализовать обновление токенов
                            // val newTokens = refreshTokens(refreshToken)
                            // tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                            val token = runBlocking { tokenManager.getAccessTokenSync() }
                            BearerTokens(accessToken = token ?: "", refreshToken = refreshToken)
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }
                }
                sendWithoutRequest { request ->
                    !request.url.encodedPath.contains("/auth/")
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
            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException
                val serverException = exception as? ServerResponseException

                when {
                    clientException != null -> {
                        val status = clientException.response.status
                        val errorBody = try {
                            clientException.response.bodyAsText()
                        } catch (e: Exception) {
                            "Не удалось прочитать тело ошибки"
                        }

                        throw NetworkException.ClientError(
                            status = status,
                            message = "HTTP $status: $errorBody",
                            cause = exception
                        )
                    }

                    serverException != null -> {
                        val status = serverException.response.status
                        val errorBody = try {
                            serverException.response.bodyAsText()
                        } catch (e: Exception) {
                            "Не удалось прочитать тело ошибки"
                        }

                        throw NetworkException.ServerError(
                            status = status,
                            message = "HTTP $status: $errorBody",
                            cause = exception
                        )
                    }

                    exception is HttpRequestTimeoutException -> {
                        throw NetworkException.TimeoutError(
                            message = "Таймаут запроса",
                            cause = exception
                        )
                    }

                    exception is ConnectTimeoutException -> {
                        throw NetworkException.ConnectionError(
                            message = "Таймаут подключения",
                            cause = exception
                        )
                    }

                    else -> throw NetworkException.UnknownError(
                        message = exception.message ?: "Неизвестная ошибка сети",
                        cause = exception
                    )
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
            logger = Logger.DEFAULT
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
    cause: Throwable? = null
) : Exception(message, cause) {

    data class ClientError(
        val status: HttpStatusCode,
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

    data class ServerError(
        val status: HttpStatusCode,
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkException(message, cause)

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