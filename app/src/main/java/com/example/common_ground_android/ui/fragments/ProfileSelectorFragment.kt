package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.data.models.Profile
import com.example.common_ground_android.databinding.FragmentProfileSelectorBinding
import com.example.common_ground_android.ui.adapters.ProfilesAdapter

class ProfileSelectorFragment : Fragment() {
    private lateinit var binding: FragmentProfileSelectorBinding
    private lateinit var profilesAdapter: ProfilesAdapter

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

        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        profilesAdapter = ProfilesAdapter { profile ->
            navigateToHome()
        }

        binding.profilesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = profilesAdapter
        }

        loadProfiles()
    }

    private fun setupClickListeners() {
        binding.createProfileCard.setOnClickListener {
            navigateToCreateProfile()
        }
    }

    private fun loadProfiles() {
        val profiles = listOf(
            Profile("Александр", listOf("Технологии", "Музыка", "Игры", "Кулинария", "Чтение")),
            Profile("Саша (работа)", listOf("Технологии", "Наука"))
        )
        profilesAdapter.submitList(profiles)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_profileSelectorFragment_to_homeFragment)
    }

    private fun navigateToCreateProfile() {
        findNavController().navigate(R.id.action_profileSelectorFragment_to_profileFragment)
    }
}