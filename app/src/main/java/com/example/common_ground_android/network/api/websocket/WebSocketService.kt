package com.example.common_ground_android.network.api.websocket

import com.example.common_ground_android.network.client.KtorClientFactory
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteErrorData
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.chat_roulette.ChatRouletteWebSocketServerEvent
import com.example.common_ground_android.network.model.websocket.room.RoomErrorData
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketClientEvent
import com.example.common_ground_android.network.model.websocket.room.RoomWebSocketServerEvent
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

interface WebSocketService<ClientEvent, ServerEvent> {
    suspend fun connect(url: String): Connection<ClientEvent, ServerEvent>
    suspend fun sendEvent(connection: Connection<ClientEvent, ServerEvent>, event: ClientEvent)
    suspend fun disconnect(connection: Connection<ClientEvent, ServerEvent>)

    data class Connection<ClientEvent, ServerEvent>(
        val session: DefaultClientWebSocketSession,
        val incomingEvents: Flow<ServerEvent>,
        val outgoingEvents: Channel<ClientEvent>
    )
}

class RoomWebSocketServiceImpl : WebSocketService<RoomWebSocketClientEvent, RoomWebSocketServerEvent> {
    private val client = KtorClientFactory.getInstance().webSocketClient
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun connect(url: String): WebSocketService.Connection<RoomWebSocketClientEvent, RoomWebSocketServerEvent> {
        val session = client.webSocketSession(url)

        val incomingEvents = session.incoming
            .receiveAsFlow()
            .filter { it is Frame.Text }
            .map { frame ->
                val text = (frame as Frame.Text).readText()
                try {
                    json.decodeFromString<RoomWebSocketServerEvent>(text)
                } catch (e: Exception) {
                    RoomWebSocketServerEvent.Error(
                        data = RoomErrorData("Failed to parse event: ${e.message}"),
                        timestamp = System.currentTimeMillis().toString()
                    )
                }
            }

        val outgoingEvents = Channel<RoomWebSocketClientEvent>(Channel.UNLIMITED)

        return WebSocketService.Connection(session, incomingEvents, outgoingEvents)
    }

    override suspend fun sendEvent(
        connection: WebSocketService.Connection<RoomWebSocketClientEvent, RoomWebSocketServerEvent>,
        event: RoomWebSocketClientEvent
    ) {
        val text = json.encodeToString(event)
        connection.session.send(Frame.Text(text))
    }

    override suspend fun disconnect(connection: WebSocketService.Connection<RoomWebSocketClientEvent, RoomWebSocketServerEvent>) {
        connection.session.close()
        connection.outgoingEvents.close()
    }
}

class ChatRouletteWebSocketServiceImpl : WebSocketService<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent> {
    private val client = KtorClientFactory.getInstance().webSocketClient
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun connect(url: String): WebSocketService.Connection<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent> {
        val session = client.webSocketSession(url)

        val incomingEvents = session.incoming
            .receiveAsFlow()
            .filter { it is Frame.Text }
            .map { frame ->
                val text = (frame as Frame.Text).readText()
                try {
                    json.decodeFromString<ChatRouletteWebSocketServerEvent>(text)
                } catch (e: Exception) {
                    ChatRouletteWebSocketServerEvent.Error(
                        data = ChatRouletteErrorData("Failed to parse event: ${e.message}"),
                        timestamp = System.currentTimeMillis().toString()
                    )
                }
            }

        val outgoingEvents = Channel<ChatRouletteWebSocketClientEvent>(Channel.UNLIMITED)

        return WebSocketService.Connection(session, incomingEvents, outgoingEvents)
    }

    override suspend fun sendEvent(
        connection: WebSocketService.Connection<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent>,
        event: ChatRouletteWebSocketClientEvent
    ) {
        val text = json.encodeToString(event)
        connection.session.send(Frame.Text(text))
    }

    override suspend fun disconnect(connection: WebSocketService.Connection<ChatRouletteWebSocketClientEvent, ChatRouletteWebSocketServerEvent>) {
        connection.session.close()
        connection.outgoingEvents.close()
    }
}