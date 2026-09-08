package com.example.lanchat.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory store for messages + known peers, exposed as StateFlow so
 * Compose screens recompose automatically as new messages arrive.
 *
 * TODO: back this with Room so chat history survives app restarts. Since
 * there's no cloud backend here, Room (local SQLite) is the natural choice
 * -- each device just keeps its own copy of the conversations it was part of.
 */
class MessageRepository {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _peers = MutableStateFlow<List<Peer>>(emptyList())
    val peers: StateFlow<List<Peer>> = _peers

    fun addMessage(message: Message) {
        _messages.update { it + message }
    }

    fun messagesWith(peerId: String): List<Message> =
        _messages.value.filter { it.fromPeerId == peerId || it.chatPeerId == peerId }

    fun upsertPeer(peer: Peer) {
        _peers.update { current ->
            if (current.any { it.id == peer.id }) {
                current.map { if (it.id == peer.id) peer else it }
            } else {
                current + peer
            }
        }
    }

    fun markPeerOffline(peerId: String) {
        _peers.update { list -> list.map { if (it.id == peerId) it.copy(isOnline = false) else it } }
    }
}
