package com.example.common_ground_android.ui.viewmodels.profile

import com.example.common_ground_android.network.model.domain.Profile

sealed class ProfileSelectorState {
    data class Success(val profiles: List<Profile>) : ProfileSelectorState()
    object Loading : ProfileSelectorState()
    data class Error(val message: String, val errorCode: String? = null) : ProfileSelectorState()
    object Empty : ProfileSelectorState()
}