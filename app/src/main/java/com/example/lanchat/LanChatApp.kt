package com.example.lanchat

import android.app.Application

/**
 * Application entry point. Holds app-wide singletons that need to survive
 * across screens: the local server (if this device becomes host), the
 * discovery manager, and the shared message repository.
 *
 * Kept deliberately simple (no DI framework) so the skeleton is easy to
 * read and swap pieces out of. Wire up Hilt/Koin later if the project grows.
 */
class LanChatApp : Application() {

    lateinit var session: LanSession
        private set

    override fun onCreate() {
        super.onCreate()
        session = LanSession(this)
    }
}
