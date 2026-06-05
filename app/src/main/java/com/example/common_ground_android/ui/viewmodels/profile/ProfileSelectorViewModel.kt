package com.example.common_ground_android.ui.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.R
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.utils.Res
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class ProfileSelectorViewModel(
    private val profileRepository: ProfileRepository,
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileSelectorState>(ProfileSelectorState.Loading)
    val state: StateFlow<ProfileSelectorState> = _state

    fun loadProfiles() {
        viewModelScope.launch {
            _state.value = ProfileSelectorState.Loading

            when (val result = profileRepository.getMyProfiles()) {
                is NetworkResult.Success -> {
                    val profileResponses = result.data
                    val profilesWithInterests = mutableListOf<Profile>()

                    val deferredInterests = profileResponses.map { profileResponse ->
                        async {
                            val username = profileResponse.username
                            val interestsResult = profileRepository.getProfileInterests(username)
                            val interests = when (interestsResult) {
                                is NetworkResult.Success -> interestsResult.data.map { Interest.fromResponse(it) }
                                else -> emptyList()
                            }
                            Profile.fromResponse(profileResponse).copy(interests = interests)
                        }
                    }
                    profilesWithInterests.addAll(deferredInterests.awaitAll())

                    if (profilesWithInterests.isEmpty()) {
                        _state.value = ProfileSelectorState.Empty
                    } else {
                        _state.value = ProfileSelectorState.Success(profilesWithInterests)
                    }
                }
                is NetworkResult.Error -> {
                    _state.value = ProfileSelectorState.Error(result.errorMessage, result.errorCode)
                }
                else -> {
                    _state.value = ProfileSelectorState.Error(Res.getString(R.string.error_unknown_error))
                }
            }
        }
    }

    suspend fun selectProfile(profile: Profile): Boolean {
        return when (val result = authRepository.selectProfile(profile.id)) {
            is NetworkResult.Success -> true
            else -> false
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}