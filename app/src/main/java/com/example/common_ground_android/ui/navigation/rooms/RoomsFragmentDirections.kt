package com.example.common_ground_android.ui.navigation.rooms

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class RoomsFragmentDirections private constructor() {
    companion object {
        fun actionRoomsFragmentToCreateRoomFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_roomsFragment_to_createRoomFragment)

        fun actionRoomsFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_roomsFragment_to_loginFragment)

        fun actionRoomsFragmentToGroupRoomFragment(roomId: String, sourceFragmentId: Int): NavDirections =
            RoomsFragmentToGroupRoomFragment(roomId, sourceFragmentId)
    }
}

data class RoomsFragmentToGroupRoomFragment(val roomId: String, val sourceFragmentId: Int) : NavDirections {
    override val actionId: Int = R.id.action_roomsFragment_to_groupRoomFragment
    override val arguments: Bundle = Bundle().apply {
        putString("roomId", roomId)
        putInt("sourceFragmentId", sourceFragmentId)
    }
}