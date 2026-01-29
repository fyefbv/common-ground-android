package com.example.common_ground_android.network.model.domain.chat_roulette

import com.example.common_ground_android.network.model.domain.profile.Profile
import com.example.common_ground_android.network.model.response.chat_roulette.ChatRouletteSessionResponse
import com.example.common_ground_android.network.model.response.chat_roulette.ChatRouletteStatisticsResponse
import com.example.common_ground_android.network.utils.DateUtils
import java.util.Date

enum class ChatRouletteStatus {
    WAITING,
    ACTIVE,
    COMPLETED,
    LEFT,
    REPORTED,
    CANCELLED;

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
    val profile2Id: String,
    val matchedInterestId: String?,
    val status: ChatRouletteStatus,
    val durationMinutes: Int,
    val extensionMinutes: Int?,
    val startedAt: Date,
    val expiresAt: Date,
    val endedAt: Date?,
    val createdAt: Date,
    val matchedProfile: Profile?,
    val commonInterests: List<String>,
    val timeRemaining: Int?,
    val extensionApprovedByProfile1: Boolean,
    val extensionApprovedByProfile2: Boolean
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
                startedAt = DateUtils.parseIsoDate(response.startedAt),
                expiresAt = DateUtils.parseIsoDate(response.expiresAt),
                endedAt = DateUtils.parseIsoDateNullable(response.endedAt),
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                matchedProfile = response.matchedProfile?.let { Profile.fromResponse(it) },
                commonInterests = response.commonInterests,
                timeRemaining = response.timeRemaining,
                extensionApprovedByProfile1 = response.extensionApprovedByProfile1,
                extensionApprovedByProfile2 = response.extensionApprovedByProfile2
            )
        }
    }
}

data class ChatRouletteStatistics(
    val totalSessions: Int,
    val completedSessions: Int,
    val averageRating: Float,
    val completionRate: Float
) {
    companion object {
        fun fromResponse(response: ChatRouletteStatisticsResponse): ChatRouletteStatistics {
            return ChatRouletteStatistics(
                totalSessions = response.totalSessions,
                completedSessions = response.completedSessions,
                averageRating = response.averageRating,
                completionRate = response.completionRate
            )
        }
    }
}