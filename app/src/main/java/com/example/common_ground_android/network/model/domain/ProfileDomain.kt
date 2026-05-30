package com.example.common_ground_android.network.model.domain

import com.example.common_ground_android.network.model.response.MatchedProfileResponse
import com.example.common_ground_android.network.model.response.ProfileResponse
import com.example.common_ground_android.network.model.response.ProfileStatisticsResponse
import com.example.common_ground_android.utils.DateUtils
import java.util.Date

data class Profile(
    val id: String,
    val userId: String,
    val username: String,
    val bio: String?,
    val reputationScore: Float,
    val createdAt: Date,
    val updatedAt: Date,
    val avatarUrl: String?,
    val interests: List<Interest> = emptyList()
) {
    companion object {
        fun fromResponse(response: ProfileResponse): Profile {
            return Profile(
                id = response.id,
                userId = response.userId,
                username = response.username,
                bio = response.bio,
                reputationScore = response.reputationScore,
                createdAt = DateUtils.parseIsoDate(response.createdAt),
                updatedAt = DateUtils.parseIsoDate(response.updatedAt),
                avatarUrl = response.avatarUrl
            )
        }
        fun fromMatchedResponse(response: MatchedProfileResponse): Profile {
            return Profile(
                id = response.id,
                userId = "",
                username = response.username,
                bio = response.bio,
                reputationScore = response.reputationScore,
                createdAt = Date(),
                updatedAt = Date(),
                avatarUrl = response.avatarUrl,
                interests = emptyList()
            )
        }
    }
}

data class ProfileStatistics(
    val totalSessions: Int,
    val reputationScore: Float,
    val totalRooms: Int
) {
    companion object {
        fun fromResponse(response: ProfileStatisticsResponse): ProfileStatistics {
            return ProfileStatistics(
                totalSessions = response.totalSessions,
                reputationScore = response.reputationScore,
                totalRooms = response.totalRooms
            )
        }
    }
}