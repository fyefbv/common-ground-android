package com.example.common_ground_android.ui.navigation.profile

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class ProfileViewFragmentDirections private constructor() {
    companion object {
        fun actionProfileViewFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileViewFragment_to_loginFragment)
    }
}