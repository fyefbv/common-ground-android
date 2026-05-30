package com.example.common_ground_android.ui.fragments.chat_roulette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentSelectInterestsBinding
import com.example.common_ground_android.ui.navigation.chat_roulette.SelectInterestsFragmentDirections
import com.example.common_ground_android.ui.navigation.rooms.GroupRoomFragmentDirections
import com.example.common_ground_android.ui.viewmodels.chat_roulette.SelectInterestsState
import com.example.common_ground_android.ui.viewmodels.chat_roulette.SelectInterestsViewModel
import com.example.common_ground_android.ui.viewmodels.chat_roulette.SelectInterestsViewModelFactory
import com.example.common_ground_android.utils.ErrorHandler
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SelectInterestsFragment : Fragment() {

    private var _binding: FragmentSelectInterestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SelectInterestsViewModel by viewModels {
        SelectInterestsViewModelFactory(requireContext())
    }
    private lateinit var interestAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectInterestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoComplete()
        setupListeners()
        setupObservers()
    }

    private fun setupAutoComplete() {
        interestAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_interest_dropdown,
            R.id.interest_name,
            mutableListOf()
        )
        binding.interestAutoComplete.setAdapter(interestAdapter)
        binding.interestAutoComplete.threshold = 1

        binding.interestAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = interestAdapter.getItem(position)
            val currentState = viewModel.state.value
            if (currentState is SelectInterestsState.Ready) {
                val interest = currentState.allInterests.find { it.name == selectedName }
                interest?.let {
                    viewModel.toggleInterest(it.id)
                    binding.interestAutoComplete.text?.clear()
                }
            }
        }

        binding.interestAutoComplete.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && interestAdapter.count > 0) {
                binding.interestAutoComplete.showDropDown()
            }
        }
        binding.interestAutoComplete.setOnClickListener {
            if (interestAdapter.count > 0) {
                binding.interestAutoComplete.showDropDown()
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.startSearchButton.setOnClickListener {
            val currentState = viewModel.state.value
            if (currentState is SelectInterestsState.Ready) {
                val selectedIds = currentState.selectedIds.toList()
                val direction = SelectInterestsFragmentDirections.actionSelectInterestsFragmentToChatRouletteFragment(selectedIds.toTypedArray())
                findNavController().navigate(direction)
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is SelectInterestsState.Loading -> {

                        }
                        is SelectInterestsState.Ready -> {
                            val names = state.allInterests.map { it.name }
                            interestAdapter.clear()
                            interestAdapter.addAll(names)
                            interestAdapter.notifyDataSetChanged()
                            updateSelectedInterestsChips(state.selectedIds)
                            updateSelectedCount(state.selectedIds.size)
                        }
                        is SelectInterestsState.Error -> {
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

    private fun updateSelectedInterestsChips(selectedIds: Set<String>) {
        binding.selectedInterestsChipGroup.removeAllViews()
        val currentState = viewModel.state.value
        if (currentState !is SelectInterestsState.Ready) return
        val allInterests = currentState.allInterests
        selectedIds.forEach { interestId ->
            val interest = allInterests.find { it.id == interestId }
            interest?.let { interestItem ->
                val chip = Chip(requireContext()).apply {
                    text = interestItem.name
                    isCloseIconVisible = true
                    setCloseIconResource(R.drawable.ic_close)
                    setOnCloseIconClickListener {
                        viewModel.toggleInterest(interestItem.id)
                    }
                    isClickable = false
                }
                binding.selectedInterestsChipGroup.addView(chip)
            }
        }
    }

    private fun updateSelectedCount(count: Int) {
        binding.selectedCountText.text = "$count/5"
    }

    private fun navigateToLogin() {
        findNavController().navigate(SelectInterestsFragmentDirections.actionSelectInterestsFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}