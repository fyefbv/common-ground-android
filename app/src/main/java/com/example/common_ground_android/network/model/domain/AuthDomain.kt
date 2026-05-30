package com.example.common_ground_android.network.model.domain

import com.example.common_ground_android.network.model.response.UserResponse
import com.example.common_ground_android.utils.DateUtils
import java.util.Date

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String
)

data class User(
    val id: String,
    val email: String,
    val createdAt: Date,
    val updatedAt: Date
) {
    companion object {
        fun fromResponse(response: UserResponse): User {
            return User(
                id = response.id,
                email = response.email,
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                updatedAt = DateUtils.parseIsoDate(response.updatedAt)
            )
        }
    }
}