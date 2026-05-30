package com.example.common_ground_android.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentProfileViewBinding
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.ProfileStatistics
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.navigation.profile.ProfileViewFragmentDirections
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewState
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewViewModel
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileViewFragment : Fragment() {

    private var _binding: FragmentProfileViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewViewModel by viewModels {
        ProfileViewViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val profileId = arguments?.getString("profileId") ?: return

        binding.backButton.setOnClickListener { findNavController().navigateUp() }

        viewModel.loadProfile(profileId)
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ProfileViewState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is ProfileViewState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            updateProfile(state.profile, state.interests, state.statistics)
                        }
                        is ProfileViewState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            if (ErrorHandler.isAuthError(state.errorCode)) {
                                viewModel.clearTokensAndLogout()
                                Snackbar.make(binding.root, "Сессия истекла. Пожалуйста, войдите заново.", Snackbar.LENGTH_LONG).show()
                                navigateToLogin()
                            } else {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateProfile(
        profile: Profile,
        interests: List<Interest>,
        statistics: ProfileStatistics?
    ) {
        binding.profileUsername.text = profile.username
        binding.profileBio.text = profile.bio ?: ""

        Glide.with(requireContext())
            .load(profile.avatarUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.profileAvatar)

        binding.interestsChipGroup.removeAllViews()
        if (interests.isEmpty()) {
            binding.interestsChipGroup.visibility = View.GONE
            binding.noInterestsHint.visibility = View.VISIBLE
        } else {
            binding.noInterestsHint.visibility = View.GONE
            binding.interestsChipGroup.visibility = View.VISIBLE
            interests.forEach { interest ->
                val chip = Chip(requireContext()).apply {
                    text = interest.name
                    isClickable = false
                }
                binding.interestsChipGroup.addView(chip)
            }
        }

        binding.sessionsCountText.text = statistics?.totalSessions?.toString() ?: "0"
        binding.ratingText.text = statistics?.reputationScore?.let { String.format("%.1f", it) } ?: "0.0"
        binding.roomsCountText.text = statistics?.totalRooms?.toString() ?: "0"
    }

    private fun navigateToLogin() {
        findNavController().navigate(ProfileViewFragmentDirections.actionProfileViewFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}