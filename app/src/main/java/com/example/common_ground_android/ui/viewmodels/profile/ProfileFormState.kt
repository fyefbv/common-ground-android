package com.example.common_ground_android.ui.viewmodels.profile

sealed class ProfileFormState {
    object Idle : ProfileFormState()
    object Loading : ProfileFormState()
    data class Success(val message: String) : ProfileFormState()
    data class Error(val message: String, val errorCode: String? = null) : ProfileFormState()
}