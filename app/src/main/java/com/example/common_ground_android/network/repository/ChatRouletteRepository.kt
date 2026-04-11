package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.ChatRouletteService
import com.example.common_ground_android.network.model.request.chat_roulette.*
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.chat_roulette.*
import com.example.common_ground_android.network.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRouletteRepository(
    private val chatRouletteService: ChatRouletteService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun startSearch(priorityInterestIds: List<String> = emptyList()): NetworkResult<SearchResponse> {
        return withContext(dispatcher) {
            try {
                val response = chatRouletteService.startSearch(priorityInterestIds)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.startSearch")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun cancelSearch(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                chatRouletteService.cancelSearch()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.cancelSearch")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getActiveSession(): NetworkResult<ChatRouletteSessionResponse> {
        return withContext(dispatcher) {
            try {
                val response = chatRouletteService.getActiveSession()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.getActiveSession")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun extendSession(): NetworkResult<SessionExtensionResponse> {
        return withContext(dispatcher) {
            try {
                val response = chatRouletteService.extendSession()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.extendSession")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun endSession(reason: String? = null): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                chatRouletteService.endSession(reason)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.endSession")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun ratePartner(rating: Int, feedback: String? = null): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                chatRouletteService.ratePartner(rating, feedback)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.ratePartner")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun reportPartner(reason: String? = null, details: String? = null): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                chatRouletteService.reportPartner(reason, details)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.reportPartner")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getStatistics(): NetworkResult<ChatRouletteStatisticsResponse> {
        return withContext(dispatcher) {
            try {
                val response = chatRouletteService.getStatistics()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.getStatistics")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun sendMessage(content: String): NetworkResult<ChatRouletteMessageResponse> {
        return withContext(dispatcher) {
            try {
                val response = chatRouletteService.sendMessage(content)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ChatRouletteRepository.sendMessage")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }
}