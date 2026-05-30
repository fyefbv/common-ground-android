package com.example.common_ground_android.ui.navigation.auth

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class LoginFragmentDirections private constructor() {
    companion object {
        fun actionLoginFragmentToRegisterFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_loginFragment_to_registerFragment)

        fun actionLoginFragmentToProfileSelectorFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_loginFragment_to_profileSelectorFragment)
    }
}