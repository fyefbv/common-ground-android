package com.example.common_ground_android.ui.navigation.auth

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class RegisterFragmentDirections private constructor() {
    companion object {
        fun actionRegisterFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_registerFragment_to_loginFragment)

        fun actionRegisterFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_registerFragment_to_profileSelectorFragment)
    }
}