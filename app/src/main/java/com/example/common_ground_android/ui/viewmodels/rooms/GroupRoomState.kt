package com.example.common_ground_android.ui.viewmodels.rooms

import com.example.common_ground_android.network.model.domain.Message
import com.example.common_ground_android.network.model.domain.Room

sealed class GroupRoomState {
    object Loading : GroupRoomState()
    data class Success(
        val room: Room,
        val messages: List<Message>,
        val onlineCount: Int,
        val isMuted: Boolean = false,
        val currentRole: String = "MEMBER"
    ) : GroupRoomState()
    data class Error(val message: String, val errorCode: String? = null) : GroupRoomState()
}