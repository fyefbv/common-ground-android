package com.example.common_ground_android.network.repository

import com.example.common_ground_android.network.api.service.RoomService
import com.example.common_ground_android.network.model.request.room.*
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.response.room.*
import com.example.common_ground_android.network.utils.ErrorHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomRepository(
    private val roomService: RoomService,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun searchRooms(
        query: String? = null,
        interestIds: List<String>? = null,
        tags: List<String>? = null,
        myRooms: Boolean = false,
        sortBy: String = "created_at",
        sortOrder: String = "desc",
        limit: Int = 50,
        offset: Int = 0
    ): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val filter = RoomFilter(query, interestIds, tags, myRooms, sortBy, sortOrder, limit, offset)
                val response = roomService.searchRooms(filter)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.searchRooms")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getAllTags(): NetworkResult<List<String>> = withContext(dispatcher) {
        try {
            val response = roomService.getAllTags()
            NetworkResult.Success(response)
        } catch (e: Exception) {
            val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getAllTags")
            NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
        }
    }

    suspend fun getPopularRooms(limit: Int = 20): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getPopularRooms(limit)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getPopularRooms")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getMyRooms(): NetworkResult<List<RoomResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getMyRooms()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getMyRooms")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.createRoom")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getRoomById(roomId: String): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getRoomById(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getRoomById")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.updateRoom")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun deleteRoom(roomId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.deleteRoom(roomId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.deleteRoom")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun joinRoom(roomId: String): NetworkResult<RoomResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.joinRoom(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.joinRoom")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun leaveRoom(roomId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.leaveRoom(roomId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.leaveRoom")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getRoomParticipants(roomId: String, includeBanned: Boolean = false): NetworkResult<List<ParticipantResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getRoomParticipants(roomId, includeBanned)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getRoomParticipants")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun kickParticipant(roomId: String, profileId: String, reason: String? = null): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.kickParticipant(roomId, profileId, reason)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.kickParticipant")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getRoomMessages")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.sendMessage")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun updateMessage(messageId: String, content: String): NetworkResult<MessageResponse> {
        return withContext(dispatcher) {
            try {
                val response = roomService.updateMessage(messageId, content)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.updateMessage")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun deleteMessage(messageId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.deleteMessage(messageId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.deleteMessage")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun muteParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.muteParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.muteParticipant")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun unmuteParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.unmuteParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.unmuteParticipant")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun banParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.banParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.banParticipant")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun unbanParticipant(roomId: String, participantId: String): NetworkResult<Unit> {
        return withContext(dispatcher) {
            try {
                roomService.unbanParticipant(roomId, participantId)
                NetworkResult.Success(Unit)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.unbanParticipant")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }

    suspend fun getBannedParticipants(roomId: String): NetworkResult<List<ParticipantResponse>> {
        return withContext(dispatcher) {
            try {
                val response = roomService.getBannedParticipants(roomId)
                NetworkResult.Success(response)
            } catch (e: Exception) {
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.getBannedParticipants")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
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
                val (errorCode, errorMessage) = ErrorHandler.handleException(e, "RoomRepository.changeParticipantRole")
                NetworkResult.Error(errorCode = errorCode, errorMessage = errorMessage, exception = e)
            }
        }
    }
}