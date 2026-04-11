package com.example.common_ground_android.utils

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
            trimmed.isEmpty() -> "Введите email"
            !isValidEmail(trimmed) -> "Введите корректный email"
            else -> null
        }
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Введите пароль"
            password.length < MIN_PASSWORD_LENGTH -> "Минимум $MIN_PASSWORD_LENGTH символов"
            password.length > MAX_PASSWORD_LENGTH -> "Максимум $MAX_PASSWORD_LENGTH символов"
            !password.any { it.isUpperCase() } -> "Должна быть хотя бы одна заглавная буква"
            !password.any { it.isLowerCase() } -> "Должна быть хотя бы одна строчная буква"
            !password.any { it.isDigit() } -> "Должна быть хотя бы одна цифра"
            password.all { it.isLetterOrDigit() } -> "Должен быть хотя бы один спецсимвол (например, !@#\$%)"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
    }

    fun getPasswordStrengthErrorMessage(): String {
        return "Пароль должен содержать от 8 до 100 символов, включая заглавную и строчную буквы, цифру и спецсимвол (например, !@#\$%)"
    }

    fun isValidUsername(username: String): Boolean {
        return Pattern.compile(USERNAME_PATTERN).matcher(username).matches()
    }
}