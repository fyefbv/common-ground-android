package com.example.common_ground_android.ui.navigation.rooms

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class RoomManagementFragmentDirections private constructor() {
    companion object {
        fun actionRoomManagementFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_roomManagementFragment_to_loginFragment)

        fun actionRoomManagementFragmentToProfileViewFragment(profileId: String): NavDirections =
            RoomManagementFragmentToProfileViewFragment(profileId)
    }
}

data class RoomManagementFragmentToProfileViewFragment(val profileId: String) : NavDirections {
    override val actionId: Int = R.id.action_roomManagementFragment_to_profileViewFragment
    override val arguments: Bundle = Bundle().apply { putString("profileId", profileId) }
}