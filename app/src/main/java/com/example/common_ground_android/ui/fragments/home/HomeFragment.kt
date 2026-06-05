package com.example.common_ground_android.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.ui.adapters.RoomAdapter
import com.example.common_ground_android.ui.navigation.home.HomeFragmentDirections
import com.example.common_ground_android.ui.viewmodels.home.HomeState
import com.example.common_ground_android.ui.viewmodels.home.HomeViewModel
import com.example.common_ground_android.ui.viewmodels.home.HomeViewModelFactory
import com.example.common_ground_android.utils.ErrorHandler
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireContext())
    }

    private lateinit var adapter: RoomAdapter
    private var savedRoomId: String? = null
    private var isFirstResume = true

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        })

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        setupObservers()

        viewModel.loadData()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        viewModel.refreshRooms()
    }

    private fun setupRecyclerView() {
        val currentProfileId = viewModel.getCurrentProfileId()
        adapter = RoomAdapter(
            currentProfileId = currentProfileId,
            getInterestName = { interestId -> viewModel.getInterestName(interestId) },
            onJoinClick = { room ->
                savedRoomId = room.id
                viewModel.joinRoom(room)
            },
            onLeaveClick = { room ->
                savedRoomId = room.id
                viewModel.leaveRoom(room)
            },
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
        binding.switchProfileButton.setOnClickListener {
            viewModel.switchProfile()
            navigateToProfileSelector()
        }
        binding.profileAvatar.setOnClickListener {
            navigateToProfile()
        }
        binding.chatRouletteCard.setOnClickListener {
            navigateToSelectInterests()
        }
    }

    private fun setupObservers() {
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.interestsMap.collect { map ->
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
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
                binding.emptyStateText.visibility = View.GONE
            }
            is HomeState.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.emptyStateText.visibility = View.GONE
                binding.roomsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.rooms)
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, state.rooms.size, state.rooms.size)

                if (viewModel.shouldScrollToTop.value) {
                    binding.roomsRecyclerView.post {
                        binding.roomsRecyclerView.scrollToPosition(0)
                        viewModel.resetScrollToTop()
                    }
                }
                else if (savedRoomId != null) {
                    val newPosition = state.rooms.indexOfFirst { it.id == savedRoomId }
                    if (newPosition != -1) {
                        binding.roomsRecyclerView.post {
                            binding.roomsRecyclerView.scrollToPosition(newPosition)
                        }
                    }
                    savedRoomId = null
                }
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
                    Toast.makeText(requireContext(), R.string.error_session_expired_relogin, Toast.LENGTH_LONG).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openRoom(room: Room) {
        if (!room.isJoined && room.creatorId != viewModel.getCurrentProfileId()) {
            Toast.makeText(requireContext(), R.string.you_are_not_member_click_join, Toast.LENGTH_SHORT).show()
        } else if (room.isBanned) {
            Toast.makeText(requireContext(), R.string.you_are_banned_in_this_room, Toast.LENGTH_SHORT).show()
        } else {
            findNavController().navigate(
                HomeFragmentDirections.Companion.actionHomeFragmentToGroupRoomFragment(room.id, R.id.homeFragment)
            )
        }
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(HomeFragmentDirections.Companion.actionHomeFragmentToProfileSelectorFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(HomeFragmentDirections.Companion.actionHomeFragmentToLoginFragment())
    }

    private fun navigateToProfile() {
        findNavController().navigate(HomeFragmentDirections.Companion.actionHomeFragmentToProfileFragment())
    }

    private fun navigateToSelectInterests() {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSelectInterestsFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}