package com.example.common_ground_android.ui.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.ProfileStatistics
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileViewState>(ProfileViewState.Loading)
    val state: StateFlow<ProfileViewState> = _state.asStateFlow()

    fun loadProfile(profileId: String) {
        viewModelScope.launch {
            _state.value = ProfileViewState.Loading

            when (val profileResult = profileRepository.getProfileById(profileId)) {
                is NetworkResult.Success -> {
                    val profile = Profile.fromResponse(profileResult.data)

                    val interests = when (val interestsResult = profileRepository.getProfileInterests(profile.username)) {
                        is NetworkResult.Success -> interestsResult.data.map { Interest.fromResponse(it) }
                        else -> emptyList()
                    }

                    val statistics = when (val statsResult = profileRepository.getProfileStatisticsById(profileId)) {
                        is NetworkResult.Success -> ProfileStatistics.fromResponse(statsResult.data)
                        else -> null
                    }

                    _state.value = ProfileViewState.Success(profile, interests, statistics)
                }
                is NetworkResult.Error -> {
                    _state.value = ProfileViewState.Error(profileResult.errorMessage, profileResult.errorCode)
                }
                else -> {}
            }
        }
    }

    private fun getCurrentProfileId(): String {
        return try {
            KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}