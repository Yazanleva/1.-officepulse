package com.officepulse.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Day = lightColorScheme(
    primary = Color(0xFF006F60), onPrimary = Color.White,
    primaryContainer = Color(0xFFC1F4DB), onPrimaryContainer = Color(0xFF00493F),
    secondary = Color(0xFF435791), background = Color(0xFFF2F5F3),
    surface = Color.White, surfaceVariant = Color(0xFFE6EBE8),
    onSurface = Color(0xFF182D33), onBackground = Color(0xFF182D33),
    onSurfaceVariant = Color(0xFF445B5B), outline = Color(0xFF748480)
)
private val Night = darkColorScheme(
    primary = Color(0xFF82DEC0), primaryContainer = Color(0xFF124F44),
    onPrimaryContainer = Color(0xFFC1F4DB), background = Color(0xFF101C20),
    surface = Color(0xFF192B30), onSurface = Color(0xFFE2EEE9)
)
@Composable
fun OfficePulseTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) Night else Day, typography = Typography, content = content)
}
