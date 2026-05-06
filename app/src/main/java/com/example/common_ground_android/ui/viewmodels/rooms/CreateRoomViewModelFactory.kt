package com.example.common_ground_android.ui.viewmodels.rooms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.repository.RepositoryFactory

class CreateRoomViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateRoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateRoomViewModel(
                roomRepository = RepositoryFactory.createRoomRepository(),
                interestRepository = RepositoryFactory.createInterestRepository(),
                authRepository = RepositoryFactory.createAuthRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}