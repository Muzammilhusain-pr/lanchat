package com.example.lanchat

import android.content.Context
import com.example.lanchat.data.Message
import com.example.lanchat.data.MessageRepository
import com.example.lanchat.data.Peer
import com.example.lanchat.network.LocalServer
import com.example.lanchat.network.NsdDiscoveryManager
import com.example.lanchat.network.SignalingClient
import com.example.lanchat.network.WireMessage
import java.util.UUID

/**
 * Top-level glue object, one per app process. Ties together discovery,
 * the (optional) local server, the signaling client, and the message
 * repository so the UI layer (screens/viewmodels) never has to touch
 * networking directly.
 *
 * Usage from the Home screen:
 *  - session.becomeHost()  -- this device turns on its hotspot (user does
 *    that manually in system settings for now -- see HomeScreen TODO) and
 *    starts LocalServer + advertises via NSD.
 *  - session.joinAsClient() -- this device runs NsdDiscoveryManager.discover(),
 *    and connects a SignalingClient to whatever host it finds.
 *
 * Either way, once connected, sending a chat message / file / location /
 * call signal all go through the same `signalingClient`.
 */
class LanSession(private val context: Context) {

    val myPeerId: String = UUID.randomUUID().toString()
    val repository = MessageRepository()

    private val discoveryManager = NsdDiscoveryManager(context)
    private var localServer: LocalServer? = null
    var signalingClient: SignalingClient? = null
        private set

    var isHost: Boolean = false
        private set

    /** Call once this device's Wi-Fi hotspot is on. Starts the local server + advertises it. */
    fun becomeHost() {
        isHost = true
        val storageDir = context.getExternalFilesDir("lanchat_files") ?: context.filesDir

        localServer = LocalServer(storageDir = storageDir, onMessageReceived = ::handleIncoming).also {
            it.start()
        }
        discoveryManager.advertise(port = 8080)

        // Host also runs a client connection to itself so it participates
        // in the same message flow as everyone else.
        connectSignaling(hostAddress = "127.0.0.1", hostPort = 8080)
    }

    /** Call to discover and join a host already running on the local network. */
    fun joinAsClient() {
        isHost = false
        discoveryManager.discover { host, port ->
            connectSignaling(hostAddress = host, hostPort = port)
        }
    }

    private fun connectSignaling(hostAddress: String, hostPort: Int) {
        signalingClient = SignalingClient(
            myPeerId = myPeerId,
            hostAddress = hostAddress,
            hostPort = hostPort
        ).also { it.connect() }

        // TODO: collect signalingClient.incoming (a Flow<WireMessage>) on a
        // coroutine scope here and route CALL_* types to WebRtcClient,
        // everything else to handleIncoming().
    }

    private fun handleIncoming(wireMessage: WireMessage) {
        repository.upsertPeer(
            Peer(
                id = wireMessage.fromPeerId,
                displayName = wireMessage.fromPeerId.take(6),
                ipAddress = "", // TODO: populate from the resolved NSD service info
                isHost = isHost
            )
        )

        // TODO: map WireMessage -> Message properly per type (TEXT/IMAGE/etc,
        // triggering FileTransferManager.download() for file types) instead
        // of this simplified text-only mapping.
        repository.addMessage(
            Message(
                id = UUID.randomUUID().toString(),
                chatPeerId = wireMessage.fromPeerId,
                fromPeerId = wireMessage.fromPeerId,
                type = wireMessage.type,
                timestamp = wireMessage.timestamp,
                text = wireMessage.text
            )
        )
    }

    fun teardown() {
        discoveryManager.stopAdvertising()
        discoveryManager.stopDiscovery()
        signalingClient?.disconnect()
        localServer?.stop()
    }
}
