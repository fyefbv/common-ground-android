package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.request.room.*
import com.example.common_ground_android.network.model.response.room.*
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.client.replacePathParams
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.Parameters

class RoomService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun searchRooms(filter: RoomFilter): List<RoomResponse> {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS)

            parameters {
                filter.query?.let { append(ApiConfig.QueryParams.QUERY, it) }
                filter.interestId?.let { append(ApiConfig.QueryParams.INTEREST_ID, it) }
                filter.tags?.forEach { tag ->
                    append(ApiConfig.QueryParams.TAGS, tag)
                }
                append(ApiConfig.QueryParams.LIMIT, filter.limit.toString())
                append(ApiConfig.QueryParams.OFFSET, filter.offset.toString())
            }
        }.body()
    }

    suspend fun getPopularRooms(limit: Int = 20): List<RoomResponse> {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_POPULAR)
            parameter(ApiConfig.QueryParams.LIMIT, limit)
        }.body()
    }

    suspend fun getMyRooms(): List<RoomResponse> {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_MY)
        }.body()
    }

    suspend fun createRoom(request: CreateRoomRequest): RoomResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getRoomById(roomId: String): RoomResponse {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_BY_ID.replacePathParams("room_id" to roomId))
        }.body()
    }

    suspend fun updateRoom(roomId: String, request: UpdateRoomRequest): RoomResponse {
        return client.patch {
            url(ApiConfig.Endpoints.ROOMS_BY_ID.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteRoom(roomId: String): DeleteResponse {
        return client.delete {
            url(ApiConfig.Endpoints.ROOMS_BY_ID.replacePathParams("room_id" to roomId))
        }.body()
    }

    suspend fun joinRoom(roomId: String): RoomResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_JOIN.replacePathParams("room_id" to roomId))
        }.body()
    }

    suspend fun leaveRoom(roomId: String): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_LEAVE.replacePathParams("room_id" to roomId))
        }.body()
    }

    suspend fun getRoomParticipants(roomId: String, includeBanned: Boolean = false): List<ParticipantResponse> {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_PARTICIPANTS.replacePathParams("room_id" to roomId))
            parameter(ApiConfig.QueryParams.INCLUDE_BANNED, includeBanned.toString())
        }.body()
    }

    suspend fun kickParticipant(roomId: String, profileId: String, reason: String? = null): DeleteResponse {
        return client.delete {
            url(ApiConfig.Endpoints.ROOMS_PARTICIPANTS.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(KickParticipantRequest(profileId, reason))
        }.body()
    }

    suspend fun getRoomMessages(
        roomId: String,
        before: String? = null,
        limit: Int = 50
    ): MessagesResponse {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_MESSAGES.replacePathParams("room_id" to roomId))
            parameters {
                before?.let { append(ApiConfig.QueryParams.BEFORE, it) }
                append(ApiConfig.QueryParams.LIMIT, limit.toString())
            }
        }.body()
    }

    suspend fun sendMessage(roomId: String, request: SendMessageRequest): MessageResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_MESSAGES.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateMessage(messageId: String, content: String): MessageResponse {
        return client.patch {
            url(ApiConfig.Endpoints.ROOMS_MESSAGES_BY_ID.replacePathParams("message_id" to messageId))
            contentType(ContentType.Application.Json)
            setBody(UpdateMessageRequest(content))
        }.body()
    }

    suspend fun deleteMessage(messageId: String): DeleteResponse {
        return client.delete {
            url(ApiConfig.Endpoints.ROOMS_MESSAGES_BY_ID.replacePathParams("message_id" to messageId))
        }.body()
    }

    suspend fun muteParticipant(roomId: String, participantId: String): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_MUTE.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(MuteParticipantRequest(participantId))
        }.body()
    }

    suspend fun unmuteParticipant(roomId: String, participantId: String): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_UNMUTE.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(MuteParticipantRequest(participantId))
        }.body()
    }

    suspend fun banParticipant(roomId: String, participantId: String): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_BAN.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(BanParticipantRequest(participantId))
        }.body()
    }

    suspend fun unbanParticipant(roomId: String, participantId: String): DeleteResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_UNBAN.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(BanParticipantRequest(participantId))
        }.body()
    }

    suspend fun getBannedParticipants(roomId: String): List<ParticipantResponse> {
        return client.get {
            url(ApiConfig.Endpoints.ROOMS_BANNED.replacePathParams("room_id" to roomId))
        }.body()
    }

    suspend fun changeParticipantRole(roomId: String, targetProfileId: String, newRole: String): ParticipantResponse {
        return client.post {
            url(ApiConfig.Endpoints.ROOMS_CHANGE_ROLE.replacePathParams("room_id" to roomId))
            contentType(ContentType.Application.Json)
            setBody(ChangeRoleRequest(targetProfileId, newRole))
        }.body()
    }
}