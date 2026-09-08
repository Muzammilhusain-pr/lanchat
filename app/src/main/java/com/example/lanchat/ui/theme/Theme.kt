package com.example.lanchat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LanGreenPrimary,
    onPrimary = OnPrimary,
    secondary = LanGreenLight,
    background = ChatBackground,
    surface = OnPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = LanGreenDark,
    onPrimary = OnPrimary,
    secondary = LanGreenLight
)

@Composable
fun LanChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LanChatTypography,
        content = content
    )
}
