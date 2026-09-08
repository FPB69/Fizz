package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BubbleWaterColorScheme = darkColorScheme(
  primary = BubbleAquaPrimary,
  onPrimary = DarkBackground,
  primaryContainer = BubbleAquaDark,
  onPrimaryContainer = BubbleFoamWhite,
  secondary = BubbleCarbonationGreen,
  onSecondary = DarkBackground,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = BubbleCarbonationGreen,
  tertiary = BubbleCyan,
  onTertiary = DarkBackground,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkSurfaceHigh,
  error = ErrorRed,
  onError = DarkBackground
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = BubbleWaterColorScheme,
    typography = Typography,
    content = content
  )
}
