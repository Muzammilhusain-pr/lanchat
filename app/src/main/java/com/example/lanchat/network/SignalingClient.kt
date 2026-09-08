package com.example.lanchat.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

/**
 * Runs on every device (including the host, connecting to itself) as the
 * client side of the chat/signaling channel. Wraps a single WebSocket
 * connection to the host found via NsdDiscoveryManager and exposes incoming
 * messages as a Flow that ChatViewModel / CallViewModel can collect.
 *
 * This same channel carries plain chat messages AND WebRTC signaling
 * (offer/answer/ICE candidates) -- see MessageType.CALL_* in Protocol.kt.
 * Since host and clients share one local network, no STUN/TURN server is
 * needed; this WebSocket IS the entire signaling path.
 */
class SignalingClient(
    private val myPeerId: String,
    hostAddress: String,
    hostPort: Int
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _incoming = MutableSharedFlow<WireMessage>(extraBufferCapacity = 64)
    val incoming: SharedFlow<WireMessage> = _incoming.asSharedFlow()

    private val client = object : WebSocketClient(URI("ws://$hostAddress:$hostPort")) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            Log.i(TAG, "Connected to host $hostAddress:$hostPort")
            send(WireMessage(type = MessageType.PEER_JOINED, fromPeerId = myPeerId))
        }

        override fun onMessage(message: String) {
            val wireMessage = runCatching { json.decodeFromString(WireMessage.serializer(), message) }
                .getOrNull() ?: return
            _incoming.tryEmit(wireMessage)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Log.i(TAG, "Disconnected from host: $reason")
        }

        override fun onError(ex: Exception) {
            Log.e(TAG, "SignalingClient error", ex)
        }
    }

    fun connect() = client.connect()
    fun disconnect() = client.close()

    fun send(message: WireMessage) {
        if (!client.isOpen) {
            Log.w(TAG, "Tried to send while disconnected, dropping: $message")
            return
        }
        client.send(json.encodeToString(message))
    }

    companion object {
        private const val TAG = "SignalingClient"
    }
}
