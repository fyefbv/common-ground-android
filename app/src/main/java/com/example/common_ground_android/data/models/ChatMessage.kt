package com.example.common_ground_android.data.models

import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val fromMe: Boolean,
    val time: String,
    val userName: String = ""
) {
    companion object {
        fun createUserMessage(text: String): ChatMessage {
            return ChatMessage(
                text = text,
                fromMe = true,
                time = getCurrentTime()
            )
        }

        fun createPartnerMessage(text: String, userName: String = ""): ChatMessage {
            return ChatMessage(
                text = text,
                fromMe = false,
                time = getCurrentTime(),
                userName = userName
            )
        }

        private fun getCurrentTime(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

data class ChatPartner(
    val name: String,
    val interests: List<String>
)