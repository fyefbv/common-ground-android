package com.example.common_ground_android.network.model.request.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    @SerialName("email")
    val email: String? = null,

    @SerialName("password")
    val password: String? = null
)