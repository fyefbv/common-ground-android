package com.example.common_ground_android.network.model.websocket.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class RoomWebSocketClientEvent {
    @Serializable
    @SerialName("send_message")
    data class SendMessage(
        @SerialName("content")
        val content: String,
        @SerialName("parent_message_id")
        val parentMessageId: String? = null
    ) : RoomWebSocketClientEvent()

    @Serializable
    @SerialName("typing_started")
    object TypingStarted : RoomWebSocketClientEvent()

    @Serializable
    @SerialName("typing_stopped")
    object TypingStopped : RoomWebSocketClientEvent()

    @Serializable
    @SerialName("ping")
    object Ping : RoomWebSocketClientEvent()
}

sealed class RoomWebSocketServerEvent {
    @Serializable
    @SerialName("connection_established")
    data class ConnectionEstablished(
        @SerialName("data")
        val data: ConnectionEstablishedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_sent")
    data class MessageSent(
        @SerialName("data")
        val data: MessageSentData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_updated")
    data class MessageUpdated(
        @SerialName("data")
        val data: MessageUpdatedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_deleted")
    data class MessageDeleted(
        @SerialName("data")
        val data: MessageDeletedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_joined")
    data class ParticipantJoined(
        @SerialName("data")
        val data: ParticipantJoinedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_left")
    data class ParticipantLeft(
        @SerialName("data")
        val data: ParticipantLeftData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_kicked")
    data class ParticipantKicked(
        @SerialName("data")
        val data: ParticipantKickedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_banned")
    data class ParticipantBanned(
        @SerialName("data")
        val data: ParticipantBannedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_unbanned")
    data class ParticipantUnbanned(
        @SerialName("data")
        val data: ParticipantUnbannedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_role_changed")
    data class ParticipantRoleChanged(
        @SerialName("data")
        val data: ParticipantRoleChangedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_muted")
    data class ParticipantMuted(
        @SerialName("data")
        val data: ParticipantMutedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_unmuted")
    data class ParticipantUnmuted(
        @SerialName("data")
        val data: ParticipantUnmutedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("room_updated")
    data class RoomUpdated(
        @SerialName("data")
        val data: RoomUpdatedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("room_deleted")
    data class RoomDeleted(
        @SerialName("data")
        val data: RoomDeletedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("typing_started")
    data class TypingStarted(
        @SerialName("data")
        val data: TypingStartedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("typing_stopped")
    data class TypingStopped(
        @SerialName("data")
        val data: TypingStoppedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("error")
    data class Error(
        @SerialName("data")
        val data: RoomErrorData,
        @SerialName("timestamp")
        val timestamp: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("pong")
    data class Pong(
        @SerialName("data")
        val data: PongData,
        @SerialName("timestamp")
        val timestamp: String
    ) : RoomWebSocketServerEvent()
}

// Вспомогательные data классы
@Serializable
data class ConnectionEstablishedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("online_count")
    val onlineCount: Int,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class MessageSentData(
    @SerialName("message")
    val message: WebSocketMessage,
    @SerialName("sender_profile_id")
    val senderProfileId: String
)

@Serializable
data class MessageUpdatedData(
    @SerialName("message_id")
    val messageId: String,
    @SerialName("content")
    val content: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("editor_profile_id")
    val editorProfileId: String
)

@Serializable
data class MessageDeletedData(
    @SerialName("message_id")
    val messageId: String,
    @SerialName("deleter_profile_id")
    val deleterProfileId: String
)

@Serializable
data class ParticipantJoinedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("joined_at")
    val joinedAt: String,
    @SerialName("online_count")
    val onlineCount: Int
)

@Serializable
data class ParticipantLeftData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("left_at")
    val leftAt: String,
    @SerialName("online_count")
    val onlineCount: Int
)

@Serializable
data class ParticipantKickedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("kicker_profile_id")
    val kickerProfileId: String,
    @SerialName("is_kicked")
    val isKicked: Boolean
)

@Serializable
data class ParticipantBannedData(
    @SerialName("banned_profile_id")
    val bannedProfileId: String,
    @SerialName("banner_profile_id")
    val bannerProfileId: String
)

@Serializable
data class ParticipantUnbannedData(
    @SerialName("unbanned_profile_id")
    val unbannedProfileId: String,
    @SerialName("unbanner_profile_id")
    val unbannerProfileId: String
)

@Serializable
data class ParticipantRoleChangedData(
    @SerialName("target_profile_id")
    val targetProfileId: String,
    @SerialName("old_role")
    val oldRole: String,
    @SerialName("new_role")
    val newRole: String,
    @SerialName("changer_profile_id")
    val changerProfileId: String,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class ParticipantMutedData(
    @SerialName("muted_profile_id")
    val mutedProfileId: String,
    @SerialName("muter_profile_id")
    val muterProfileId: String
)

@Serializable
data class ParticipantUnmutedData(
    @SerialName("unmuted_profile_id")
    val unmutedProfileId: String,
    @SerialName("unmuter_profile_id")
    val unmuterProfileId: String
)

@Serializable
data class RoomUpdatedData(
    @SerialName("room")
    val room: WebSocketRoom,
    @SerialName("updater_profile_id")
    val updaterProfileId: String
)

@Serializable
data class RoomDeletedData(
    @SerialName("room_id")
    val roomId: String,
    @SerialName("deleter_profile_id")
    val deleterProfileId: String
)

@Serializable
data class TypingStartedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("room_id")
    val roomId: String
)

@Serializable
data class TypingStoppedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("room_id")
    val roomId: String
)

@Serializable
data class RoomErrorData(
    @SerialName("message")
    val message: String
)

@Serializable
data class PongData(
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class WebSocketMessage(
    @SerialName("id")
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("content")
    val content: String,
    @SerialName("parent_message_id")
    val parentMessageId: String? = null,
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
data class WebSocketRoom(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("primary_interest_id")
    val primaryInterestId: String? = null,
    @SerialName("creator_id")
    val creatorId: String,
    @SerialName("tags")
    val tags: List<String> = emptyList(),
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