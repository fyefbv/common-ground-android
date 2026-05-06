package com.example.common_ground_android.ui.fragments.navigation.home

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class HomeFragmentDirections private constructor() {
    companion object {
        fun actionHomeFragmentToChatRouletteFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_chatRouletteFragment)

        fun actionHomeFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_profileSelectorFragment)

        fun actionHomeFragmentToProfileFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_profileFragment)

        fun actionHomeFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_loginFragment)

        fun actionHomeFragmentToRoomFragment(roomId: String): NavDirections =
            HomeFragmentToRoomFragment(roomId)
    }
}

data class HomeFragmentToRoomFragment(val roomId: String) : NavDirections {
    override val actionId: Int = R.id.action_homeFragment_to_groupRoomFragment
    override val arguments: Bundle = Bundle().apply { putString("roomId", roomId) }
}