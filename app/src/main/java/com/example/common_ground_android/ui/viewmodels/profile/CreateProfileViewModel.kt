package com.example.common_ground_android.ui.viewmodels.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.domain.interest.Interest
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

class CreateProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val interestRepository: InterestRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileFormState>(ProfileFormState.Idle)
    val state: StateFlow<ProfileFormState> = _state.asStateFlow()

    private val _interests = MutableStateFlow<List<Interest>>(emptyList())
    val interests: StateFlow<List<Interest>> = _interests.asStateFlow()

    private val _selectedInterestIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedInterestIds: StateFlow<Set<String>> = _selectedInterestIds.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _bioError = MutableStateFlow<String?>(null)
    val bioError: StateFlow<String?> = _bioError.asStateFlow()

    private val _selectedAvatarBytes = MutableStateFlow<ByteArray?>(null)
    val selectedAvatarBytes: StateFlow<ByteArray?> = _selectedAvatarBytes.asStateFlow()

    init {
        loadInterests()
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
        val trimmed = bio.trim()
        _bio.value = trimmed
        _bioError.value = if (trimmed.length > 200) "Биография не должна превышать 200 символов" else null
    }

    fun addInterest(interest: Interest) {
        if (!_selectedInterestIds.value.contains(interest.id)) {
            _selectedInterestIds.update { it + interest.id }
        }
    }

    fun removeInterest(interestId: String) {
        _selectedInterestIds.update { it - interestId }
    }

    fun setAvatarBytes(bytes: ByteArray) {
        _selectedAvatarBytes.value = bytes
    }

    fun clearAvatarBytes() {
        _selectedAvatarBytes.value = null
    }

    fun createProfile() {
        updateUsername(_username.value)
        updateBio(_bio.value)

        val errors = getValidationErrors()
        if (errors.isNotEmpty()) {
            val message = errors.joinToString("\n")
            _state.value = ProfileFormState.Error(message)
            return
        }

        val username = _username.value.trim()
        val bio = _bio.value.trim().takeIf { it.isNotEmpty() }

        viewModelScope.launch {
            _state.value = ProfileFormState.Loading
            val username = _username.value.trim()
            val bio = _bio.value.trim().takeIf { it.isNotEmpty() }

            when (val result = profileRepository.createProfile(username, bio)) {
                is NetworkResult.Success -> {
                    val profile = result.data
                    val selectedIds = _selectedInterestIds.value.toList()
                    if (selectedIds.isNotEmpty()) {
                        val interestResult = profileRepository.addInterestsToProfileByUsername(profile.username, selectedIds)
                        if (interestResult is NetworkResult.Error) {
                            _state.value = ProfileFormState.Error("Профиль создан, но не удалось добавить интересы: ${interestResult.errorMessage}", interestResult.errorCode)
                            return@launch
                        }
                    }
                    val avatarBytes = _selectedAvatarBytes.value
                    if (avatarBytes != null) {
                        val avatarResult = profileRepository.uploadAvatarByUsername(profile.username, avatarBytes)
                        if (avatarResult is NetworkResult.Error) {
                            _state.value = ProfileFormState.Error("Профиль создан, но не удалось загрузить аватар: ${avatarResult.errorMessage}", avatarResult.errorCode)
                            return@launch
                        }
                    }
                    _state.value = ProfileFormState.Success("Профиль создан")
                }
                is NetworkResult.Error -> {
                    _state.value = ProfileFormState.Error(result.errorMessage, result.errorCode)
                }
                else -> {
                    _state.value = ProfileFormState.Error("Неизвестная ошибка")
                }
            }
        }
    }

    private fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        _usernameError.value?.let { errors.add("Поле 'Имя': $it") }
        _bioError.value?.let { errors.add("Поле 'Биография': $it") }
        return errors
    }

    fun resetState() {
        _state.value = ProfileFormState.Idle
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}