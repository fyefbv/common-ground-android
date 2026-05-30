package com.example.common_ground_android.ui.viewmodels.chat_roulette

sealed class ChatRouletteEvent {
    data class Error(val message: String, val errorCode: String?) : ChatRouletteEvent()
    data class Success(val message: String, val code: String? = null) : ChatRouletteEvent()
}