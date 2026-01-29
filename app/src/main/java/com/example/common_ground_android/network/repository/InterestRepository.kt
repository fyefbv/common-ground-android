package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.InterestService
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.interest.InterestResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InterestRepository(
    private val interestService: InterestService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllInterests(language: String = "ru"): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val response = interestService.getAllInterests(language)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getInterestById(interestId: String, language: String = "ru"): NetworkResult<InterestResponse> {
        return withContext(dispatcher) {
            try {
                val response = interestService.getInterestById(interestId, language)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getInterestsByIds(interestIds: List<String>, language: String = "ru"): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val allInterests = interestService.getAllInterests(language)
                val filtered = allInterests.filter { interestIds.contains(it.id) }
                NetworkResult.Success(filtered)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}