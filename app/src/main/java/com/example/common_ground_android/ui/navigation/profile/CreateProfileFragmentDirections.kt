package com.example.common_ground_android.ui.navigation.profile

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class CreateProfileFragmentDirections private constructor() {
    companion object {
        fun actionCreateProfileFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_createProfileFragment_to_profileSelectorFragment)

        fun actionCreateProfileFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_createProfileFragment_to_loginFragment)
    }
}