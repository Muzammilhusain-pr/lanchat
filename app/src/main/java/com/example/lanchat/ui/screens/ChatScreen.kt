package com.example.lanchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lanchat.LanSession
import com.example.lanchat.data.Message
import com.example.lanchat.network.MessageType
import com.example.lanchat.network.WireMessage
import com.example.lanchat.ui.theme.IncomingBubble
import com.example.lanchat.ui.theme.OutgoingBubble
import java.util.UUID

/**
 * 1:1 chat thread -- WhatsApp-style bubbles (outgoing right/green,
 * incoming left/white), a text input row, an attach button that opens
 * MediaPickerSheet, and a call button in the top bar.
 */
@Composable
fun ChatScreen(session: LanSession, peerId: String, navController: NavController) {
    val allMessages by session.repository.messages.collectAsState()
    val thread = allMessages.filter { it.chatPeerId == peerId }

    var draft by remember { mutableStateOf("") }
    var showAttachSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerId.take(6)) },
                actions = {
                    IconButton(onClick = { navController.navigate("call/$peerId") }) {
                        Icon(Icons.Filled.Call, contentDescription = "Voice call")
                    }
                    IconButton(onClick = { navController.navigate("call/$peerId") }) {
                        Icon(Icons.Filled.Videocam, contentDescription = "Video call")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAttachSheet = true }) {
                    Icon(Icons.Filled.AttachFile, contentDescription = "Attach")
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") }
                )
                IconButton(onClick = {
                    if (draft.isNotBlank()) {
                        session.signalingClient?.send(
                            WireMessage(
                                type = MessageType.TEXT,
                                fromPeerId = session.myPeerId,
                                toPeerId = peerId,
                                text = draft
                            )
                        )
                        draft = ""
                    }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(thread.reversed()) { message -> MessageBubble(message) }
        }
    }

    if (showAttachSheet) {
        MediaPickerSheet(
            onDismiss = { showAttachSheet = false },
            onOptionSelected = { option ->
                showAttachSheet = false
                // TODO: route to FileTransferManager.upload(...) for IMAGE/VIDEO/DOCUMENT,
                // or LocationSharer.getCurrentLocationMessage(...) for LOCATION, then
                // session.signalingClient?.send(...) once the upload/lookup finishes.
            }
        )
    }
}

@Composable
private fun MessageBubble(message: Message) {
    val isOutgoing = message.fromPeerId != message.chatPeerId
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isOutgoing) OutgoingBubble else IncomingBubble,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 260.dp)
        ) {
            when (message.type) {
                MessageType.TEXT -> Text(message.text ?: "")
                MessageType.LOCATION -> Text("📍 Location: ${message.latitude}, ${message.longitude}")
                MessageType.CONTACT -> Text("👤 ${message.contactName}: ${message.contactNumber}")
                MessageType.IMAGE -> Text("🖼️ Image: ${message.fileName ?: "photo"}")
                MessageType.VIDEO -> Text("🎬 Video: ${message.fileName ?: "video"}")
                MessageType.DOCUMENT -> Text("📄 Document: ${message.fileName ?: "file"}")
                else -> Text(message.text ?: message.type.name, textAlign = TextAlign.Start)
            }
        }
    }
}
