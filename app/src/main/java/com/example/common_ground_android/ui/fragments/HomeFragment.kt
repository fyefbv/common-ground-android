package com.example.common_ground_android.ui.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentHomeBinding
import com.example.common_ground_android.network.model.domain.profile.Profile
import com.example.common_ground_android.network.model.domain.room.Room
import com.example.common_ground_android.network.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.RoomsAdapter
import com.example.common_ground_android.ui.fragments.navigation.home.HomeFragmentDirections
import com.example.common_ground_android.ui.viewmodels.home.HomeState
import com.example.common_ground_android.ui.viewmodels.home.HomeViewModel
import com.example.common_ground_android.ui.viewmodels.home.HomeViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }

    private lateinit var adapter: RoomsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeState()

        viewModel.loadData()
    }

    private fun setupRecyclerView() {
        adapter = RoomsAdapter(
            getInterestName = { interestId -> viewModel.getInterestName(interestId) },
            onJoinClick = { room -> viewModel.joinRoom(room) },
            onLeaveClick = { room -> viewModel.leaveRoom(room) },
            onRoomClick = { room -> openRoom(room) }
        )
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.roomsRecyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshRooms()
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.md_primary,
            R.color.md_secondary,
            R.color.md_tertiary
        )
    }

    private fun setupClickListeners() {
        binding.chatRouletteCard.setOnClickListener {
            navigateToChatRoulette()
        }
        binding.switchProfileButton.setOnClickListener {
            viewModel.switchProfile()
            navigateToProfileSelector()
        }
        binding.profileAvatar.setOnClickListener {
            navigateToProfile()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    profile?.let { updateProfileUI(it) }
                }
            }
        }
    }

    private fun updateProfileUI(profile: Profile) {
        binding.userNameText.text = profile.username
        Glide.with(requireContext())
            .load(profile.avatarUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.profileAvatar)
    }

    private fun handleState(state: HomeState) {
        when (state) {
            is HomeState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
                binding.errorStateText.visibility = View.GONE
                binding.emptyStateText.visibility = View.GONE
            }
            is HomeState.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.emptyStateText.visibility = View.GONE
                binding.roomsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.rooms) {
                    binding.roomsRecyclerView.scrollToPosition(0)
                }
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, state.rooms.size, state.rooms.size)
            }
            is HomeState.Empty -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.emptyStateText.visibility = View.VISIBLE
                binding.roomsRecyclerView.visibility = View.GONE
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, 0, 0)
            }
            is HomeState.Error -> {
                binding.swipeRefreshLayout.isRefreshing = false
                if (ErrorHandler.isAuthError(state.errorCode)) {
                    viewModel.clearTokensAndLogout()
                    Snackbar.make(binding.root, "Сессия истекла. Пожалуйста, войдите заново.", Snackbar.LENGTH_LONG).show()
                    navigateToLogin()
                } else {
                    binding.errorStateText.visibility = View.VISIBLE
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openRoom(room: Room) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRoomFragment(room.id))
    }

    private fun navigateToChatRoulette() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChatRouletteFragment())
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileSelectorFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
    }

    private fun navigateToProfile() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}