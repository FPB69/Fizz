package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalMedicalTheme

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val bullets: List<String>
)

@Composable
fun OnboardingGuideScreen(
    onFinishGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    var currentStepIdx by remember { mutableIntStateOf(0) }

    val steps = listOf(
        GuideStep(
            stepNumber = 1,
            title = "Pair with QR Code",
            description = "Connect directly with peers by showing or scanning a QR code.",
            icon = Icons.Default.QrCode,
            bullets = listOf(
                "Direct connection over Tor",
                "No phone number or email needed",
                "Your Onion address is your identity"
            )
        ),
        GuideStep(
            stepNumber = 2,
            title = "Private Chat & Trade",
            description = "Send encrypted messages and browse peer listings directly.",
            icon = Icons.Default.ShoppingBag,
            bullets = listOf(
                "End-to-end encrypted",
                "Optional auto-destruct timers",
                "Direct P2P listings"
            )
        ),
        GuideStep(
            stepNumber = 3,
            title = "Local & Secure",
            description = "All keys and data stay on your phone.",
            icon = Icons.Default.Lock,
            bullets = listOf(
                "No central servers",
                "No activity tracking",
                "One-tap panic wipe"
            )
        )
    )

    val currentStep = steps[currentStepIdx]
    val isLast = currentStepIdx == steps.lastIndex

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_guide_screen"),
        color = theme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Guide",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textSecondary
                )
                TextButton(onClick = onFinishGuide) {
                    Text("Skip", fontSize = 13.sp, color = theme.textMuted)
                }
            }

            // Step dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                steps.indices.forEach { idx ->
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (idx == currentStepIdx) 24.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentStepIdx) theme.accentPrimary else theme.border)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "guide_step_content",
                modifier = Modifier.weight(1f)
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.15f))
                            .border(1.5.dp, theme.borderGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Step ${step.stepNumber} of ${steps.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.accentPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = step.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.description,
                        fontSize = 13.sp,
                        color = theme.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, theme.border, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            step.bullets.forEach { text ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = theme.alertGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = text,
                                        fontSize = 12.5.sp,
                                        color = theme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStepIdx > 0) {
                    OutlinedButton(
                        onClick = { currentStepIdx-- },
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(theme.border)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back", fontSize = 13.sp, color = theme.textSecondary)
                    }
                }

                Button(
                    onClick = {
                        if (isLast) onFinishGuide() else currentStepIdx++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(if (currentStepIdx > 0) 1.5f else 1f)
                        .height(44.dp)
                        .testTag("guide_next_button")
                ) {
                    Text(
                        text = if (isLast) "Get Started" else "Next",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
