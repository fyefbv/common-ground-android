package com.example.common_ground_android.ui.viewmodels.chat_roulette

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.repository.RepositoryFactory

class ChatRouletteViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatRouletteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatRouletteViewModel(
                chatRouletteRepository = RepositoryFactory.createChatRouletteRepository(),
                profileRepository = RepositoryFactory.createProfileRepository(),
                authRepository = RepositoryFactory.createAuthRepository(),
                interestRepository = RepositoryFactory.createInterestRepository(),
                webSocketRepo = KtorClientFactory.getChatRouletteWebSocketRepository(),
                currentProfileId = KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}