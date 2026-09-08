package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
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
import com.example.ui.theme.LocalMedicalTheme

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
    val theme = LocalMedicalTheme.current
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
                .border(1.dp, theme.border, RoundedCornerShape(24.dp))
                .testTag("transparency_inspector_dialog"),
            color = theme.surface
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(theme.accentPrimary.copy(alpha = 0.12f))
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TRANSPARENCY INSPECTOR",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Live audit of device activity & Tor proxying",
                                fontSize = 11.sp,
                                color = theme.accentPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_transparency_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    text = "Activity Audit Log (${filteredEvents.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
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
                                            tint = theme.textMuted,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear", fontSize = 10.5.sp, color = theme.textMuted)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(bottom = 2.dp)
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedCategoryFilter == null,
                                        onClick = { selectedCategoryFilter = null },
                                        label = { Text("All Actions", fontSize = 10.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = theme.accentPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = theme.surfaceElevated,
                                            labelColor = theme.textSecondary
                                        )
                                    )
                                }

                                items(TransparencyCategory.values()) { cat ->
                                    FilterChip(
                                        selected = selectedCategoryFilter == cat,
                                        onClick = {
                                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                        },
                                        label = { Text("${cat.emoji} ${cat.label}", fontSize = 10.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = theme.accentPrimary,
                                            selectedLabelColor = Color.White,
                                            containerColor = theme.surfaceElevated,
                                            labelColor = theme.textSecondary
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
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No events logged for this category",
                                    fontSize = 11.5.sp,
                                    color = theme.textMuted
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
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = theme.accentPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Tor Socket", fontSize = 11.5.sp, color = theme.accentPrimary)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dismiss", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
    val theme = LocalMedicalTheme.current
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
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
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
                            .size(9.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(if (activityState.isWorking) theme.accentSecondary else theme.alertGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CURRENT VITALS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = theme.accentPrimary,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(theme.accentPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${activityState.category.emoji} ${activityState.category.label}",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activityState.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = activityState.subtitle,
                fontSize = 11.5.sp,
                color = theme.textSecondary,
                lineHeight = 15.sp
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
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = theme.alertGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zero-Cloud Architecture Audit", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Your Device", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = theme.accentPrimary)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("100% Local", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)
                        Text("$myListingsCount listings • SQLite", fontSize = 8.5.sp, color = theme.textMuted)
                    }
                }

                // Box 2: Central Servers
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = theme.alertGreen, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cloud Servers", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = theme.alertGreen)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("0 Bytes", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.alertGreen)
                        Text("Zero external DB", fontSize = 8.5.sp, color = theme.textMuted)
                    }
                }

                // Box 3: Tor Transit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = theme.accentSecondary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tor Tunnel", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = theme.accentSecondary)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("Encrypted", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = theme.accentSecondary)
                        Text("Port ${torStatus.socksProxyPort}", fontSize = 8.5.sp, color = theme.textMuted)
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
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.category.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.action,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.timeFormatted,
                        fontSize = 9.5.sp,
                        color = theme.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Expand details",
                        tint = theme.textMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = event.description,
                fontSize = 11.sp,
                color = theme.textSecondary,
                lineHeight = 14.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Technical Details:",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.technicalDetails,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = theme.textPrimary,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
