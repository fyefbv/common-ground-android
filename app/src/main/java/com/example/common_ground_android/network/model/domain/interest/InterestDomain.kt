package com.example.common_ground_android.network.model.domain.interest

import com.example.common_ground_android.network.model.response.interest.InterestResponse

data class Interest(
    val id: String,
    val name: String
) {
    companion object {
        fun fromResponse(response: InterestResponse): Interest {
            return Interest(
                id = response.id,
                name = response.name
            )
        }

        fun fromResponses(responses: List<InterestResponse>): List<Interest> {
            return responses.map { fromResponse(it) }
        }
    }
}