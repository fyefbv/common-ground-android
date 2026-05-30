package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.CreateProfileRequest
import com.example.common_ground_android.network.model.request.ProfileInterestsRequest
import com.example.common_ground_android.network.model.request.UpdateProfileRequest
import com.example.common_ground_android.network.model.response.AvatarResponse
import com.example.common_ground_android.network.model.response.DeleteProfileResponse
import com.example.common_ground_android.network.model.response.ProfileResponse
import com.example.common_ground_android.network.model.response.InterestResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.client.replacePathParams
import com.example.common_ground_android.network.model.response.ProfileStatisticsResponse
import com.example.common_ground_android.utils.LocaleUtils
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class ProfileService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun getAllProfiles(): List<ProfileResponse> {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES)
        }.body()
    }

    suspend fun getProfilesBatch(profileIds: List<String>): List<ProfileResponse> {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_BATCH)
            contentType(ContentType.Application.Json)
            setBody(mapOf("profile_ids" to profileIds))
        }.body()
    }

    suspend fun createProfile(request: CreateProfileRequest): ProfileResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getMyProfiles(): List<ProfileResponse> {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_ME)
        }.body()
    }

    suspend fun getMyStatistics(): ProfileStatisticsResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_ME_STATISTICS)
        }.body()
    }

    suspend fun getProfileStatisticsById(profileId: String): ProfileStatisticsResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILE_STATISTICS_BY_ID.replacePathParams("profile_id" to profileId))
        }.body()
    }

    suspend fun getCurrentProfile(): ProfileResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_CURRENT)
        }.body()
    }

    suspend fun getProfileById(profileId: String): ProfileResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILE_BY_ID.replacePathParams("profile_id" to profileId))
        }.body()
    }

    suspend fun updateMyProfile(request: UpdateProfileRequest): ProfileResponse {
        return client.patch {
            url(ApiConfig.Endpoints.PROFILES_ME)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteMyProfile(): DeleteProfileResponse {
        return client.delete {
            url(ApiConfig.Endpoints.PROFILES_ME)
        }.body()
    }

    suspend fun uploadAvatar(fileBytes: ByteArray): AvatarResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_AVATAR)
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                    })
                }
            ))
        }.body()
    }

    suspend fun uploadAvatarByUsername(username: String, fileBytes: ByteArray): AvatarResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_AVATAR_BY_USERNAME.replacePathParams("username" to username))
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"avatar.jpg\"")
                    })
                }
            ))
        }.body()
    }

    suspend fun deleteAvatar(): DeleteProfileResponse {
        return client.delete {
            url(ApiConfig.Endpoints.PROFILES_AVATAR)
        }.body()
    }

    suspend fun getProfileInterests(username: String): List<InterestResponse> {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_INTERESTS.replacePathParams("username" to username))
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, LocaleUtils.getCurrentLanguage())
        }.body()
    }

    suspend fun addInterestsToMyProfile(request: ProfileInterestsRequest): DeleteProfileResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_ME_INTERESTS)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun addInterestsToProfileByUsername(username: String, request: ProfileInterestsRequest): DeleteProfileResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_INTERESTS.replacePathParams("username" to username))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun removeInterestsFromMyProfile(request: ProfileInterestsRequest): DeleteProfileResponse {
        return client.delete {
            url(ApiConfig.Endpoints.PROFILES_ME_INTERESTS)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}