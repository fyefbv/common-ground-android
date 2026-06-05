package com.example.common_ground_android.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRouletteSessionResponse(
    @SerialName("id")
    val id: String,

    @SerialName("profile1_id")
    val profile1Id: String?,

    @SerialName("profile2_id")
    val profile2Id: String?,

    @SerialName("matched_interest_id")
    val matchedInterestId: String?,

    @SerialName("status")
    val status: String,

    @SerialName("duration_minutes")
    val durationMinutes: Int,

    @SerialName("extension_minutes")
    val extensionMinutes: Int?,

    @SerialName("started_at")
    val startedAt: String?,

    @SerialName("expires_at")
    val expiresAt: String?,

    @SerialName("ended_at")
    val endedAt: String?,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("matched_profile")
    val matchedProfile: MatchedProfileResponse? = null,

    @SerialName("common_interests")
    val commonInterests: List<String>? = null,

    @SerialName("time_remaining")
    val timeRemaining: Int? = null,

    @SerialName("extension_approved_by_profile1")
    val extensionApprovedByProfile1: Boolean = false,

    @SerialName("extension_approved_by_profile2")
    val extensionApprovedByProfile2: Boolean = false,

    @SerialName("partner_online")
    val partnerOnline: Boolean = false
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
data class ChatRouletteMessageResponse(
    @SerialName("session_id")
    val sessionId: String,

    @SerialName("sender_id")
    val senderId: String?,

    @SerialName("content")
    val content: String,

    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class DeleteChatRouletteResponse(
    @SerialName("detail")
    val detail: String
)

@Serializable
data class MatchedProfileResponse(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String,
    @SerialName("bio") val bio: String? = null,
    @SerialName("reputation_score") val reputationScore: Float,
    @SerialName("avatar_url") val avatarUrl: String? = null
)