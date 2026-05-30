package com.example.common_ground_android.ui.viewmodels.rooms

import com.example.common_ground_android.network.model.domain.Room

sealed class CreateRoomState {
    object Idle : CreateRoomState()
    object Loading : CreateRoomState()
    data class Success(val room: Room) : CreateRoomState()
    data class Error(val message: String, val errorCode: String? = null) : CreateRoomState()
}