package com.example.common_ground_android.ui.viewmodels.profile

import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.ProfileStatistics

sealed class ProfileViewState {
    object Loading : ProfileViewState()
    data class Success(
        val profile: Profile,
        val interests: List<Interest>,
        val statistics: ProfileStatistics?
    ) : ProfileViewState()
    data class Error(val message: String, val errorCode: String? = null) : ProfileViewState()
}