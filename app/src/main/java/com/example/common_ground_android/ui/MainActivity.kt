package com.example.common_ground_android.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.apply {
            setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                val show = when (destination.id) {
                    R.id.loginFragment,
                    R.id.registerFragment,
                    R.id.profileSelectorFragment,
                    R.id.chatRouletteFragment,
                    R.id.accountSettingsFragment,
                    R.id.createProfileFragment,
                    R.id.groupRoomFragment,
                    R.id.splashFragment,
                    R.id.createRoomFragment-> false
                    else -> true
                }
                showBottomNavigation(show)
            }
        }
    }

    private fun showBottomNavigation(show: Boolean) {
        binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
        adjustFragmentContainerMargin(show)
    }

    private fun adjustFragmentContainerMargin(show: Boolean) {
        val params = binding.fragmentContainer.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = if (show) {
            resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        } else {
            0
        }
        binding.fragmentContainer.layoutParams = params
    }
}