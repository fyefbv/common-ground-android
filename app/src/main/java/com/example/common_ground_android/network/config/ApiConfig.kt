package com.example.common_ground_android.network.config

object ApiConfig {
    const val BASE_URL = "http://localhost:8000/api"
    const val WS_BASE_URL = "ws://localhost:8000/ws"

    const val CONNECT_TIMEOUT = 30_000L
    const val SOCKET_TIMEOUT = 30_000L
    const val REQUEST_TIMEOUT = 30_000L

    const val WS_PING_INTERVAL = 25_000L
    const val WS_RECONNECT_DELAY = 3_000L
    const val WS_MAX_RECONNECT_ATTEMPTS = 5

    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"

    const val CONTENT_TYPE_JSON = "application/json"
    const val DEFAULT_LANGUAGE = "ru"

    const val TOKEN_TYPE_BEARER = "Bearer"
    const val TOKEN_EXPIRY_BUFFER = 60_000L

    object Endpoints {
        const val AUTH_REGISTER = "/auth/register"
        const val AUTH_LOGIN = "/auth/login"
        const val AUTH_SELECT_PROFILE = "/auth/select-profile"
        const val AUTH_REFRESH = "/auth/refresh"

        const val USERS_ME = "/users/me"

        const val PROFILES = "/profiles"
        const val PROFILES_ME = "/profiles/me"
        const val PROFILES_BY_USERNAME = "/profiles/{username}"
        const val PROFILES_AVATAR = "/profiles/me/avatar"
        const val PROFILES_INTERESTS = "/profiles/{username}/interests"
        const val PROFILES_ME_INTERESTS = "/profiles/me/interests"

        const val INTERESTS = "/interests"

        const val ROOMS = "/rooms"
        const val ROOMS_POPULAR = "/rooms/popular"
        const val ROOMS_MY = "/rooms/my"
        const val ROOMS_BY_ID = "/rooms/{room_id}"
        const val ROOMS_JOIN = "/rooms/{room_id}/join"
        const val ROOMS_LEAVE = "/rooms/{room_id}/leave"
        const val ROOMS_PARTICIPANTS = "/rooms/{room_id}/participants"
        const val ROOMS_MESSAGES = "/rooms/{room_id}/messages"
        const val ROOMS_MESSAGES_BY_ID = "/rooms/messages/{message_id}"
        const val ROOMS_MUTE = "/rooms/{room_id}/participants/mute"
        const val ROOMS_UNMUTE = "/rooms/{room_id}/participants/unmute"
        const val ROOMS_BAN = "/rooms/{room_id}/participants/ban"
        const val ROOMS_UNBAN = "/rooms/{room_id}/participants/unban"
        const val ROOMS_BANNED = "/rooms/{room_id}/banned"
        const val ROOMS_CHANGE_ROLE = "/rooms/{room_id}/participants/change-role"

        const val CHAT_ROULETTE_SEARCH = "/chat-roulette/search"
        const val CHAT_ROULETTE_SEARCH_CANCEL = "/chat-roulette/search/cancel"
        const val CHAT_ROULETTE_SESSION = "/chat-roulette/session"
        const val CHAT_ROULETTE_SESSION_EXTEND = "/chat-roulette/session/extend"
        const val CHAT_ROULETTE_SESSION_END = "/chat-roulette/session/end"
        const val CHAT_ROULETTE_RATE = "/chat-roulette/rate"
        const val CHAT_ROULETTE_REPORT = "/chat-roulette/report"
        const val CHAT_ROULETTE_STATISTICS = "/chat-roulette/statistics"
        const val CHAT_ROULETTE_MESSAGES = "/chat-roulette/messages"

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