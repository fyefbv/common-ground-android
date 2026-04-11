package com.example.common_ground_android.ui.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    fun updateEmail(email: String) {
        _email.value = email
        _emailError.value = ValidationUtils.validateEmail(email)
    }

    fun updatePassword(password: String) {
        _password.value = password
        _passwordError.value = ValidationUtils.validatePassword(password)
        updateConfirmPasswordError(_confirmPassword.value)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        _confirmPasswordError.value = ValidationUtils.validateConfirmPassword(_password.value, confirmPassword)
    }

    private fun updateConfirmPasswordError(confirmPassword: String) {
        val password = _password.value
        _confirmPasswordError.value = when {
            confirmPassword.isEmpty() -> null
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }

    fun register() {
        updateEmail(_email.value)
        updatePassword(_password.value)
        updateConfirmPassword(_confirmPassword.value)

        val errors = getValidationErrors()
        if (errors.isNotEmpty()) {
            _registerState.value = AuthState.Error(errors.joinToString("\n"), "validation_error")
            return
        }

        val email = _email.value.trim()
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        val emailErrorMsg = _emailError.value
        if (emailErrorMsg != null) {
            _registerState.value = AuthState.Error(emailErrorMsg, "validation_error")
            return
        }

        val passwordErrorMsg = _passwordError.value
        if (passwordErrorMsg != null) {
            _registerState.value = AuthState.Error(passwordErrorMsg, "validation_error")
            return
        }

        val confirmPasswordErrorMsg = _confirmPasswordError.value
        if (confirmPasswordErrorMsg != null) {
            _registerState.value = AuthState.Error(confirmPasswordErrorMsg, "validation_error")
            return
        }

        viewModelScope.launch {
            _registerState.value = AuthState.Loading
            when (val result = authRepository.register(email, password)) {
                is NetworkResult.Success -> {
                    _registerState.value = AuthState.Success("Регистрация успешна")
                }
                is NetworkResult.Error -> {
                    _registerState.value = AuthState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    private fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        _emailError.value?.let { errors.add("Поле 'Email': $it") }
        _passwordError.value?.let { errors.add("Поле 'Пароль': $it") }
        _confirmPasswordError.value?.let { errors.add("Поле 'Подтверждение пароля': $it") }
        return errors
    }

    fun resetState() {
        _registerState.value = AuthState.Idle
    }
}