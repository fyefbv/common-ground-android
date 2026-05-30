package com.example.common_ground_android.network.client

import android.content.Context
import android.content.res.Resources
import com.example.common_ground_android.network.api.websocket.ChatRouletteWebSocketServiceImpl
import com.example.common_ground_android.network.api.websocket.RoomWebSocketServiceImpl
import com.example.common_ground_android.network.repository.websocket.ChatRouletteWebSocketRepository
import com.example.common_ground_android.network.repository.websocket.RoomWebSocketRepository

object KtorClientFactory {
    private var ktorClient: KtorClient? = null
    private var tokenManager: TokenManager? = null
    private var roomWebSocketRepo: RoomWebSocketRepository? = null
    private var chatRouletteWebSocketRepo: ChatRouletteWebSocketRepository? = null

    fun create(context: Context): KtorClient {
        return ktorClient ?: synchronized(this) {
            ktorClient ?: run {
                val tm = TokenManager(context.applicationContext)
                tokenManager = tm
                KtorClient(tm).also { ktorClient = it }
            }
        }
    }

    fun getTokenManager(): TokenManager {
        return tokenManager ?: throw IllegalStateException("Not initialized")
    }

    fun getInstance(): KtorClient {
        return ktorClient ?: throw IllegalStateException("KtorClient not initialized. Call create() first.")
    }

    fun getRoomWebSocketRepository(): RoomWebSocketRepository {
        return roomWebSocketRepo ?: synchronized(this) {
            roomWebSocketRepo ?: RoomWebSocketRepository(RoomWebSocketServiceImpl()).also {
                roomWebSocketRepo = it
            }
        }
    }

    fun getChatRouletteWebSocketRepository(): ChatRouletteWebSocketRepository {
        return chatRouletteWebSocketRepo ?: synchronized(this) {
            chatRouletteWebSocketRepo ?: ChatRouletteWebSocketRepository(ChatRouletteWebSocketServiceImpl()).also {
                chatRouletteWebSocketRepo = it
            }
        }
    }

    fun close() {
        ktorClient?.close()
        ktorClient = null
        tokenManager = null
    }
}