package com.example.lanchat.data

/** A device visible on the local network -- either the host or another client. */
data class Peer(
    val id: String,        // stable per-install UUID, exchanged on PEER_JOINED
    val displayName: String,
    val ipAddress: String,
    val isHost: Boolean = false,
    val isOnline: Boolean = true
)
