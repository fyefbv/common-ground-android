package com.example.common_ground_android.network.model.request.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("primary_interest_id")
    val primaryInterestId: String? = null,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("max_participants")
    val maxParticipants: Int = 50,

    @SerialName("is_private")
    val isPrivate: Boolean = false
)

@Serializable
data class UpdateRoomRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("tags")
    val tags: List<String>? = null,

    @SerialName("max_participants")
    val maxParticipants: Int? = null,

    @SerialName("is_private")
    val isPrivate: Boolean? = null
)

@Serializable
data class KickParticipantRequest(
    @SerialName("profile_id")
    val profileId: String,

    @SerialName("reason")
    val reason: String? = null
)

@Serializable
data class MuteParticipantRequest(
    @SerialName("participant_id")
    val participantId: String
)

@Serializable
data class BanParticipantRequest(
    @SerialName("participant_id")
    val participantId: String
)

@Serializable
data class ChangeRoleRequest(
    @SerialName("target_profile_id")
    val targetProfileId: String,

    @SerialName("new_role")
    val newRole: String
)

@Serializable
data class SendMessageRequest(
    @SerialName("content")
    val content: String,

    @SerialName("parent_message_id")
    val parentMessageId: String? = null
)

@Serializable
data class UpdateMessageRequest(
    @SerialName("content")
    val content: String
)

data class RoomFilter(
    val query: String? = null,
    val interestId: String? = null,
    val tags: List<String>? = null,
    val limit: Int = 50,
    val offset: Int = 0
)