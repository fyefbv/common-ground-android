package com.example.common_ground_android.network.model.websocket.chat_roulette

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ChatRouletteWebSocketClientEvent {
    @Serializable
    @SerialName("send_message")
    data class SendMessage(
        @SerialName("content")
        val content: String
    ) : ChatRouletteWebSocketClientEvent()

    @Serializable
    @SerialName("ping")
    object Ping : ChatRouletteWebSocketClientEvent()
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ChatRouletteWebSocketServerEvent {
    @Serializable
    @SerialName("connection_established")
    data class ConnectionEstablished(
        @SerialName("data")
        val data: ConnectionEstablishedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("partner_connected")
    data class PartnerConnected(
        @SerialName("data")
        val data: PartnerConnectedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("partner_disconnected")
    data class PartnerDisconnected(
        @SerialName("data")
        val data: PartnerDisconnectedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("message_sent")
    data class MessageSent(
        @SerialName("data")
        val data: MessageSentData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("message_received")
    data class MessageReceived(
        @SerialName("data")
        val data: MessageReceivedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("session_ended")
    data class SessionEnded(
        @SerialName("data")
        val data: SessionEndedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("session_extended")
    data class SessionExtended(
        @SerialName("data")
        val data: SessionExtendedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("session_expired")
    data class SessionExpired(
        @SerialName("data")
        val data: SessionExpiredData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("extension_requested")
    data class ExtensionRequested(
        @SerialName("data")
        val data: ExtensionRequestedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("extension_approved")
    data class ExtensionApproved(
        @SerialName("data")
        val data: ExtensionApprovedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("extension_rejected")
    data class ExtensionRejected(
        @SerialName("data")
        val data: ExtensionRejectedData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("extension_cancelled")
    data class ExtensionCancelled(
        @SerialName("data")
        val data: ExtensionCancelledData,
        @SerialName("timestamp")
        val timestamp: String,
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("sender_profile_id")
        val senderProfileId: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("error")
    data class Error(
        @SerialName("data")
        val data: ChatRouletteErrorData,
        @SerialName("timestamp")
        val timestamp: String
    ) : ChatRouletteWebSocketServerEvent()

    @Serializable
    @SerialName("pong")
    data class Pong(
        @SerialName("data")
        val data: PongData,
        @SerialName("timestamp")
        val timestamp: String
    ) : ChatRouletteWebSocketServerEvent()
}

@Serializable
data class ConnectionEstablishedData(
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class PartnerConnectedData(
    @SerialName("partner_profile_id")
    val partnerProfileId: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class PartnerDisconnectedData(
    @SerialName("partner_profile_id")
    val partnerProfileId: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class MessageSentData(
    @SerialName("message")
    val message: ChatRouletteWebSocketMessage,
    @SerialName("sender_profile_id")
    val senderProfileId: String
)

@Serializable
data class MessageReceivedData(
    @SerialName("message_id")
    val messageId: String,
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class SessionStartedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("partner_profile_id")
    val partnerProfileId: String,
    @SerialName("duration_minutes")
    val durationMinutes: Int,
    @SerialName("expires_at")
    val expiresAt: String,
    @SerialName("matched_interest_id")
    val matchedInterestId: String? = null
)

@Serializable
data class SessionEndedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("reason")
    val reason: String
)

@Serializable
data class SessionExtendedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("profile_id")
    val profileId: String,
    @SerialName("extended_minutes")
    val extendedMinutes: Int,
    @SerialName("new_expires_at")
    val newExpiresAt: String
)

@Serializable
data class SessionExpiredData(
    @SerialName("session_id")
    val sessionId: String
)

@Serializable
data class TimerUpdateData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("time_remaining")
    val timeRemaining: Int
)

@Serializable
data class TimeAlmostUpData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("time_remaining")
    val timeRemaining: Int
)

@Serializable
data class ExtensionRequestedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("requesting_profile_id")
    val requestingProfileId: String
)

@Serializable
data class ExtensionApprovedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("approving_profile_id")
    val approvingProfileId: String
)

@Serializable
data class ExtensionRejectedData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("rejecting_profile_id")
    val rejectingProfileId: String
)

@Serializable
data class ExtensionCancelledData(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("cancelling_profile_id")
    val cancellingProfileId: String
)

@Serializable
data class ChatRouletteErrorData(
    @SerialName("message")
    val message: String
)

@Serializable
data class PongData(
    @SerialName("timestamp")
    val timestamp: String
)

@Serializable
data class ChatRouletteWebSocketMessage(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("content")
    val content: String,
    @SerialName("created_at")
    val createdAt: String
)