package com.example.common_ground_android.ui.viewmodels.account

sealed class AccountSettingsState {
    object Idle : AccountSettingsState()
    object Loading : AccountSettingsState()
    data class Success(val message: String) : AccountSettingsState()
    data class Error(val message: String, val errorCode: String? = null) : AccountSettingsState()
    object LoggedOut : AccountSettingsState()
    object DeleteAccount : AccountSettingsState()
}