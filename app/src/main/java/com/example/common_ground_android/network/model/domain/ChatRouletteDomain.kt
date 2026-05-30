package com.example.common_ground_android.network.model.domain

import com.example.common_ground_android.network.model.response.ChatRouletteMessageResponse
import com.example.common_ground_android.network.model.response.ChatRouletteSessionResponse
import com.example.common_ground_android.utils.DateUtils
import java.util.Date

enum class ChatRouletteStatus {
    WAITING, ACTIVE, COMPLETED, LEFT, REPORTED, CANCELLED;

    companion object {
        fun fromString(status: String): ChatRouletteStatus {
            return when (status.uppercase()) {
                "WAITING" -> WAITING
                "ACTIVE" -> ACTIVE
                "COMPLETED" -> COMPLETED
                "LEFT" -> LEFT
                "REPORTED" -> REPORTED
                else -> CANCELLED
            }
        }
    }
}

data class ChatRouletteSession(
    val id: String,
    val profile1Id: String,
    val profile2Id: String?,
    val matchedInterestId: String?,
    val status: ChatRouletteStatus,
    val durationMinutes: Int,
    val extensionMinutes: Int?,
    val startedAt: Date?,
    val expiresAt: Date?,
    val endedAt: Date?,
    val createdAt: Date,
    val matchedProfile: Profile? = null,
    val commonInterests: List<String>? = null,
    val timeRemaining: Int? = null,
    val extensionApprovedByProfile1: Boolean,
    val extensionApprovedByProfile2: Boolean,
    val partnerOnline: Boolean = false
) {
    companion object {
        fun fromResponse(response: ChatRouletteSessionResponse): ChatRouletteSession {
            return ChatRouletteSession(
                id = response.id,
                profile1Id = response.profile1Id,
                profile2Id = response.profile2Id,
                matchedInterestId = response.matchedInterestId,
                status = ChatRouletteStatus.fromString(response.status),
                durationMinutes = response.durationMinutes,
                extensionMinutes = response.extensionMinutes,
                startedAt = DateUtils.parseIsoDateNullable(response.startedAt),
                expiresAt = DateUtils.parseIsoDateNullable(response.expiresAt),
                endedAt = DateUtils.parseIsoDateNullable(response.endedAt),
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                matchedProfile = response.matchedProfile?.let { Profile.fromMatchedResponse(it) },
                commonInterests = response.commonInterests,
                timeRemaining = response.timeRemaining,
                extensionApprovedByProfile1 = response.extensionApprovedByProfile1,
                extensionApprovedByProfile2 = response.extensionApprovedByProfile2,
                partnerOnline = response.partnerOnline
            )
        }
    }
}

data class ChatRouletteMessage(
    val sessionId: String,
    val senderId: String,
    val content: String,
    val createdAt: Date
) {
    companion object {
        fun fromResponse(response: ChatRouletteMessageResponse): ChatRouletteMessage {
            return ChatRouletteMessage(
                sessionId = response.sessionId,
                senderId = response.senderId,
                content = response.content,
                createdAt = DateUtils.parseIsoDate(response.createdAt)
            )
        }
    }
}