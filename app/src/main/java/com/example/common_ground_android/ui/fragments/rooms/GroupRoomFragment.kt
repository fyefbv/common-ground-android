package com.example.common_ground_android.ui.fragments.rooms

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.common_ground_android.R
import com.example.common_ground_android.databinding.FragmentGroupRoomBinding
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Message
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.DisplayItem
import com.example.common_ground_android.ui.adapters.RoomMessageAdapter
import com.example.common_ground_android.ui.navigation.rooms.GroupRoomFragmentDirections
import com.example.common_ground_android.ui.viewmodels.rooms.GroupRoomState
import com.example.common_ground_android.ui.viewmodels.rooms.GroupRoomViewModel
import com.example.common_ground_android.ui.viewmodels.rooms.GroupRoomViewModelFactory
import com.example.common_ground_android.ui.viewmodels.rooms.RoomEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GroupRoomFragment : Fragment() {

    private var _binding: FragmentGroupRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupRoomViewModel by viewModels {
        val roomId = arguments?.getString("roomId") ?: ""
        val currentProfileId = getCurrentProfileId()
        GroupRoomViewModelFactory(requireContext(), roomId, currentProfileId)
    }
    private lateinit var messageAdapter: RoomMessageAdapter
    private var currentProfileId = ""
    private var editingMessageId: String? = null
    private var sourceFragmentId = R.id.roomsFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sourceFragmentId = arguments?.getInt("sourceFragmentId") ?: R.id.roomsFragment
        currentProfileId = getCurrentProfileId()

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        viewModel.setActive(true)
    }

    override fun onStop() {
        super.onStop()
        viewModel.setActive(false)
    }

    private fun getCurrentProfileId(): String {
        return KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
    }

    private fun setupRecyclerView() {
        messageAdapter = RoomMessageAdapter(
            currentProfileId = currentProfileId,
            recyclerView = binding.messagesRecyclerView,
            onMessageLongClick = { message, anchorView -> showMessageOptionsDialog(message, anchorView) },
            getParentMessage = { parentId -> viewModel.messages.value.find { it.id == parentId } },
            onReplyClick = { parentId ->
                val position = viewModel.messages.value.indexOfFirst { it.id == parentId }
                if (position != -1) {
                    binding.messagesRecyclerView.scrollToPosition(position)
                    messageAdapter.highlightMessage(parentId)
                }
            }
        )
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.messagesRecyclerView.adapter = messageAdapter
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            viewModel.forceDisconnect()
            findNavController().navigateUp()
        }
        binding.sendButton.setOnClickListener {
            if (viewModel.isMuted.value) {
                Snackbar.make(binding.root, "Вы замучены и не можете отправлять сообщения", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendNewMessage()
        }
        binding.messageEditText.doOnTextChanged { _, _, _, _ ->
            viewModel.onUserTyping()
        }
        binding.roomMenuButton.setOnClickListener {
            val roomId = arguments?.getString("roomId") ?: return@setOnClickListener
            findNavController().navigate(
                GroupRoomFragmentDirections.actionGroupRoomFragmentToRoomManagementFragment(roomId, sourceFragmentId)
            )
        }
    }

    private fun setupObservers(){
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is GroupRoomState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is GroupRoomState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.roomNameText.text = state.room.name
                            updateOnlineCount(state.onlineCount)
                        }
                        is GroupRoomState.Error -> {
                            binding.progressBar.visibility = View.GONE
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
        var lastMessageCount = 0
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    val wasEmpty = lastMessageCount == 0
                    val isAtBottom = !binding.messagesRecyclerView.canScrollVertically(1)
                    val hasNewMessage = messages.size > lastMessageCount
                    val lastMessage = if (hasNewMessage) messages.lastOrNull() else null
                    val isSelfMessage = lastMessage?.senderId == currentProfileId

                    messageAdapter.submitMessages(messages) {
                        if (wasEmpty || isSelfMessage || isAtBottom) {
                            val lastMessagePos = messageAdapter.currentList.indexOfLast { it is DisplayItem.MessageItem }
                            if (lastMessagePos != -1) {
                                binding.messagesRecyclerView.scrollToPosition(lastMessagePos)
                            }
                        }
                    }
                    lastMessageCount = messages.size
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onlineCount.collect { count ->
                    updateOnlineCount(count)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.senderProfiles.collect { profiles ->
                    messageAdapter.updateProfiles(profiles)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.typingIndicatorText.collect { text ->
                    binding.typingIndicator.text = text ?: ""
                    binding.typingIndicator.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.replyTargetMessage.collect { message ->
                    if (message != null) {
                        binding.replyPreviewContainer.visibility = View.VISIBLE
                        binding.replyPreviewText.text = message.content
                        binding.cancelReplyButton.setOnClickListener {
                            viewModel.clearReplyTarget()
                        }
                    } else {
                        binding.replyPreviewContainer.visibility = View.GONE
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is RoomEvent.Error -> {
                            if (event.errorCode in setOf("banned", "kicked", "room_deleted")) {
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                                if (!findNavController().popBackStack(sourceFragmentId, false)) {
                                    findNavController().popBackStack(R.id.roomsFragment, false)
                                }
                            } else {
                                Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        is RoomEvent.Success -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateOnlineCount(count: Int) {
        val text = resources.getQuantityString(R.plurals.online_count, count, count)
        binding.onlineStatusText.text = text
    }

    private fun startEditingMessage(message: Message) {
        editingMessageId = message.id
        binding.messageEditText.setText(message.content)
        binding.messageEditText.requestFocus()
        binding.sendButton.setImageResource(R.drawable.ic_check)
        binding.cancelEditButton.visibility = View.VISIBLE
        binding.sendButton.setOnClickListener { saveEditMessage() }
        binding.cancelEditButton.setOnClickListener { cancelEditing() }
    }

    private fun saveEditMessage() {
        val newText = binding.messageEditText.text.toString().trim()
        if (newText.isNotEmpty() && editingMessageId != null) {
            viewModel.editMessage(editingMessageId!!, newText)
            cancelEditing()
        }
    }

    private fun cancelEditing() {
        editingMessageId = null
        binding.messageEditText.text?.clear()
        binding.sendButton.setImageResource(R.drawable.ic_send)
        binding.cancelEditButton.visibility = View.GONE
        binding.sendButton.setOnClickListener {
            if (viewModel.isMuted.value) {
                Snackbar.make(binding.root, "Вы замучены и не можете отправлять сообщения", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendNewMessage()
        }
    }

    private fun sendNewMessage() {
        val text = binding.messageEditText.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessageWithReply(text)
            binding.messageEditText.text?.clear()
        }
    }

    private fun showMessageOptionsDialog(message: Message, anchor: View) {
        val isOwn = message.senderId == currentProfileId
        val currentRole = (viewModel.state.value as? GroupRoomState.Success)?.currentRole ?: "MEMBER"
        val senderRole = viewModel.senderRoles.value[message.senderId] ?: "MEMBER"

        val canDelete = isOwn ||
                (currentRole == "CREATOR") ||
                (currentRole == "MODERATOR" && senderRole == "MEMBER")

        val wrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu)
        val popupMenu = PopupMenu(wrapper, anchor)
        popupMenu.gravity = if (isOwn) Gravity.END else Gravity.START
        popupMenu.inflate(R.menu.message_context_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reply -> viewModel.setReplyTarget(message.id)
                R.id.action_edit -> startEditingMessage(message)
                R.id.action_delete -> showDeleteMessageDialog(message)
            }
            true
        }
        popupMenu.menu.findItem(R.id.action_edit).isVisible = isOwn
        popupMenu.menu.findItem(R.id.action_delete).isVisible = canDelete
        popupMenu.show()
    }

    private fun showDeleteMessageDialog(message: Message) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
            .setTitle("Удалить сообщение")
            .setMessage("Вы уверены, что хотите удалить это сообщение?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteMessage(message.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun navigateToLogin() {
        findNavController().navigate(GroupRoomFragmentDirections.actionGroupRoomFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}