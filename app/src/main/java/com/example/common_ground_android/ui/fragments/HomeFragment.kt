package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentHomeBinding
import com.example.common_ground_android.network.client.KtorClientFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        updateUI()
    }

    private fun setupClickListeners() {
        binding.chatRouletteCard.setOnClickListener {
            navigateToChatRoulette()
        }

        binding.switchProfileButton.setOnClickListener {
            switchProfile()
        }

        binding.profileButton.setOnClickListener {
            navigateToProfile()
        }
    }

    private fun updateUI() {
        binding.userNameText.text = "Александр"

        binding.switchProfileButton.visibility = View.VISIBLE

        binding.roomsCountChip.text = getString(R.string.rooms_count, 8)
    }

    private fun switchProfile() {
        lifecycleScope.launch {
            val tokenManager = KtorClientFactory.getTokenManager()
            tokenManager.clearProfileId()
            navigateToProfileSelector()
        }
    }

    private fun navigateToChatRoulette() {
        findNavController().navigate(R.id.action_homeFragment_to_chatRouletteFragment)
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(R.id.action_homeFragment_to_profileSelectorFragment)
    }

    private fun navigateToProfile(){
        findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
    }
}