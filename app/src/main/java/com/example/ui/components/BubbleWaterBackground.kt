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
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import kotlin.math.sin
import kotlin.random.Random

private data class BubbleParticle(
    val initialXRatio: Float,
    val speed: Float,
    val radius: Float,
    val alpha: Float,
    val wobbleFreq: Float,
    val phase: Float
)

@Composable
fun BubbleWaterBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bubbles = remember {
        val random = Random(42)
        List(28) {
            BubbleParticle(
                initialXRatio = random.nextFloat(),
                speed = 0.08f + random.nextFloat() * 0.14f,
                radius = 4f + random.nextFloat() * 16f,
                alpha = 0.15f + random.nextFloat() * 0.35f,
                wobbleFreq = 2f + random.nextFloat() * 4f,
                phase = random.nextFloat() * 6.28f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "bubbles_anim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbles_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        DarkSurface,
                        Color(0xFF031930)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            for (bubble in bubbles) {
                // Rising motion: y moves upwards from 1 to 0, looping
                val rawY = (bubble.phase + progress * bubble.speed * 8f) % 1f
                val y = canvasHeight * (1f - rawY)

                // Sine wobble for realistic effervescent drift
                val wobble = sin(rawY * bubble.wobbleFreq * 6.28f + bubble.phase) * 18f
                val x = (bubble.initialXRatio * canvasWidth + wobble).coerceIn(0f, canvasWidth)

                // Translucent bubble body
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = bubble.alpha * 0.4f),
                    radius = bubble.radius,
                    center = Offset(x, y)
                )

                // Crisp bubble rim / outline
                drawCircle(
                    color = Color(0xFFE0F7FA).copy(alpha = bubble.alpha * 0.7f),
                    radius = bubble.radius,
                    center = Offset(x, y),
                    style = Stroke(width = 1.2f)
                )

                // Tiny sparkling specular highlight at top-left
                drawCircle(
                    color = Color.White.copy(alpha = bubble.alpha * 0.9f),
                    radius = bubble.radius * 0.25f,
                    center = Offset(x - bubble.radius * 0.35f, y - bubble.radius * 0.35f)
                )
            }
        }

        content()
    }
}
