package com.example.common_ground_android.ui.fragments.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
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
import com.example.common_ground_android.databinding.FragmentProfileBinding
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.ProfileStatistics
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.navigation.profile.ProfileFragmentDirections
import com.example.common_ground_android.ui.viewmodels.profile.ProfileFormState
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewModel
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewModelFactory
import com.example.common_ground_android.utils.ImageUtils
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    private lateinit var interestAdapter: ArrayAdapter<String>

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val bitmap = ImageUtils.getBitmapFromUri(requireContext().contentResolver, it)
                val byteArray = ImageUtils.compressBitmapToByteArray(bitmap, 80)
                viewModel.setNewAvatarBytes(byteArray)
                binding.profileAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.failed_load_image, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        })

        setupAutoComplete()
        setupObservers()
        setupListeners()
    }

    private fun setupAutoComplete() {
        interestAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.interestAutoComplete.setAdapter(interestAdapter)
        binding.interestAutoComplete.threshold = 1

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
        binding.interestAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedName = interestAdapter.getItem(position)
            val interest = viewModel.interests.value.find { it.name == selectedName }
            interest?.let {
                viewModel.addInterest(it)
                binding.interestAutoComplete.text?.clear()
            }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    handleState(state)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileData.collect { profile ->
                    profile?.let { updateProfileInfo(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isEditMode.collect { isEditMode ->
                    updateEditMode(isEditMode)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.statistics.collect { statistics ->
                    updateStatistics(statistics)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.interests.collect { interests ->
                    val names = interests.map { it.name }
                    interestAdapter.clear()
                    interestAdapter.addAll(names)
                    interestAdapter.notifyDataSetChanged()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.usernameError.collect { error ->
                    binding.nameInputLayout.error = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bioError.collect { error ->
                    binding.bioInputLayout.error = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pendingInterestIds.collect { pendingIds ->
                    updateEditInterestsChips(pendingIds)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedInterestIds.collect { selectedIds ->
                    updateViewInterestsChips(selectedIds)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newAvatarBytes.collect { bytes ->
                    val hasCurrentAvatar = viewModel.profileData.value?.avatarUrl != null
                    binding.avatarClearButton.visibility =
                        if (viewModel.isEditMode.value && (bytes != null || hasCurrentAvatar)) View.VISIBLE else View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteAvatar.collect { deleteFlag ->
                    if (viewModel.isEditMode.value && deleteFlag) {
                        binding.avatarClearButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateProfileInfo(profile: Profile) {
        if (!viewModel.isEditMode.value) {
            binding.nameEditText.setText(profile.username)
            binding.bioEditText.setText(profile.bio ?: "")
        }

        Glide.with(requireContext())
            .load(profile.avatarUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.profileAvatar)

        val isEmptyProfile = profile.username.isBlank() && profile.bio.isNullOrBlank()
        binding.emptyProfileHint.visibility = if (isEmptyProfile) View.VISIBLE else View.GONE
    }

    private fun updateStatistics(statistics: ProfileStatistics?) {
        if (statistics != null) {
            binding.sessionsCountText.text = statistics.totalSessions.toString()
            binding.ratingText.text = String.format("%.1f", statistics.reputationScore)
            binding.roomsCountText.text = statistics.totalRooms.toString()
        } else {
            binding.sessionsCountText.text = "0"
            binding.ratingText.text = "0.0"
            binding.roomsCountText.text = "0"
        }
    }

    private fun updateEditMode(isEditMode: Boolean) {
        binding.saveButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
        binding.avatarEditButton.visibility = if (isEditMode) View.VISIBLE else View.GONE

        val hasCurrentAvatar = viewModel.profileData.value?.avatarUrl != null
        val hasNewAvatar = viewModel.newAvatarBytes.value != null
        val deleteFlag = viewModel.deleteAvatar.value
        binding.avatarClearButton.visibility = if (isEditMode && (hasNewAvatar || hasCurrentAvatar || deleteFlag)) View.VISIBLE else View.GONE

        binding.nameInputLayout.isEnabled = isEditMode
        binding.bioInputLayout.isEnabled = isEditMode

        binding.interestsViewMode.visibility = if (isEditMode) View.GONE else View.VISIBLE
        binding.interestsEditMode.visibility = if (isEditMode) View.VISIBLE else View.GONE

        if (!isEditMode) {
            binding.nameEditText.setText(viewModel.username.value)
            binding.bioEditText.setText(viewModel.bio.value)
        }
    }

    private fun updateViewInterestsChips(selectedIds: Set<String>) {
        binding.selectedInterestsChipGroup.removeAllViews()
        if (selectedIds.isEmpty()) {
            binding.noInterestsHint.visibility = View.VISIBLE
        } else {
            binding.noInterestsHint.visibility = View.GONE
            val allInterests = viewModel.interests.value
            selectedIds.forEach { id ->
                val interest = allInterests.find { it.id == id }
                interest?.let {
                    val chip = Chip(requireContext()).apply {
                        text = it.name
                        isClickable = false
                        isCheckable = false
                    }
                    binding.selectedInterestsChipGroup.addView(chip)
                }
            }
        }
    }

    private fun updateEditInterestsChips(pendingIds: Set<String>) {
        binding.editSelectedInterestsChipGroup.removeAllViews()
        val allInterests = viewModel.interests.value
        pendingIds.forEach { interestId ->
            val interest = allInterests.find { it.id == interestId }
            interest?.let {
                val chip = Chip(requireContext()).apply {
                    text = it.name
                    tag = interestId
                    isCloseIconVisible = true
                    setCloseIconResource(R.drawable.ic_close)
                    setOnCloseIconClickListener {
                        val id = tag as? String ?: return@setOnCloseIconClickListener
                        viewModel.removeInterest(id)
                    }
                    isClickable = false
                }
                binding.editSelectedInterestsChipGroup.addView(chip)
            }
        }
    }

    private fun setupListeners() {
        binding.editButton.setOnClickListener {
            if (viewModel.isEditMode.value) {
                viewModel.cancelEditing()
                val currentAvatarUrl = viewModel.profileData.value?.avatarUrl
                if (currentAvatarUrl != null) {
                    Glide.with(requireContext())
                        .load(currentAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.profileAvatar)
                } else {
                    binding.profileAvatar.setImageResource(R.drawable.ic_person)
                }
            } else {
                viewModel.toggleEditMode()
            }
        }

        binding.saveButton.setOnClickListener {
            viewModel.updateProfile()
        }

        binding.switchProfileButton.setOnClickListener {
            viewModel.switchProfile()
        }

        binding.fillProfileButton.setOnClickListener {
            viewModel.toggleEditMode()
        }

        binding.avatarEditButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.avatarClearButton.setOnClickListener {
            if (viewModel.newAvatarBytes.value != null) {
                viewModel.cancelAvatarChanges()
                val currentAvatarUrl = viewModel.profileData.value?.avatarUrl
                if (currentAvatarUrl != null) {
                    Glide.with(requireContext())
                        .load(currentAvatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.profileAvatar)
                } else {
                    binding.profileAvatar.setImageResource(R.drawable.ic_person)
                }
                Toast.makeText(requireContext(), R.string.avatar_changes_cancelled, Toast.LENGTH_SHORT).show()
            } else if (viewModel.profileData.value?.avatarUrl != null) {
                viewModel.markAvatarForDeletion()
                binding.profileAvatar.setImageResource(R.drawable.ic_person)
                Toast.makeText(requireContext(), R.string.avatar_will_be_deleted_on_save, Toast.LENGTH_SHORT).show()
            }
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        binding.deleteProfileButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
                .setTitle(R.string.delete_profile)
                .setMessage(R.string.delete_profile_confirmation)
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewModel.deleteCurrentProfile()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        binding.nameEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateUsername(text.toString())
        }

        binding.bioEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateBio(text.toString())
        }
    }

    private fun handleState(state: ProfileFormState) {
        when (state) {
            is ProfileFormState.Idle -> setLoading(false)
            is ProfileFormState.Loading -> setLoading(true)
            is ProfileFormState.Success -> {
                setLoading(false)
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                if (state.message == getString(R.string.profile_switch_success) || state.message == getString(R.string.profile_deleted_success)) {
                    navigateToProfileSelector()
                }
            }
            is ProfileFormState.Error -> {
                setLoading(false)
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

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.editButton.isEnabled = !isLoading
        binding.saveButton.isEnabled = !isLoading
        binding.switchProfileButton.isEnabled = !isLoading
        binding.avatarEditButton.isEnabled = !isLoading
        binding.avatarClearButton.isEnabled = !isLoading
        binding.interestAutoComplete.isEnabled = !isLoading
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToProfileSelectorFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}