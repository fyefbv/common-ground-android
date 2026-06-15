package com.example.common_ground_android.ui.fragments.chat_roulette

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.widget.doOnTextChanged
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
import com.example.common_ground_android.databinding.FragmentChatRouletteBinding
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.ui.adapters.ChatRouletteMessageAdapter
import com.example.common_ground_android.ui.navigation.chat_roulette.ChatRouletteFragmentDirections
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteEvent
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteState
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModel
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ChatRouletteViewModelFactory
import com.example.common_ground_android.ui.viewmodels.chat_roulette.ExtensionState
import com.example.common_ground_android.utils.ErrorHandler
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ChatRouletteFragment : Fragment() {

    private var _binding: FragmentChatRouletteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatRouletteViewModel by viewModels {
        ChatRouletteViewModelFactory(requireContext())
    }
    private lateinit var adapter: ChatRouletteMessageAdapter
    private var lastMessageCount = 0
    private var extensionDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRouletteBinding.inflate(inflater, container, false)
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
        setupListeners()
        setupObservers()

        val interestIds = arguments?.getStringArray("interestIds")?.toList() ?: emptyList()
        viewModel.startSearch(interestIds)
    }

    private fun setupRecyclerView() {
        val currentProfileId = getCurrentProfileId()
        adapter = ChatRouletteMessageAdapter(currentProfileId)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.messagesRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val text = binding.messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.messageEditText.text?.clear()
            }
        }
        binding.extendButton.setOnClickListener { viewModel.requestExtension() }
        binding.completeButton.setOnClickListener { showEndSessionDialog() }
        binding.reportButton.setOnClickListener {
            ReportDialogFragment().show(childFragmentManager, "report_dialog")
        }
        binding.cancelSearchButton.setOnClickListener {
            viewModel.cancelSearch()
            findNavController().navigateUp()
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
                viewModel.event.collect { event ->
                    when (event) {
                        is ChatRouletteEvent.Success -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            if (event.code == "extension_cancelled"){
                                extensionDialog?.dismiss()
                            }
                        }
                        is ChatRouletteEvent.Error -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    val wasEmpty = lastMessageCount == 0
                    val isAtBottom = !binding.messagesRecyclerView.canScrollVertically(1)
                    val hasNewMessage = messages.size > lastMessageCount
                    val lastMessage = if (hasNewMessage) messages.lastOrNull() else null
                    val isSelfMessage = lastMessage?.senderId == getCurrentProfileId()

                    adapter.submitList(messages) {
                        if (wasEmpty || isSelfMessage || isAtBottom) {
                            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                    lastMessageCount = messages.size
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.timerSeconds.collect { seconds ->
                    updateTimer(seconds)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.partnerOnline.collect { isOnline ->
                    updatePartnerOnline(isOnline)
                }
            }
        }
    }

    private fun handleState(state: ChatRouletteState) {
        when (state) {
            is ChatRouletteState.Searching -> {
                showSearchingState(true)
                showChatState(false)
                setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.md_surface_container), true)
            }
            is ChatRouletteState.ActiveSession -> {
                showSearchingState(false)
                showChatState(true)
                updatePartnerInfo(state.partner, state.commonInterests, state.matchedInterest)
                updateExtensionButton(state.extensionState)
                showExtensionRequestDialog(state.extensionState)
                setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.md_primary_container), true)
            }
            is ChatRouletteState.Rating -> {
                RatePartnerDialogFragment().show(childFragmentManager, "rate_dialog")
            }
            is ChatRouletteState.Finished -> {
                findNavController().navigateUp()
            }
            is ChatRouletteState.Error -> {
                if (ErrorHandler.isAuthError(state.errorCode)) {
                    viewModel.clearTokensAndLogout()
                    Toast.makeText(requireContext(), R.string.error_session_expired_relogin, Toast.LENGTH_LONG).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    if (state.errorCode == "no_matching_found" || state.errorCode == "search_cancelled") {
                        findNavController().navigateUp()
                    }
                }
            }
            else -> {}
        }
    }

    private fun showSearchingState(show: Boolean) {
        binding.searchingState.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.searchingToolbar.visibility = View.GONE
            binding.chatState.visibility = View.GONE
        }
    }

    private fun showChatState(show: Boolean) {
        if (show) {
            binding.searchingState.visibility = View.GONE
            binding.searchingToolbar.visibility = View.VISIBLE
            binding.chatState.visibility = View.VISIBLE
        }
    }

    private fun updatePartnerInfo(
        partner: Profile?,
        commonInterests: List<Interest>,
        matchedInterest: Interest?
    ) {
        if (partner == null) {
            binding.partnerName.text = getString(R.string.unknown_partner)
            binding.partnerAvatar.setImageResource(R.drawable.ic_person)
            binding.matchedInterest.visibility = View.GONE
            binding.commonInterests.visibility = View.GONE
            return
        }

        binding.partnerName.text = partner.username
        Glide.with(requireContext())
            .load(partner.avatarUrl)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.partnerAvatar)

        if (matchedInterest != null) {
            binding.matchedInterestText.text = matchedInterest.name
            binding.matchedInterest.visibility = View.VISIBLE
        } else {
            binding.matchedInterest.visibility = View.GONE
        }

        binding.commonInterestsContainer.removeAllViews()
        if (commonInterests.isNotEmpty()) {
            commonInterests.forEach { interest ->
                val badge = TextView(requireContext()).apply {
                    text = interest.name
                    setTextColor(ContextCompat.getColor(context, R.color.md_on_primary_container))
                    setBackgroundResource(R.drawable.badge_background)
                    textSize = 10f
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.spacing_8),
                        resources.getDimensionPixelSize(R.dimen.spacing_2),
                        resources.getDimensionPixelSize(R.dimen.spacing_8),
                        resources.getDimensionPixelSize(R.dimen.spacing_2)
                    )
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_4) }
                }
                binding.commonInterestsContainer.addView(badge)
            }
            binding.commonInterests.visibility = View.VISIBLE
        } else {
            binding.commonInterests.visibility = View.GONE
        }
    }

    private fun updateTimer(seconds: Int) {
        val minutes = seconds / 60
        val secs = seconds % 60
        binding.sessionTimer.text = String.format("%02d:%02d", minutes, secs)
    }

    private fun updatePartnerOnline(isOnline: Boolean) {
        if (isOnline) {
            binding.onlineStatusContainer.visibility = View.VISIBLE
            binding.offlineStatusContainer.visibility = View.GONE
        } else {
            binding.onlineStatusContainer.visibility = View.GONE
            binding.offlineStatusContainer.visibility = View.VISIBLE
        }
    }

    private fun showExtensionRequestDialog(extensionState: ExtensionState) {
        if (extensionState == ExtensionState.REQUESTED_BY_PARTNER) {
            extensionDialog?.dismiss()
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
                .setTitle(R.string.extend_session_dialog_title)
                .setMessage(R.string.extend_session_dialog_message)
                .setPositiveButton(R.string.extend_session_agree) { _, _ ->
                    viewModel.acceptExtension()
                }
                .setNegativeButton(R.string.extend_session_decline) { _, _ ->
                    viewModel.rejectExtension()
                }
                .setOnCancelListener {
                    viewModel.rejectExtension()
                }
                .create()
            dialog.setOnDismissListener { extensionDialog = null }
            extensionDialog = dialog
            dialog.show()
        }
    }

    private fun showEndSessionDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
            .setTitle(R.string.end_session_dialog_title)
            .setMessage(R.string.end_session_dialog_message)
            .setPositiveButton(R.string.end_session_confirm) { _, _ -> viewModel.endSession() }
            .setNegativeButton(R.string.end_session_cancel, null)
            .show()
    }

    private fun updateExtensionButton(extensionState: ExtensionState) {
        when (extensionState) {
            ExtensionState.REQUESTED_BY_ME -> {
                binding.extendButton.text = getString(R.string.cancel)
                binding.extendButton.setOnClickListener { viewModel.cancelExtensionRequest() }
            }
            else -> {
                binding.extendButton.text = getString(R.string.extend_session_button)
                binding.extendButton.setOnClickListener { viewModel.requestExtension() }
            }
        }
    }

    private fun getCurrentProfileId(): String {
        return KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
    }

    private fun navigateToLogin() {
        findNavController().navigate(ChatRouletteFragmentDirections.actionChatRouletteFragmentToLoginFragment())
    }

    private fun setStatusBarColor(@ColorInt color: Int, lightIcons: Boolean = false) {
        val window = requireActivity().window
        window.statusBarColor = color

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = lightIcons
    }

    private fun restoreStatusBarColor() {
        setStatusBarColor(
            ContextCompat.getColor(requireContext(), R.color.md_surface_container),
            true
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBarColor()
        _binding = null
    }
}