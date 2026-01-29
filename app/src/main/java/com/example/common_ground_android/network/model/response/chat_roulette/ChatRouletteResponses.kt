package com.example.common_ground_android.network.model.response.chat_roulette

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.example.common_ground_android.network.model.response.profile.ProfileResponse

@Serializable
data class ChatRouletteSessionResponse(
    @SerialName("id")
    val id: String,

    @SerialName("profile1_id")
    val profile1Id: String,

    @SerialName("profile2_id")
    val profile2Id: String,

    @SerialName("matched_interest_id")
    val matchedInterestId: String?,

    @SerialName("status")
    val status: String,

    @SerialName("duration_minutes")
    val durationMinutes: Int,

    @SerialName("extension_minutes")
    val extensionMinutes: Int? = null,

    @SerialName("started_at")
    val startedAt: String,

    @SerialName("expires_at")
    val expiresAt: String,

    @SerialName("ended_at")
    val endedAt: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("matched_profile")
    val matchedProfile: ProfileResponse? = null,

    @SerialName("common_interests")
    val commonInterests: List<String> = emptyList(),

    @SerialName("time_remaining")
    val timeRemaining: Int? = null,

    @SerialName("extension_approved_by_profile1")
    val extensionApprovedByProfile1: Boolean = false,

    @SerialName("extension_approved_by_profile2")
    val extensionApprovedByProfile2: Boolean = false
)

@Serializable
data class SearchResponse(
    @SerialName("session")
    val session: ChatRouletteSessionResponse? = null,

    @SerialName("immediate_match")
    val immediateMatch: Boolean,

    @SerialName("search_id")
    val searchId: String? = null
)

@Serializable
data class SessionExtensionResponse(
    @SerialName("session_id")
    val sessionId: String,

    @SerialName("extended_minutes")
    val extendedMinutes: Int,

    @SerialName("new_expires_at")
    val newExpiresAt: String
)

@Serializable
data class ChatRouletteStatisticsResponse(
    @SerialName("total_sessions")
    val totalSessions: Int,

    @SerialName("completed_sessions")
    val completedSessions: Int,

    @SerialName("average_rating")
    val averageRating: Float,

    @SerialName("completion_rate")
    val completionRate: Float
)

@Serializable
data class ChatRouletteMessageResponse(
    @SerialName("session_id")
    val sessionId: String,

    @SerialName("sender_id")
    val senderId: String,

    @SerialName("content")
    val content: String,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class DeleteResponse(
    @SerialName("detail")
    val detail: String
)