package com.example.common_ground_android.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Serializable
data class ApiErrorResponse(
    @SerialName("success")
    val success: Boolean = false,

    @SerialName("error")
    val error: ErrorDetails
)

@Serializable
data class ErrorDetails(
    @SerialName("code")
    val code: String,

    @SerialName("message")
    val message: String,

    @SerialName("timestamp")
    val timestamp: String? = null,

    @SerialName("details")
    val details: List<ValidationErrorDetail>? = null
)

@Serializable
data class ValidationErrorDetail(
    @SerialName("type")
    val type: String,

    @SerialName("loc")
    val loc: List<String>,

    @SerialName("msg")
    val msg: String,

    @SerialName("input")
    val input: String? = null,

    @SerialName("ctx")
    val ctx: Map<String, String>? = null
)

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val errorCode: String? = null,
        val errorMessage: String,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing>()

    object Loading : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
}

object ErrorParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseErrorJson(jsonText: String): ApiErrorResponse? {
        return try {
            json.decodeFromString<ApiErrorResponse>(jsonText)
        } catch (e: Exception) {
            null
        }
    }

    fun parseErrorCode(jsonText: String): String? {
        return try {
            val jsonElement = json.parseToJsonElement(jsonText)
            val errorObject = jsonElement.jsonObject["error"]?.jsonObject
            errorObject?.get("code")?.toString()?.trim('"')
        } catch (e: Exception) {
            null
        }
    }

    fun parseErrorMessage(jsonText: String): String {
        return try {
            val jsonElement = json.parseToJsonElement(jsonText)
            val errorObject = jsonElement.jsonObject["error"]?.jsonObject
            errorObject?.get("message")?.toString()?.trim('"') ?: jsonText
        } catch (e: Exception) {
            jsonText
        }
    }
}