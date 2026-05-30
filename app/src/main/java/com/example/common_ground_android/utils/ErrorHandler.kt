package com.example.common_ground_android.utils

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
                    "validation_error" -> "Ошибка валидации данных"
                    "database_error" -> "Ошибка базы данных"
                    "internal_server_error" -> "Внутренняя ошибка сервера"

                    "invalid_token" -> "Недействительный токен"
                    "expired_token" -> "Срок действия токена истек"
                    "missing_token" -> "Токен отсутствует"
                    "authentication_failed" -> "Ошибка аутентификации"

                    "user_not_found" -> "Пользователь не найден"
                    "user_already_exists" -> "Пользователь уже существует"

                    "profile_not_found" -> "Профиль не найден"
                    "profile_already_exists" -> "Профиль уже существует"
                    "profile_permission_denied" -> "Нет доступа к профилю"
                    "profile_not_selected" -> "Профиль не выбран"

                    "interest_not_found" -> "Интерес не найден"

                    "unsupported_media_type" -> "Неподдерживаемый тип файла"
                    "file_too_large" -> "Файл слишком большой"

                    "room_not_found" -> "Комната не найдена"
                    "room_already_exists" -> "Комната уже существует"
                    "room_permission_denied" -> "Нет доступа к комнате"
                    "room_max_participants_too_low" -> "Попытка установить лимит участников ниже текущего количества"
                    "room_full" -> "Комната заполнена"
                    "room_private" -> "Комната приватная"
                    "not_room_member" -> "Вы не участник комнаты"
                    "participant_banned" -> "Вы забанены в этой комнате"
                    "participant_muted" -> "Вы замучены в этой комнате"
                    "participant_not_found" -> "Участник не найден"
                    "message_not_found" -> "Сообщение не найдено"
                    "invalid_role" -> "Недопустимая роль"
                    "participant_already_has_role" -> "Участник уже имеет эту роль"

                    "already_in_search" -> "Уже идет поиск собеседника"
                    "already_in_session" -> "Уже есть активная сессия"
                    "no_active_search" -> "Активный поиск не найден"
                    "no_active_session" -> "Активная сессия не найдена"
                    "session_not_found" -> "Сессия не найдена"
                    "partner_not_found" -> "Собеседник не найден"
                    "session_expired" -> "Сессия истекла"
                    "session_already_ended" -> "Сессия уже завершена"
                    "cannot_rate_yourself" -> "Нельзя оценить самого себя"
                    "already_rated" -> "Уже оценили этого собеседника"
                    "cannot_rate_non_completed_session" -> "Нельзя оценить незавершенную сессию"
                    "extension_not_approved" -> "Запрос на продление отправлен"
                    "no_matching_found" -> "Собеседник не найден, попробуйте ещё раз"

                    else -> exception.message ?: "Неизвестная ошибка"
                }
                code to message
            }

            is ConnectException,
            is SocketTimeoutException,
            is UnknownHostException -> {
                null to "Нет подключения к интернету. Проверьте соединение и попробуйте снова."
            }

            is SSLHandshakeException -> {
                null to "Ошибка безопасного соединения. Проверьте дату и время устройства."
            }

            else -> {
                null to when {
                    exception.message?.contains("timed out") == true -> "Таймаут соединения"
                    exception.message?.contains("Unable to resolve host") == true ->
                        "Сервер не найден. Проверьте подключение"
                    else -> exception.message ?: "Неизвестная ошибка"
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