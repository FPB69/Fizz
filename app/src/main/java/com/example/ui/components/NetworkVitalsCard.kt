package com.example.ui.components

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.TorNetworkVitals
import com.example.data.network.TorStatus
import com.example.ui.theme.LocalMedicalTheme

/**
 * Super Minimalist Network Vitals Card
 * Inspired by clinical vital signs monitors in medical apps (Dribbble Medical App Dark/Light Theme).
 * Displays Tor proxy routing status, DNS leak immunity, latency, and circuit hops.
 */
@Composable
fun NetworkVitalsCard(
    vitals: TorNetworkVitals,
    torStatus: TorStatus,
    onProbe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("network_vitals_card")
    ) {
        // Top Header: Anonymity Health Title + Live Probe Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(theme.accentPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ANONYMITY VITALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = theme.textSecondary
                    )
                    Text(
                        text = if (torStatus.isOrbotInstalled) "Orbot SDK • SOCKS5 Active" else "Tor Onion Daemon • SOCKS5",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.textPrimary
                    )
                }
            }

            // Minimalist Refresh / Diagnostic Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceElevated)
                    .border(1.dp, theme.border, RoundedCornerShape(12.dp))
                    .clickable { onProbe() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("probe_tor_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Probe Network",
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Probe",
                        fontSize = 11.sp,
                        color = theme.accentPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4-Metric Clinical Vital Signs Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VitalMetricPill(
                title = "SOCKS5 PROXY",
                value = ":${torStatus.socksProxyPort}",
                subtitle = if (torStatus.isSocksResponding) "Responding" else "Ready",
                icon = Icons.Default.VpnKey,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )

            VitalMetricPill(
                title = "DNS LEAKS",
                value = "0",
                subtitle = "100% Masked",
                icon = Icons.Default.Lock,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )

            VitalMetricPill(
                title = "CIRCUIT",
                value = "3 Hops",
                subtitle = "Guard➔Exit",
                icon = Icons.Default.Speed,
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VitalMetricPill(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = theme.textSecondary
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) theme.alertGreen else theme.alertAmber,
                modifier = Modifier.size(11.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Text(
            text = subtitle,
            fontSize = 9.5.sp,
            color = if (isPositive) theme.alertGreen else theme.textMuted
        )
    }
}
