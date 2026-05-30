package com.example.common_ground_android.ui.viewmodels.chat_roulette

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.repository.RepositoryFactory

class SelectInterestsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectInterestsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectInterestsViewModel(
                profileRepository = RepositoryFactory.createProfileRepository(),
                authRepository = RepositoryFactory.createAuthRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}