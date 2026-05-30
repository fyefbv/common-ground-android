package com.example.common_ground_android.ui.viewmodels.chat_roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.network.repository.InterestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectInterestsViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SelectInterestsState>(SelectInterestsState.Loading)
    val state: StateFlow<SelectInterestsState> = _state.asStateFlow()

    init {
        loadInterests()
    }

    private fun loadInterests() {
        viewModelScope.launch {
            when (val profileResult = profileRepository.getCurrentProfile()) {
                is NetworkResult.Success -> {
                    val profile = Profile.fromResponse(profileResult.data)
                    when (val interestsResult = profileRepository.getProfileInterests(profile.username)) {
                        is NetworkResult.Success -> {
                            val interests = interestsResult.data.map { Interest.fromResponse(it) }
                            _state.value = SelectInterestsState.Ready(interests, emptySet())
                        }
                        is NetworkResult.Error -> {
                            _state.value = SelectInterestsState.Error(interestsResult.errorMessage, interestsResult.errorCode)
                        }
                        else -> {}
                    }
                }
                is NetworkResult.Error -> {
                    _state.value = SelectInterestsState.Error(profileResult.errorMessage, profileResult.errorCode)
                }
                else -> {}
            }
        }
    }

    fun toggleInterest(interestId: String) {
        val current = _state.value
        if (current is SelectInterestsState.Ready) {
            val newSelected = if (current.selectedIds.contains(interestId)) {
                current.selectedIds - interestId
            } else {
                if (current.selectedIds.size < 5) current.selectedIds + interestId else current.selectedIds
            }
            _state.value = current.copy(selectedIds = newSelected)
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}