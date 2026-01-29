package com.example.common_ground_android.network.repository.websocket

import com.example.common_ground_android.network.api.websocket.WebSocketService
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketServerEvent
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketServerEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

abstract class WebSocketRepository<ClientEvent, ServerEvent>(
    private val webSocketService: WebSocketService<ClientEvent, ServerEvent>,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    protected var connection: WebSocketService.Connection<ClientEvent, ServerEvent>? = null
    protected var outgoingJob: Job? = null
    protected var incomingJob: Job? = null

    protected val _incomingEvents = MutableSharedFlow<ServerEvent>()
    val incomingEvents: SharedFlow<ServerEvent> = _incomingEvents.asSharedFlow()

    protected val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()

    suspend fun connect(url: String) {
        if (connection != null) {
            disconnect()
        }

        _connectionState.value = WebSocketConnectionState.Connecting

        try {
            connection = webSocketService.connect(url)
            val conn = connection ?: return

            incomingJob = coroutineScope.launch {
                conn.incomingEvents.collect { event ->
                    _incomingEvents.emit(event)
                }
            }

            outgoingJob = coroutineScope.launch {
                for (event in conn.outgoingEvents) {
                    try {
                        webSocketService.sendEvent(conn, event)
                    } catch (e: Exception) {

                    }
                }
            }

            _connectionState.value = WebSocketConnectionState.Connected
        } catch (e: Exception) {
            _connectionState.value = WebSocketConnectionState.Error(e.message ?: "Connection failed")
            disconnect()
        }
    }

    fun sendEvent(event: ClientEvent) {
        val conn = connection ?: return
        coroutineScope.launch {
            conn.outgoingEvents.send(event)
        }
    }

    suspend fun disconnect() {
        connection?.let { webSocketService.disconnect(it) }
        incomingJob?.cancel()
        outgoingJob?.cancel()
        connection = null
        incomingJob = null
        outgoingJob = null
        _connectionState.value = WebSocketConnectionState.Disconnected
    }

    fun isConnected(): Boolean = connection != null && _connectionState.value is WebSocketConnectionState.Connected
}

class RoomWebSocketRepository(
    webSocketService: WebSocketService<RoomWebSocketClientEvent, RoomWebSocketServerEvent>
) : WebSocketRepository<RoomWebSocketClientEvent, RoomWebSocketServerEvent>(webSocketService)

class ChatRouletteWebSocketRepository(
    webSocketService: WebSocketService<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent>
) : WebSocketRepository<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent>(webSocketService)

sealed class WebSocketConnectionState {
    object Disconnected : WebSocketConnectionState()
    object Connecting : WebSocketConnectionState()
    object Connected : WebSocketConnectionState()
    data class Error(val message: String) : WebSocketConnectionState()
}