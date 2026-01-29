package com.example.common_ground_android.network.model.response.interest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterestResponse(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String
)