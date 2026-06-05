package com.example.common_ground_android.network.model.websocket.room

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RoomWebSocketClientEvent {

    @Serializable
    @SerialName("send_message")
    data class SendMessage(
        val content: String,
        @SerialName("parent_message_id") val parentMessageId: String? = null
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


@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class RoomWebSocketServerEvent {

    @Serializable
    @SerialName("connection_established")
    data class ConnectionEstablished(
        val data: ConnectionEstablishedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_sent")
    data class MessageSent(
        val data: MessageSentData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_updated")
    data class MessageUpdated(
        val data: MessageUpdatedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("message_deleted")
    data class MessageDeleted(
        val data: MessageDeletedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_joined")
    data class ParticipantJoined(
        val data: ParticipantJoinedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_left")
    data class ParticipantLeft(
        val data: ParticipantLeftData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_kicked")
    data class ParticipantKicked(
        val data: ParticipantKickedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_banned")
    data class ParticipantBanned(
        val data: ParticipantBannedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_unbanned")
    data class ParticipantUnbanned(
        val data: ParticipantUnbannedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_role_changed")
    data class ParticipantRoleChanged(
        val data: ParticipantRoleChangedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_muted")
    data class ParticipantMuted(
        val data: ParticipantMutedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("participant_unmuted")
    data class ParticipantUnmuted(
        val data: ParticipantUnmutedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("room_updated")
    data class RoomUpdated(
        val data: RoomUpdatedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("room_deleted")
    data class RoomDeleted(
        val data: RoomDeletedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("typing_started")
    data class TypingStarted(
        val data: TypingStartedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("typing_stopped")
    data class TypingStopped(
        val data: TypingStoppedData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("profile_online")
    data class ProfileOnline(
        val data: ProfileOnlineData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("profile_offline")
    data class ProfileOffline(
        val data: ProfileOfflineData,
        val timestamp: String,
        @SerialName("room_id") val roomId: String,
        @SerialName("sender_profile_id") val senderProfileId: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("error")
    data class Error(
        val data: RoomErrorData,
        val timestamp: String
    ) : RoomWebSocketServerEvent()

    @Serializable
    @SerialName("pong")
    data class Pong(
        val data: PongData,
        val timestamp: String
    ) : RoomWebSocketServerEvent()
}


@Serializable
data class ConnectionEstablishedData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("online_count") val onlineCount: Int,
    val timestamp: String
)

@Serializable
data class MessageSentData(
    val message: WebSocketMessage,
    @SerialName("sender_profile_id") val senderProfileId: String
)

@Serializable
data class MessageUpdatedData(
    val message: WebSocketMessage,
    @SerialName("updater_profile_id") val updaterProfileId: String
)

@Serializable
data class MessageDeletedData(
    @SerialName("message_id") val messageId: String,
    @SerialName("deleter_profile_id") val deleterProfileId: String
)

@Serializable
data class ParticipantJoinedData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("joined_at") val joinedAt: String,
    @SerialName("online_count") val onlineCount: Int
)

@Serializable
data class ParticipantLeftData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("left_at") val leftAt: String,
    @SerialName("online_count") val onlineCount: Int
)

@Serializable
data class ParticipantKickedData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("kicker_profile_id") val kickerProfileId: String,
    @SerialName("is_kicked") val isKicked: Boolean
)

@Serializable
data class ParticipantBannedData(
    @SerialName("banned_profile_id") val bannedProfileId: String,
    @SerialName("banner_profile_id") val bannerProfileId: String
)

@Serializable
data class ParticipantUnbannedData(
    @SerialName("unbanned_profile_id") val unbannedProfileId: String,
    @SerialName("unbanner_profile_id") val unbannerProfileId: String
)

@Serializable
data class ParticipantRoleChangedData(
    @SerialName("target_profile_id") val targetProfileId: String,
    @SerialName("old_role") val oldRole: String,
    @SerialName("new_role") val newRole: String,
    @SerialName("changer_profile_id") val changerProfileId: String,
    val timestamp: String
)

@Serializable
data class ParticipantMutedData(
    @SerialName("muted_profile_id") val mutedProfileId: String,
    @SerialName("muter_profile_id") val muterProfileId: String
)

@Serializable
data class ParticipantUnmutedData(
    @SerialName("unmuted_profile_id") val unmutedProfileId: String,
    @SerialName("unmuter_profile_id") val unmuterProfileId: String
)

@Serializable
data class RoomUpdatedData(
    val room: WebSocketRoom,
    @SerialName("updater_profile_id") val updaterProfileId: String
)

@Serializable
data class RoomDeletedData(
    @SerialName("room_id") val roomId: String,
    @SerialName("deleter_profile_id") val deleterProfileId: String
)

@Serializable
data class TypingStartedData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("room_id") val roomId: String
)

@Serializable
data class TypingStoppedData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("room_id") val roomId: String
)

@Serializable
data class ProfileOnlineData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("online_count") val onlineCount: Int,
    val timestamp: String
)

@Serializable
data class ProfileOfflineData(
    @SerialName("profile_id") val profileId: String,
    @SerialName("online_count") val onlineCount: Int,
    val timestamp: String
)

@Serializable
data class RoomErrorData(
    val message: String
)

@Serializable
data class PongData(
    val timestamp: String
)

@Serializable
data class WebSocketMessage(
    val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("sender_id") val senderId: String?,
    val content: String,
    @SerialName("parent_message_id") val parentMessageId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_edited") val isEdited: Boolean,
    @SerialName("is_deleted") val isDeleted: Boolean
)

@Serializable
data class WebSocketRoom(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("primary_interest_id") val primaryInterestId: String? = null,
    @SerialName("creator_id") val creatorId: String,
    val tags: List<String> = emptyList(),
    @SerialName("max_participants") val maxParticipants: Int,
    @SerialName("is_private") val isPrivate: Boolean,
    @SerialName("participants_count") val participantsCount: Int,
    @SerialName("messages_count") val messagesCount: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("is_joined") val isJoined: Boolean
)