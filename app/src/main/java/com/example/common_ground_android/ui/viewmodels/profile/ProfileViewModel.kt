package com.example.common_ground_android.ui.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.TokenManager
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.ProfileStatistics
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileFormState>(ProfileFormState.Idle)
    val profileState: StateFlow<ProfileFormState> = _profileState.asStateFlow()

    private val _profileData = MutableStateFlow<Profile?>(null)
    val profileData: StateFlow<Profile?> = _profileData.asStateFlow()

    private val _statistics = MutableStateFlow<ProfileStatistics?>(null)
    val statistics: StateFlow<ProfileStatistics?> = _statistics.asStateFlow()

    private val _interests = MutableStateFlow<List<Interest>>(emptyList())
    val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    private val _selectedInterestIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedInterestIds: StateFlow<Set<String>> = _selectedInterestIds.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _newAvatarBytes = MutableStateFlow<ByteArray?>(null)
    val newAvatarBytes: StateFlow<ByteArray?> = _newAvatarBytes.asStateFlow()

    private val _deleteAvatar = MutableStateFlow(false)
    val deleteAvatar: StateFlow<Boolean> = _deleteAvatar.asStateFlow()

    private val _pendingInterestIds = MutableStateFlow<Set<String>>(emptySet())
    val pendingInterestIds: StateFlow<Set<String>> = _pendingInterestIds.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()
    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()
    private val _bioError = MutableStateFlow<String?>(null)
    val bioError: StateFlow<String?> = _bioError.asStateFlow()

    init {
        loadProfile()
        loadInterests()
        loadStatistics()
    }

    fun updateUsername(username: String) {
        _username.value = username
        _usernameError.value = when {
            username.isBlank() -> "Введите имя профиля"
            username.contains(" ") -> "Имя не должно содержать пробелы"
            !ValidationUtils.isValidUsername(username) -> "Имя должно содержать 3-20 символов (буквы, цифры, _)"
            else -> null
        }
    }

    fun updateBio(bio: String) {
        _bio.value = bio
        _bioError.value = if (bio.length > 200) "Биография не должна превышать 200 символов" else null
    }

    fun setNewAvatarBytes(bytes: ByteArray) {
        _newAvatarBytes.value = bytes
        _deleteAvatar.value = false
    }

    fun markAvatarForDeletion() {
        _deleteAvatar.value = true
        _newAvatarBytes.value = null
    }

    fun cancelAvatarChanges() {
        _newAvatarBytes.value = null
        _deleteAvatar.value = false
    }

    fun addInterest(interest: Interest) {
        if (!_pendingInterestIds.value.contains(interest.id)) {
            _pendingInterestIds.update { it + interest.id }
        }
    }

    fun removeInterest(interestId: String) {
        _pendingInterestIds.update { it - interestId }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileFormState.Loading
            when (val profileResult = profileRepository.getCurrentProfile()) {
                is NetworkResult.Success -> {
                    val currentProfile = Profile.fromResponse(profileResult.data)
                    _profileData.value = currentProfile
                    _username.value = currentProfile.username
                    _bio.value = currentProfile.bio ?: ""

                    when (val interestsResult = fetchProfileInterests(currentProfile.username)) {
                        is NetworkResult.Success -> {
                            val interestIds = interestsResult.data
                            _selectedInterestIds.value = interestIds
                            _pendingInterestIds.value = interestIds
                            _profileState.value = ProfileFormState.Idle
                        }
                        is NetworkResult.Error -> {
                            _profileState.value = ProfileFormState.Error(
                                "Профиль загружен, но не удалось загрузить интересы: ${interestsResult.errorMessage}",
                                interestsResult.errorCode
                            )
                        }
                        else -> {
                            _profileState.value = ProfileFormState.Error("Неизвестная ошибка при загрузке интересов")
                        }
                    }
                }
                is NetworkResult.Error -> {
                    _profileState.value = ProfileFormState.Error(profileResult.errorMessage, profileResult.errorCode)
                }
                else -> {
                    _profileState.value = ProfileFormState.Error("Неизвестная ошибка при загрузке профиля")
                }
            }
        }
    }

    private fun loadInterests() {
        viewModelScope.launch {
            when (val result = interestRepository.getAllInterests()) {
                is NetworkResult.Success -> {
                    _interests.value = result.data.map { Interest.fromResponse(it) }
                }
                is NetworkResult.Error -> { }
                else -> {}
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = profileRepository.getMyStatistics()) {
                is NetworkResult.Success -> {
                    _statistics.value = ProfileStatistics.fromResponse(result.data)
                }
                else -> {}
            }
        }
    }

    private suspend fun fetchProfileInterests(username: String): NetworkResult<Set<String>> {
        return when (val result = profileRepository.getProfileInterests(username)) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.map { it.id }.toSet())
            is NetworkResult.Error -> NetworkResult.Error(result.errorCode, result.errorMessage)
            else -> NetworkResult.Error(errorMessage = "Неизвестная ошибка")
        }
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun cancelEditing() {
        _profileData.value?.let { profile ->
            _username.value = profile.username
            _bio.value = profile.bio ?: ""
            _usernameError.value = null
            _bioError.value = null
            _pendingInterestIds.value = _selectedInterestIds.value
            _newAvatarBytes.value = null
            _deleteAvatar.value = false
        }
        _isEditMode.value = false
    }

    fun updateProfile() {
        updateUsername(_username.value)
        updateBio(_bio.value)

        val usernameErrorMsg = _usernameError.value
        if (usernameErrorMsg != null) {
            _profileState.value = ProfileFormState.Error(usernameErrorMsg)
            return
        }
        val bioErrorMsg = _bioError.value
        if (bioErrorMsg != null) {
            _profileState.value = ProfileFormState.Error(bioErrorMsg)
            return
        }

        viewModelScope.launch {
            _profileState.value = ProfileFormState.Loading

            when (val updateResult = profileRepository.updateMyProfile(_username.value, _bio.value.takeIf { it.isNotEmpty() })) {
                is NetworkResult.Success -> {
                    val updatedProfileResponse = updateResult.data
                    val updatedProfile = Profile.fromResponse(updatedProfileResponse).copy(
                        interests = _pendingInterestIds.value.map { id ->
                            _interests.value.find { it.id == id } ?: return@launch
                        }
                    )
                    _profileData.value = updatedProfile
                    _username.value = updatedProfile.username
                    _bio.value = updatedProfile.bio ?: ""
                }
                is NetworkResult.Error -> {
                    _profileState.value = ProfileFormState.Error(updateResult.errorMessage, updateResult.errorCode)
                    return@launch
                }
                else -> {}
            }

            val newIds = _pendingInterestIds.value
            val oldIds = _selectedInterestIds.value
            if (newIds != oldIds) {
                val toRemove = oldIds.filter { !newIds.contains(it) }
                if (toRemove.isNotEmpty()) {
                    when (val removeResult = profileRepository.removeInterestsFromMyProfile(toRemove)) {
                        is NetworkResult.Error -> {
                            _profileState.value = ProfileFormState.Error("Профиль обновлён, но не удалось удалить интересы: ${removeResult.errorMessage}", removeResult.errorCode)
                            return@launch
                        }
                        else -> {}
                    }
                }
                val toAdd = newIds.filter { !oldIds.contains(it) }
                if (toAdd.isNotEmpty()) {
                    when (val addResult = profileRepository.addInterestsToMyProfile(toAdd)) {
                        is NetworkResult.Error -> {
                            _profileState.value = ProfileFormState.Error("Профиль обновлён, но не удалось добавить интересы: ${addResult.errorMessage}", addResult.errorCode)
                            return@launch
                        }
                        else -> {}
                    }
                }
                _selectedInterestIds.value = newIds
            }

            if (_deleteAvatar.value) {
                when (val deleteResult = profileRepository.deleteAvatar()) {
                    is NetworkResult.Success -> {
                        _profileData.update { current -> current?.copy(avatarUrl = null) }
                        _deleteAvatar.value = false
                    }
                    is NetworkResult.Error -> {
                        _profileState.value = ProfileFormState.Error("Профиль обновлён, но не удалось удалить аватар: ${deleteResult.errorMessage}", deleteResult.errorCode)
                        return@launch
                    }
                    else -> {}
                }
            } else if (_newAvatarBytes.value != null) {
                when (val uploadResult = profileRepository.uploadAvatar(_newAvatarBytes.value!!)) {
                    is NetworkResult.Success -> {
                        _profileData.update { current -> current?.copy(avatarUrl = uploadResult.data) }
                        _newAvatarBytes.value = null
                    }
                    is NetworkResult.Error -> {
                        _profileState.value = ProfileFormState.Error("Профиль обновлён, но не удалось загрузить аватар: ${uploadResult.errorMessage}", uploadResult.errorCode)
                        return@launch
                    }
                    else -> {}
                }
            }

            _isEditMode.value = false
            _profileState.value = ProfileFormState.Success("Профиль обновлён")
        }
    }

    fun switchProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileFormState.Loading
            tokenManager.clearProfileId()
            _profileState.value = ProfileFormState.Success("Переключение профиля")
        }
    }

    fun deleteCurrentProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileFormState.Loading
            when (val result = profileRepository.deleteMyProfile()) {
                is NetworkResult.Success -> {
                    tokenManager.clearProfileId()
                    _profileState.value = ProfileFormState.Success("Профиль удалён")
                }
                is NetworkResult.Error -> {
                    _profileState.value = ProfileFormState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}