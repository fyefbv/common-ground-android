package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.RoomService
import com.example.common_ground_android.network.model.request.room.*
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.room.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomRepository(
    private val roomService: RoomService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun searchRooms(
        query: String? = null,
        interestId: String? = null,
        tags: List<String>? = null,
        limit: Int = 50,
        offset: Int = 0
    ): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val filter = RoomFilter(query, interestId, tags, limit, offset)
                val response = roomService.searchRooms(filter)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getPopularRooms(limit: Int = 20): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getPopularRooms(limit)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getMyRooms(): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getMyRooms()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun createRoom(
        name: String,
        description: String? = null,
        primaryInterestId: String? = null,
        tags: List<String> = emptyList(),
        maxParticipants: Int = 50,
        isPrivate: Boolean = false
    ): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val request = CreateRoomRequest(name, description, primaryInterestId, tags, maxParticipants, isPrivate)
                val response = roomService.createRoom(request)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getRoomById(roomId: String): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getRoomById(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updateRoom(
        roomId: String,
        name: String? = null,
        description: String? = null,
        tags: List<String>? = null,
        maxParticipants: Int? = null,
        isPrivate: Boolean? = null
    ): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val request = UpdateRoomRequest(name, description, tags, maxParticipants, isPrivate)
                val response = roomService.updateRoom(roomId, request)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun deleteRoom(roomId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.deleteRoom(roomId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun joinRoom(roomId: String): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.joinRoom(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun leaveRoom(roomId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.leaveRoom(roomId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getRoomParticipants(roomId: String, includeBanned: Boolean = false): NetworkResult<List<ParticipantResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getRoomParticipants(roomId, includeBanned)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun kickParticipant(roomId: String, profileId: String, reason: String? = null): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.kickParticipant(roomId, profileId, reason)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getRoomMessages(
        roomId: String,
        before: String? = null,
        limit: Int = 50
    ): NetworkResult<MessagesResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getRoomMessages(roomId, before, limit)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun sendMessage(roomId: String, content: String, parentMessageId: String? = null): NetworkResult<MessageResponse> {
        return withContext(dispatcher) {
            try {
                val request = SendMessageRequest(content, parentMessageId)
                val response = roomService.sendMessage(roomId, request)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun updateMessage(messageId: String, content: String): NetworkResult<MessageResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.updateMessage(messageId, content)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun deleteMessage(messageId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.deleteMessage(messageId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun muteParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.muteParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun unmuteParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.unmuteParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun banParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.banParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun unbanParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                val response = roomService.unbanParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun getBannedParticipants(roomId: String): NetworkResult<List<ParticipantResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getBannedParticipants(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }

    suspend fun changeParticipantRole(
        roomId: String,
        targetProfileId: String,
        newRole: String
    ): NetworkResult<ParticipantResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.changeParticipantRole(roomId, targetProfileId, newRole)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.Error(exception = e)
            }
        }
    }
}