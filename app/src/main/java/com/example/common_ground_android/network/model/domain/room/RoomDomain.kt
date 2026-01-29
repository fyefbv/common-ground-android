package com.example.common_ground_android.network.model.domain.room

import com.example.common_ground_android.network.model.response.room.MessageResponse
import com.example.common_ground_android.network.model.response.room.ParticipantResponse
import com.example.common_ground_android.network.model.response.room.RoomResponse
import com.example.common_ground_android.network.utils.DateUtils
import java.util.Date

enum class ParticipantRole {
    CREATOR,
    MODERATOR,
    MEMBER;

    companion object {
        fun fromString(role: String): ParticipantRole {
            return when (role.uppercase()) {
                "CREATOR" -> CREATOR
                "MODERATOR" -> MODERATOR
                else -> MEMBER
            }
        }
    }
}

data class Room(
    val id: String,
    val name: String,
    val description: String?,
    val primaryInterestId: String?,
    val creatorId: String,
    val tags: List<String>,
    val maxParticipants: Int,
    val isPrivate: Boolean,
    val participantsCount: Int,
    val messagesCount: Int,
    val createdAt: Date,
    val updatedAt: Date,
    val isJoined: Boolean
) {
    companion object {
        fun fromResponse(response: RoomResponse): Room {
            return Room(
                id = response.id,
                name = response.name,
                description = response.description,
                primaryInterestId = response.primaryInterestId,
                creatorId = response.creatorId,
                tags = response.tags,
                maxParticipants = response.maxParticipants,
                isPrivate = response.isPrivate,
                participantsCount = response.participantsCount,
                messagesCount = response.messagesCount,
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                updatedAt = DateUtils.parseIsoDate(response.updatedAt),
                isJoined = response.isJoined
            )
        }

        fun fromResponses(responses: List<RoomResponse>): List<Room> {
            return responses.map { fromResponse(it) }
        }
    }
}

data class Participant(
    val profileId: String,
    val roomId: String,
    val role: ParticipantRole,
    val joinedAt: Date,
    val isOnline: Boolean,
    val isMuted: Boolean,
    val isBanned: Boolean
) {
    companion object {
        fun fromResponse(response: ParticipantResponse): Participant {
            return Participant(
                profileId = response.profileId,
                roomId = response.roomId,
                role = ParticipantRole.fromString(response.role),
                joinedAt = DateUtils.parseIsoDate(response.joinedAt),
                isOnline = response.isOnline,
                isMuted = response.isMuted,
                isBanned = response.isBanned
            )
        }

        fun fromResponses(responses: List<ParticipantResponse>): List<Participant> {
            return responses.map { fromResponse(it) }
        }
    }
}

data class Message(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val parentMessageId: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val isEdited: Boolean,
    val isDeleted: Boolean
) {
    companion object {
        fun fromResponse(response: MessageResponse): Message {
            return Message(
                id = response.id,
                roomId = response.roomId,
                senderId = response.senderId,
                content = response.content,
                parentMessageId = response.parentMessageId,
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                updatedAt = DateUtils.parseIsoDate(response.updatedAt),
                isEdited = response.isEdited,
                isDeleted = response.isDeleted
            )
        }

        fun fromResponses(responses: List<MessageResponse>): List<Message> {
            return responses.map { fromResponse(it) }
        }
    }
}