package com.example.common_ground_android.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StartSearchRequest(
    @SerialName("priority_interest_ids")
    val priorityInterestIds: List<String> = emptyList()
)

@Serializable
data class RatePartnerRequest(
    @SerialName("rating")
    val rating: Int,

    @SerialName("feedback")
    val feedback: String? = null
)

@Serializable
data class ReportPartnerRequest(
    @SerialName("reason")
    val reason: String? = null,

    @SerialName("details")
    val details: String? = null
)

@Serializable
data class EndSessionRequest(
    @SerialName("reason")
    val reason: String? = null
)

@Serializable
data class SendChatRouletteMessageRequest(
    @SerialName("content")
    val content: String
)