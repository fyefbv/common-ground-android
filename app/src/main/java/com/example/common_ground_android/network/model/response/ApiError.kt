package com.example.common_ground_android.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("error")
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    @SerialName("code")
    val code: String,

    @SerialName("message")
    val message: String,

    @SerialName("timestamp")
    val timestamp: String? = null
)

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val code: String? = null,
        val message: String? = null,
        val httpCode: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    object Loading : NetworkResult<Nothing>()
}

object ApiErrorCodes {
    const val INVALID_TOKEN = "invalid_token"
    const val EXPIRED_TOKEN = "expired_token"
    const val MISSING_TOKEN = "missing_token"
    const val AUTHENTICATION_FAILED = "authentication_failed"

    const val USER_NOT_FOUND = "user_not_found"
    const val USER_ALREADY_EXISTS = "user_already_exists"

    const val PROFILE_NOT_FOUND = "profile_not_found"
    const val PROFILE_ALREADY_EXISTS = "profile_already_exists"
    const val PROFILE_PERMISSION_DENIED = "profile_permission_denied"
    const val PROFILE_NOT_SELECTED = "profile_not_selected"

    const val ROOM_NOT_FOUND = "room_not_found"
    const val ROOM_ALREADY_EXISTS = "room_already_exists"
    const val ROOM_PERMISSION_DENIED = "room_permission_denied"
    const val ROOM_FULL = "room_full"
    const val ROOM_PRIVATE = "room_private"
    const val NOT_ROOM_MEMBER = "not_room_member"
    const val PARTICIPANT_BANNED = "participant_banned"
    const val PARTICIPANT_MUTED = "participant_muted"

    const val ALREADY_IN_SEARCH = "already_in_search"
    const val ALREADY_IN_SESSION = "already_in_session"
    const val NO_MATCHING_FOUND = "no_matching_found"
    const val SESSION_NOT_FOUND = "session_not_found"
    const val EXTENSION_NOT_APPROVED = "extension_not_approved"
}