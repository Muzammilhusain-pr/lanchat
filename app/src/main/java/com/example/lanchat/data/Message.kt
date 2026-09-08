package com.example.lanchat.data

import com.example.lanchat.network.MessageType

/** UI/DB-facing chat message -- mapped from/to WireMessage at the network boundary. */
data class Message(
    val id: String,
    val chatPeerId: String,   // which 1:1 (or broadcast) conversation this belongs to
    val fromPeerId: String,
    val type: MessageType,
    val timestamp: Long,
    val text: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val contactName: String? = null,
    val contactNumber: String? = null,
    val isOutgoing: Boolean = false,
    val deliveryState: DeliveryState = DeliveryState.SENT
)

enum class DeliveryState { SENDING, SENT, FAILED }
