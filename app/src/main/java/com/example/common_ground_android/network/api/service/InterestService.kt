package com.example.common_ground_android.network.api.service

import com.example.common_ground_android.network.config.ApiConfig
import com.example.common_ground_android.network.model.response.interest.InterestResponse
import com.example.common_ground_android.network.client.KtorClientFactory
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class InterestService {
    private val client = KtorClientFactory.getInstance().httpClient

    suspend fun getAllInterests(language: String = ApiConfig.DEFAULT_LANGUAGE): List<InterestResponse> {
        return client.get {
            url(ApiConfig.Endpoints.INTERESTS)
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, language)
        }.body()
    }

    suspend fun getInterestById(interestId: String, language: String = ApiConfig.DEFAULT_LANGUAGE): InterestResponse {
        return client.get {
            url("${ApiConfig.Endpoints.INTERESTS}/$interestId")
            header(ApiConfig.HEADER_ACCEPT_LANGUAGE, language)
        }.body()
    }
}