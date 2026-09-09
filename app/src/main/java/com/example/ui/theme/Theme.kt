package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal to provide Arco Global Architectural Design Tokens
data class MedicalThemeColors(
  val isDark: Boolean,
  val background: Color,
  val surface: Color,
  val surfaceElevated: Color,
  val surfaceHigh: Color,
  val border: Color,
  val borderGold: Color,
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
    background = ArcoDarkBg,
    surface = ArcoDarkSurface,
    surfaceElevated = ArcoDarkSurfaceElevated,
    surfaceHigh = ArcoDarkSurfaceHigh,
    border = ArcoDarkBorder,
    borderGold = ArcoDarkBorderGold,
    textPrimary = ArcoDarkTextPrimary,
    textSecondary = ArcoDarkTextSecondary,
    textMuted = ArcoDarkTextMuted,
    accentPrimary = ArcoGold,
    accentSecondary = ArcoBronze,
    alertGreen = ArcoEmerald,
    alertAmber = ArcoAmber,
    alertRed = ArcoCrimson
  )
}

private val ArcoDarkColorScheme = darkColorScheme(
  primary = ArcoGold,
  onPrimary = ArcoDarkBg,
  primaryContainer = ArcoDarkSurfaceElevated,
  onPrimaryContainer = ArcoGoldLight,
  secondary = ArcoBronze,
  onSecondary = ArcoDarkBg,
  secondaryContainer = ArcoDarkSurfaceHigh,
  onSecondaryContainer = ArcoBronze,
  tertiary = ArcoGoldDark,
  onTertiary = ArcoDarkBg,
  background = ArcoDarkBg,
  onBackground = ArcoDarkTextPrimary,
  surface = ArcoDarkSurface,
  onSurface = ArcoDarkTextPrimary,
  surfaceVariant = ArcoDarkSurfaceElevated,
  onSurfaceVariant = ArcoDarkTextSecondary,
  outline = ArcoDarkBorder,
  outlineVariant = ArcoDarkBorderGold,
  error = ArcoCrimson,
  onError = ArcoDarkBg
)

private val ArcoLightColorScheme = lightColorScheme(
  primary = ArcoGoldDark,
  onPrimary = Color.White,
  primaryContainer = ArcoLightSurfaceElevated,
  onPrimaryContainer = ArcoLightTextPrimary,
  secondary = ArcoBronzeDark,
  onSecondary = Color.White,
  secondaryContainer = ArcoLightSurfaceHigh,
  onSecondaryContainer = ArcoBronzeDark,
  tertiary = ArcoGold,
  onTertiary = Color.White,
  background = ArcoLightBg,
  onBackground = ArcoLightTextPrimary,
  surface = ArcoLightSurface,
  onSurface = ArcoLightTextPrimary,
  surfaceVariant = ArcoLightSurfaceElevated,
  onSurfaceVariant = ArcoLightTextSecondary,
  outline = ArcoLightBorder,
  outlineVariant = ArcoLightBorderGold,
  error = ArcoCrimson,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) ArcoDarkColorScheme else ArcoLightColorScheme
  val medicalColors = if (darkTheme) {
    MedicalThemeColors(
      isDark = true,
      background = ArcoDarkBg,
      surface = ArcoDarkSurface,
      surfaceElevated = ArcoDarkSurfaceElevated,
      surfaceHigh = ArcoDarkSurfaceHigh,
      border = ArcoDarkBorder,
      borderGold = ArcoDarkBorderGold,
      textPrimary = ArcoDarkTextPrimary,
      textSecondary = ArcoDarkTextSecondary,
      textMuted = ArcoDarkTextMuted,
      accentPrimary = ArcoGold,
      accentSecondary = ArcoBronze,
      alertGreen = ArcoEmerald,
      alertAmber = ArcoAmber,
      alertRed = ArcoCrimson
    )
  } else {
    MedicalThemeColors(
      isDark = false,
      background = ArcoLightBg,
      surface = ArcoLightSurface,
      surfaceElevated = ArcoLightSurfaceElevated,
      surfaceHigh = ArcoLightSurfaceHigh,
      border = ArcoLightBorder,
      borderGold = ArcoLightBorderGold,
      textPrimary = ArcoLightTextPrimary,
      textSecondary = ArcoLightTextSecondary,
      textMuted = ArcoLightTextMuted,
      accentPrimary = ArcoGoldDark,
      accentSecondary = ArcoBronzeDark,
      alertGreen = ArcoEmeraldDark,
      alertAmber = ArcoAmber,
      alertRed = ArcoCrimson
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
