package com.example.common_ground_android.ui.viewmodels.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.Profile
import com.example.common_ground_android.network.model.domain.Message
import com.example.common_ground_android.network.model.domain.Participant
import com.example.common_ground_android.network.model.domain.Room
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketServerEvent
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.network.repository.RoomRepository
import com.example.common_ground_android.network.repository.websocket.RoomWebSocketRepository
import com.example.common_ground_android.network.repository.websocket.WebSocketConnectionState
import com.example.common_ground_android.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GroupRoomViewModel(
    private val roomRepository: RoomRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val webSocketRepo: RoomWebSocketRepository,
    private val roomId: String,
    private val currentProfileId: String
) : ViewModel() {

    private val _state = MutableStateFlow<GroupRoomState>(GroupRoomState.Loading)
    val state: StateFlow<GroupRoomState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<RoomEvent>()
    val event: SharedFlow<RoomEvent> = _event.asSharedFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _onlineCount = MutableStateFlow(0)
    val onlineCount: StateFlow<Int> = _onlineCount.asStateFlow()

    private val _socketConnected = MutableStateFlow(false)
    val socketConnected: StateFlow<Boolean> = _socketConnected.asStateFlow()

    private val _senderProfiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    val senderProfiles: StateFlow<Map<String, Profile>> = _senderProfiles.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _senderRoles = MutableStateFlow<Map<String, String>>(emptyMap())
    val senderRoles: StateFlow<Map<String, String>> = _senderRoles.asStateFlow()

    private val _isActive = MutableStateFlow(false)

    private val _typingProfiles = MutableStateFlow<Set<String>>(emptySet())
    val typingIndicatorText: StateFlow<String?> = combine(
        _typingProfiles,
        _senderProfiles
    ) { typingSet, profiles ->
        when (typingSet.size) {
            0 -> null
            1 -> {
                val id = typingSet.first()
                val name = profiles[id]?.username ?: "Кто-то"
                "$name печатает..."
            }
            else -> "Несколько человек печатают..."
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _replyTargetMessageId = MutableStateFlow<String?>(null)
    val replyTargetMessageId: StateFlow<String?> = _replyTargetMessageId.asStateFlow()
    val replyTargetMessage: StateFlow<Message?> = _replyTargetMessageId.map { id ->
        id?.let { _messages.value.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val typingTimeouts = mutableMapOf<String, Job>()

    init {
        loadRoomData()
        connectWebSocket()
    }

    private fun loadRoomData() {
        viewModelScope.launch {
            _state.value = GroupRoomState.Loading
            val roomResult = roomRepository.getRoomById(roomId)
            val messagesResult = roomRepository.getRoomMessages(roomId, limit = null)
            val participantsResult = roomRepository.getRoomParticipants(roomId, includeBanned = false)

            when {
                roomResult is NetworkResult.Error -> {
                    _state.value = GroupRoomState.Error(roomResult.errorMessage, roomResult.errorCode)
                }
                messagesResult is NetworkResult.Error -> {
                    _state.value = GroupRoomState.Error(messagesResult.errorMessage, messagesResult.errorCode)
                }
                participantsResult is NetworkResult.Error -> {
                    _state.value = GroupRoomState.Error(participantsResult.errorMessage, participantsResult.errorCode)
                }
                roomResult is NetworkResult.Success && messagesResult is NetworkResult.Success && participantsResult is NetworkResult.Success -> {
                    val room = Room.fromResponse(roomResult.data)
                    val messages = messagesResult.data.messages.map { Message.fromResponse(it) }
                    loadProfilesForMessages(messages)

                    val participants = participantsResult.data.map { Participant.fromResponse(it) }
                    val roles = participantsResult.data.associate { it.profileId to it.role }
                    val currentParticipant = participants.find { it.profileId == currentProfileId }
                    val isBanned = currentParticipant?.isBanned ?: false
                    val isMuted = currentParticipant?.isMuted ?: false
                    val currentRole = currentParticipant?.role?.name ?: "MEMBER"
                    val onlineCount = participants.count { it.isOnline }

                    _isMuted.value = isMuted
                    _onlineCount.value = onlineCount
                    _senderRoles.value = roles

                    if (isBanned) {
                        _state.value = GroupRoomState.Error("Вы были забанены в этой комнате", "banned")
                        return@launch
                    }

                    _messages.value = messages
                    _state.value = GroupRoomState.Success(
                        room = room,
                        messages = messages,
                        onlineCount = onlineCount,
                        isMuted = isMuted,
                        currentRole = currentRole
                    )
                }
                else -> {}
            }
        }
    }

    private fun addSystemMessage(content: String) {
        val systemMsg = Message.systemMessage(content)
        _messages.update { it + systemMsg }
    }

    private suspend fun loadProfilesForMessages(messages: List<Message>) {
        val uniqueIds = messages.map { it.senderId }.distinct()
        if (uniqueIds.isEmpty()) return

        when (val result = profileRepository.getProfilesBatch(uniqueIds)) {
            is NetworkResult.Success -> {
                val profiles = result.data.map { Profile.fromResponse(it) }
                _senderProfiles.value = profiles.associateBy { it.id }
            }
            is NetworkResult.Error -> {
                _state.value = GroupRoomState.Error(result.errorMessage, result.errorCode)
            }
            else -> {}
        }
    }

    private suspend fun loadSingleProfile(profileId: String) {
        if (_senderProfiles.value.containsKey(profileId)) return
        when (val result = profileRepository.getProfileById(profileId)) {
            is NetworkResult.Success -> {
                val profile = Profile.fromResponse(result.data)
                _senderProfiles.update { it + (profileId to profile) }
            }
            is NetworkResult.Error -> {
                _state.value = GroupRoomState.Error(result.errorMessage, result.errorCode)
            }
            else -> {}
        }
    }

    private fun connectWebSocket() {
        if (webSocketRepo.isConnected()) {
            _socketConnected.value = true
            return
        }
        viewModelScope.launch {
            try {
                val url = KtorClientFactory.getInstance().createRoomWebSocketUrl(roomId)
                webSocketRepo.connect(url)
                _socketConnected.value = true
                webSocketRepo.incomingEvents.onEach { event ->
                    handleWebSocketEvent(event)
                }.launchIn(viewModelScope)
                webSocketRepo.connectionState.collect { state ->
                    _socketConnected.value = state is WebSocketConnectionState.Connected
                }
            } catch (e: Exception) {
                _state.value = GroupRoomState.Error("Failed to connect: ${e.message}")
            }
        }
    }

    private fun handleWebSocketEvent(event: RoomWebSocketServerEvent) {
        when (event) {
            is RoomWebSocketServerEvent.ConnectionEstablished -> {
                _onlineCount.value = event.data.onlineCount
            }
            is RoomWebSocketServerEvent.MessageSent -> {
                val webMsg = event.data.message
                val newMessage = Message(
                    id = webMsg.id,
                    roomId = webMsg.roomId,
                    senderId = webMsg.senderId,
                    content = webMsg.content,
                    parentMessageId = webMsg.parentMessageId,
                    createdAt = DateUtils.parseIsoDate(webMsg.createdAt),
                    updatedAt = DateUtils.parseIsoDate(webMsg.updatedAt),
                    isEdited = webMsg.isEdited,
                    isDeleted = webMsg.isDeleted
                )
                _messages.update { it + newMessage }
                viewModelScope.launch { loadSingleProfile(webMsg.senderId) }
            }
            is RoomWebSocketServerEvent.ParticipantJoined -> {
                val profileId = event.data.profileId
                viewModelScope.launch {
                    loadSingleProfile(profileId)
                    val name = _senderProfiles.value[profileId]?.username ?: profileId.take(8)
                    addSystemMessage("$name присоединился к комнате")
                    val participantsResult = roomRepository.getRoomParticipants(roomId, includeBanned = false)
                    if (participantsResult is NetworkResult.Success) {
                        val roles = participantsResult.data.associate { it.profileId to it.role }
                        _senderRoles.value = roles
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantLeft -> {
                val profileId = event.data.profileId
                viewModelScope.launch {
                    loadSingleProfile(profileId)
                    val name = _senderProfiles.value[profileId]?.username ?: profileId.take(8)
                    addSystemMessage("$name покинул комнату")
                }
            }
            is RoomWebSocketServerEvent.MessageUpdated -> {
                val updatedMsg = event.data.message
                _messages.update { messages ->
                    messages.map { msg ->
                        if (msg.id == updatedMsg.id) {
                            msg.copy(
                                content = updatedMsg.content,
                                updatedAt = DateUtils.parseIsoDate(updatedMsg.updatedAt),
                                isEdited = updatedMsg.isEdited
                            )
                        } else msg
                    }
                }
            }
            is RoomWebSocketServerEvent.MessageDeleted -> {
                val deletedId = event.data.messageId
                _messages.update { messages ->
                    messages.filterNot { it.id == deletedId }.map { msg ->
                        if (msg.parentMessageId == deletedId) {
                            msg.copy(parentMessageId = null)
                        } else msg
                    }
                }
            }
            is RoomWebSocketServerEvent.ProfileOnline -> {
                _onlineCount.value = event.data.onlineCount
            }
            is RoomWebSocketServerEvent.ProfileOffline -> {
                _onlineCount.value = event.data.onlineCount
            }
            is RoomWebSocketServerEvent.TypingStarted -> {
                val profileId = event.data.profileId
                viewModelScope.launch {
                    loadSingleProfile(profileId)
                    _typingProfiles.update { it + profileId }
                    typingTimeouts[profileId]?.cancel()
                    typingTimeouts[profileId] = viewModelScope.launch {
                        delay(3000)
                        _typingProfiles.update { it - profileId }
                        typingTimeouts.remove(profileId)
                    }
                }
            }
            is RoomWebSocketServerEvent.TypingStopped -> {
                val profileId = event.data.profileId
                typingTimeouts[profileId]?.cancel()
                typingTimeouts.remove(profileId)
                _typingProfiles.update { it - profileId }
            }
            is RoomWebSocketServerEvent.ParticipantKicked -> {
                val kickedId = event.data.profileId
                val kickerId = event.data.kickerProfileId
                viewModelScope.launch {
                    loadSingleProfile(kickedId)
                    loadSingleProfile(kickerId)
                    val kickedName = _senderProfiles.value[kickedId]?.username ?: kickedId.take(8)
                    val kickerName = _senderProfiles.value[kickerId]?.username ?: kickerId.take(8)
                    addSystemMessage("$kickedName был исключён из комнаты пользователем $kickerName")
                    if (kickedId == currentProfileId) {
                        _event.emit(RoomEvent.Error("Вы были исключены из комнаты пользователем $kickerName", "kicked"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantBanned -> {
                val bannedId = event.data.bannedProfileId
                val bannerId = event.data.bannerProfileId
                viewModelScope.launch {
                    loadSingleProfile(bannedId)
                    loadSingleProfile(bannerId)
                    val bannedName = _senderProfiles.value[bannedId]?.username ?: bannedId.take(8)
                    val bannerName = _senderProfiles.value[bannerId]?.username ?: bannerId.take(8)
                    addSystemMessage("$bannedName был забанен в комнате пользователем $bannerName")
                    if (bannedId == currentProfileId) {
                        _event.emit(RoomEvent.Error("Вы были забанены в этой комнате пользователем $bannerName", "banned"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantUnbanned -> {
                val unbannedId = event.data.unbannedProfileId
                val unbannerId = event.data.unbannerProfileId
                viewModelScope.launch {
                    loadSingleProfile(unbannedId)
                    loadSingleProfile(unbannerId)
                    val unbannedName = _senderProfiles.value[unbannedId]?.username ?: unbannedId.take(8)
                    val unbannerName = _senderProfiles.value[unbannerId]?.username ?: unbannerId.take(8)
                    addSystemMessage("$unbannedName был разбанен в комнате пользователем $unbannerName")
                }
            }
            is RoomWebSocketServerEvent.ParticipantRoleChanged -> {
                val targetId = event.data.targetProfileId
                val newRole = event.data.newRole
                val changerId = event.data.changerProfileId
                viewModelScope.launch {
                    loadSingleProfile(targetId)
                    loadSingleProfile(changerId)
                    val targetName = _senderProfiles.value[targetId]?.username ?: targetId.take(8)
                    val changerName = _senderProfiles.value[changerId]?.username ?: changerId.take(8)
                    var roleText = if (newRole == "MODERATOR") "модератором" else "участником"
                    addSystemMessage("$targetName стал $roleText по решению $changerName")
                    if (targetId == currentProfileId) {
                        _state.update { currentState ->
                            if (currentState is GroupRoomState.Success) {
                                currentState.copy(currentRole = newRole)
                            } else currentState
                        }
                        roleText = if (newRole == "MODERATOR") "модератора" else "участника"
                        _event.emit(RoomEvent.Success("Ваша роль изменена на $roleText пользователем $changerName", "role_changed"))
                    }
                    _senderRoles.update { it + (targetId to newRole) }
                }
            }
            is RoomWebSocketServerEvent.ParticipantMuted -> {
                val mutedId = event.data.mutedProfileId
                val muterId = event.data.muterProfileId
                viewModelScope.launch {
                    loadSingleProfile(mutedId)
                    loadSingleProfile(muterId)
                    val mutedName = _senderProfiles.value[mutedId]?.username ?: mutedId.take(8)
                    val muterName = _senderProfiles.value[muterId]?.username ?: muterId.take(8)
                    addSystemMessage("$mutedName был замучен пользователем $muterName")
                    if (mutedId == currentProfileId) {
                        _isMuted.value = true
                        _event.emit(RoomEvent.Success("Вы были замучены пользователем $muterName", "muted"))
                    }
                }
            }
            is RoomWebSocketServerEvent.ParticipantUnmuted -> {
                val unmutedId = event.data.unmutedProfileId
                val unmuterId = event.data.unmuterProfileId
                viewModelScope.launch {
                    loadSingleProfile(unmutedId)
                    loadSingleProfile(unmuterId)
                    val unmutedName = _senderProfiles.value[unmutedId]?.username ?: unmutedId.take(8)
                    val unmuterName = _senderProfiles.value[unmuterId]?.username ?: unmuterId.take(8)
                    addSystemMessage("$unmutedName был размучен пользователем $unmuterName")
                    if (unmutedId == currentProfileId) {
                        _isMuted.value = false
                        _event.emit(RoomEvent.Success("С вас снял мут пользователь $unmuterName", "unmuted"))
                    }
                }
            }
            is RoomWebSocketServerEvent.RoomUpdated -> {
                addSystemMessage("Информация о комнате обновлена")
                refreshRoomData()
            }
            is RoomWebSocketServerEvent.RoomDeleted -> {
                viewModelScope.launch {
                    _event.emit(RoomEvent.Error("Комната удалена", "room_deleted"))
                }
            }
            else -> {}
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            when (val result = roomRepository.updateMessage(messageId, newContent)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = GroupRoomState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            when (val result = roomRepository.deleteMessage(messageId)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _state.value = GroupRoomState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    private fun refreshRoomData() {
        viewModelScope.launch {
            roomId.let {
                when (val result = roomRepository.getRoomById(it)) {
                    is NetworkResult.Success -> {
                        val updatedRoom = Room.fromResponse(result.data)
                        _state.update { currentState ->
                            if (currentState is GroupRoomState.Success) {
                                currentState.copy(room = updatedRoom)
                            } else {
                                currentState
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        _state.value = GroupRoomState.Error(result.errorMessage, result.errorCode)
                    }

                    else -> {}
                }
            }
        }
    }

    fun setReplyTarget(messageId: String?) {
        _replyTargetMessageId.value = messageId
    }

    fun clearReplyTarget() {
        _replyTargetMessageId.value = null
    }

    fun sendMessageWithReply(content: String) {
        if (_isMuted.value) {
            viewModelScope.launch {
                _event.emit(RoomEvent.Error("Вы замучены и не можете отправлять сообщения", "muted"))
            }
            return
        }
        val parentId = _replyTargetMessageId.value
        sendMessage(content, parentId)
    }

    private fun sendMessage(content: String, parentId: String? = null) {
        if (!_socketConnected.value) return
        val event = RoomWebSocketClientEvent.SendMessage(content = content, parentMessageId = parentId)
        webSocketRepo.sendEvent(event)
        clearReplyTarget()
    }

    fun sendTypingStarted() {
        if (!_socketConnected.value) return
        webSocketRepo.sendEvent(RoomWebSocketClientEvent.TypingStarted)
    }

    fun sendTypingStopped() {
        if (!_socketConnected.value) return
        webSocketRepo.sendEvent(RoomWebSocketClientEvent.TypingStopped)
    }

    fun setActive(active: Boolean) {
        _isActive.value = active
    }

    fun onUserTyping() {
        if (!_isActive.value) return
        sendTypingStarted()
        viewModelScope.launch {
            delay(1000)
            sendTypingStopped()
        }
    }

    fun forceDisconnect() {
        viewModelScope.launch {
            webSocketRepo.disconnect()
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}