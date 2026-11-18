package com.officepulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Day = lightColorScheme(
    primary = Color(0xFFCC0C61), onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E7), onPrimaryContainer = Color(0xFF67002E),
    secondaryContainer = Color(0xFFFFD9E7), onSecondaryContainer = Color(0xFF67002E), secondary = Color(0xFF85506C), background = Color(0xFFF9F5F8),
    surface = Color.White, surfaceVariant = Color(0xFFF2E5EB),
    onSurface = Color(0xFF301C28), onBackground = Color(0xFF301C28),
    onSurfaceVariant = Color(0xFF644E59), outline = Color(0xFF88727F)
)
private val Night = darkColorScheme(
    primary = Color(0xFFFFAACC), primaryContainer = Color(0xFF72103F),
    onPrimaryContainer = Color(0xFFFFD9E7), background = Color(0xFF20151D),
    surface = Color(0xFF30202A), onSurface = Color(0xFFF7E6F0)
)
@Composable
fun OfficePulseTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Night else Day, typography = Typography, content = content)
}
