package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.ConnectionMode
import com.example.data.network.OrbotState
import com.example.data.network.TorStatus
import com.example.ui.theme.LocalMedicalTheme

/**
 * Super Minimalist Tor Status Badge
 * Medical style pill showing Tor proxy routing state.
 */
@Composable
fun TorStatusBadge(
    status: TorStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    val indicatorColor by animateColorAsState(
        when {
            status.orbotState == OrbotState.RUNNING || status.isTorNetworkVerified -> theme.alertGreen
            status.isSocksResponding -> theme.accentPrimary
            status.orbotState == OrbotState.STARTING -> theme.alertAmber
            status.isConnected -> theme.alertGreen
            else -> theme.textMuted
        },
        label = "tor_color"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surface)
            .border(1.dp, theme.border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp)
            .testTag("tor_status_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Heartbeat dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )

        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Tor Security",
            tint = theme.accentPrimary,
            modifier = Modifier.size(13.dp)
        )

        val label = if (status.connectionMode == ConnectionMode.TOR_ONION_ROUTING) {
            if (status.orbotState == OrbotState.RUNNING) "Orbot Active" else "Tor 3-Hop"
        } else {
            "Direct LAN"
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.textPrimary
        )
    }
}
