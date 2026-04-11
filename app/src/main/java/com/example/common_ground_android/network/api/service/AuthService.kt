package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.auth.*
import com.example.common_ground_android.network.model.response.auth.AuthTokensResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.*
import io.ktor.http.*

class AuthService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun register(email: String, password: String): AuthTokensResponse {
        return client.post {
            attributes.put(AuthCircuitBreaker, Unit)
            url(ApiConfig.Endpoints.AUTH_REGISTER)
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password))
        }.body()
    }

    suspend fun login(email: String, password: String): AuthTokensResponse {
        return client.post {
            attributes.put(AuthCircuitBreaker, Unit)
            url(ApiConfig.Endpoints.AUTH_LOGIN)
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun selectProfile(profileId: String): AuthTokensResponse {
        return client.post {
            url(ApiConfig.Endpoints.AUTH_SELECT_PROFILE)
            contentType(ContentType.Application.Json)
            setBody(SelectProfileRequest(profileId))
        }.body()
    }

    suspend fun refreshToken(refreshToken: String): AuthTokensResponse {
        return client.post {
            attributes.put(AuthCircuitBreaker, Unit)
            url(ApiConfig.Endpoints.AUTH_REFRESH)
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(refreshToken))
        }.body()
    }
}