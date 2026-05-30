package com.example.common_ground_android.ui.fragments.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentAccountSettingsBinding
import com.example.common_ground_android.ui.navigation.account.AccountSettingsFragmentDirections
import com.example.common_ground_android.ui.viewmodels.account.AccountSettingsState
import com.example.common_ground_android.ui.viewmodels.account.AccountSettingsViewModel
import com.example.common_ground_android.ui.viewmodels.account.AccountSettingsViewModelFactory
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.utils.ValidationUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountSettingsViewModel by viewModels {
        AccountSettingsViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
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
                viewModel.currentEmail.collect { email ->
                    binding.currentEmailText.text = email
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newPasswordError.collect { error ->
                    binding.newPasswordLayout.error = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.confirmPasswordError.collect { error ->
                    binding.confirmPasswordLayout.error = error
                }
            }
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changeEmailButton.setOnClickListener {
            showChangeEmailDialog()
        }

        binding.savePasswordButton.setOnClickListener {
            viewModel.updatePassword()
        }

        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
        }

        binding.newPasswordEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNewPassword(text.toString())
        }

        binding.confirmPasswordEdit.doOnTextChanged { text, _, _, _ ->
            viewModel.updateConfirmPassword(text.toString())
        }
    }

    private fun showChangeEmailDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_email, null)
        val emailInput = dialogView.findViewById<TextInputEditText>(R.id.email_input)
        val emailLayout = dialogView.findViewById<TextInputLayout>(R.id.email_input_layout)

        emailInput.doOnTextChanged { text, _, _, _ ->
            val email = text.toString().trim()
            emailLayout.error = if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) {
                "Введите корректный email"
            } else null
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.change_email)
            .setView(dialogView)
            .setPositiveButton("Сохранить", null)
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val email = emailInput.text.toString().trim()
                if (ValidationUtils.isValidEmail(email)) {
                    viewModel.updateEmail(email)
                    dialog.dismiss()
                } else {
                    emailLayout.error = "Введите корректный email"
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_confirmation)
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun handleState(state: AccountSettingsState) {
        when (state) {
            is AccountSettingsState.Loading -> setLoading(true)
            is AccountSettingsState.Success -> {
                setLoading(false)
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            is AccountSettingsState.Error -> {
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
            is AccountSettingsState.LoggedOut -> {
                setLoading(false)
                navigateToLogin()
            }
            AccountSettingsState.Idle -> setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.savePasswordButton.isEnabled = !isLoading
        binding.changeEmailButton.isEnabled = !isLoading
        binding.deleteAccountButton.isEnabled = !isLoading
        binding.logoutButton.isEnabled = !isLoading
    }

    private fun navigateToLogin() {
        findNavController().navigate(AccountSettingsFragmentDirections.Companion.actionAccountSettingsFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}