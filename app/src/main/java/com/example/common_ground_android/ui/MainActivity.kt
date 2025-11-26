package com.example.common_ground_android.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
                visibility = when (destination.id) {
                    R.id.loginFragment,
                    R.id.registerFragment,
                    R.id.profileSelectorFragment,
                    R.id.chatRouletteFragment,
                    R.id.groupRoomFragment -> {
                        View.GONE
                    }

                    else -> {
                        View.VISIBLE
                    }
                }
            }
        }
    }

    fun showBottomNavigation(show: Boolean) {
        binding.bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
    }
}