package com.example.lanchat.network

import kotlinx.serialization.Serializable

/**
 * Wire protocol for everything sent over the local WebSocket connection
 * between host and clients. Every message on the wire is a WireMessage,
 * JSON-encoded. Binary payloads (images/video/docs) are NOT sent through
 * this socket -- they go through LocalServer's HTTP file endpoints instead
 * (see FileTransferManager), and a WireMessage of type FILE just carries
 * the download URL + metadata once the upload finishes.
 */
@Serializable
enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    DOCUMENT,
    LOCATION,
    CONTACT,
    CALL_OFFER,
    CALL_ANSWER,
    CALL_ICE_CANDIDATE,
    CALL_HANGUP,
    PEER_JOINED,
    PEER_LEFT
}

@Serializable
data class WireMessage(
    val type: MessageType,
    val fromPeerId: String,
    val toPeerId: String? = null, // null = broadcast to all connected peers
    val timestamp: Long = System.currentTimeMillis(),

    // TEXT
    val text: String? = null,

    // IMAGE / VIDEO / DOCUMENT -- populated after FileTransferManager upload completes
    val fileUrl: String? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long? = null,
    val mimeType: String? = null,

    // LOCATION
    val latitude: Double? = null,
    val longitude: Double? = null,

    // CONTACT
    val contactName: String? = null,
    val contactNumber: String? = null,

    // CALL_* (WebRTC signaling payloads, opaque SDP/ICE strings)
    val sdp: String? = null,
    val iceCandidate: String? = null,
    val isVideoCall: Boolean? = null
)
