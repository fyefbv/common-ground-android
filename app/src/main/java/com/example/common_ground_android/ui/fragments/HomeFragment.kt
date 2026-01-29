package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentHomeBinding

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
            navigateToProfileSelector()
        }
    }

    private fun updateUI() {
        binding.userNameText.text = "Александр"

        binding.switchProfileButton.visibility = View.VISIBLE

        binding.roomsCountChip.text = getString(R.string.rooms_count, 8)
    }

    private fun navigateToChatRoulette() {
        findNavController().navigate(R.id.action_homeFragment_to_chatRouletteFragment)
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(R.id.action_homeFragment_to_profileSelectorFragment)
    }
}