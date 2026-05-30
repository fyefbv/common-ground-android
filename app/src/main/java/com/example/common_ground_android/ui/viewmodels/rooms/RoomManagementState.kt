package com.example.common_ground_android.ui.viewmodels.rooms

import com.example.common_ground_android.network.model.domain.Participant
import com.example.common_ground_android.network.model.domain.Room

sealed class RoomManagementState {
    object Idle : RoomManagementState()
    object Loading : RoomManagementState()
    data class Success(
        val room: Room,
        val participants: List<Participant>,
        val currentRole: String,
        val isBanned: Boolean,
        val isMuted: Boolean
    ) : RoomManagementState()
    data class Error(val message: String, val errorCode: String? = null) : RoomManagementState()
}