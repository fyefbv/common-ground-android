package com.example.common_ground_android.ui.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Participant
import com.example.common_ground_android.network.model.domain.ParticipantRole
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketServerEvent
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.network.repository.RoomRepository
import com.example.common_ground_android.network.repository.websocket.RoomWebSocketRepository
import com.example.common_ground_android.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RoomManagementViewModel(
    private val roomRepository: RoomRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val interestRepository: InterestRepository,
    private val webSocketRepo: RoomWebSocketRepository,
    private val roomId: String,
    private val currentProfileId: String
) : ViewModel() {

    private val _state = MutableStateFlow<RoomManagementState>(RoomManagementState.Idle)
    val state: StateFlow<RoomManagementState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<RoomEvent>()
    val event: SharedFlow<RoomEvent> = _event.asSharedFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _room = MutableStateFlow<Room?>(null)
    val room: StateFlow<Room?> = _room.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _participantsProfiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    val participantsProfiles: StateFlow<Map<String, Profile>> = _participantsProfiles.asStateFlow()

    private val _currentUserRole = MutableStateFlow(ParticipantRole.MEMBER)
    val currentUserRole: StateFlow<ParticipantRole> = _currentUserRole.asStateFlow()

    private val _editRoomName = MutableStateFlow("")
    val editRoomName: StateFlow<String> = _editRoomName.asStateFlow()
    private val _editRoomDescription = MutableStateFlow("")
    val editRoomDescription: StateFlow<String> = _editRoomDescription.asStateFlow()
    private val _editPrimaryInterestId = MutableStateFlow<String?>(null)
    val editPrimaryInterestId: StateFlow<String?> = _editPrimaryInterestId.asStateFlow()
    private val _editTags = MutableStateFlow<List<String>>(emptyList())
    val editTags: StateFlow<List<String>> = _editTags.asStateFlow()
    private val _editMaxParticipants = MutableStateFlow(50)
    val editMaxParticipants: StateFlow<Int> = _editMaxParticipants.asStateFlow()
    private val _editIsPrivate = MutableStateFlow(false)
    val editIsPrivate: StateFlow<Boolean> = _editIsPrivate.asStateFlow()

    private val _availableInterests = MutableStateFlow<List<Interest>>(emptyList())
    val availableInterests: StateFlow<List<Interest>> = _availableInterests.asStateFlow()

    init {
        loadData()
        subscribeToWebSocketEvents()
        loadAvailableInterests()
    }

    private fun loadAvailableInterests() {
        viewModelScope.launch {
            when (val result = interestRepository.getAllInterests()) {
                is NetworkResult.Success -> {
                    _availableInterests.value = result.data.map { Interest.fromResponse(it) }
                }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = RoomManagementState.Loading
            val roomResult = roomRepository.getRoomById(roomId)
            val participantsResult = roomRepository.getRoomParticipants(roomId, includeBanned = true)

            when {
                roomResult is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(roomResult.errorMessage, roomResult.errorCode)
                    return@launch
                }
                participantsResult is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(participantsResult.errorMessage, participantsResult.errorCode)
                    return@launch
                }
                roomResult is NetworkResult.Success && participantsResult is NetworkResult.Success -> {
                    val room = Room.fromResponse(roomResult.data)
                    val participants = participantsResult.data.map { Participant.fromResponse(it) }

                    val uniqueIds = participants.map { it.profileId }.distinct()
                    if (uniqueIds.isNotEmpty()) {
                        val profilesResult = profileRepository.getProfilesBatch(uniqueIds)
                        if (profilesResult is NetworkResult.Success) {
                            val profilesMap = profilesResult.data.associate { it.id to Profile.fromResponse(it) }
                            _participantsProfiles.value = profilesMap
                        }
                    }

                    _room.value = room
                    _participants.value = participants

                    val currentParticipant = participants.find { it.profileId == currentProfileId }
                    val isBanned = currentParticipant?.isBanned ?: false
                    val isMuted = currentParticipant?.isMuted ?: false
                    val currentRole = currentParticipant?.role?.name ?: "MEMBER"
                    _currentUserRole.value = when (currentRole) {
                        "CREATOR" -> ParticipantRole.CREATOR
                        "MODERATOR" -> ParticipantRole.MODERATOR
                        else -> ParticipantRole.MEMBER
                    }

                    _state.value = RoomManagementState.Success(room, participants, currentRole, isBanned, isMuted)

                    _editRoomName.value = room.name
                    _editRoomDescription.value = room.description ?: ""
                    _editPrimaryInterestId.value = room.primaryInterestId
                    _editTags.value = room.tags
                    _editMaxParticipants.value = room.maxParticipants
                    _editIsPrivate.value = room.isPrivate
                }
                else -> {}
            }
        }
    }

    private suspend fun loadSingleProfile(profileId: String) {
        if (_participantsProfiles.value.containsKey(profileId)) return
        when (val result = profileRepository.getProfileById(profileId)) {
            is NetworkResult.Success -> {
                val profile = Profile.fromResponse(result.data)
                _participantsProfiles.update { it + (profileId to profile) }
            }
            is NetworkResult.Error -> {
                _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
            }
            else -> {}
        }
    }

    private fun subscribeToWebSocketEvents() {
        viewModelScope.launch {
            webSocketRepo.incomingEvents.collect { event ->
                handleWebSocketEvent(event)
            }
        }
    }

    private fun handleWebSocketEvent(event: RoomWebSocketServerEvent) {
        when (event) {
            is RoomWebSocketServerEvent.ProfileOnline -> {
                updateParticipantStatus(event.data.profileId, true)
            }
            is RoomWebSocketServerEvent.ProfileOffline -> {
                updateParticipantStatus(event.data.profileId, false)
            }
            is RoomWebSocketServerEvent.ParticipantJoined -> {
                val newParticipant = Participant(
                    profileId = event.data.profileId,
                    roomId = roomId,
                    role = ParticipantRole.MEMBER,
                    joinedAt = DateUtils.parseIsoDate(event.data.joinedAt),
                    isOnline = false,
                    isMuted = false,
                    isBanned = false
                )
                addParticipant(newParticipant)
                viewModelScope.launch { loadSingleProfile(event.data.profileId) }
            }
            is RoomWebSocketServerEvent.ParticipantLeft -> {
                removeParticipant(event.data.profileId)
            }
            is RoomWebSocketServerEvent.ParticipantRoleChanged -> {
                updateParticipantRole(event.data.targetProfileId, event.data.newRole)
                if (event.data.targetProfileId == currentProfileId) {
                    viewModelScope.launch {
                        loadSingleProfile(event.data.changerProfileId)
                        val changerName = _participantsProfiles.value[event.data.changerProfileId]?.username ?: event.data.changerProfileId.take(8)
                        val roleName = if (event.data.newRole == "MODERATOR") "модератора" else "участника"
                        _event.emit(RoomEvent.Success("Ваша роль изменена на $roleName пользователем $changerName", "role_changed"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantMuted -> {
                updateParticipantMuted(event.data.mutedProfileId, true)
                if (event.data.mutedProfileId == currentProfileId) {
                    viewModelScope.launch {
                        loadSingleProfile(event.data.muterProfileId)
                        val muterName = _participantsProfiles.value[event.data.muterProfileId]?.username ?: event.data.muterProfileId.take(8)
                        _event.emit(RoomEvent.Success("Вы были замучены пользователем $muterName", "muted"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantUnmuted -> {
                updateParticipantMuted(event.data.unmutedProfileId, false)
                if (event.data.unmutedProfileId == currentProfileId) {
                    viewModelScope.launch {
                        loadSingleProfile(event.data.unmuterProfileId)
                        val unmuterName = _participantsProfiles.value[event.data.unmuterProfileId]?.username ?: event.data.unmuterProfileId.take(8)
                        _event.emit(RoomEvent.Success("С вас снял мут пользователь $unmuterName", "unmuted"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantUnbanned -> {
                updateParticipantBanned(event.data.unbannedProfileId, false)
            }
            is RoomWebSocketServerEvent.RoomUpdated -> {
                viewModelScope.launch {
                    val roomResult = roomRepository.getRoomById(roomId)
                    if (roomResult is NetworkResult.Success) {
                        val updatedRoom = Room.fromResponse(roomResult.data)
                        _room.value = updatedRoom
                        if (!_isEditMode.value) {
                            _editRoomName.value = updatedRoom.name
                            _editRoomDescription.value = updatedRoom.description ?: ""
                            _editPrimaryInterestId.value = updatedRoom.primaryInterestId
                            _editTags.value = updatedRoom.tags
                            _editMaxParticipants.value = updatedRoom.maxParticipants
                            _editIsPrivate.value = updatedRoom.isPrivate
                        }
                        _event.emit(RoomEvent.Success("Информация о комнате обновлена", "room_updated"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantKicked -> {
                if (event.data.profileId == currentProfileId) {
                    viewModelScope.launch {
                        loadSingleProfile(event.data.kickerProfileId)
                        val kickerName = _participantsProfiles.value[event.data.kickerProfileId]?.username ?: event.data.kickerProfileId.take(8)
                        _event.emit(RoomEvent.Error("Вы были исключены из комнаты пользователем $kickerName", "kicked"))
                    }
                } else {
                    removeParticipant(event.data.profileId)
                }
            }
            is RoomWebSocketServerEvent.ParticipantBanned -> {
                if (event.data.bannedProfileId == currentProfileId) {
                    viewModelScope.launch {
                        loadSingleProfile(event.data.bannerProfileId)
                        val bannerName = _participantsProfiles.value[event.data.bannerProfileId]?.username ?: event.data.bannerProfileId.take(8)
                        _event.emit(RoomEvent.Error("Вы были забанены в этой комнате пользователем $bannerName", "banned"))
                    }
                } else {
                    updateParticipantBanned(event.data.bannedProfileId, true)
                }
            }
            is RoomWebSocketServerEvent.RoomDeleted -> {
                viewModelScope.launch {
                    _event.emit(RoomEvent.Error("Комната была удалена", "room_deleted"))
                }
            }
            else -> {}
        }
    }

    private fun updateParticipantStatus(profileId: String, isOnline: Boolean) {
        _participants.update { participants ->
            participants.map {
                if (it.profileId == profileId) it.copy(isOnline = isOnline)
                else it
            }
        }
    }

    private fun addParticipant(participant: Participant) {
        _participants.update { it + participant }
    }

    private fun removeParticipant(profileId: String) {
        _participants.update { participants -> participants.filterNot { it.profileId == profileId } }
    }

    private fun updateParticipantRole(profileId: String, newRole: String) {
        val role = when (newRole) {
            "CREATOR" -> ParticipantRole.CREATOR
            "MODERATOR" -> ParticipantRole.MODERATOR
            else -> ParticipantRole.MEMBER
        }
        _participants.update { participants ->
            participants.map {
                if (it.profileId == profileId) it.copy(role = role)
                else it
            }
        }
        if (profileId == currentProfileId) {
            _currentUserRole.value = role
        }
    }

    private fun updateParticipantMuted(profileId: String, isMuted: Boolean) {
        _participants.update { participants ->
            participants.map {
                if (it.profileId == profileId) it.copy(isMuted = isMuted)
                else it
            }
        }
    }

    private fun updateParticipantBanned(profileId: String, isBanned: Boolean) {
        _participants.update { participants ->
            participants.map {
                if (it.profileId == profileId) it.copy(isBanned = isBanned)
                else it
            }
        }
    }

    fun toggleEditMode() {
        if (_isEditMode.value) {
            _room.value?.let { room ->
                _editRoomName.value = room.name
                _editRoomDescription.value = room.description ?: ""
                _editPrimaryInterestId.value = room.primaryInterestId
                _editTags.value = room.tags
                _editMaxParticipants.value = room.maxParticipants
                _editIsPrivate.value = room.isPrivate
            }
        }
        _isEditMode.value = !_isEditMode.value
    }

    fun updateEditRoomName(name: String) {
        _editRoomName.value = name
    }

    fun updateEditRoomDescription(description: String) {
        _editRoomDescription.value = description
    }

    fun updateEditPrimaryInterest(interestId: String?) {
        _editPrimaryInterestId.value = interestId
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && _editTags.value.size < 10 && tag.length <= 50 && !_editTags.value.contains(tag)) {
            _editTags.update { it + tag }
        }
    }

    fun removeTag(tag: String) {
        _editTags.update { it - tag }
    }

    fun updateEditMaxParticipants(value: Int) {
        _editMaxParticipants.value = value
    }

    fun updateEditIsPrivate(isPrivate: Boolean) {
        _editIsPrivate.value = isPrivate
    }

    fun saveRoomChanges() {
        val name = _editRoomName.value.trim()
        if (name.length !in 3..100) {
            _state.value = RoomManagementState.Error("Название должно быть от 3 до 100 символов")
            return
        }
        viewModelScope.launch {
            when (val result = roomRepository.updateRoom(
                roomId = roomId,
                name = name,
                description = _editRoomDescription.value.trim().takeIf { it.isNotEmpty() },
                primaryInterestId = _editPrimaryInterestId.value,
                tags = _editTags.value,
                maxParticipants = _editMaxParticipants.value,
                isPrivate = _editIsPrivate.value
            )) {
                is NetworkResult.Success -> {
                    _isEditMode.value = false
                }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            when (val result = roomRepository.leaveRoom(roomId)) {
                is NetworkResult.Success -> {
                    webSocketRepo.disconnect()
                    _event.emit(RoomEvent.Error("Вы вышли из комнаты", "left"))
                }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun deleteRoom() {
        viewModelScope.launch {
            when (val result = roomRepository.deleteRoom(roomId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun kickParticipant(participantId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.kickParticipant(roomId, participantId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun banParticipant(participantId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.banParticipant(roomId, participantId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun unbanParticipant(participantId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.unbanParticipant(roomId, participantId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun muteParticipant(participantId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.muteParticipant(roomId, participantId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun unmuteParticipant(participantId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.unmuteParticipant(roomId, participantId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun changeRole(participantId: String, newRole: String) {
        viewModelScope.launch {
            when (val result = roomRepository.changeParticipantRole(roomId, participantId, newRole)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = RoomManagementState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun refreshRoomStatus() {
        viewModelScope.launch {
            val roomResult = roomRepository.getRoomById(roomId)
            if (roomResult is NetworkResult.Error && roomResult.errorCode == "room_not_found") {
                _event.emit(RoomEvent.Error("Комната была удалена", "room_deleted"))
                return@launch
            }

            val participantsResult = roomRepository.getRoomParticipants(roomId, includeBanned = true)
            if (participantsResult is NetworkResult.Error) {
                when (participantsResult.errorCode) {
                    "participant_banned" -> {
                        _event.emit(RoomEvent.Error("Вы были забанены в этой комнате", "banned"))
                    }
                    "not_room_member" -> {
                        _event.emit(RoomEvent.Error("Вы были исключены из комнаты", "kicked"))
                    }
                }
            }
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}