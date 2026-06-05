package com.example.common_ground_android.utils

import com.example.common_ground_android.R
import java.util.regex.Pattern

object ValidationUtils {
    private const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    private const val USERNAME_PATTERN = "^[a-zA-Zа-яА-Я0-9_]{3,20}\$"
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_PASSWORD_LENGTH = 100

    fun isValidEmail(email: String): Boolean {
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches()
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> Res.getString(R.string.validation_email_required)
            !isValidEmail(trimmed) -> Res.getString(R.string.validation_email_invalid)
            else -> null
        }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> Res.getString(R.string.validation_password_required)
            password.length < MIN_PASSWORD_LENGTH -> Res.getString(R.string.validation_password_min_length)
            password.length > MAX_PASSWORD_LENGTH -> Res.getString(R.string.validation_password_max_length)
            !password.any { it.isUpperCase() } -> Res.getString(R.string.validation_password_uppercase)
            !password.any { it.isLowerCase() } -> Res.getString(R.string.validation_password_lowercase)
            !password.any { it.isDigit() } -> Res.getString(R.string.validation_password_digit)
            password.all { it.isLetterOrDigit() } -> Res.getString(R.string.validation_password_special)
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> Res.getString(R.string.validation_confirm_password_required)
            password != confirmPassword -> Res.getString(R.string.error_passwords_dont_match)
            else -> null
        }
    }

    fun getPasswordStrengthErrorMessage(): String {
        return Res.getString(R.string.validation_password_strength)
    }

    fun isValidUsername(username: String): Boolean {
        return Pattern.compile(USERNAME_PATTERN).matcher(username).matches()
    }
}