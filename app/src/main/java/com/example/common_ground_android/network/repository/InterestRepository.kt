package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.InterestService
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.InterestResponse
import com.example.common_ground_android.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InterestRepository(
    private val interestService: InterestService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllInterests(): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val response = interestService.getAllInterests()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "InterestRepository.getAllInterests")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getInterestsBatch(interestIds: List<String>): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val response = interestService.getInterestsBatch(interestIds)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "InterestRepository.getInterestsBatch")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }
}