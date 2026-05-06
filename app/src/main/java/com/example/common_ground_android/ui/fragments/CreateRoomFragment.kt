package com.example.common_ground_android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentCreateRoomBinding
import com.example.common_ground_android.network.utils.ErrorHandler
import com.example.common_ground_android.ui.fragments.navigation.rooms.CreateRoomFragmentDirections
import com.example.common_ground_android.ui.viewmodels.rooms.CreateRoomState
import com.example.common_ground_android.ui.viewmodels.rooms.CreateRoomViewModel
import com.example.common_ground_android.ui.viewmodels.rooms.CreateRoomViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateRoomFragment : Fragment() {

    private var _binding: FragmentCreateRoomBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateRoomViewModel by viewModels {
        CreateRoomViewModelFactory(requireContext())
    }

    private lateinit var interestAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPrimaryInterestAutoComplete()
        setupTagInput()
        setupListeners()
        setupObservers()
        setupSlider()
    }

    private fun setupPrimaryInterestAutoComplete() {
        interestAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        binding.primaryInterestAutoComplete.setAdapter(interestAdapter)
        binding.primaryInterestAutoComplete.threshold = 1

        lifecycleScope.launch {
            viewModel.availableInterests.collect { interests ->
                val names = interests.map { it.name }
                interestAdapter.clear()
                interestAdapter.addAll(names)
                interestAdapter.notifyDataSetChanged()
            }
        }

        binding.primaryInterestAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = interestAdapter.getItem(position)
            val interest = viewModel.availableInterests.value.find { it.name == selectedName }
            viewModel.updatePrimaryInterest(interest?.id)
        }
    }

    private fun setupTagInput() {
        binding.tagInputLayout.setEndIconOnClickListener {
            val tag = binding.tagEditText.text.toString().trim()
            if (tag.isNotEmpty()) {
                viewModel.addTag(tag)
                binding.tagEditText.text?.clear()
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.createRoomButton.setOnClickListener {
            viewModel.createRoom()
        }
        binding.privateRoomSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateIsPrivate(isChecked)
        }
        binding.nameEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateName(text.toString())
        }
        binding.descriptionEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateDescription(text.toString())
        }
    }

    private fun setupSlider() {
        binding.maxParticipantsSlider.addOnChangeListener { _, value, _ ->
            val intValue = value.toInt()
            viewModel.updateMaxParticipants(intValue)
            binding.maxParticipantsValue.text = resources.getQuantityString(R.plurals.participants_count, intValue, intValue)
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
                viewModel.nameError.collect { error ->
                    binding.nameInputLayout.error = error
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.descriptionError.collect { error ->
                    binding.descriptionInputLayout.error = error
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tags.collect { tags ->
                    updateTagsChips(tags)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.maxParticipants.collect { value ->
                    binding.maxParticipantsSlider.value = value.toFloat()
                    binding.maxParticipantsValue.text = resources.getQuantityString(R.plurals.participants_count, value, value)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPrivate.collect { isPrivate ->
                    binding.privateRoomSwitch.isChecked = isPrivate
                }
            }
        }
    }

    private fun updateTagsChips(tags: List<String>) {
        binding.tagsChipGroup.removeAllViews()
        tags.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isCloseIconVisible = true
                setCloseIconResource(R.drawable.ic_close)
                setOnCloseIconClickListener {
                    viewModel.removeTag(tag)
                }
            }
            binding.tagsChipGroup.addView(chip)
        }
    }

    private fun handleState(state: CreateRoomState) {
        when (state) {
            is CreateRoomState.Idle -> setLoading(false)
            is CreateRoomState.Loading -> setLoading(true)
            is CreateRoomState.Success -> {
                setLoading(false)
                Snackbar.make(binding.root, "Комната создана", Snackbar.LENGTH_SHORT).show()
                navigateToRooms()
            }
            is CreateRoomState.Error -> {
                setLoading(false)
                if (ErrorHandler.isAuthError(state.errorCode)) {
                    viewModel.clearTokensAndLogout()
                    Snackbar.make(binding.root, "Сессия истекла. Пожалуйста, войдите заново.", Snackbar.LENGTH_LONG).show()
                    navigateToLogin()
                } else {
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                viewModel.resetState()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.createRoomButton.isEnabled = !isLoading
        binding.createRoomButton.text = if (isLoading) "Создание..." else getString(R.string.create_room)
        binding.nameEditText.isEnabled = !isLoading
        binding.descriptionEditText.isEnabled = !isLoading
        binding.primaryInterestAutoComplete.isEnabled = !isLoading
        binding.tagEditText.isEnabled = !isLoading
        binding.maxParticipantsSlider.isEnabled = !isLoading
        binding.privateRoomSwitch.isEnabled = !isLoading
    }

    private fun navigateToRooms() {
        findNavController().navigate(CreateRoomFragmentDirections.actionCreateRoomFragmentToRoomsFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(CreateRoomFragmentDirections.actionCreateRoomFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}