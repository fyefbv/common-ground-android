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
import com.example.common_ground_android.databinding.FragmentLoginBinding
import com.example.common_ground_android.ui.MainActivity
import com.example.common_ground_android.ui.fragments.navigation.auth.LoginFragmentDirections
import com.example.common_ground_android.ui.viewmodels.auth.AuthState
import com.example.common_ground_android.ui.viewmodels.auth.AuthViewModelFactory
import com.example.common_ground_android.ui.viewmodels.auth.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupObservers()
        setupListeners()
    }

    private fun setupViews() {
        binding.emailEditText.setText(viewModel.email.value)
        binding.passwordEditText.setText(viewModel.password.value)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    handleLoginState(state)
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
    }

    private fun setupListeners() {
        binding.emailEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateEmail(text.toString())
        }

        binding.passwordEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updatePassword(text.toString())
        }

        binding.loginButton.setOnClickListener {
            viewModel.login()
        }

        binding.switchToRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun handleLoginState(state: AuthState) {
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
        binding.loginButton.isEnabled = !isLoading
        binding.loginButton.text = if (isLoading) "Загрузка..." else "Войти"
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showError(message: String, errorCode: String?) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

        when (errorCode) {
            "user_not_found" -> {
                snackbar.setAction("Зарегистрироваться") {
                    navigateToRegister()
                }
            }
            "expired_token", "invalid_token" -> { }
        }

        snackbar.show()
    }

    private fun navigateToRegister() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
    }

    private fun navigateToProfileSelector() {
        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToProfileSelectorFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}