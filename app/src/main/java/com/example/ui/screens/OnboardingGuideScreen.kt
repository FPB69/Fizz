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
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalMedicalTheme

data class GuideStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val features: List<Pair<String, String>>,
    val protocolTag: String
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
            title = "Tor v3 Onion & Bridge Routing",
            subtitle = "Sovereign Multi-Hop Encrypted Circuitry",
            description = "All network traffic is strictly isolated within the Tor SOCKS5 proxy daemon. Your IP and physical location are decoupled through a 3 to 5 hop onion circuit with zero DNS leaks.",
            icon = Icons.Default.WifiTethering,
            features = listOf(
                "Pluggable Bridges" to "obfs4 noise scrambling & Snowflake WebRTC bypass DPI firewalls.",
                "Jurisdiction Filter" to "Optionally restrict routing strictly to Non-14-Eyes privacy nations.",
                "Tor Hidden Service" to "Direct .onion peer addressing with zero central DNS dependency."
            ),
            protocolTag = "TOR v3 • SOCKS5"
        ),
        GuideStep(
            stepNumber = 2,
            title = "4-Layer Cryptographic Cascade",
            subtitle = "Multi-Layer Sequential Cipher Armor",
            description = "Every payload is sequentially wrapped in multiple layers of military-grade encryption before touching the network wire.",
            icon = Icons.Default.Key,
            features = listOf(
                "Layer 1: AES-256-GCM" to "Galois/Counter Mode for authenticated payload privacy.",
                "Layer 2: ChaCha20-Poly1305" to "High-speed stream cipher defense against cryptographic breaks.",
                "Layer 3: Hybrid RSA & Signatures" to "Asymmetric handshake with tamper-evident HMAC.",
                "Layer 4: Zero-Knowledge Traffic Padding" to "Pads payloads to 1024-byte uniform chunks to defeat timing correlation attacks."
            ),
            protocolTag = "CASCADE 4-CIPHER"
        ),
        GuideStep(
            stepNumber = 3,
            title = "0-Cloud Storage & Auto-Destruct",
            subtitle = "Local-Only SQLite with Cryptographic Wipe",
            description = "Fizz.1 has NO servers and NO cloud databases. All listings and chat logs are stored strictly inside your phone's encrypted SQLite sandbox.",
            icon = Icons.Default.AutoDelete,
            features = listOf(
                "Auto-Destruct Timers" to "Set messages to vaporize from local storage after 10s, 1m, 1h, or 24h.",
                "Zero Cloud Footprint" to "No telemetry, no logs, and no analytics uploaded anywhere.",
                "Panic Data Purge" to "One-tap emergency wipe destroys all SQLite tables and zeros Keystore keys."
            ),
            protocolTag = "LOCAL SQLITE • AUTO-PURGE"
        ),
        GuideStep(
            stepNumber = 4,
            title = "Surveillance & Interception Defense",
            subtitle = "Real-Time Hop Latency & Threat Scanner",
            description = "Inspect live hop-by-hop latency and monitor for eavesdropping, DNS leaks, MITM attacks, or timing correlation surveillance in real-time.",
            icon = Icons.Default.Radar,
            features = listOf(
                "Real-Time Hop Ping" to "Live latency monitoring for Entry Guard, Middle Relays, and Exit.",
                "Interception Detector" to "Automated algorithmic scan alerts you if network anomaly occurs.",
                "Instant Circuit Renewal" to "One-tap Tor circuit rotation neutralizes any trace instantly."
            ),
            protocolTag = "THREAT SCANNER • 0-MITM"
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
            // Header with Skip option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FIZZ.1 ARCHITECTURAL GUIDE",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = theme.textSecondary
                )
                TextButton(onClick = onFinishGuide) {
                    Text("Skip Guide", fontSize = 12.sp, color = theme.textMuted)
                }
            }

            // Step Progress Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 10.dp)
            ) {
                steps.indices.forEach { idx ->
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (idx == currentStepIdx) 28.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (idx == currentStepIdx) theme.accentPrimary else theme.border)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Step Body
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
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.15f))
                            .border(1.5.dp, theme.borderGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "STEP ${step.stepNumber} OF 4",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = theme.accentPrimary
                    )

                    Text(
                        text = step.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = step.subtitle,
                        fontSize = 12.sp,
                        color = theme.accentPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = step.description,
                        fontSize = 11.5.sp,
                        color = theme.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key features card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, theme.border, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            step.features.forEach { (featTitle, featDesc) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = theme.alertGreen,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = featTitle,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary
                                        )
                                        Text(
                                            text = featDesc,
                                            fontSize = 10.5.sp,
                                            color = theme.textMuted,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Controls (Back / Next / Finish)
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
                        Text("Previous", fontSize = 12.sp, color = theme.textSecondary)
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
                        text = if (isLast) "Launch Sovereign Vault (Fizz.1)" else "Next Architectural Layer",
                        fontSize = 12.sp,
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
