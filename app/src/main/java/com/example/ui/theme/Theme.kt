package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = Yellow500,
    onPrimary = CameraPitchBlack,
    primaryContainer = Zinc900,
    onPrimaryContainer = Yellow500,
    secondary = LeicaRed,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF3F1D1D),
    onSecondaryContainer = PureWhite,
    tertiary = StudioGreen,
    onTertiary = CameraPitchBlack,
    background = CameraPitchBlack,
    onBackground = Zinc100,
    surface = Zinc900,
    onSurface = Zinc100,
    surfaceVariant = Zinc800,
    onSurfaceVariant = Zinc400,
    outline = Zinc700
)

@Composable
fun SB7ProCameraTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SophisticatedDarkColorScheme,
        typography = Typography,
        content = content
    )
}
