package com.example.common_ground_android.utils

import com.example.common_ground_android.R
import com.example.common_ground_android.network.client.NetworkException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

object ErrorHandler {

    fun handleException(exception: Exception, context: String = ""): Pair<String?, String> {
        Timber.e(exception, "Error in $context")

        return when (exception) {
            is NetworkException -> {
                val code = exception.errorCode
                val message = when (code) {
                    "validation_error" -> Res.getString(R.string.error_validation_data)
                    "database_error" -> Res.getString(R.string.error_database)
                    "internal_server_error" -> Res.getString(R.string.error_internal_server)

                    "invalid_token" -> Res.getString(R.string.error_invalid_token)
                    "expired_token" -> Res.getString(R.string.error_expired_token)
                    "missing_token" -> Res.getString(R.string.error_missing_token)
                    "authentication_failed" -> Res.getString(R.string.error_authentication_failed)

                    "user_not_found" -> Res.getString(R.string.error_user_not_found)
                    "user_already_exists" -> Res.getString(R.string.error_user_already_exists)

                    "profile_not_found" -> Res.getString(R.string.error_profile_not_found)
                    "profile_already_exists" -> Res.getString(R.string.error_profile_already_exists)
                    "profile_permission_denied" -> Res.getString(R.string.error_profile_permission_denied)
                    "profile_not_selected" -> Res.getString(R.string.error_profile_not_selected)

                    "interest_not_found" -> Res.getString(R.string.error_interest_not_found)

                    "unsupported_media_type" -> Res.getString(R.string.error_unsupported_media_type)
                    "file_too_large" -> Res.getString(R.string.error_file_too_large)

                    "room_not_found" -> Res.getString(R.string.error_room_not_found)
                    "room_already_exists" -> Res.getString(R.string.error_room_already_exists)
                    "room_permission_denied" -> Res.getString(R.string.error_room_permission_denied)
                    "room_max_participants_too_low" -> Res.getString(R.string.error_room_max_participants_too_low)
                    "room_full" -> Res.getString(R.string.error_room_full)
                    "room_private" -> Res.getString(R.string.error_room_private)
                    "not_room_member" -> Res.getString(R.string.error_not_room_member)
                    "participant_banned" -> Res.getString(R.string.error_participant_banned)
                    "participant_muted" -> Res.getString(R.string.error_participant_muted)
                    "participant_not_found" -> Res.getString(R.string.error_participant_not_found)
                    "message_not_found" -> Res.getString(R.string.error_message_not_found)
                    "invalid_role" -> Res.getString(R.string.error_invalid_role)
                    "participant_already_has_role" -> Res.getString(R.string.error_participant_already_has_role)

                    "already_in_search" -> Res.getString(R.string.error_already_in_search)
                    "already_in_session" -> Res.getString(R.string.error_already_in_session)
                    "no_active_search" -> Res.getString(R.string.error_no_active_search)
                    "no_active_session" -> Res.getString(R.string.error_no_active_session)
                    "session_not_found" -> Res.getString(R.string.error_session_not_found)
                    "partner_not_found" -> Res.getString(R.string.error_partner_not_found)
                    "session_expired" -> Res.getString(R.string.error_session_expired)
                    "session_already_ended" -> Res.getString(R.string.error_session_already_ended)
                    "cannot_rate_yourself" -> Res.getString(R.string.error_cannot_rate_yourself)
                    "already_rated" -> Res.getString(R.string.error_already_rated)
                    "cannot_rate_non_completed_session" -> Res.getString(R.string.error_cannot_rate_non_completed_session)
                    "extension_not_approved" -> Res.getString(R.string.error_extension_not_approved)
                    "no_matching_found" -> Res.getString(R.string.error_no_matching_found)
                    "search_cancelled" -> Res.getString(R.string.error_search_cancelled_new_device)

                    else -> exception.message ?: Res.getString(R.string.error_unknown_error)
                }
                code to message
            }

            is ConnectException,
            is SocketTimeoutException,
            is UnknownHostException -> {
                null to Res.getString(R.string.error_network_connection)
            }

            is SSLHandshakeException -> {
                null to Res.getString(R.string.error_ssl_handshake)
            }

            else -> {
                null to when {
                    exception.message?.contains("timed out") == true -> Res.getString(R.string.error_timeout)
                    exception.message?.contains("Unable to resolve host") == true -> Res.getString(R.string.error_host_not_found)
                    else -> exception.message ?: Res.getString(R.string.error_unknown_error)
                }
            }
        }
    }

    fun isAuthError(errorCode: String?): Boolean {
        return errorCode in listOf(
            "invalid_token",
            "expired_token",
            "missing_token",
            "authentication_failed"
        )
    }

    fun isNetworkError(exception: Exception): Boolean {
        return exception is NetworkException.ConnectionError ||
                exception is NetworkException.TimeoutError ||
                exception is ConnectException ||
                exception is SocketTimeoutException ||
                exception is UnknownHostException
    }
}