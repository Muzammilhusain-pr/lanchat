package com.example.lanchat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lanchat.LanSession

/**
 * First screen. Two ways in, matching the two roles in the star topology:
 *
 *  - "Start as Host" -- for whoever's phone will run the hotspot. Reminds
 *    them to turn on the hotspot in system settings first (Android doesn't
 *    let a third-party app enable it programmatically without special
 *    system permissions), then calls session.becomeHost().
 *  - "Join Nearby Chat" -- for everyone else. Reminds them to connect to
 *    the host's hotspot Wi-Fi first, then calls session.joinAsClient(),
 *    which auto-discovers the host via NSD.
 */
@Composable
fun HomeScreen(session: LanSession, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LanChat", style = MaterialTheme.typography.titleLarge)
        Text(
            "Chat, call, and share files over your local Wi-Fi/hotspot -- no internet needed.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Button(
            onClick = {
                session.becomeHost()
                navController.navigate("chats")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start as Host (turn hotspot on first)")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                session.joinAsClient()
                navController.navigate("chats")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Nearby Chat")
        }

        // TODO: show live NSD discovery status here ("Searching...", "Found host at 192.168.x.x")
        // instead of navigating immediately -- right now this is optimistic-UI only.
    }
}
