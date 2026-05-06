package com.example.common_ground_android.ui.viewmodels.rooms

import com.example.common_ground_android.network.model.domain.room.Room

sealed class RoomsState {
    object Loading : RoomsState()
    data class Success(val rooms: List<Room>) : RoomsState()
    object Empty : RoomsState()
    data class Error(val message: String, val errorCode: String? = null) : RoomsState()
}