package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.request.EndSessionRequest
import com.example.common_ground_android.network.model.request.RatePartnerRequest
import com.example.common_ground_android.network.model.request.ReportPartnerRequest
import com.example.common_ground_android.network.model.request.SendChatRouletteMessageRequest
import com.example.common_ground_android.network.model.request.StartSearchRequest
import com.example.common_ground_android.network.model.response.ChatRouletteMessageResponse
import com.example.common_ground_android.network.model.response.ChatRouletteSessionResponse
import com.example.common_ground_android.network.model.response.DeleteChatRouletteResponse
import com.example.common_ground_android.network.model.response.SearchResponse
import com.example.common_ground_android.network.model.response.SessionExtensionResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ChatRouletteService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun startSearch(priorityInterestIds: List<String> = emptyList()): SearchResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SEARCH)
            contentType(ContentType.Application.Json)
            setBody(StartSearchRequest(priorityInterestIds))
        }.body()
    }

    suspend fun cancelSearch(): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SEARCH_CANCEL)
        }.body()
    }

    suspend fun getActiveSession(): ChatRouletteSessionResponse {
        return client.get {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION)
        }.body()
    }

    suspend fun extendSession(): SessionExtensionResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION_EXTEND)
        }.body()
    }

    suspend fun endSession(reason: String? = null): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION_END)
            contentType(ContentType.Application.Json)
            setBody(EndSessionRequest(reason))
        }.body()
    }

    suspend fun ratePartner(rating: Int, feedback: String? = null): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_RATE)
            contentType(ContentType.Application.Json)
            setBody(RatePartnerRequest(rating, feedback))
        }.body()
    }

    suspend fun reportPartner(reason: String? = null, details: String? = null): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_REPORT)
            contentType(ContentType.Application.Json)
            setBody(ReportPartnerRequest(reason, details))
        }.body()
    }

    suspend fun rejectExtension(): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION_EXTENSION_REJECT)
        }.body()
    }

    suspend fun cancelExtensionRequest(): DeleteChatRouletteResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION_EXTENSION_CANCEL)
        }.body()
    }

    suspend fun sendMessage(content: String): ChatRouletteMessageResponse {
        return client.post {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_MESSAGES)
            contentType(ContentType.Application.Json)
            setBody(SendChatRouletteMessageRequest(content))
        }.body()
    }

    suspend fun getSessionMessages(limit: Int? = null, before: String? = null): List<ChatRouletteMessageResponse> {
        return client.get {
            url(ApiConfig.Endpoints.CHAT_ROULETTE_SESSION_MESSAGES)
            limit?.let { parameter("limit", it.toString()) }
            before?.let { parameter("before", it) }
        }.body()
    }
}