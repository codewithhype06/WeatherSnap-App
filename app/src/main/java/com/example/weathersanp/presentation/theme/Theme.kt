// File: app/src/main/java/com/example/weathersnap/presentation/theme/Theme.kt
package com.example.weathersnap.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC5E1A5), // WeatherSnap Green
    background = Color(0xFF121212),
    surface = Color(0xFF2C2C2C)
)

@Composable
fun WeatherSnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}