package com.example.common_ground_android.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("username")
    val username: String,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("reputation_score")
    val reputationScore: Float,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class AvatarResponse(
    @SerialName("avatar_url")
    val avatarUrl: String
)

@Serializable
data class DeleteProfileResponse(
    @SerialName("detail")
    val detail: String
)

@Serializable
data class ProfileStatisticsResponse(
    @SerialName("total_sessions")
    val totalSessions: Int,

    @SerialName("reputation_score")
    val reputationScore: Float,

    @SerialName("total_rooms")
    val totalRooms: Int
)
