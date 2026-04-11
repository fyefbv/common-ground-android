package com.example.common_ground_android.ui.viewmodels.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.repository.RepositoryFactory

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val authRepository = RepositoryFactory.createAuthRepository()
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository) as T
        } else if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            val authRepository = RepositoryFactory.createAuthRepository()
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}