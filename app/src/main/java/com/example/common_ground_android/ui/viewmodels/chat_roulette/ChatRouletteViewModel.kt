package com.example.common_ground_android.ui.viewmodels.chat_roulette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.domain.ChatRouletteMessage
import com.example.common_ground_android.network.model.domain.ChatRouletteSession
import com.example.common_ground_android.network.model.domain.ChatRouletteStatus
import com.example.common_ground_android.network.model.domain.Interest
import com.example.common_ground_android.network.model.response.NetworkResult
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketServerEvent
import com.example.common_ground_android.network.repository.AuthRepository
import com.example.common_ground_android.network.repository.ChatRouletteRepository
import com.example.common_ground_android.network.repository.InterestRepository
import com.example.common_ground_android.network.repository.ProfileRepository
import com.example.common_ground_android.network.repository.websocket.ChatRouletteWebSocketRepository
import com.example.common_ground_android.network.repository.websocket.WebSocketConnectionState
import com.example.common_ground_android.utils.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatRouletteViewModel(
    private val chatRouletteRepository: ChatRouletteRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val interestRepository: InterestRepository,
    private val webSocketRepo: ChatRouletteWebSocketRepository,
    private val currentProfileId: String
) : ViewModel() {

    private val _state = MutableStateFlow<ChatRouletteState>(ChatRouletteState.Idle)
    val state: StateFlow<ChatRouletteState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<ChatRouletteEvent>()
    val event: SharedFlow<ChatRouletteEvent> = _event.asSharedFlow()

    private val _messages = MutableStateFlow<List<ChatRouletteMessage>>(emptyList())
    val messages: StateFlow<List<ChatRouletteMessage>> = _messages.asStateFlow()

    private val _socketConnected = MutableStateFlow(false)
    val socketConnected: StateFlow<Boolean> = _socketConnected.asStateFlow()

    private val _partnerOnline = MutableStateFlow(false)
    val partnerOnline: StateFlow<Boolean> = _partnerOnline.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _sessionEnded = MutableStateFlow(false)

    private var timerJob: Job? = null
    private var sessionId: String? = null

    init {
        checkActiveSession()
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.getActiveSession()) {
                is NetworkResult.Success -> {
                    val session = ChatRouletteSession.fromResponse(result.data)
                    if (session.status == ChatRouletteStatus.ACTIVE) {
                        enterActiveSession(session)
                    }
                }
                else -> {}
            }
        }
    }

    fun startSearch(priorityInterestIds: List<String>) {
        viewModelScope.launch {
            _state.value = ChatRouletteState.Searching
            when (val result = chatRouletteRepository.startSearch(priorityInterestIds)) {
                is NetworkResult.Success -> {
                    if (result.data.immediateMatch && result.data.session != null) {
                        val session = ChatRouletteSession.fromResponse(result.data.session)
                        enterActiveSession(session)
                    } else {
                        _state.value = ChatRouletteState.Error("Ошибка поиска", "unknown")
                    }
                }
                is NetworkResult.Error -> {
                    _state.value = ChatRouletteState.Error(result.errorMessage, result.errorCode)
                }
                else -> {}
            }
        }
    }

    fun cancelSearch() {
        viewModelScope.launch {
            chatRouletteRepository.cancelSearch()
            _state.value = ChatRouletteState.Idle
        }
    }

    private fun enterActiveSession(session: ChatRouletteSession) {
        sessionId = session.id
        val partner = session.matchedProfile
        val commonInterestIds = session.commonInterests
        val matchedInterestId = session.matchedInterestId
        val expiresAt = session.expiresAt?.time ?: 0L
        _partnerOnline.value = session.partnerOnline

        viewModelScope.launch {
            var messagesHistory: List<ChatRouletteMessage> = emptyList()
            when (val result = chatRouletteRepository.getSessionMessages()) {
                is NetworkResult.Success -> {
                    messagesHistory = result.data.map { ChatRouletteMessage.fromResponse(it) }
                    _messages.value = messagesHistory
                }
                else -> {}
            }

            var commonInterestsObjects: List<Interest> = emptyList()
            if (commonInterestIds != null) {
                when (val result = interestRepository.getInterestsBatch(commonInterestIds)) {
                    is NetworkResult.Success -> {
                        commonInterestsObjects = result.data.map { Interest.fromResponse(it) }
                    }
                    else -> {}
                }
            }

            var matchedInterestObject: Interest? = null
            if (matchedInterestId != null) {
                when (val result = interestRepository.getInterestsBatch(listOf(matchedInterestId))) {
                    is NetworkResult.Success -> {
                        matchedInterestObject = result.data.firstOrNull()?.let { Interest.fromResponse(it) }
                    }
                    else -> {}
                }
            }

            _state.value = ChatRouletteState.ActiveSession(
                session = session,
                partner = partner,
                commonInterests = commonInterestsObjects,
                matchedInterest = matchedInterestObject,
                messages = messagesHistory,
                extensionState = ExtensionState.NONE,
                expiresAt = expiresAt
            )
            connectWebSocket(session.id)
            _sessionEnded.value = false
            startTimer()
        }
    }

    private fun connectWebSocket(sessionId: String) {
        if (webSocketRepo.isConnected()) {
            _socketConnected.value = true
            return
        }
        viewModelScope.launch {
            try {
                val url = KtorClientFactory.getInstance().createChatRouletteWebSocketUrl(sessionId)
                webSocketRepo.connect(url)
                _socketConnected.value = true
                webSocketRepo.incomingEvents.onEach { event ->
                    handleWebSocketEvent(event)
                }.launchIn(viewModelScope)
                webSocketRepo.connectionState.collect { state ->
                    _socketConnected.value = state is WebSocketConnectionState.Connected
                }
            } catch (e: Exception) {
                _state.value = ChatRouletteState.Error("Failed to connect: ${e.message}", "failed_connect")
            }
        }
    }

    private fun handleWebSocketEvent(event: ChatRouletteWebSocketServerEvent) {
        when (event) {
            is ChatRouletteWebSocketServerEvent.MessageSent -> {
                val msg = event.data.message
                val newMessage = ChatRouletteMessage(
                    sessionId = sessionId!!,
                    senderId = msg.senderId,
                    content = msg.content,
                    createdAt = DateUtils.parseIsoDate(msg.createdAt)
                )
                _messages.update { it + newMessage }
            }
            is ChatRouletteWebSocketServerEvent.PartnerConnected -> {
                _partnerOnline.value = true
            }
            is ChatRouletteWebSocketServerEvent.PartnerDisconnected -> {
                _partnerOnline.value = false
            }
            is ChatRouletteWebSocketServerEvent.SessionExtended -> {
                val newExpiresAt = try {
                    DateUtils.parseIsoDate(event.data.newExpiresAt).time
                } catch (e: Exception) {
                    System.currentTimeMillis() + 5 * 60 * 1000L
                }
                val current = _state.value
                if (current is ChatRouletteState.ActiveSession) {
                    _state.value = current.copy(expiresAt = newExpiresAt)
                }
            }
            is ChatRouletteWebSocketServerEvent.SessionEnded -> {
                if (_sessionEnded.value) return

                _sessionEnded.value = true
                _timerSeconds.value = 0
                val reason = event.data.reason
                if (reason.startsWith("Reported")) {
                    val isInitiator = event.data.profileId == currentProfileId
                    val message: String = if (isInitiator){
                        "Жалоба отправлена, сессия завершена"
                    } else {
                        "На вас отправлена жалоба, сессия завершена"
                    }
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success(message)) }
                    finishSession()
                } else if (reason.startsWith("Left by user")){
                    val isInitiator = event.data.profileId == currentProfileId
                    val message: String = if (isInitiator){
                        "Сессия завершена"
                    } else {
                        "Собеседник завершил сессию"
                    }
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success(message)) }
                    finishSession()
                } else if (reason == "Session expired automatically"){
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Сессия завершена по истечению времени")) }
                    _state.value = ChatRouletteState.Rating
                }
                disconnectWebSocket()
            }
            is ChatRouletteWebSocketServerEvent.ExtensionRequested -> {
                val isRequestingProfile = event.data.requestingProfileId == currentProfileId
                if (isRequestingProfile) {
                    updateExtensionState(ExtensionState.REQUESTED_BY_ME)
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Запрос на продление отправлен")) }
                } else {
                    updateExtensionState(ExtensionState.REQUESTED_BY_PARTNER)
                }
            }
            is ChatRouletteWebSocketServerEvent.ExtensionApproved -> {
                updateExtensionState(ExtensionState.NONE)
                val isApprovingProfile = event.data.approvingProfileId == currentProfileId
                if (!isApprovingProfile){
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Собеседник согласился продлить сессию")) }
                }
            }
            is ChatRouletteWebSocketServerEvent.ExtensionRejected -> {
                updateExtensionState(ExtensionState.NONE)
                val isRejectingProfile = event.data.rejectingProfileId == currentProfileId
                if (!isRejectingProfile){
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Собеседник отказался продлевать сессию")) }
                }
            }
            is ChatRouletteWebSocketServerEvent.ExtensionCancelled -> {
                updateExtensionState(ExtensionState.NONE)
                val isCancellingProfile = event.data.cancellingProfileId == currentProfileId
                if (isCancellingProfile){
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Запрос на продление сессии отменён")) }
                } else {
                    viewModelScope.launch { _event.emit(ChatRouletteEvent.Success("Запрос на продление сессии отменён", "extension_cancelled")) }
                }
            }
            else -> {}
        }
    }

    private fun updateExtensionState(state: ExtensionState) {
        val current = _state.value
        if (current is ChatRouletteState.ActiveSession) {
            _state.value = current.copy(extensionState = state)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value is ChatRouletteState.ActiveSession) {
                val currentState = _state.value as? ChatRouletteState.ActiveSession ?: break
                val remaining = ((currentState.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                _timerSeconds.value = remaining.toInt()

                if (remaining <= 0 && !_sessionEnded.value && _state.value is ChatRouletteState.ActiveSession) {
                    _sessionEnded.value = true
                    _state.value = ChatRouletteState.Rating
                    disconnectWebSocket()
                    viewModelScope.launch {
                        _event.emit(ChatRouletteEvent.Success("Сессия завершена по истечению времени"))
                    }
                    return@launch
                }
                delay(300)
            }
        }
    }

    private fun updateTimer(seconds: Int) {
        _timerSeconds.value = seconds
    }

    fun sendMessage(content: String) {
        if (!_socketConnected.value) return
        val event = ChatRouletteWebSocketClientEvent.SendMessage(content)
        webSocketRepo.sendEvent(event)
    }

    fun requestExtension() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.extendSession()) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun acceptExtension() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.extendSession()) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun rejectExtension() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.rejectExtension()) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun cancelExtensionRequest() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.cancelExtensionRequest()) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun endSession() {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.endSession("Пользователь завершил диалог")) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun reportPartner(reason: String, details: String?) {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.reportPartner(reason, details)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun ratePartner(rating: Int, feedback: String?) {
        viewModelScope.launch {
            when (val result = chatRouletteRepository.ratePartner(rating, feedback)) {
                is NetworkResult.Success -> { }
                is NetworkResult.Error -> {
                    _event.emit(ChatRouletteEvent.Error(result.errorMessage, result.errorCode))
                }
                else -> {}
            }
        }
    }

    fun finishSession() {
        _state.value = ChatRouletteState.Finished
    }

    private fun disconnectWebSocket() {
        viewModelScope.launch {
            webSocketRepo.disconnect()
            timerJob?.cancel()
        }
    }

    fun clearTokensAndLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}