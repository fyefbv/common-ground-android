package com.example.common_ground_android.network.client

import android.content.Context

object KtorClientFactory {
    private var ktorClient: KtorClient? = null

    fun create(context: Context): KtorClient {
        return ktorClient ?: synchronized(this) {
            ktorClient ?: KtorClient(TokenManager(context)).also {
                ktorClient = it
            }
        }
    }

    fun getInstance(): KtorClient {
        return ktorClient ?: throw IllegalStateException("KtorClient not initialized. Call create() first.")
    }

    fun close() {
        ktorClient?.close()
        ktorClient = null
    }
}