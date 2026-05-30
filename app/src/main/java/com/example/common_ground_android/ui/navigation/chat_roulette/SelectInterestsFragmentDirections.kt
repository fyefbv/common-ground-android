package com.example.common_ground_android.ui.navigation.chat_roulette

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class SelectInterestsFragmentDirections private constructor() {
    companion object {
        fun actionSelectInterestsFragmentToChatRouletteFragment(interestIds: Array<String>): NavDirections =
            SelectInterestsFragmentToChatRouletteFragment(interestIds)

        fun actionSelectInterestsFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_selectInterestsFragment_to_loginFragment)
    }
}

data class SelectInterestsFragmentToChatRouletteFragment(val interestIds: Array<String>) : NavDirections {
    override val actionId: Int = R.id.action_selectInterestsFragment_to_chatRouletteFragment
    override val arguments: Bundle = Bundle().apply { putStringArray("interestIds", interestIds) }
}