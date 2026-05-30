package com.example.common_ground_android.ui.viewmodels.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.repository.RepositoryFactory

class ProfileViewViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewViewModel(
                profileRepository = RepositoryFactory.createProfileRepository(),
                authRepository = RepositoryFactory.createAuthRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}