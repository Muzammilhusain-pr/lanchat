package com.example.lanchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lanchat.ui.screens.CallScreen
import com.example.lanchat.ui.screens.ChatListScreen
import com.example.lanchat.ui.screens.ChatScreen
import com.example.lanchat.ui.screens.HomeScreen
import com.example.lanchat.ui.theme.LanChatTheme

/**
 * Single-activity host. Real screens live under ui/screens and are wired
 * together with Navigation-Compose. This is intentionally thin -- all the
 * actual chat/call/network logic lives in LanSession and the network/
 * package, not in the Activity.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = (application as LanChatApp).session

        setContent {
            LanChatTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") { HomeScreen(session = session, navController = navController) }
                    composable("chats") { ChatListScreen(session = session, navController = navController) }
                    composable("chat/{peerId}") { backStackEntry ->
                        val peerId = backStackEntry.arguments?.getString("peerId") ?: return@composable
                        ChatScreen(session = session, peerId = peerId, navController = navController)
                    }
                    composable("call/{peerId}") { backStackEntry ->
                        val peerId = backStackEntry.arguments?.getString("peerId") ?: return@composable
                        CallScreen(session = session, peerId = peerId, navController = navController)
                    }
                }
            }
        }
    }
}
