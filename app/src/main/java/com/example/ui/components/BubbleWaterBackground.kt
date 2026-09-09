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
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.LocalMedicalTheme

/**
 * Fizz 1.1 Blue Bubble Water Ambient Canvas
 * Pure oceanic sapphire water with sparkling rising bubbles and translucent cyan caustic glows.
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
                Color(0xFF020914), // Deepest ocean abyss top
                Color(0xFF051329), // Marine sapphire mid
                Color(0xFF081C38), // Glowing aquatic deep
                Color(0xFF030A17)  // Bottom abyss
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0F9FF), // Sparkling spring top
                Color(0xFFE0F2FE), // Light sky aqua
                Color(0xFFBAE6FD), // Refreshing cyan mid
                Color(0xFFF0F9FF)  // Clean water base
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right electric cyan caustic light glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = if (theme.isDark) 0.12f else 0.18f),
                        Color(0xFF00B4D8).copy(alpha = if (theme.isDark) 0.05f else 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.85f, height * 0.12f)
            )

            // Bottom-left sky blue water glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = if (theme.isDark) 0.10f else 0.14f),
                        Color(0xFF0284C7).copy(alpha = if (theme.isDark) 0.04f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.82f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.15f, height * 0.82f)
            )

            // Carbonation bubbles - sparkling aesthetic spherical rings & highlight reflections
            val bubbleAlpha = if (theme.isDark) 0.22f else 0.35f
            val bubbleFillAlpha = if (theme.isDark) 0.04f else 0.08f

            val bubbles = listOf(
                Triple(0.12f, 0.22f, 22f),
                Triple(0.82f, 0.28f, 16f),
                Triple(0.28f, 0.45f, 32f),
                Triple(0.74f, 0.58f, 26f),
                Triple(0.18f, 0.72f, 18f),
                Triple(0.88f, 0.78f, 36f),
                Triple(0.48f, 0.88f, 14f),
                Triple(0.62f, 0.16f, 12f),
                Triple(0.36f, 0.94f, 20f)
            )

            bubbles.forEach { (xRatio, yRatio, r) ->
                val center = Offset(width * xRatio, height * yRatio)
                // Translucent bubble body
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = bubbleFillAlpha),
                    radius = r,
                    center = center
                )
                // Crisp bubble rim
                drawCircle(
                    color = Color(0xFF70F3FF).copy(alpha = bubbleAlpha),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
                // Glint / light specular reflection
                drawCircle(
                    color = Color.White.copy(alpha = bubbleAlpha * 1.5f),
                    radius = r * 0.25f,
                    center = Offset(center.x - r * 0.35f, center.y - r * 0.35f)
                )
            }
        }

        content()
    }
}

