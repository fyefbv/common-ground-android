package com.example.common_ground_android.ui.navigation.home

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class HomeFragmentDirections private constructor() {
    companion object {
        fun actionHomeFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_profileSelectorFragment)

        fun actionHomeFragmentToProfileFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_profileFragment)

        fun actionHomeFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_loginFragment)

        fun actionHomeFragmentToGroupRoomFragment(roomId: String, sourceFragmentId: Int): NavDirections =
            HomeFragmentToGroupRoomFragment(roomId, sourceFragmentId)

        fun actionHomeFragmentToSelectInterestsFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_homeFragment_to_selectInterestsFragment)
    }
}

data class HomeFragmentToGroupRoomFragment(val roomId: String, val sourceFragmentId: Int) : NavDirections {
    override val actionId: Int = R.id.action_homeFragment_to_groupRoomFragment
    override val arguments: Bundle = Bundle().apply {
        putString("roomId", roomId)
        putInt("sourceFragmentId", sourceFragmentId)
    }
}