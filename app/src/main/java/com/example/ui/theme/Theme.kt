package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EditorialIceBlue,
    onPrimary = EditorialDark,
    primaryContainer = EditorialContainer,
    onPrimaryContainer = EditorialIceBlue,
    secondary = EditorialIceBlueActive,
    onSecondary = EditorialDark,
    secondaryContainer = EditorialActive,
    onSecondaryContainer = EditorialIceBlue,
    tertiary = EditorialIceBlueLight,
    onTertiary = EditorialDark,
    background = EditorialDark,
    onBackground = TextPrimary,
    surface = EditorialSurface,
    onSurface = TextPrimary,
    surfaceVariant = EditorialContainer,
    onSurfaceVariant = TextMuted,
    outline = SurfaceGlassBorder
)

@Composable
fun SonoraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

