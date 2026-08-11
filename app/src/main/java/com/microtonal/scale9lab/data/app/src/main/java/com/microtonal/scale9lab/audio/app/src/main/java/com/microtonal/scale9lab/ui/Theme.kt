package com.microtonal.scale9lab.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val KeyBackground = Color(0xFF1E1E2C)
val KeyPressed = Color(0xFF3D3D5C)
val AccentGold = Color(0xFFFFD700)
val AccentCyan = Color(0xFF00E5FF)

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    secondary = AccentGold,
    background = DarkBackground,
    surface = KeyBackground,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MicrotonalTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
