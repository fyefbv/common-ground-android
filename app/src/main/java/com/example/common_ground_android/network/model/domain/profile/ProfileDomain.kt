package com.example.common_ground_android.network.model.domain.profile

import com.example.common_ground_android.network.model.domain.interest.Interest
import com.example.common_ground_android.network.model.response.profile.ProfileResponse
import com.example.common_ground_android.network.utils.DateUtils
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

        fun fromResponses(responses: List<ProfileResponse>): List<Profile> {
            return responses.map { fromResponse(it) }
        }
    }
}