package com.example.lanchat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lanchat.LanSession

/**
 * Audio/video call screen. Placeholder full-screen surface for now --
 * once WebRtcClient.kt (call/WebRtcClient.kt) is wired up, the video
 * branch here swaps in the local/remote SurfaceViewRenderers from
 * stream-webrtc-android; the audio branch just shows the avatar+timer
 * shown below plus mute/speaker controls.
 */
@Composable
fun CallScreen(session: LanSession, peerId: String, navController: NavController) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 48.dp)) {
                Text(peerId.take(6), color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("Calling over local network...", color = Color.LightGray)
                // TODO: local/remote video surfaces (stream-webrtc-android's
                // VideoTextureViewRenderer) go here once WebRtcClient is wired up.
            }

            FloatingActionButton(
                onClick = {
                    // TODO: webRtcClient.hangUp()
                    navController.popBackStack()
                },
                containerColor = Color.Red
            ) {
                Icon(Icons.Filled.CallEnd, contentDescription = "End call", tint = Color.White)
            }
        }
    }
}
