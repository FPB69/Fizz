package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.LocalMedicalTheme
import kotlin.math.sin

private data class AnimatedBubbleSpec(
    val initialX: Float, // 0.0 .. 1.0
    val radius: Float,
    val speedMultiplier: Float,
    val phaseOffset: Float,
    val wobbleFrequency: Float,
    val wobbleAmplitude: Float
)

/**
 * Fizz 1.1 Blue Bubble Water Animated Canvas
 * Pure oceanic sapphire water with living, rising effervescent carbonation bubbles,
 * sinusoidal wobble, specular glints, and translucent cyan caustic glows.
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

    val infiniteTransition = rememberInfiniteTransition(label = "bubbles_motion")

    // Master continuous loop (0.0 to 1.0 over 12 seconds)
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbles_rise"
    )

    // Secondary pulsating glow (0.8 to 1.2 over 4 seconds)
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "caustic_pulse"
    )

    // Predefined bubble specs for organic dispersion
    val bubbleSpecs = remember {
        listOf(
            AnimatedBubbleSpec(initialX = 0.10f, radius = 24f, speedMultiplier = 1.0f, phaseOffset = 0.05f, wobbleFrequency = 3.5f, wobbleAmplitude = 18f),
            AnimatedBubbleSpec(initialX = 0.22f, radius = 14f, speedMultiplier = 1.4f, phaseOffset = 0.35f, wobbleFrequency = 4.2f, wobbleAmplitude = 12f),
            AnimatedBubbleSpec(initialX = 0.35f, radius = 32f, speedMultiplier = 0.8f, phaseOffset = 0.65f, wobbleFrequency = 2.8f, wobbleAmplitude = 22f),
            AnimatedBubbleSpec(initialX = 0.48f, radius = 18f, speedMultiplier = 1.2f, phaseOffset = 0.15f, wobbleFrequency = 3.8f, wobbleAmplitude = 14f),
            AnimatedBubbleSpec(initialX = 0.60f, radius = 28f, speedMultiplier = 0.9f, phaseOffset = 0.85f, wobbleFrequency = 3.0f, wobbleAmplitude = 20f),
            AnimatedBubbleSpec(initialX = 0.73f, radius = 12f, speedMultiplier = 1.6f, phaseOffset = 0.45f, wobbleFrequency = 5.0f, wobbleAmplitude = 10f),
            AnimatedBubbleSpec(initialX = 0.86f, radius = 36f, speedMultiplier = 0.75f, phaseOffset = 0.25f, wobbleFrequency = 2.5f, wobbleAmplitude = 24f),
            AnimatedBubbleSpec(initialX = 0.93f, radius = 16f, speedMultiplier = 1.3f, phaseOffset = 0.75f, wobbleFrequency = 4.0f, wobbleAmplitude = 15f),
            AnimatedBubbleSpec(initialX = 0.18f, radius = 20f, speedMultiplier = 1.1f, phaseOffset = 0.55f, wobbleFrequency = 3.2f, wobbleAmplitude = 16f),
            AnimatedBubbleSpec(initialX = 0.52f, radius = 10f, speedMultiplier = 1.8f, phaseOffset = 0.95f, wobbleFrequency = 5.5f, wobbleAmplitude = 8f),
            AnimatedBubbleSpec(initialX = 0.80f, radius = 22f, speedMultiplier = 1.05f, phaseOffset = 0.10f, wobbleFrequency = 3.6f, wobbleAmplitude = 18f),
            AnimatedBubbleSpec(initialX = 0.30f, radius = 15f, speedMultiplier = 1.5f, phaseOffset = 0.80f, wobbleFrequency = 4.5f, wobbleAmplitude = 12f)
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

            // Top-right electric cyan caustic light glow (pulsing)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = (if (theme.isDark) 0.13f else 0.19f) * pulseGlow),
                        Color(0xFF00B4D8).copy(alpha = (if (theme.isDark) 0.05f else 0.08f) * pulseGlow),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.7f * pulseGlow
                ),
                radius = width * 0.7f * pulseGlow,
                center = Offset(width * 0.85f, height * 0.12f)
            )

            // Bottom-left sky blue water glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = if (theme.isDark) 0.11f else 0.15f),
                        Color(0xFF0284C7).copy(alpha = if (theme.isDark) 0.04f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.82f),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(width * 0.15f, height * 0.82f)
            )

            // Dynamic Animated Carbonation Bubbles
            val bubbleAlpha = if (theme.isDark) 0.28f else 0.40f
            val bubbleFillAlpha = if (theme.isDark) 0.06f else 0.10f

            bubbleSpecs.forEach { spec ->
                // Calculate continuous rising Y position (wrapping from bottom to top)
                val rawYProgress = (animationProgress * spec.speedMultiplier + spec.phaseOffset) % 1.0f
                val yPos = height * (1.0f - rawYProgress)

                // Sinusoidal horizontal wobble
                val wobbleAngle = (rawYProgress * Math.PI * 2 * spec.wobbleFrequency).toFloat()
                val xOffset = sin(wobbleAngle) * spec.wobbleAmplitude
                val xPos = (width * spec.initialX) + xOffset

                val center = Offset(xPos, yPos)
                val r = spec.radius

                // Fading near top and bottom for smooth entry/exit
                val fade = when {
                    rawYProgress < 0.08f -> rawYProgress / 0.08f
                    rawYProgress > 0.90f -> (1.0f - rawYProgress) / 0.10f
                    else -> 1.0f
                }

                val currentFillAlpha = bubbleFillAlpha * fade
                val currentRimAlpha = bubbleAlpha * fade

                if (fade > 0.01f) {
                    // Translucent bubble body
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = currentFillAlpha),
                        radius = r,
                        center = center
                    )
                    // Crisp luminous bubble rim
                    drawCircle(
                        color = Color(0xFF70F3FF).copy(alpha = currentRimAlpha),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    // Specular light glint on top-left of bubble
                    drawCircle(
                        color = Color.White.copy(alpha = currentRimAlpha * 1.4f),
                        radius = r * 0.26f,
                        center = Offset(center.x - r * 0.35f, center.y - r * 0.35f)
                    )
                    // Subtle bottom-right counter-reflection
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = currentRimAlpha * 0.6f),
                        radius = r * 0.16f,
                        center = Offset(center.x + r * 0.30f, center.y + r * 0.30f)
                    )
                }
            }
        }

        content()
    }
}
