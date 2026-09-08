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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.ConnectionMode
import com.example.data.network.OrbotState
import com.example.data.network.TorStatus
import com.example.ui.theme.BubbleAquaLight
import com.example.ui.theme.BubbleAquaPrimary
import com.example.ui.theme.BubbleCarbonationGreen
import com.example.ui.theme.BubbleCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun TorStatusBadge(
    status: TorStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        when {
            status.orbotState == OrbotState.RUNNING || status.isTorNetworkVerified -> BubbleCarbonationGreen
            status.isSocksResponding -> BubbleCyan
            status.orbotState == OrbotState.STARTING -> BubbleAquaLight
            status.isConnected -> BubbleCarbonationGreen
            else -> Color.Gray
        },
        label = "tor_color"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceElevated.copy(alpha = 0.9f))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("tor_status_badge"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Glowing effervescent bubble indicator
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )

        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Tor Security",
            tint = BubbleAquaPrimary,
            modifier = Modifier.size(14.dp)
        )

        val label = if (status.connectionMode == ConnectionMode.TOR_ONION_ROUTING) {
            if (status.orbotState == OrbotState.RUNNING) "Tor / Orbot Online" else "Tor Onion: 3 Hops"
        } else {
            "Direct P2P LAN"
        }

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BubbleAquaLight
        )
    }
}

