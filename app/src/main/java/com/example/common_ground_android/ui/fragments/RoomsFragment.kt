package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.common_ground_android.network.model.domain.room.Room
import com.example.common_ground_android.network.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.RoomsAdapter
import com.example.common_ground_android.ui.fragments.navigation.rooms.RoomsFragmentDirections
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsState
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsViewModel
import com.example.common_ground_android.ui.viewmodels.rooms.RoomsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RoomsFragment : Fragment() {

    private var _binding: FragmentRoomsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RoomsViewModel by viewModels {
        RoomsViewModelFactory(requireContext())
    }

    private lateinit var adapter: RoomsAdapter

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

        setupRecyclerView()
        setupSwipeRefresh()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = RoomsAdapter(
            getInterestName = { interestId -> viewModel.interestMap.value[interestId] },
            onJoinClick = { room -> viewModel.joinRoom(room) },
            onLeaveClick = { room -> viewModel.leaveRoom(room) },
            onRoomClick = { room -> openRoom(room) }
        )
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.roomsRecyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadRooms()
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
    }

    private fun handleState(state: RoomsState) {
        when (state) {
            is RoomsState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
                binding.emptyStateText.visibility = View.GONE
                binding.errorStateText.visibility = View.GONE
            }
            is RoomsState.Success -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.emptyStateText.visibility = View.GONE
                binding.errorStateText.visibility = View.GONE
                binding.roomsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(state.rooms) {
                    binding.roomsRecyclerView.scrollToPosition(0)
                }
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, state.rooms.size, state.rooms.size)
            }
            is RoomsState.Empty -> {
                binding.swipeRefreshLayout.isRefreshing = false
                binding.errorStateText.visibility = View.GONE
                binding.roomsRecyclerView.visibility = View.GONE
                binding.emptyStateText.visibility = View.VISIBLE
                binding.roomsCountChip.text = resources.getQuantityString(R.plurals.rooms_count, 0, 0)
            }
            is RoomsState.Error -> {
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
        findNavController().navigate(RoomsFragmentDirections.actionRoomsFragmentToGroupRoomFragment(room.id))
    }

    private fun navigateToLogin() {
        findNavController().navigate(RoomsFragmentDirections.actionRoomsFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}