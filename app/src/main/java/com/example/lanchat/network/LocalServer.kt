package com.example.lanchat.network

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.io.File
import java.net.InetSocketAddress
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Runs ONLY on the device that is acting as host (the one that turned on
 * its Wi-Fi hotspot). Two servers, both LAN-only, no internet involved:
 *
 *  1. A WebSocket server (chat + WebRTC signaling) -- low latency,
 *     bidirectional, used for TEXT / LOCATION / CONTACT / CALL_* messages.
 *  2. An HTTP file server (NanoHTTPD) -- used for uploading/downloading
 *     IMAGE / VIDEO / DOCUMENT payloads, since shoving large binaries
 *     through the WebSocket isn't a good fit.
 *
 * Every other device on the hotspot connects to this host at its local IP
 * (found via NsdDiscoveryManager) -- e.g. ws://192.168.43.1:8080/chat and
 * http://192.168.43.1:8081/files/...
 */
class LocalServer(
    private val storageDir: File,
    private val wsPort: Int = 8080,
    private val httpPort: Int = 8081,
    private val onMessageReceived: (WireMessage) -> Unit
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val connectedClients = mutableSetOf<WebSocket>()

    private val webSocketServer = object : WebSocketServer(InetSocketAddress(wsPort)) {
        override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
            connectedClients += conn
            Log.i(TAG, "Peer connected: ${conn.remoteSocketAddress}")
        }

        override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
            connectedClients -= conn
            Log.i(TAG, "Peer disconnected: ${conn.remoteSocketAddress}")
        }

        override fun onMessage(conn: WebSocket, message: String) {
            val wireMessage = runCatching { json.decodeFromString(WireMessage.serializer(), message) }
                .getOrNull() ?: return

            onMessageReceived(wireMessage)
            relay(wireMessage, excluding = conn)
        }

        override fun onError(conn: WebSocket?, ex: Exception) {
            Log.e(TAG, "WebSocket error", ex)
        }

        override fun onStart() {
            Log.i(TAG, "WebSocket signaling server listening on port $wsPort")
        }
    }

    private val httpServer = object : NanoHTTPD(httpPort) {
        override fun serve(session: IHTTPSession): Response {
            return when (session.method) {
                Method.POST -> handleUpload(session)
                Method.GET -> handleDownload(session)
                else -> newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "")
            }
        }

        private fun handleUpload(session: IHTTPSession): Response {
            // TODO: parse multipart body, write bytes under storageDir with a
            // generated file name, return { "url": ".../files/<name>" } as JSON.
            // NanoHTTPD's session.parseBody(files) populates a temp-file map --
            // move the temp file into storageDir and stream it back via GET below.
            return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, MIME_PLAINTEXT, "upload TODO")
        }

        private fun handleDownload(session: IHTTPSession): Response {
            val requestedFile = File(storageDir, session.uri.removePrefix("/files/"))
            if (!requestedFile.exists()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "not found")
            }
            return newChunkedResponse(Response.Status.OK, guessMime(requestedFile), requestedFile.inputStream())
        }

        private fun guessMime(file: File): String = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }

    fun start() {
        storageDir.mkdirs()
        webSocketServer.start()
        httpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
    }

    fun stop() {
        runCatching { webSocketServer.stop() }
        runCatching { httpServer.stop() }
    }

    /** Broadcast a message to every connected client except [excluding] (if given). */
    fun relay(message: WireMessage, excluding: WebSocket? = null) {
        val payload = json.encodeToString(message)
        connectedClients.forEach { client ->
            if (client != excluding) runCatching { client.send(payload) }
        }
    }

    companion object {
        private const val TAG = "LocalServer"
    }
}
