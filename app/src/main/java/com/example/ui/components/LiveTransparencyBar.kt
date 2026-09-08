package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.transparency.CurrentActivityState
import com.example.ui.theme.LocalMedicalTheme

/**
 * Super Minimalist Live Transparency Bar
 * Medical-grade status monitor showing real-time background operations
 * and cryptographic state.
 */
@Composable
fun LiveTransparencyBar(
    activityState: CurrentActivityState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    val infiniteTransition = rememberInfiniteTransition(label = "medical_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = if (activityState.isWorking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (activityState.isWorking) 600 else 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surface.copy(alpha = if (theme.isDark) 0.85f else 0.95f))
            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("live_transparency_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Heartbeat / Vitals indicator
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(if (activityState.isWorking) theme.accentPrimary else theme.alertGreen)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(
                            targetState = activityState.title,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "title_anim"
                        ) { title ->
                            Text(
                                text = title,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = activityState.subtitle,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "subtitle_anim"
                    ) { subtitle ->
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = theme.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Minimalist Inspector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.surfaceElevated)
                    .border(1.dp, theme.border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Transparency Inspector",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inspect",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.accentPrimary
                    )
                }
            }
        }
    }
}
