package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.network.client.KtorClientFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val tokenManager = KtorClientFactory.getTokenManager()
            val accessToken = tokenManager.accessToken.firstOrNull()
            val profileId = tokenManager.profileId.firstOrNull()

            val destination = when {
                accessToken.isNullOrEmpty() -> R.id.loginFragment
                profileId == null -> R.id.profileSelectorFragment
                else -> R.id.homeFragment
            }

            findNavController().navigate(destination)
        }
    }
}