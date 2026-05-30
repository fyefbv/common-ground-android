package com.example.common_ground_android.ui.navigation.rooms

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class CreateRoomFragmentDirections private constructor() {
    companion object {
        fun actionCreateRoomFragmentToRoomsFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_createRoomFragment_to_roomsFragment)

        fun actionCreateRoomFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_createRoomFragment_to_loginFragment)
    }
}