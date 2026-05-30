package com.example.common_ground_android.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentProfileSelectorBinding
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.ProfileAdapter
import com.example.common_ground_android.ui.viewmodels.profile.ProfileSelectorState
import com.example.common_ground_android.ui.viewmodels.profile.ProfileSelectorViewModel
import com.example.common_ground_android.ui.navigation.profile.ProfileSelectorFragmentDirections
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileSelectorFragment : Fragment() {

    private var _binding: FragmentProfileSelectorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileSelectorViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    private lateinit var adapter: ProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        })

        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()
        setupListeners()

        viewModel.loadProfiles()
    }

    private fun setupRecyclerView() {
        adapter = ProfileAdapter(emptyList()) { profile ->
            selectProfile(profile)
        }

        binding.profilesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.profilesRecyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadProfiles()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.md_primary,
            R.color.md_secondary,
            R.color.md_tertiary
        )
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.createProfileCard.setOnClickListener {
            navigateToCreateProfile()
        }

        binding.createProfileFromEmpty.setOnClickListener {
            navigateToCreateProfile()
        }

        binding.settingsButton.setOnClickListener {
            navigateToAccountSettings()
        }
    }

    private fun handleState(state: ProfileSelectorState) {
        when (state) {
            is ProfileSelectorState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
                showEmptyState(false)
                binding.profilesRecyclerView.visibility = View.GONE
            }
            is ProfileSelectorState.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                val profiles = state.profiles
                if (profiles.isEmpty()) {
                    showEmptyState(true)
                    binding.profilesRecyclerView.visibility = View.GONE
                } else {
                    showEmptyState(false)
                    binding.profilesRecyclerView.visibility = View.VISIBLE
                    adapter = ProfileAdapter(profiles) { profile ->
                        selectProfile(profile)
                    }
                    binding.profilesRecyclerView.adapter = adapter
                }
            }
            is ProfileSelectorState.Error -> {
                binding.swipeRefreshLayout.isRefreshing = false
                showEmptyState(false)
                if (ErrorHandler.isAuthError(state.errorCode)) {
                    viewModel.clearTokensAndLogout()
                    Snackbar.make(binding.root, "Сессия истекла. Пожалуйста, войдите заново.", Snackbar.LENGTH_LONG).show()
                    navigateToLogin()
                } else {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
            is ProfileSelectorState.Empty -> {
                binding.swipeRefreshLayout.isRefreshing = false
                showEmptyState(true)
                binding.profilesRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun selectProfile(profile: Profile) {
        viewLifecycleOwner.lifecycleScope.launch {
            val success = viewModel.selectProfile(profile)
            if (success) {
                navigateToHome()
            } else {
                Snackbar.make(binding.root, "Ошибка выбора профиля", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToCreateProfile() {
        findNavController().navigate(ProfileSelectorFragmentDirections.actionProfileSelectorFragmentToCreateProfileFragment())
    }

    private fun navigateToAccountSettings() {
        findNavController().navigate(R.id.action_profileSelectorFragment_to_accountSettingsFragment)
    }

    private fun navigateToHome() {
        findNavController().navigate(ProfileSelectorFragmentDirections.actionProfileSelectorFragmentToHomeFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(ProfileSelectorFragmentDirections.actionProfileSelectorFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}