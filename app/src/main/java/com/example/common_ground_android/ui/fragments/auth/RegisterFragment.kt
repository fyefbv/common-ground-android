package com.example.common_ground_android.ui.fragments.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.common_ground_android.databinding.FragmentRegisterBinding
import com.example.common_ground_android.ui.navigation.auth.RegisterFragmentDirections
import com.example.common_ground_android.ui.viewmodels.auth.AuthState
import com.example.common_ground_android.ui.viewmodels.auth.AuthViewModelFactory
import com.example.common_ground_android.ui.viewmodels.auth.RegisterViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().moveTaskToBack(true)
            }
        })

        setupViews()
        setupObservers()
        setupListeners()
    }

    private fun setupViews() {
        binding.emailEditText.setText(viewModel.email.value)
        binding.passwordEditText.setText(viewModel.password.value)
        binding.confirmPasswordEditText.setText(viewModel.confirmPassword.value)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    handleRegisterState(state)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.emailError.collect { error ->
                    binding.emailInputLayout.error = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.passwordError.collect { error ->
                    binding.passwordInputLayout.error = error
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.confirmPasswordError.collect { error ->
                    binding.confirmPasswordInputLayout.error = error
                }
            }
        }
    }

    private fun setupListeners() {
        binding.emailEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateEmail(text.toString())
        }

        binding.passwordEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updatePassword(text.toString())
        }

        binding.confirmPasswordEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateConfirmPassword(text.toString())
        }

        binding.registerButton.setOnClickListener {
            hideKeyboard()
            viewModel.register()
        }

        binding.switchToLogin.setOnClickListener {
            navigateToLogin()
        }

        binding.root.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun handleRegisterState(state: AuthState) {
        when (state) {
            is AuthState.Idle -> {
                setLoading(false)
            }
            is AuthState.Loading -> {
                setLoading(true)
            }
            is AuthState.Success -> {
                setLoading(false)
                showSuccess(state.message)
                navigateToProfileSelector()
            }
            is AuthState.Error -> {
                setLoading(false)
                showError(state.message, state.errorCode)
                viewModel.resetState()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.registerButton.isEnabled = !isLoading
        binding.switchToLogin.isEnabled = !isLoading
        binding.registerButton.text = if (isLoading) "Регистрация..." else "Зарегистрироваться"
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String, errorCode: String?) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

        when (errorCode) {
            "user_already_exists" -> {
                snackbar.setAction("Войти") {
                    navigateToLogin()
                }
            }
        }

        snackbar.show()
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun navigateToLogin() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToProfileSelectorFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}