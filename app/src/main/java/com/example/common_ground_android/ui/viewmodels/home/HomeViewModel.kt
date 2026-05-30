package com.example.common_ground_android.ui.viewmodels.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.TokenManager
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.network.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val roomRepository: RoomRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val interestRepository: InterestRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _interestsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val interestsMap: StateFlow<Map<String, String>> = _interestsMap.asStateFlow()

    private val _shouldScrollToTop = MutableStateFlow(false)
    val shouldScrollToTop: StateFlow<Boolean> = _shouldScrollToTop.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            when (val profileResult = profileRepository.getCurrentProfile()) {
                is NetworkResult.Success -> {
                    val profile = Profile.fromResponse(profileResult.data)
                    _profile.value = profile
                    when (val interestsResult = interestRepository.getAllInterests()) {
                        is NetworkResult.Success -> {
                            _interestsMap.value = interestsResult.data.associate { it.id to it.name }
                        }
                        is NetworkResult.Error -> {
                            _state.value = HomeState.Error(interestsResult.errorMessage, interestsResult.errorCode)
                            return@launch
                        }
                        else -> {}
                    }
                    loadRooms(profile, scrollToTop = false)
                }
                is NetworkResult.Error -> {
                    _state.value = HomeState.Error(profileResult.errorMessage, profileResult.errorCode)
                }
                else -> {}
            }
        }
    }

    fun getInterestName(interestId: String?): String? = interestId?.let { _interestsMap.value[it] }

    private suspend fun loadRooms(profile: Profile?, scrollToTop: Boolean = false) {
        when (val result = roomRepository.getPopularRooms(20)) {
            is NetworkResult.Success -> {
                val rooms = result.data.map { Room.fromResponse(it) }
                _shouldScrollToTop.value = scrollToTop
                if (rooms.isEmpty()) _state.value = HomeState.Empty(profile)
                else _state.value = HomeState.Success(rooms, profile)
            }
            is NetworkResult.Error -> {
                _state.value = HomeState.Error(result.errorMessage, result.errorCode)
            }
            else -> {}
        }
    }

    fun refreshRooms() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            loadRooms(_profile.value, scrollToTop = true)
        }
    }


    fun switchProfile() {
        viewModelScope.launch {
            tokenManager.clearProfileId()
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun getCurrentProfileId(): String = tokenManager.getProfileIdSync() ?: ""

    fun resetScrollToTop() {
        _shouldScrollToTop.value = false
    }

    fun joinRoom(room: Room) {
        viewModelScope.launch {
            when (val result = roomRepository.joinRoom(room.id)) {
                is NetworkResult.Success -> {
                    loadRooms(_profile.value, scrollToTop = false)
                }
                is NetworkResult.Error -> {
                    _state.value = HomeState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun leaveRoom(room: Room) {
        viewModelScope.launch {
            when (val result = roomRepository.leaveRoom(room.id)) {
                is NetworkResult.Success -> {
                    loadRooms(_profile.value, scrollToTop = false)
                }
                is NetworkResult.Error -> {
                    _state.value = HomeState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }
}