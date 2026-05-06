package com.example.common_ground_android.ui.viewmodels.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.UserRepository
import com.example.common_ground_android.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AccountSettingsViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AccountSettingsState>(AccountSettingsState.Idle)
    val state: StateFlow<AccountSettingsState> = _state

    private val _currentEmail = MutableStateFlow("")
    val currentEmail: StateFlow<String> = _currentEmail

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _newPasswordError = MutableStateFlow<String?>(null)
    val newPasswordError: StateFlow<String?> = _newPasswordError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    init {
        loadUserEmail()
    }

    private fun loadUserEmail() {
        viewModelScope.launch {
            when (val result = userRepository.getCurrentUser()) {
                is NetworkResult.Success -> {
                    _currentEmail.value = result.data.email
                }
                is NetworkResult.Error -> {
                    _state.value = AccountSettingsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun updateNewPassword(password: String) {
        _newPassword.value = password
        _newPasswordError.value = ValidationUtils.validatePassword(password)
    }

    fun updateConfirmPassword(password: String) {
        _confirmPassword.value = password
        _confirmPasswordError.value = if (_newPassword.value != password) "Пароли не совпадают" else null
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            _state.value = AccountSettingsState.Loading
            when (val result = userRepository.updateEmail(newEmail)) {
                is NetworkResult.Success -> {
                    _currentEmail.value = result.data.email
                    _state.value = AccountSettingsState.Success("Email успешно обновлён")
                }
                is NetworkResult.Error -> {
                    _state.value = AccountSettingsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun updatePassword() {
        val new = _newPassword.value
        val confirm = _confirmPassword.value

        val errors = mutableListOf<String>()
        _newPasswordError.value?.let { errors.add("Поле 'Новый пароль': $it") }
        _confirmPasswordError.value?.let { errors.add("Поле 'Подтверждение пароля': $it") }

        if (errors.isNotEmpty()) {
            _state.value = AccountSettingsState.Error(errors.joinToString("\n"))
            return
        }

        viewModelScope.launch {
            _state.value = AccountSettingsState.Loading
            when (val result = userRepository.updatePassword(new)) {
                is NetworkResult.Success -> {
                    _state.value = AccountSettingsState.Success("Пароль успешно изменён")
                    _newPassword.value = ""
                    _confirmPassword.value = ""
                    _newPasswordError.value = null
                    _confirmPasswordError.value = null
                }
                is NetworkResult.Error -> {
                    _state.value = AccountSettingsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _state.value = AccountSettingsState.Loading
            when (val result = userRepository.deleteUser()) {
                is NetworkResult.Success -> {
                    authRepository.logout()
                    _state.value = AccountSettingsState.LoggedOut
                }
                is NetworkResult.Error -> {
                    _state.value = AccountSettingsState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = AccountSettingsState.Loading
            authRepository.logout()
            _state.value = AccountSettingsState.LoggedOut
        }
    }

    fun resetState() {
        _state.value = AccountSettingsState.Idle
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}