package com.example.common_ground_android.ui.fragments.navigation.profile

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class ProfileSelectorFragmentDirections private constructor() {
    companion object {
        fun actionProfileSelectorFragmentToCreateProfileFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileSelectorFragment_to_createProfileFragment)

        fun actionProfileSelectorFragmentToHomeFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileSelectorFragment_to_homeFragment)

        fun actionProfileSelectorFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_profileSelectorFragment_to_loginFragment)
    }
}