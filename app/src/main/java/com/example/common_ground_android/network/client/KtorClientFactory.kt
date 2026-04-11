package com.example.common_ground_android.network.client

import android.content.Context

object KtorClientFactory {
    private var ktorClient: KtorClient? = null
    private var tokenManager: TokenManager? = null

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

    fun close() {
        ktorClient?.close()
        ktorClient = null
        tokenManager = null
    }
}