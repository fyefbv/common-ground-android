package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentProfileSelectorBinding

class ProfileSelectorFragment : Fragment() {
    private lateinit var binding: FragmentProfileSelectorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.createProfileCard.setOnClickListener {

        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_profileSelectorFragment_to_homeFragment)
    }
}