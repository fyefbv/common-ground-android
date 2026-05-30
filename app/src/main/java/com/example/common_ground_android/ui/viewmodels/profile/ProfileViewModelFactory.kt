package com.example.common_ground_android.ui.viewmodels.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.repository.RepositoryFactory

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProfileSelectorViewModel::class.java) -> {
                val profileRepository = RepositoryFactory.createProfileRepository()
                val interestRepository = RepositoryFactory.createInterestRepository()
                val authRepository = RepositoryFactory.createAuthRepository()
                ProfileSelectorViewModel(profileRepository, interestRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(CreateProfileViewModel::class.java) -> {
                val profileRepository = RepositoryFactory.createProfileRepository()
                val interestRepository = RepositoryFactory.createInterestRepository()
                val authRepository = RepositoryFactory.createAuthRepository()
                CreateProfileViewModel(profileRepository, interestRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val profileRepository = RepositoryFactory.createProfileRepository()
                val interestRepository = RepositoryFactory.createInterestRepository()
                val authRepository = RepositoryFactory.createAuthRepository()
                val tokenManager = KtorClientFactory.getTokenManager()
                ProfileViewModel(profileRepository, interestRepository, authRepository, tokenManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}