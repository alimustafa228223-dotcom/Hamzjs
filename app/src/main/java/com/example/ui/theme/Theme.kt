package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val KainatDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = PitchBlack,
    primaryContainer = DeepObsidian,
    onPrimaryContainer = NeonCyan,
    secondary = VividPurple,
    onSecondary = TextPrimary,
    secondaryContainer = GlassSurfaceHigh,
    onSecondaryContainer = TextPrimary,
    tertiary = ElectricBlue,
    onTertiary = TextPrimary,
    background = PitchBlack,
    onBackground = TextPrimary,
    surface = AbyssalBlack,
    onSurface = TextPrimary,
    surfaceVariant = DeepObsidian,
    onSurfaceVariant = TextSecondary,
    outline = GlassSubtleBorder,
    error = CrimsonError,
    onError = PitchBlack
)

val KainatShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun KainatAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KainatDarkColorScheme,
        typography = KainatTypography,
        shapes = KainatShapes,
        content = content
    )
}
