package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LocalMedicalTheme

/**
 * Super Minimalist Medical Canvas Background
 * Supports both pristine Light Theme and sleek Obsidian Dark Theme,
 * modeled after clean healthcare / medical interfaces.
 */
@Composable
fun BubbleWaterBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalMedicalTheme.current

    val backgroundBrush = if (theme.isDark) {
        Brush.verticalGradient(
            colors = listOf(
                theme.background,
                Color(0xFF0F172A),
                theme.background
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                theme.background,
                Color(0xFFFFFFFF),
                Color(0xFFF1F5F9)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Minimal subtle ambient light glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val glowColor = if (theme.isDark) {
                theme.accentPrimary.copy(alpha = 0.035f)
            } else {
                theme.accentPrimary.copy(alpha = 0.025f)
            }

            drawCircle(
                color = glowColor,
                radius = width * 0.45f,
                center = Offset(width * 0.85f, height * 0.1f)
            )

            drawCircle(
                color = theme.alertGreen.copy(alpha = if (theme.isDark) 0.025f else 0.015f),
                radius = width * 0.5f,
                center = Offset(width * 0.1f, height * 0.75f)
            )
        }

        content()
    }
}
