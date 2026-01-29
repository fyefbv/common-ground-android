package com.example.common_ground_android.network.model.request.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
)

@Serializable
data class LoginRequest(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
)

@Serializable
data class SelectProfileRequest(
    @SerialName("profile_id")
    val profileId: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("token")
    val token: String
)