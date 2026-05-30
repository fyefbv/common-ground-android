package com.example.common_ground_android.ui.fragments.rooms

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.example.common_ground_android.databinding.FragmentRoomManagementBinding
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Participant
import com.example.common_ground_android.network.model.domain.ParticipantRole
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.utils.ErrorHandler
import com.example.common_ground_android.ui.adapters.ParticipantAdapter
import com.example.common_ground_android.ui.navigation.rooms.RoomManagementFragmentDirections
import com.example.common_ground_android.ui.viewmodels.rooms.RoomEvent
import com.example.common_ground_android.ui.viewmodels.rooms.RoomManagementState
import com.example.common_ground_android.ui.viewmodels.rooms.RoomManagementViewModel
import com.example.common_ground_android.ui.viewmodels.rooms.RoomManagementViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch

class RoomManagementFragment : Fragment() {

    private var _binding: FragmentRoomManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RoomManagementViewModel by viewModels {
        val roomId = arguments?.getString("roomId") ?: ""
        val currentProfileId = getCurrentProfileId()
        RoomManagementViewModelFactory(requireContext(), roomId, currentProfileId)
    }
    private lateinit var adapter: ParticipantAdapter
    private lateinit var interestAdapter: ArrayAdapter<String>
    private var sourceFragmentId = R.id.roomsFragment
    private var currentSearchQuery = ""
    private var isUserEditingName = false
    private var isUserEditingDescription = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sourceFragmentId = arguments?.getInt("sourceFragmentId") ?: R.id.roomsFragment

