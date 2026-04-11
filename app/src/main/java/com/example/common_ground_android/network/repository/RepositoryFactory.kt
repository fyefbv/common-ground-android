package com.example.common_ground_android.network.repository

import android.content.Context
import com.example.common_ground_android.network.api.service.*
import com.example.common_ground_android.network.api.websocket.ChatRouletteWebSocketServiceImpl
import com.example.common_ground_android.network.api.websocket.RoomWebSocketServiceImpl
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.client.TokenManager
import com.example.common_ground_android.network.repository.websocket.ChatRouletteWebSocketRepository
import com.example.common_ground_android.network.repository.websocket.RoomWebSocketRepository
import kotlinx.coroutines.Dispatchers

object RepositoryFactory {
    private var authRepository: AuthRepository? = null
    private var userRepository: UserRepository? = null
    private var profileRepository: ProfileRepository? = null
    private var interestRepository: InterestRepository? = null
    private var roomRepository: RoomRepository? = null
    private var chatRouletteRepository: ChatRouletteRepository? = null
    private var roomWebSocketRepository: RoomWebSocketRepository? = null
    private var chatRouletteWebSocketRepository: ChatRouletteWebSocketRepository? = null

    fun createAuthRepository(): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: AuthRepository(
                authService = AuthService(),
                tokenManager = KtorClientFactory.getTokenManager(),
                dispatcher = Dispatchers.IO
            ).also { authRepository = it }
        }
    }

    fun createUserRepository(): UserRepository {
        return userRepository ?: synchronized(this) {
            userRepository ?: UserRepository(
                userService = UserService(),
                dispatcher = Dispatchers.IO
            ).also { userRepository = it }
        }
    }

    fun createProfileRepository(): ProfileRepository {
        return profileRepository ?: synchronized(this) {
            profileRepository ?: ProfileRepository(
                profileService = ProfileService(),
                dispatcher = Dispatchers.IO
            ).also { profileRepository = it }
        }
    }

    fun createInterestRepository(): InterestRepository {
        return interestRepository ?: synchronized(this) {
            interestRepository ?: InterestRepository(
                interestService = InterestService(),
                dispatcher = Dispatchers.IO
            ).also { interestRepository = it }
        }
    }

    fun createRoomRepository(): RoomRepository {
        return roomRepository ?: synchronized(this) {
            roomRepository ?: RoomRepository(
                roomService = RoomService(),
                dispatcher = Dispatchers.IO
            ).also { roomRepository = it }
        }
    }

    fun createChatRouletteRepository(): ChatRouletteRepository {
        return chatRouletteRepository ?: synchronized(this) {
            chatRouletteRepository ?: ChatRouletteRepository(
                chatRouletteService = ChatRouletteService(),
                dispatcher = Dispatchers.IO
            ).also { chatRouletteRepository = it }
        }
    }

    fun createRoomWebSocketRepository(): RoomWebSocketRepository {
        return roomWebSocketRepository ?: synchronized(this) {
            roomWebSocketRepository ?: RoomWebSocketRepository(
                webSocketService = RoomWebSocketServiceImpl()
            ).also { roomWebSocketRepository = it }
        }
    }

    fun createChatRouletteWebSocketRepository(): ChatRouletteWebSocketRepository {
        return chatRouletteWebSocketRepository ?: synchronized(this) {
            chatRouletteWebSocketRepository ?: ChatRouletteWebSocketRepository(
                webSocketService = ChatRouletteWebSocketServiceImpl()
            ).also { chatRouletteWebSocketRepository = it }
        }
    }

    suspend fun clear() {
        authRepository = null
        userRepository = null
        profileRepository = null
        interestRepository = null
        roomRepository = null
        chatRouletteRepository = null
        roomWebSocketRepository?.disconnect()
        chatRouletteWebSocketRepository?.disconnect()
        roomWebSocketRepository = null
        chatRouletteWebSocketRepository = null
    }
}