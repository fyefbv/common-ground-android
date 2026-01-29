package com.example.common_ground_android.network.model.request.interest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterestFilterRequest(
    @SerialName("language")
    val language: String? = null
)