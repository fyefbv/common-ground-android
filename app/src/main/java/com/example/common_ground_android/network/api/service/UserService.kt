package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.UpdateUserRequest
import com.example.common_ground_android.network.model.response.UserResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class UserService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun getCurrentUser(): UserResponse {
        return client.get {
            url(ApiConfig.Endpoints.USERS_ME)
        }.body()
    }

    suspend fun updateUser(email: String? = null, password: String? = null): UserResponse {
        return client.patch {
            url(ApiConfig.Endpoints.USERS_ME)
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(email, password))
        }.body()
    }

    suspend fun deleteUser(): String {
        return client.delete {
            url(ApiConfig.Endpoints.USERS_ME)
        }.body()
    }
}