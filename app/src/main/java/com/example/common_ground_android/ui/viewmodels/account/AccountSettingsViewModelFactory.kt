package com.example.common_ground_android.ui.viewmodels.account

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.repository.RepositoryFactory

class AccountSettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountSettingsViewModel(
                userRepository = RepositoryFactory.createUserRepository(),
                authRepository = RepositoryFactory.createAuthRepository()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}