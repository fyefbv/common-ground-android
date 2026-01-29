package com.example.common_ground_android.network.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.common_ground_android.network.api.service.ProfileService
import com.example.common_ground_android.network.model.request.profile.CreateProfileRequest
import com.example.common_ground_android.network.model.request.profile.ProfileInterestsRequest
import com.example.common_ground_android.network.model.request.profile.UpdateProfileRequest
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.interest.InterestResponse
import com.example.common_ground_android.network.model.response.profile.AvatarResponse
import com.example.common_ground_android.network.model.response.profile.ProfileResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ProfileRepository(
    private val profileService: ProfileService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun getAllProfiles(): NetworkResult<List<ProfileResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getAllProfiles()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun createProfile(username: String, bio: String? = null): NetworkResult<ProfileResponse> {
        return withContext(dispatcher) {
            try {
                val request = CreateProfileRequest(username, bio)
                val response = profileService.createProfile(request)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getMyProfiles(): NetworkResult<List<ProfileResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getMyProfiles()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getProfileByUsername(username: String): NetworkResult<ProfileResponse> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfileByUsername(username)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updateMyProfile(
        username: String? = null,
        bio: String? = null
    ): NetworkResult<ProfileResponse> {
        return withContext(dispatcher) {
            try {
                val request = UpdateProfileRequest(username, bio)
                val response = profileService.updateMyProfile(request)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun deleteMyProfile(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = profileService.deleteMyProfile()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun uploadAvatar(bitmap: Bitmap): NetworkResult<String> {
        return withContext(dispatcher) {
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                outputStream.close()

                val response = profileService.uploadAvatar(byteArray)
                NetworkResult.Success(response.avatarUrl)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun uploadAvatar(byteArray: ByteArray): NetworkResult<String> {
        return withContext(dispatcher) {
            try {
                val response = profileService.uploadAvatar(byteArray)
                NetworkResult.Success(response.avatarUrl)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun deleteAvatar(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = profileService.deleteAvatar()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getProfileInterests(username: String): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfileInterests(username)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun addInterestsToMyProfile(interestIds: List<String>): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val request = ProfileInterestsRequest(interestIds)
                val response = profileService.addInterestsToMyProfile(request)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun removeInterestsFromMyProfile(interestIds: List<String>): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val request = ProfileInterestsRequest(interestIds)
                val response = profileService.removeInterestsFromMyProfile(request)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}