package com.example.common_ground_android.ui.viewmodels.rooms

sealed class RoomEvent {
    data class Error(val message: String, val errorCode: String?) : RoomEvent()
    data class Success(val message: String, val code: String? = null) : RoomEvent()
}