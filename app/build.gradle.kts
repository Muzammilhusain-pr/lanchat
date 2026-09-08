plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.lanchat"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lanchat"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-skeleton"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core / Compose UI
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Local networking: lightweight WebSocket client+server (works fine on Android, no Netty overhead)
    implementation("org.java-websocket:Java-WebSocket:1.5.6")

    // Local HTTP file server for media (images/video/docs) transfer over LAN
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // Image loading for chat thumbnails
    implementation("io.coil-kt:coil-compose:2.6.0")

    // WebRTC for audio/video calls (LAN-only signaling, no STUN/TURN needed on same network)
    implementation("io.getstream:stream-webrtc-android:1.1.1")

    // JSON serialization for the wire protocol
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
