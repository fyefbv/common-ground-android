package com.example.common_ground_android.ui.viewmodels.rooms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.repository.RepositoryFactory

class GroupRoomViewModelFactory(
    private val context: Context,
    private val roomId: String,
    private val currentProfileId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GroupRoomViewModel(
                roomRepository = RepositoryFactory.createRoomRepository(),
                profileRepository = RepositoryFactory.createProfileRepository(),
                authRepository = RepositoryFactory.createAuthRepository(),
                webSocketRepo = KtorClientFactory.getRoomWebSocketRepository(),
                roomId = roomId,
                currentProfileId = currentProfileId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}