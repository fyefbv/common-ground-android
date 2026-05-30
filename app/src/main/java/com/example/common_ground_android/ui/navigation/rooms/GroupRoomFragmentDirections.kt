package com.example.common_ground_android.ui.navigation.rooms

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class GroupRoomFragmentDirections private constructor() {
    companion object {
        fun actionGroupRoomFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_groupRoomFragment_to_loginFragment)

        fun actionGroupRoomFragmentToRoomManagementFragment(roomId: String, sourceFragmentId: Int): NavDirections =
            GroupRoomFragmentToRoomManagementFragment(roomId, sourceFragmentId)
    }
}

data class GroupRoomFragmentToRoomManagementFragment(val roomId: String, val sourceFragmentId: Int) : NavDirections {
    override val actionId: Int = R.id.action_groupRoomFragment_to_roomManagementFragment
    override val arguments: Bundle = Bundle().apply {
        putString("roomId", roomId)
        putInt("sourceFragmentId", sourceFragmentId)
    }
}