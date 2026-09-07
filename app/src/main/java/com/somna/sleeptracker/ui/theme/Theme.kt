package com.somna.sleeptracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SomnaDarkColorScheme = darkColorScheme(
    primary = IndigoAccent,
    onPrimary = SoftWhite,
    primaryContainer = IndigoLight,
    secondary = IndigoLight,
    background = DeepBlack,
    surface = SurfaceDark,
    surfaceVariant = CardSurface,
    onBackground = SoftWhite,
    onSurface = SoftWhite,
    onSurfaceVariant = MutedGray,
    error = ErrorRose
)

@Composable
fun SomnaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SomnaDarkColorScheme,
        content = content
    )
}
