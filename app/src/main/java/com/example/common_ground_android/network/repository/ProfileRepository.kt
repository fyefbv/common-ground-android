package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.ProfileService
import com.example.common_ground_android.network.model.request.CreateProfileRequest
import com.example.common_ground_android.network.model.request.ProfileInterestsRequest
import com.example.common_ground_android.network.model.request.UpdateProfileRequest
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.InterestResponse
import com.example.common_ground_android.network.model.response.ProfileResponse
import com.example.common_ground_android.network.model.response.ProfileStatisticsResponse
import com.example.common_ground_android.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getAllProfiles")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getProfilesBatch(profileIds: List<String>): NetworkResult<List<ProfileResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfilesBatch(profileIds)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getProfilesBatch")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.createProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getMyProfiles(): NetworkResult<List<ProfileResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getMyProfiles()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getMyProfiles")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getMyStatistics(): NetworkResult<ProfileStatisticsResponse> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getMyStatistics()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getMyStatistics")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getProfileStatisticsById(profileId: String): NetworkResult<ProfileStatisticsResponse> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfileStatisticsById(profileId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getProfileStatisticsById")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getCurrentProfile(): NetworkResult<ProfileResponse> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getCurrentProfile()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getCurrentProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getProfileById(profileId: String): NetworkResult<ProfileResponse> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfileById(profileId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getProfileById")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.updateMyProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun deleteMyProfile(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                profileService.deleteMyProfile()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.deleteMyProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun uploadAvatar(byteArray: ByteArray): NetworkResult<String> {
        return withContext(dispatcher) {
            try {
                val response = profileService.uploadAvatar(byteArray)
                NetworkResult.Success(response.avatarUrl)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.uploadAvatar")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun uploadAvatarByUsername(username: String, byteArray: ByteArray): NetworkResult<String> {
        return withContext(dispatcher) {
            try {
                val response = profileService.uploadAvatarByUsername(username, byteArray)
                NetworkResult.Success(response.avatarUrl)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.uploadAvatarByUsername")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun deleteAvatar(): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                profileService.deleteAvatar()
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.deleteAvatar")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getProfileInterests(username: String): NetworkResult<List<InterestResponse>> {
        return withContext(dispatcher) {
            try {
                val response = profileService.getProfileInterests(username)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.getProfileInterests")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun addInterestsToMyProfile(interestIds: List<String>): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val request = ProfileInterestsRequest(interestIds)
                profileService.addInterestsToMyProfile(request)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.addInterestsToMyProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun addInterestsToProfileByUsername(username: String, interestIds: List<String>): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val request = ProfileInterestsRequest(interestIds)
                profileService.addInterestsToProfileByUsername(username, request)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.addInterestsToProfileByUsername")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun removeInterestsFromMyProfile(interestIds: List<String>): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val request = ProfileInterestsRequest(interestIds)
                profileService.removeInterestsFromMyProfile(request)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "ProfileRepository.removeInterestsFromMyProfile")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }
}