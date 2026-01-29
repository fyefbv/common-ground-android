package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.AuthService
import com.example.common_ground_android.network.client.TokenManager
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.auth.AuthTokensResponse
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
                NetworkResult.Error(exception = e)
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
                NetworkResult.Error(exception = e)
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
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun refreshToken(): NetworkResult<Boolean> {
        return withContext(dispatcher) {
            try {
                val refreshToken = tokenManager.getRefreshTokenSync()
                if (refreshToken == null) {
                    tokenManager.clearTokens()
                    return@withContext NetworkResult.Error(message = "No refresh token")
                }

                val response = authService.refreshToken(refreshToken)
                tokenManager.saveTokens(response.accessToken, response.refreshToken)
                NetworkResult.Success(true)
            } catch (e: Exception) {
                tokenManager.clearTokens()
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun logout(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                tokenManager.clearTokens()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}