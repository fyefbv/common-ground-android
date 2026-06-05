package com.example.common_ground_android.ui.fragments.profile

import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentCreateProfileBinding
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.navigation.profile.CreateProfileFragmentDirections
import com.example.common_ground_android.ui.viewmodels.profile.CreateProfileViewModel
import com.example.common_ground_android.ui.viewmodels.profile.ProfileFormState
import com.example.common_ground_android.ui.viewmodels.profile.ProfileViewModelFactory
import com.example.common_ground_android.utils.ImageUtils
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class CreateProfileFragment : Fragment() {

    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateProfileViewModel by viewModels {
        ProfileViewModelFactory(requireContext())
    }

    private lateinit var interestAdapter: ArrayAdapter<String>

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = ImageUtils.getBitmapFromUri(requireContext().contentResolver, uri)
                val byteArray = ImageUtils.compressBitmapToByteArray(bitmap, 80)
                viewModel.setAvatarBytes(byteArray)
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
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAutoComplete()
        setupObservers()
        setupListeners()
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
            val interest = viewModel.interests.value.find { it.name == selectedName }
            interest?.let {
                viewModel.addInterest(it)
                binding.interestAutoComplete.text?.clear()
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
                viewModel.usernameError.collect { error ->
                    binding.usernameInputLayout.error = error
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
                viewModel.selectedInterestIds.collect { selectedIds ->
                    updateSelectedInterestsChips(selectedIds)
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
                viewModel.selectedAvatarBytes.collect { bytes ->
                    if (bytes == null) {
                        binding.avatarClearButton.visibility = View.GONE
                        binding.profileAvatar.setImageResource(R.drawable.ic_person)
                    } else {
                        binding.avatarClearButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateSelectedInterestsChips(selectedIds: Set<String>) {
        binding.selectedInterestsChipGroup.removeAllViews()
        val allInterests = viewModel.interests.value
        selectedIds.forEach { interestId ->
            val interest = allInterests.find { it.id == interestId }
            interest?.let { interestItem ->
                val chip = Chip(requireContext()).apply {
                    text = interestItem.name
                    isCloseIconVisible = true
                    setCloseIconResource(R.drawable.ic_close)
                    setOnCloseIconClickListener {
                        viewModel.removeInterest(interestItem.id)
                    }
                    isClickable = false
                }
                binding.selectedInterestsChipGroup.addView(chip)
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.createProfileButton.setOnClickListener {
            viewModel.createProfile()
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }

        binding.usernameEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateUsername(text.toString())
        }

        binding.bioEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateBio(text.toString())
        }

        binding.avatarEditButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.avatarClearButton.setOnClickListener {
            viewModel.clearAvatarBytes()
            binding.profileAvatar.setImageResource(R.drawable.ic_person)
            Toast.makeText(requireContext(), R.string.avatar_removed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleState(state: ProfileFormState) {
        when (state) {
            is ProfileFormState.Idle -> setLoading(false)
            is ProfileFormState.Loading -> setLoading(true)
            is ProfileFormState.Success -> {
                setLoading(false)
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                navigateToProfileSelector()
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
                viewModel.resetState()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.createProfileButton.isEnabled = !isLoading
        binding.createProfileButton.text = if (isLoading) getString(R.string.creating) else getString(R.string.create_profile_button)
        binding.avatarEditButton.isEnabled = !isLoading
        binding.interestAutoComplete.isEnabled = !isLoading
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(CreateProfileFragmentDirections.actionCreateProfileFragmentToProfileSelectorFragment())
    }

    private fun navigateToLogin() {
        findNavController().navigate(CreateProfileFragmentDirections.actionCreateProfileFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}