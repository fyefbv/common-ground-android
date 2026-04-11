package com.example.common_ground_android.ui.fragments.navigation.profile

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class ProfileFragmentDirections private constructor() {
    companion object {
        fun actionProfileFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileFragment_to_profileSelectorFragment)

        fun actionProfileFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileFragment_to_loginFragment)
    }
}