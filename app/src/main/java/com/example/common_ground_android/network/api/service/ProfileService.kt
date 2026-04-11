package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.profile.CreateProfileRequest
import com.example.common_ground_android.network.model.request.profile.ProfileInterestsRequest
import com.example.common_ground_android.network.model.request.profile.UpdateProfileRequest
import com.example.common_ground_android.network.model.response.profile.AvatarResponse
import com.example.common_ground_android.network.model.response.profile.DeleteResponse
import com.example.common_ground_android.network.model.response.profile.ProfileResponse
import com.example.common_ground_android.network.model.response.interest.InterestResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.client.replacePathParams
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import java.io.File

class ProfileService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun getAllProfiles(): List<ProfileResponse> {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES)
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

    suspend fun getCurrentProfile(): ProfileResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_CURRENT)
        }.body()
    }

    suspend fun getProfileByUsername(username: String): ProfileResponse {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_BY_USERNAME.replacePathParams("username" to username))
        }.body()
    }

    suspend fun updateMyProfile(request: UpdateProfileRequest): ProfileResponse {
        return client.patch {
            url(ApiConfig.Endpoints.PROFILES_ME)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteMyProfile(): DeleteResponse {
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

    suspend fun deleteAvatar(): DeleteResponse {
        return client.delete {
            url(ApiConfig.Endpoints.PROFILES_AVATAR)
        }.body()
    }

    suspend fun getProfileInterests(username: String): List<InterestResponse> {
        return client.get {
            url(ApiConfig.Endpoints.PROFILES_INTERESTS.replacePathParams("username" to username))
        }.body()
    }

    suspend fun addInterestsToMyProfile(request: ProfileInterestsRequest): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_ME_INTERESTS)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun addInterestsToProfileByUsername(username: String, request: ProfileInterestsRequest): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.PROFILES_INTERESTS.replacePathParams("username" to username))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun removeInterestsFromMyProfile(request: ProfileInterestsRequest): DeleteResponse {
        return client.delete {
            url(ApiConfig.Endpoints.PROFILES_ME_INTERESTS)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}