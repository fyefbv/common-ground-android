package com.example.common_ground_android.ui.viewmodels.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    fun updateEmail(email: String) {
        _email.value = email
        _emailError.value = ValidationUtils.validateEmail(email)
    }

    fun updatePassword(password: String) {
        _password.value = password
        _passwordError.value = ValidationUtils.validatePassword(password)
    }

    fun login() {
        updateEmail(_email.value)
        updatePassword(_password.value)

        val errors = getValidationErrors()
        if (errors.isNotEmpty()) {
            val message = errors.joinToString("\n")
            _loginState.value = AuthState.Error(message, "validation_error")
            return
        }

        val email = _email.value.trim()
        val password = _password.value

        val emailErrorMsg = _emailError.value
        if (emailErrorMsg != null) {
            _loginState.value = AuthState.Error(emailErrorMsg, "validation_error")
            return
        }

        val passwordErrorMsg = _passwordError.value
        if (passwordErrorMsg != null) {
            _loginState.value = AuthState.Error(passwordErrorMsg, "validation_error")
            return
        }

        viewModelScope.launch {
            _loginState.value = AuthState.Loading
            when (val result = authRepository.login(email, password)) {
                is NetworkResult.Success -> {
                    _loginState.value = AuthState.Success("Вход выполнен успешно")
                }
                is NetworkResult.Error -> {
                    _loginState.value = AuthState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    private fun getValidationErrors(): List<String> {
        val errors = mutableListOf<String>()
        _emailError.value?.let { errors.add("Поле 'Email': $it") }
        _passwordError.value?.let { errors.add("Поле 'Пароль': $it") }
        return errors
    }

    fun resetState() {
        _loginState.value = AuthState.Idle
    }
}