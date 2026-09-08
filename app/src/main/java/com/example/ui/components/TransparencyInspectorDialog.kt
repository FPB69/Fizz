package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.network.TorStatus
import com.example.data.transparency.CurrentActivityState
import com.example.data.transparency.TransparencyCategory
import com.example.data.transparency.TransparencyEvent
import com.example.ui.theme.BubbleAquaDark
import com.example.ui.theme.BubbleAquaLight
import com.example.ui.theme.BubbleAquaPrimary
import com.example.ui.theme.BubbleCarbonationGreen
import com.example.ui.theme.BubbleCyan
import com.example.ui.theme.BubbleFoamWhite
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun TransparencyInspectorDialog(
    activityState: CurrentActivityState,
    events: List<TransparencyEvent>,
    torStatus: TorStatus,
    myListingsCount: Int,
    peerListingsCount: Int,
    onProbeTor: () -> Unit,
    onClearLog: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<TransparencyCategory?>(null) }
    var expandedEventId by remember { mutableStateOf<String?>(null) }

    val filteredEvents = remember(events, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) events
        else events.filter { it.category == selectedCategoryFilter }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BubbleAquaPrimary.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .testTag("transparency_inspector_dialog"),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BubbleAquaDark)
                                .border(1.dp, BubbleAquaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🫧", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Transparency Inspector",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = BubbleFoamWhite
                            )
                            Text(
                                text = "Live audit of what Fizz is doing",
                                fontSize = 12.sp,
                                color = BubbleAquaLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_transparency_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Current Live Status Card
                    item {
                        CurrentStatusCard(activityState = activityState, torStatus = torStatus)
                    }

                    // 2. Data Storage & Privacy Reality Card (Zero Cloud Proof)
                    item {
                        DataStorageRealityCard(
                            myListingsCount = myListingsCount,
                            peerListingsCount = peerListingsCount,
                            torStatus = torStatus
                        )
                    }

                    // 3. Filter Category Chips
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Activity Audit Log (${filteredEvents.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BubbleFoamWhite
                                )

                                if (events.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { onClearLog() }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteSweep,
                                            contentDescription = "Clear Log",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 4.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryFilter == null,
                                        onClick = { selectedCategoryFilter = null },
                                        label = { Text("All Actions", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BubbleAquaPrimary,
                                            selectedLabelColor = DarkBackground,
                                            containerColor = DarkSurfaceElevated,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }

                                items(TransparencyCategory.values()) { cat ->
                                    FilterChip(
                                        selected = selectedCategoryFilter == cat,
                                        onClick = {
                                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                        },
                                        label = { Text("${cat.emoji} ${cat.label}", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = BubbleAquaPrimary,
                                            selectedLabelColor = DarkBackground,
                                            containerColor = DarkSurfaceElevated,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 4. Audit Log Items
                    if (filteredEvents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No events logged for this category",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        items(filteredEvents, key = { it.id }) { event ->
                            val isExpanded = expandedEventId == event.id
                            TransparencyEventCard(
                                event = event,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedEventId = if (isExpanded) null else event.id
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onProbeTor,
                        colors = ButtonDefaults.buttonColors(containerColor = BubbleAquaDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = BubbleCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Tor Socket", fontSize = 12.sp, color = BubbleCyan)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = BubbleAquaPrimary, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Got it", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentStatusCard(
    activityState: CurrentActivityState,
    torStatus: TorStatus
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (activityState.isWorking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BubbleAquaPrimary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (activityState.isWorking) BubbleCyan else BubbleCarbonationGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RIGHT NOW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BubbleAquaLight,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BubbleAquaDark)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${activityState.category.emoji} ${activityState.category.label}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BubbleCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activityState.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BubbleFoamWhite
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = activityState.subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DataStorageRealityCard(
    myListingsCount: Int,
    peerListingsCount: Int,
    torStatus: TorStatus
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = BubbleCarbonationGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zero-Cloud Architecture Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BubbleFoamWhite)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Columns: This Phone vs Cloud vs Network
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Box 1: On this phone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = BubbleAquaPrimary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Your Phone", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BubbleAquaPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("100% Data", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BubbleFoamWhite)
                        Text("$myListingsCount listings • SQLite DB", fontSize = 9.sp, color = TextMuted)
                    }
                }

                // Box 2: Central Servers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = BubbleCarbonationGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cloud Servers", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BubbleCarbonationGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("0 Bytes", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BubbleCarbonationGreen)
                        Text("Zero logs • No accounts", fontSize = 9.sp, color = TextMuted)
                    }
                }

                // Box 3: Tor Transit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BubbleCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tor Tunnel", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BubbleCyan)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Encrypted", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BubbleCyan)
                        Text("Port ${torStatus.socksProxyPort} • 3 Hops", fontSize = 9.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransparencyEventCard(
    event: TransparencyEvent,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.category.emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.action,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BubbleFoamWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.timeFormatted,
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expand details",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Technical Details:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BubbleAquaPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.technicalDetails,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BubbleFoamWhite,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
