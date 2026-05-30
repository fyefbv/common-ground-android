package com.example.common_ground_android.ui.viewmodels.chat_roulette

import com.example.common_ground_android.network.model.domain.Interest

sealed class SelectInterestsState {
    object Loading : SelectInterestsState()
    data class Ready(
        val allInterests: List<Interest>,
        val selectedIds: Set<String>
    ) : SelectInterestsState()
    data class Error(val message: String, val errorCode: String?) : SelectInterestsState()
}