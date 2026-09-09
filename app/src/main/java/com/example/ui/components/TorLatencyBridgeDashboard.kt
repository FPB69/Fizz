package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.BridgeHealthStatus
import com.example.data.network.CircuitHopTelemetry
import com.example.data.network.SurveillanceThreatReport
import com.example.data.network.ThreatLevel
import com.example.data.network.TorBridgeType
import com.example.data.network.TorStatus
import com.example.ui.theme.LocalMedicalTheme

@Composable
fun TorLatencyBridgeDashboard(
    torStatus: TorStatus,
    onRunScan: () -> Unit,
    onRotateCircuit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    var showDetailedVectorModal by remember { mutableStateOf(false) }
    var expandedHopIdx by remember { mutableStateOf<Int?>(null) }

    val threatReport = torStatus.threatReport
    val bridgeHealth = torStatus.bridgeHealth
    val telemetryHops = torStatus.telemetryHops

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surface)
            .border(1.dp, theme.borderGold, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("tor_latency_bridge_dashboard")
    ) {
        // Section 1: Dashboard Header & Latency Speedometer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(theme.accentPrimary.copy(alpha = 0.15f))
                        .border(1.dp, theme.borderGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = null,
                        tint = theme.accentPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .scale(pulseScale)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TOR ROUTING & BRIDGE HEALTH",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Real-Time Hop Latency & Threat Monitor",
                        fontSize = 10.5.sp,
                        color = theme.accentPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Live Scan CTA button
            Button(
                onClick = onRunScan,
                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated, contentColor = theme.accentPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .border(1.dp, theme.borderGold, RoundedCornerShape(10.dp))
                    .testTag("run_surveillance_scan_button")
            ) {
                if (torStatus.isScanningSurveillance) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = theme.accentPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scanning...", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deep Scan", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Surveillance & Interception Detection Banner (CRITICAL REQUIREMENT)
        SurveillanceInterceptionBanner(
            report = threatReport,
            isScanning = torStatus.isScanningSurveillance,
            onToggleDetails = { showDetailedVectorModal = !showDetailedVectorModal },
            onRotateCircuit = onRotateCircuit,
            isExpanded = showDetailedVectorModal
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: High-Level Real-Time Network Vitals (Latency, Jitter, Packet Loss, Anonymity)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricGlancePill(
                title = "TOTAL LATENCY",
                value = "${threatReport.totalRoundTripLatencyMs} ms",
                subtitle = "Round-Trip Ping",
                color = if (threatReport.totalRoundTripLatencyMs < 160) theme.alertGreen else theme.alertAmber,
                modifier = Modifier.weight(1f)
            )

            MetricGlancePill(
                title = "PACKET JITTER",
                value = "±${threatReport.averageJitterMs} ms",
                subtitle = "Loss: 0.0%",
                color = theme.alertGreen,
                modifier = Modifier.weight(1f)
            )

            MetricGlancePill(
                title = "SHIELD SCORE",
                value = "${threatReport.anonymityIntegrityScore}%",
                subtitle = "Clean Circuit",
                color = theme.accentPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: Active Bridge Connection Telemetry
        BridgeConnectionCard(bridgeHealth = bridgeHealth, currentBridge = torStatus.bridgeType)

        Spacer(modifier = Modifier.height(14.dp))

        // Section 5: Hop-by-Hop Circuit Latency Breakdown
        Text(
            text = "CIRCUIT HOPS & REAL-TIME NODE PING",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = theme.textSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            telemetryHops.forEachIndexed { index, hop ->
                CircuitHopLatencyRow(
                    hop = hop,
                    isExpanded = expandedHopIdx == index,
                    onToggle = {
                        expandedHopIdx = if (expandedHopIdx == index) null else index
                    }
                )
            }
        }
    }
}

@Composable
fun SurveillanceInterceptionBanner(
    report: SurveillanceThreatReport,
    isScanning: Boolean,
    onToggleDetails: () -> Unit,
    onRotateCircuit: () -> Unit,
    isExpanded: Boolean
) {
    val theme = LocalMedicalTheme.current

    val bannerBg = when (report.threatLevel) {
        ThreatLevel.SECURE_CLEAN -> theme.alertGreen.copy(alpha = 0.08f)
        ThreatLevel.ELEVATED_OBSERVATION -> theme.alertAmber.copy(alpha = 0.08f)
        ThreatLevel.INTERCEPTION_WARNING -> theme.alertRed.copy(alpha = 0.12f)
    }

    val bannerBorder = when (report.threatLevel) {
        ThreatLevel.SECURE_CLEAN -> theme.alertGreen.copy(alpha = 0.4f)
        ThreatLevel.ELEVATED_OBSERVATION -> theme.alertAmber.copy(alpha = 0.4f)
        ThreatLevel.INTERCEPTION_WARNING -> theme.alertRed
    }

    val statusIcon = when (report.threatLevel) {
        ThreatLevel.SECURE_CLEAN -> Icons.Default.Shield
        ThreatLevel.ELEVATED_OBSERVATION -> Icons.Default.Info
        ThreatLevel.INTERCEPTION_WARNING -> Icons.Default.Warning
    }

    val statusTint = when (report.threatLevel) {
        ThreatLevel.SECURE_CLEAN -> theme.alertGreen
        ThreatLevel.ELEVATED_OBSERVATION -> theme.alertAmber
        ThreatLevel.INTERCEPTION_WARNING -> theme.alertRed
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, bannerBorder, RoundedCornerShape(14.dp))
            .testTag("surveillance_interception_banner")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (report.isFollowedOrIntercepted) "WARNING: INTERCEPTION / SURVEILLANCE RISK" else "SURVEILLANCE CHECK: 100% CLEAN",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusTint
                        )
                        Text(
                            text = if (report.isFollowedOrIntercepted) "Adversarial correlation or packet anomaly detected" else "Zero packet sniffing, eavesdropping, or tracking detected",
                            fontSize = 10.sp,
                            color = theme.textPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.surfaceElevated)
                        .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleDetails)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isExpanded) "Hide Vectors" else "Inspect",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accentPrimary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "REAL-TIME ADVERSARIAL THREAT VECTORS:",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = theme.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    report.vectorChecks.forEach { check ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (check.isSecure) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (check.isSecure) theme.alertGreen else theme.alertAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = check.name,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = check.technicalMetric,
                                    fontSize = 9.5.sp,
                                    color = theme.textSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onRotateCircuit,
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rotate & Neutralize Trace", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BridgeConnectionCard(
    bridgeHealth: BridgeHealthStatus,
    currentBridge: TorBridgeType
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLUGGABLE BRIDGE TUNNEL",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = theme.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.borderGold, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentBridge.protocolTag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Protocol Armor", fontSize = 9.sp, color = theme.textMuted)
                    Text(text = bridgeHealth.obfuscationLevel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Throughput", fontSize = 9.sp, color = theme.textMuted)
                    Text(
                        text = "${String.format("%.1f", bridgeHealth.throughputKbps)} KB/s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.alertGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { (bridgeHealth.throughputKbps / 300f).coerceIn(0.1f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = theme.accentPrimary,
                trackColor = theme.border
            )
        }
    }
}