        setupRecyclerView()
        setupListeners()
        setupEditModeFields()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRoomStatus()
    }

    private fun getCurrentProfileId(): String {
        return KtorClientFactory.getTokenManager().getProfileIdSync() ?: ""
    }

    private fun setupRecyclerView() {
        adapter = ParticipantAdapter(
            onParticipantClick = { participant ->
                findNavController().navigate(
                    RoomManagementFragmentDirections.actionRoomManagementFragmentToProfileViewFragment(participant.profileId)
                )
            },
            onMenuClick = { participant ->
                showParticipantMenu(participant)
            },
            currentProfileId = getCurrentProfileId(),
            currentUserRole = ParticipantRole.MEMBER
        )
        binding.participantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.participantsRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener { findNavController().navigateUp() }
        binding.editRoomButton.setOnClickListener {
            viewModel.toggleEditMode()
        }
        binding.saveRoomButton.setOnClickListener { viewModel.saveRoomChanges() }
        binding.cancelEditButton.setOnClickListener { viewModel.toggleEditMode() }
        binding.leaveRoomButton.setOnClickListener { confirmLeaveRoom() }
        binding.deleteRoomButton.setOnClickListener { confirmDeleteRoom() }

        binding.searchParticipants.doOnTextChanged { text, _, _, _ ->
            currentSearchQuery = text.toString().trim().lowercase()
            filterParticipants(currentSearchQuery)
        }
    }

    private fun setupEditModeFields() {
        interestAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        (binding.editPrimaryInterest as? MaterialAutoCompleteTextView)?.setAdapter(interestAdapter)
        binding.editPrimaryInterest.threshold = 1

        lifecycleScope.launch {
            viewModel.availableInterests.collect { interests ->
                val names = interests.map { it.name }
                interestAdapter.clear()
                interestAdapter.addAll(names)
                interestAdapter.notifyDataSetChanged()
            }
        }

        binding.editPrimaryInterest.setOnItemClickListener { _, _, position, _ ->
            val selectedName = interestAdapter.getItem(position)
            val interest = viewModel.availableInterests.value.find { it.name == selectedName }
            viewModel.updateEditPrimaryInterest(interest?.id)
        }

        binding.editTagInput.setOnEditorActionListener { _, _, _ ->
            val tag = binding.editTagInput.text.toString().trim()
            if (tag.isNotEmpty()) {
                viewModel.addTag(tag)
                binding.editTagInput.text?.clear()
            }
            true
        }

        binding.editMaxParticipantsSlider.addOnChangeListener { _, value, _ ->
            viewModel.updateEditMaxParticipants(value.toInt())
        }

        binding.editPrivateSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateEditIsPrivate(isChecked)
        }

        binding.editRoomName.doOnTextChanged { text, _, _, _ ->
            isUserEditingName = true
            viewModel.updateEditRoomName(text.toString())
        }

        binding.editRoomDescription.doOnTextChanged { text, _, _, _ ->
            isUserEditingDescription = true
            viewModel.updateEditRoomDescription(text.toString())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is RoomManagementState.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is RoomManagementState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            updateRoomInfo(state.room)
                            adapter.submitList(state.participants)
                            adapter.updateCurrentUserRole(
                                when (state.currentRole) {
                                    "CREATOR" -> ParticipantRole.CREATOR
                                    "MODERATOR" -> ParticipantRole.MODERATOR
                                    else -> ParticipantRole.MEMBER
                                }
                            )
                            binding.deleteRoomButton.visibility = if (state.currentRole == "CREATOR") View.VISIBLE else View.GONE
                            binding.leaveRoomButton.visibility = if (state.currentRole != "CREATOR" && !state.isBanned) View.VISIBLE else View.GONE
                            binding.editRoomButton.visibility = if (state.currentRole == "CREATOR") View.VISIBLE else View.GONE
                        }
                        is RoomManagementState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            if (ErrorHandler.isAuthError(state.errorCode)) {
                                viewModel.clearTokensAndLogout()
                                Snackbar.make(binding.root, "Сессия истекла. Пожалуйста, войдите заново.", Snackbar.LENGTH_LONG).show()
                                navigateToLogin()
                            } else {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.participants.collect { participants ->
                    filterParticipants(currentSearchQuery)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.participantsProfiles.collect { profiles ->
                    adapter.updateProfiles(profiles)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.room.collect { room ->
                    room?.let { updateRoomInfo(it) }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isEditMode.collect { isEdit ->
                    if (!isEdit) {
                        isUserEditingName = false
                        isUserEditingDescription = false
                    }

                    binding.viewMode.visibility = if (isEdit) View.GONE else View.VISIBLE
                    binding.editMode.visibility = if (isEdit) View.VISIBLE else View.GONE
                    binding.editRoomButton.setImageResource(if (isEdit) R.drawable.ic_close else R.drawable.ic_edit)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUserRole.collect { role ->
                    adapter.updateCurrentUserRole(role)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.editTags.collect { tags ->
                binding.editTagsChipGroup.removeAllViews()
                tags.forEach { tag ->
                    val chip = Chip(requireContext()).apply {
                        text = tag
                        isCloseIconVisible = true
                        setCloseIconResource(R.drawable.ic_close)
                        setOnCloseIconClickListener {
                            viewModel.removeTag(tag)
                        }
                    }
                    binding.editTagsChipGroup.addView(chip)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.editMaxParticipants.collect { value ->
                binding.editMaxParticipantsSlider.value = value.toFloat()
                binding.editMaxParticipantsValue.text = resources.getQuantityString(R.plurals.participants_count, value, value)
            }
        }

        lifecycleScope.launch {
            viewModel.editIsPrivate.collect { isPrivate ->
                binding.editPrivateSwitch.isChecked = isPrivate
            }
        }

        lifecycleScope.launch {
            viewModel.editRoomName.collect { name ->
                if (!isUserEditingName) {
                    binding.editRoomName.setText(name)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.editRoomDescription.collect { desc ->
                if (!isUserEditingDescription) {
                    binding.editRoomDescription.setText(desc)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.editPrimaryInterestId.collect { id ->
                val interest = viewModel.availableInterests.value.find { it.id == id }
                binding.editPrimaryInterest.setText(interest?.name ?: "", false)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect { event ->
                    when (event) {
                        is RoomEvent.Error -> {
                            if (event.errorCode in setOf("banned", "kicked", "room_deleted", "left")) {
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

    private fun filterParticipants(query: String) {
        val filtered = if (query.isEmpty()) {
            viewModel.participants.value
        } else {
            viewModel.participants.value.filter {
                it.profileId.lowercase().contains(query) ||
                        (viewModel.participantsProfiles.value[it.profileId]?.username?.lowercase()?.contains(query) == true)
            }
        }
        adapter.submitList(filtered)
    }

    private fun updateRoomInfo(room: Room) {
        binding.roomName.text = room.name
        binding.roomDescription.text = room.description ?: getString(R.string.no_description)
        binding.roomParticipants.text = String.format("Участников: %d/%d", room.participantsCount, room.maxParticipants)
        binding.roomMessagesCount.text = String.format("Сообщений: %d", room.messagesCount)

        if (room.primaryInterestId != null) {
            val interestName = viewModel.availableInterests.value.find { it.id == room.primaryInterestId }?.name
            binding.primaryInterestChip.text = interestName ?: room.primaryInterestId
            binding.primaryInterestChip.visibility = View.VISIBLE
        } else {
            binding.primaryInterestChip.visibility = View.GONE
        }

        binding.tagsChipGroup.removeAllViews()
        room.tags.forEach { tag ->
            val chip = Chip(requireContext()).apply {
                text = tag
                isClickable = false
            }
            binding.tagsChipGroup.addView(chip)
        }
    }

    private fun showParticipantMenu(participant: Participant) {
        val anchor = binding.participantsRecyclerView.findViewHolderForAdapterPosition(adapter.currentList.indexOf(participant))?.itemView ?: binding.root
        val wrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenu)
        val popupMenu = PopupMenu(wrapper, anchor)
        popupMenu.gravity = Gravity.END
        popupMenu.inflate(R.menu.participant_action_menu)
        val currentProfileId = getCurrentProfileId()
        val currentRole = viewModel.currentUserRole.value
        val isCreator = currentRole == ParticipantRole.CREATOR
        val isModerator = currentRole == ParticipantRole.MODERATOR
        val isSelf = participant.profileId == currentProfileId
        val isBanned = participant.isBanned
        val isMuted = participant.isMuted
        val targetRole = participant.role

        popupMenu.menu.findItem(R.id.action_kick).isVisible = false
        popupMenu.menu.findItem(R.id.action_ban).isVisible = false
        popupMenu.menu.findItem(R.id.action_mute).isVisible = false
        popupMenu.menu.findItem(R.id.action_unmute).isVisible = false
        popupMenu.menu.findItem(R.id.action_change_role).isVisible = false
        popupMenu.menu.findItem(R.id.action_unban).isVisible = false

        if (isBanned) {
            popupMenu.menu.findItem(R.id.action_unban).isVisible = (isCreator || (isModerator && targetRole == ParticipantRole.MEMBER)) && !isSelf
        } else {
            popupMenu.menu.findItem(R.id.action_kick).isVisible = (isCreator || (isModerator && targetRole == ParticipantRole.MEMBER)) && !isSelf
            popupMenu.menu.findItem(R.id.action_ban).isVisible = (isCreator || (isModerator && targetRole == ParticipantRole.MEMBER)) && !isSelf
            if (isMuted) {
                popupMenu.menu.findItem(R.id.action_mute).isVisible = false
                popupMenu.menu.findItem(R.id.action_unmute).isVisible = (isCreator || (isModerator && targetRole == ParticipantRole.MEMBER)) && !isSelf
            } else {
                popupMenu.menu.findItem(R.id.action_mute).isVisible = (isCreator || (isModerator && targetRole == ParticipantRole.MEMBER)) && !isSelf
                popupMenu.menu.findItem(R.id.action_unmute).isVisible = false
            }
            popupMenu.menu.findItem(R.id.action_change_role).isVisible = isCreator && !isSelf && targetRole != ParticipantRole.CREATOR
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_kick -> viewModel.kickParticipant(participant.profileId)
                R.id.action_ban -> viewModel.banParticipant(participant.profileId)
                R.id.action_mute -> viewModel.muteParticipant(participant.profileId)
                R.id.action_unmute -> viewModel.unmuteParticipant(participant.profileId)
                R.id.action_change_role -> showChangeRoleDialog(participant)
                R.id.action_unban -> viewModel.unbanParticipant(participant.profileId)
            }
            true
        }
        popupMenu.show()
    }

    private fun showChangeRoleDialog(participant: Participant) {
        val roles = arrayOf("Участник", "Модератор")
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
            .setTitle("Выберите новую роль")
            .setItems(roles) { _, which ->
                val newRole = if (which == 0) "MEMBER" else "MODERATOR"
                viewModel.changeRole(participant.profileId, newRole)
            }
            .show()
    }

    private fun confirmLeaveRoom() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
            .setTitle("Выйти из комнаты")
            .setMessage("Вы уверены, что хотите покинуть комнату?")
            .setPositiveButton("Выйти") { _, _ -> viewModel.leaveRoom() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDeleteRoom() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog)
            .setTitle("Удалить комнату")
            .setMessage("Вы уверены, что хотите удалить комнату? Это действие необратимо.")
            .setPositiveButton("Удалить") { _, _ -> viewModel.deleteRoom() }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun navigateToLogin() {
        findNavController().navigate(RoomManagementFragmentDirections.actionRoomManagementFragmentToLoginFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}