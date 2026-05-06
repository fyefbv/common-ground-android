package com.example.common_ground_android.ui.viewmodels.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.repository.RepositoryFactory

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                roomRepository = RepositoryFactory.createRoomRepository(),
                profileRepository = RepositoryFactory.createProfileRepository(),
                authRepository = RepositoryFactory.createAuthRepository(),
                interestRepository = RepositoryFactory.createInterestRepository(),
                tokenManager = KtorClientFactory.getTokenManager()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}