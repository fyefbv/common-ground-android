package com.example.common_ground_android.network.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:8000"
    const val WS_BASE_URL = "http://10.0.2.2:8000"

    val CONNECT_TIMEOUT: Duration = 30.seconds
    val SOCKET_TIMEOUT: Duration = 30.seconds
    val REQUEST_TIMEOUT: Duration = 30.seconds

    val WS_PING_INTERVAL: Duration = 25.seconds
    val WS_RECONNECT_DELAY: Duration = 3.seconds
    const val WS_MAX_RECONNECT_ATTEMPTS = 5

    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"

    const val CONTENT_TYPE_JSON = "application/json"
    const val DEFAULT_LANGUAGE = "ru"

    const val TOKEN_TYPE_BEARER = "Bearer"
    val TOKEN_EXPIRY_BUFFER: Duration = 60.seconds

    object Endpoints {
        const val AUTH_REGISTER = "/api/auth/register"
        const val AUTH_LOGIN = "/api/auth/login"
        const val AUTH_SELECT_PROFILE = "/api/auth/select-profile"
        const val AUTH_REFRESH = "/api/auth/refresh"

        const val USERS_ME = "/api/users/me"

        const val PROFILES = "/api/profiles/"
        const val PROFILES_ME = "/api/profiles/me"
        const val PROFILES_BY_USERNAME = "/api/profiles/{username}"
        const val PROFILES_AVATAR = "/api/profiles/me/avatar"
        const val PROFILES_AVATAR_BY_USERNAME = "/api/profiles/{username}/avatar"
        const val PROFILES_INTERESTS = "/api/profiles/{username}/interests"
        const val PROFILES_ME_INTERESTS = "/api/profiles/me/interests"
        const val PROFILES_CURRENT = "/api/profiles/current"

        const val INTERESTS = "/api/interests/"

        const val ROOMS = "/api/rooms/"
        const val ROOMS_TAGS = "/api/rooms/tags"
        const val ROOMS_POPULAR = "/api/rooms/popular"
        const val ROOMS_MY = "/api/rooms/my"
        const val ROOMS_BY_ID = "/api/rooms/{room_id}"
        const val ROOMS_JOIN = "/api/rooms/{room_id}/join"
        const val ROOMS_LEAVE = "/api/rooms/{room_id}/leave"
        const val ROOMS_PARTICIPANTS = "/api/rooms/{room_id}/participants"
        const val ROOMS_MESSAGES = "/api/rooms/{room_id}/messages"
        const val ROOMS_MESSAGES_BY_ID = "/api/rooms/messages/{message_id}"
        const val ROOMS_MUTE = "/api/rooms/{room_id}/participants/mute"
        const val ROOMS_UNMUTE = "/api/rooms/{room_id}/participants/unmute"
        const val ROOMS_BAN = "/api/rooms/{room_id}/participants/ban"
        const val ROOMS_UNBAN = "/api/rooms/{room_id}/participants/unban"
        const val ROOMS_BANNED = "/api/rooms/{room_id}/banned"
        const val ROOMS_CHANGE_ROLE = "/api/rooms/{room_id}/participants/change-role"

        const val CHAT_ROULETTE_SEARCH = "/api/chat-roulette/search"
        const val CHAT_ROULETTE_SEARCH_CANCEL = "/api/chat-roulette/search/cancel"
        const val CHAT_ROULETTE_SESSION = "/api/chat-roulette/session"
        const val CHAT_ROULETTE_SESSION_EXTEND = "/api/chat-roulette/session/extend"
        const val CHAT_ROULETTE_SESSION_END = "/api/chat-roulette/session/end"
        const val CHAT_ROULETTE_RATE = "/api/chat-roulette/rate"
        const val CHAT_ROULETTE_REPORT = "/api/chat-roulette/report"
        const val CHAT_ROULETTE_STATISTICS = "/api/chat-roulette/statistics"
        const val CHAT_ROULETTE_MESSAGES = "/api/chat-roulette/messages"

        const val WS_ROOMS = "/ws/rooms/{room_id}"
        const val WS_CHAT_ROULETTE = "/ws/chat-roulette/{session_id}"
    }

    object QueryParams {
        const val QUERY = "query"
        const val INTEREST_ID = "interest_id"
        const val TAGS = "tags"
        const val LIMIT = "limit"
        const val OFFSET = "offset"
        const val BEFORE = "before"
        const val INCLUDE_BANNED = "include_banned"
    }
}