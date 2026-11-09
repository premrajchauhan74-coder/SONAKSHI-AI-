package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SonakshiDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF003B46),
    onPrimaryContainer = Color(0xFFB8F8FF),
    secondary = NeonMagenta,
    onSecondary = Color(0xFF330019),
    secondaryContainer = Color(0xFF4F0B30),
    onSecondaryContainer = Color(0xFFFFD8E8),
    tertiary = NeonViolet,
    onTertiary = Color(0xFF1F0A4D),
    tertiaryContainer = Color(0xFF331B6E),
    onTertiaryContainer = Color(0xFFE7DEFF),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianCardBorder,
    error = CoralAlert,
    onError = Color.White
)

private val SonakshiLightColorScheme = lightColorScheme(
    primary = DeepCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCEEFF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = DeepMagentaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E6),
    onSecondaryContainer = Color(0xFF3D001C),
    tertiary = NeonViolet,
    background = LightBg,
    onBackground = Color(0xFF0E1324),
    surface = LightSurface,
    onSurface = Color(0xFF0E1324),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF424B6B),
    outline = Color(0xFFC5CEE8)
)

val SonakshiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SonakshiDarkColorScheme else SonakshiLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SonakshiShapes,
        content = content
    )
}
