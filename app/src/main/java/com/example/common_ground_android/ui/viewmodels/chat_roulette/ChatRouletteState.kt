package com.example.common_ground_android.ui.viewmodels.chat_roulette

import com.example.common_ground_android.network.model.domain.ChatRouletteMessage
import com.example.common_ground_android.network.model.domain.ChatRouletteSession
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile

enum class ExtensionState {
    NONE,
    REQUESTED_BY_ME,
    REQUESTED_BY_PARTNER
}

sealed class ChatRouletteState {
    object Idle : ChatRouletteState()
    object Searching : ChatRouletteState()
    data class ActiveSession(
        val session: ChatRouletteSession,
        val partner: Profile?,
        val commonInterests: List<Interest>,
        val matchedInterest: Interest?,
        val messages: List<ChatRouletteMessage>,
        val extensionState: ExtensionState = ExtensionState.NONE,
        val expiresAt: Long
    ) : ChatRouletteState()
    object Rating : ChatRouletteState()
    object Finished : ChatRouletteState()
    data class Error(val message: String, val errorCode: String?) : ChatRouletteState()
}