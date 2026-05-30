package com.example.common_ground_android.ui.navigation.chat_roulette

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class ChatRouletteFragmentDirections private constructor() {
    companion object {
        fun actionChatRouletteFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_chatRouletteFragment_to_loginFragment)

        fun actionChatRouletteFragmentToSelectInterestsFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_chatRouletteFragment_to_selectInterestsFragment)
    }
}