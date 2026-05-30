package com.example.common_ground_android.ui.viewmodels.home

import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Room

sealed class HomeState {
    object Loading : HomeState()
    data class Success(val rooms: List<Room>, val profile: Profile?) : HomeState()
    data class Empty(val profile: Profile?) : HomeState()
    data class Error(val message: String, val errorCode: String? = null) : HomeState()
}