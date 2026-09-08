package com.example.lanchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lanchat.LanSession

/** List of peers currently visible on the local network -- tap one to open the chat thread. */
@Composable
fun ChatListScreen(session: LanSession, navController: NavController) {
    val peers by session.repository.peers.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (session.isHost) "LanChat (Host)" else "LanChat") }) }
    ) { padding ->
        if (peers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Waiting for other devices to join the local network...")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(peers) { peer ->
                    ListItem(
                        headlineContent = { Text(peer.displayName) },
                        supportingContent = { Text(if (peer.isHost) "Host" else "Peer") },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Filled.Circle,
                                contentDescription = if (peer.isOnline) "Online" else "Offline",
                                tint = if (peer.isOnline) Color(0xFF25D366) else Color.Gray,
                                modifier = Modifier.size(10.dp)
                            )
                        },
                        modifier = Modifier.clickable { navController.navigate("chat/${peer.id}") }
                    )
                    Divider()
                }
            }
        }
    }
}
