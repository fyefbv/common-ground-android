package com.example.common_ground_android.ui.navigation.account

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.common_ground_android.R

class AccountSettingsFragmentDirections private constructor() {
    companion object {
        fun actionAccountSettingsFragmentToLoginFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_accountSettingsFragment_to_loginFragment)
    }
}