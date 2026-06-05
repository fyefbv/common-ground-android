package com.example.common_ground_android.ui.fragments.rooms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentRoomsBinding
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.RoomAdapter
import com.example.common_ground_android.ui.navigation.rooms.RoomsFragmentDirections
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsState
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsViewModel
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsViewModelFactory
import kotlinx.coroutines.launch
import kotlin.collections.get

class RoomsFragment : Fragment() {

    private var _binding: FragmentRoomsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomsViewModel by viewModels {
        RoomsViewModelFactory(requireContext())
    }
    private lateinit var adapter: RoomAdapter
    private var savedRoomId: String? = null
    private var isFirstResume = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomsBinding.inflate(inflater, container, false)
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
        setupListeners()
        setupObservers()
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
            getInterestName = { interestId -> viewModel.interestsMap.value[interestId] },
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

    private fun setupListeners() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSearchQuery(text.toString())
        }
        binding.filterButton.setOnClickListener {
            FilterDialogFragment().show(childFragmentManager, "filter_dialog")
        }
        binding.createRoomButton.setOnClickListener {
            findNavController().navigate(RoomsFragmentDirections.actionRoomsFragmentToCreateRoomFragment())
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
                viewModel.interestsMap.collect { map ->
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }
        }
    }

    private fun handleState(state: RoomsState) {
        when (state) {
            is RoomsState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
                binding.emptyStateText.visibility = View.GONE
            }
            is RoomsState.Success -> {
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
            is RoomsState.Empty -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.roomsRecyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, 0, 0)
            }
            is RoomsState.Error -> {
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
                RoomsFragmentDirections.actionRoomsFragmentToGroupRoomFragment(room.id, R.id.roomsFragment)
            )
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(RoomsFragmentDirections.actionRoomsFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}