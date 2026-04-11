package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.AuthService
import com.example.common_ground_android.network.client.TokenManager
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.auth.AuthTokensResponse
import com.example.common_ground_android.network.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun register(email: String, password: String): NetworkResult<AuthTokensResponse> {
        return withContext(dispatcher) {
            try {
                val response = authService.register(email, password)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "AuthRepository.register")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthTokensResponse> {
        return withContext(dispatcher) {
            try {
                val response = authService.login(email, password)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "AuthRepository.login")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun selectProfile(profileId: String): NetworkResult<AuthTokensResponse> {
        return withContext(dispatcher) {
            try {
                val response = authService.selectProfile(profileId)
                tokenManager.saveTokens(response.accessToken, response.refreshToken, profileId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "AuthRepository.selectProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun refreshToken(): NetworkResult<Boolean> {
        return withContext(dispatcher) {
            try {
                val refreshToken = tokenManager.getRefreshTokenSync()
                if (refreshToken == null) {
                    tokenManager.clearTokens()
                    return@withContext NetworkResult.Error(errorMessage = "Токен обновления отсутствует")
                }
                val response = authService.refreshToken(refreshToken)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                NetworkResult.Success(true)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "AuthRepository.refreshToken")
                tokenManager.clearTokens()
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun logout(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                tokenManager.clearTokens()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "AuthRepository.logout")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }
}