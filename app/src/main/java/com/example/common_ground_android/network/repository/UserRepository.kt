package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.UserService
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.auth.UserResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userService: UserService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getCurrentUser(): NetworkResult<UserResponse> {
        return withContext(dispatcher) {
            try {
                val response = userService.getCurrentUser()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updateEmail(email: String): NetworkResult<UserResponse> {
        return withContext(dispatcher) {
            try {
                val response = userService.updateUser(email = email)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updatePassword(password: String): NetworkResult<UserResponse> {
        return withContext(dispatcher) {
            try {
                val response = userService.updateUser(password = password)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updateUser(email: String, password: String): NetworkResult<UserResponse> {
        return withContext(dispatcher) {
            try {
                val response = userService.updateUser(email = email, password = password)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun deleteUser(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = userService.deleteUser()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}