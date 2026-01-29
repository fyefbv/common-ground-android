package com.example.common_ground_android.network.model.response.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("primary_interest_id")
    val primaryInterestId: String?,

    @SerialName("creator_id")
    val creatorId: String,

    @SerialName("tags")
    val tags: List<String>,

    @SerialName("max_participants")
    val maxParticipants: Int,

    @SerialName("is_private")
    val isPrivate: Boolean,

    @SerialName("participants_count")
    val participantsCount: Int,

    @SerialName("messages_count")
    val messagesCount: Int,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("is_joined")
    val isJoined: Boolean
)

@Serializable
data class ParticipantResponse(
    @SerialName("profile_id")
    val profileId: String,

    @SerialName("room_id")
    val roomId: String,

    @SerialName("role")
    val role: String,

    @SerialName("joined_at")
    val joinedAt: String,

    @SerialName("is_online")
    val isOnline: Boolean,

    @SerialName("is_muted")
    val isMuted: Boolean,

    @SerialName("is_banned")
    val isBanned: Boolean
)

@Serializable
data class MessageResponse(
    @SerialName("id")
    val id: String,

    @SerialName("room_id")
    val roomId: String,

    @SerialName("sender_id")
    val senderId: String,

    @SerialName("content")
    val content: String,

    @SerialName("parent_message_id")
    val parentMessageId: String?,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("is_edited")
    val isEdited: Boolean,

    @SerialName("is_deleted")
    val isDeleted: Boolean
)

@Serializable
data class MessagesResponse(
    @SerialName("messages")
    val messages: List<MessageResponse>,

    @SerialName("total")
    val total: Int,

    @SerialName("has_more")
    val hasMore: Boolean
)

@Serializable
data class DeleteResponse(
    @SerialName("detail")
    val detail: String
)

@Serializable
data class ParticipantsListResponse(
    val participants: List<ParticipantResponse>
)