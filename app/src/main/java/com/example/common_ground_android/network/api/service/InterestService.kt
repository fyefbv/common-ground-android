package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.response.InterestResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.utils.LocaleUtils
import io.ktor.client.call.*
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.*

class InterestService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun getAllInterests(): List<InterestResponse> {
        return client.get {
            attributes.put(AuthCircuitBreaker, Unit)
            url(ApiConfig.Endpoints.INTERESTS)
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, LocaleUtils.getCurrentLanguage())
        }.body()
    }

    suspend fun getInterestsBatch(interestIds: List<String>): List<InterestResponse> {
        return client.post {
            attributes.put(AuthCircuitBreaker, Unit)
            url(ApiConfig.Endpoints.INTERESTS_BATCH)
            setBody(mapOf("interest_ids" to interestIds))
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, LocaleUtils.getCurrentLanguage())
        }.body()
    }
}