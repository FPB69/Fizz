package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal to allow any component to easily know current theme mode & custom tokens
data class MedicalThemeColors(
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val surfaceElevated: Color,
  val surfaceHigh: Color,
  val border: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val accentPrimary: Color,
  val accentSecondary: Color,
  val alertGreen: Color,
  val alertAmber: Color,
  val alertRed: Color
)

val LocalMedicalTheme = compositionLocalOf {
  MedicalThemeColors(
    isDark = true,
    background = MedicalDarkBg,
    surface = MedicalDarkSurface,
    surfaceElevated = MedicalDarkSurfaceElevated,
    surfaceHigh = MedicalDarkSurfaceHigh,
    border = MedicalDarkBorder,
    textPrimary = MedicalDarkTextPrimary,
    textSecondary = MedicalDarkTextSecondary,
    textMuted = MedicalDarkTextMuted,
    accentPrimary = MedicalCyan,
    accentSecondary = MedicalTeal,
    alertGreen = MedicalMint,
    alertAmber = MedicalAmber,
    alertRed = MedicalRed
  )
}

private val MinimalMedicalDarkColorScheme = darkColorScheme(
  primary = MedicalCyan,
  onPrimary = MedicalDarkBg,
  primaryContainer = MedicalDarkSurfaceElevated,
  onPrimaryContainer = MedicalDarkTextPrimary,
  secondary = MedicalMint,
  onSecondary = MedicalDarkBg,
  secondaryContainer = MedicalDarkSurfaceHigh,
  onSecondaryContainer = MedicalMint,
  tertiary = MedicalTeal,
  onTertiary = MedicalDarkBg,
  background = MedicalDarkBg,
  onBackground = MedicalDarkTextPrimary,
  surface = MedicalDarkSurface,
  onSurface = MedicalDarkTextPrimary,
  surfaceVariant = MedicalDarkSurfaceElevated,
  onSurfaceVariant = MedicalDarkTextSecondary,
  outline = MedicalDarkBorder,
  outlineVariant = MedicalDarkSurfaceHigh,
  error = MedicalRed,
  onError = MedicalDarkBg
)

private val MinimalMedicalLightColorScheme = lightColorScheme(
  primary = MedicalTealDark,
  onPrimary = Color.White,
  primaryContainer = MedicalLightSurfaceElevated,
  onPrimaryContainer = MedicalLightTextPrimary,
  secondary = MedicalMintDark,
  onSecondary = Color.White,
  secondaryContainer = MedicalLightSurfaceHigh,
  onSecondaryContainer = MedicalMintDark,
  tertiary = MedicalTeal,
  onTertiary = Color.White,
  background = MedicalLightBg,
  onBackground = MedicalLightTextPrimary,
  surface = MedicalLightSurface,
  onSurface = MedicalLightTextPrimary,
  surfaceVariant = MedicalLightSurfaceElevated,
  onSurfaceVariant = MedicalLightTextSecondary,
  outline = MedicalLightBorder,
  outlineVariant = MedicalLightSurfaceHigh,
  error = MedicalRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MinimalMedicalDarkColorScheme else MinimalMedicalLightColorScheme
  val medicalColors = if (darkTheme) {
    MedicalThemeColors(
      isDark = true,
      background = MedicalDarkBg,
      surface = MedicalDarkSurface,
      surfaceElevated = MedicalDarkSurfaceElevated,
      surfaceHigh = MedicalDarkSurfaceHigh,
      border = MedicalDarkBorder,
      textPrimary = MedicalDarkTextPrimary,
      textSecondary = MedicalDarkTextSecondary,
      textMuted = MedicalDarkTextMuted,
      accentPrimary = MedicalCyan,
      accentSecondary = MedicalTeal,
      alertGreen = MedicalMint,
      alertAmber = MedicalAmber,
      alertRed = MedicalRed
    )
  } else {
    MedicalThemeColors(
      isDark = false,
      background = MedicalLightBg,
      surface = MedicalLightSurface,
      surfaceElevated = MedicalLightSurfaceElevated,
      surfaceHigh = MedicalLightSurfaceHigh,
      border = MedicalLightBorder,
      textPrimary = MedicalLightTextPrimary,
      textSecondary = MedicalLightTextSecondary,
      textMuted = MedicalLightTextMuted,
      accentPrimary = MedicalTealDark,
      accentSecondary = MedicalTeal,
      alertGreen = MedicalMintDark,
      alertAmber = MedicalAmber,
      alertRed = MedicalRed
    )
  }

  CompositionLocalProvider(LocalMedicalTheme provides medicalColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