@Composable
fun CircuitHopLatencyRow(
    hop: CircuitHopTelemetry,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val theme = LocalMedicalTheme.current

    val pingColor = when {
        hop.latencyMs < 35 -> theme.alertGreen
        hop.latencyMs < 75 -> theme.accentPrimary
        else -> theme.alertAmber
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isExpanded) theme.borderGold else theme.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onToggle)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = hop.flag, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Hop ${hop.hopIndex}: ${hop.nodeType}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            if (hop.isBridgeTransport) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(theme.accentPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = hop.bridgeProtocol,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accentPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${hop.nickname} • ${hop.country}",
                            fontSize = 9.5.sp,
                            color = theme.textMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surface)
                            .border(1.dp, pingColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${hop.latencyMs} ms",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = pingColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = theme.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(theme.border)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Autonomous System:", fontSize = 9.sp, color = theme.textMuted)
                        Text(text = hop.asn, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = theme.textPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Jurisdiction Filter:", fontSize = 9.sp, color = theme.textMuted)
                        Text(
                            text = if (hop.is14EyesJurisdiction) "14-Eyes Alliance (Filtered if Strict)" else "Non-14-Eyes Safe",
                            fontSize = 9.5.sp,
                            color = if (hop.is14EyesJurisdiction) theme.alertAmber else theme.alertGreen
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Masked Relay IP:", fontSize = 9.sp, color = theme.textMuted)
                        Text(text = hop.ipMasked, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = theme.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricGlancePill(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
            color = theme.textSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = subtitle,
            fontSize = 8.5.sp,
            color = theme.textMuted
        )
    }
}
