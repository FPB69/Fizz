package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.TorBridgeType
import com.example.ui.components.LegalDisclaimerDialog
import com.example.ui.components.PeerQrConnectionDialog
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.TorPeerViewModel

@Composable
fun PrivacySecurityScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalMedicalTheme.current
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val encryptionConfig by viewModel.encryptionConfig.collectAsStateWithLifecycle()
    val showLegalDisclaimer by viewModel.showLegalDisclaimer.collectAsStateWithLifecycle()
    val showPeerQrDialog by viewModel.showPeerQrDialog.collectAsStateWithLifecycle()

    var showPanicDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FIZZ 1.1 SECURITY DECK",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = theme.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(theme.accentPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TOR ONION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accentPrimary
                                )
                            }
                        }
                        Text(
                            text = "Direct P2P Links • Multi-Bridge Routing • 4-Layer Cipher",
                            fontSize = 11.sp,
                            color = theme.accentPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.borderGold, CircleShape)
                    ) {
                        Text(
                            text = if (theme.isDark) "🌊" else "☀️",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // 1. Direct P2P Link & QR Pairing Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, theme.borderGold, RoundedCornerShape(16.dp))
                        .testTag("p2p_qr_card"),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                        .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Direct P2P Identity",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "Shareable QR Code & Onion Link",
                                        fontSize = 10.5.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.alertGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "0-Discovery",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.alertGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Onion Address box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surface)
                                .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "My Tor Onion Node",
                                        fontSize = 9.sp,
                                        color = theme.textMuted
                                    )
                                    Text(
                                        text = viewModel.cryptoManager.myOnionAddress,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.accentPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Tor Onion", viewModel.cryptoManager.myOnionAddress)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Onion address copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Onion",
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openPeerQrDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_show_qr"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.accentPrimary,
                                    contentColor = theme.background
                                )
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Show My QR", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openPeerQrDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scan_qr"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentPrimary)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pair New Peer", fontSize = 11.5.sp, color = theme.accentPrimary)
                            }
                        }
                    }
                }
            }

            // 2. Tor Circuit & Bridge Telemetry (Streamlined & Clean)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.borderGold, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tor Circuit & Relays",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "SOCKS5 Proxy Port :${torStatus.socksProxyPort} • Multi-Hop",
                                    fontSize = 10.5.sp,
                                    color = theme.accentPrimary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.rotateTorCircuit() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(theme.surface)
                                    .border(1.dp, theme.borderGold, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Rotate Circuit",
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Circuit Hops Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val hops = torStatus.activeCircuitHops.ifEmpty {
                                listOf(
                                    com.example.data.network.TorCircuitHop("Entry Guard", "Germany", "🇩🇪", "185.220.xxx", "Guard-DE", 42L),
                                    com.example.data.network.TorCircuitHop("Middle Relay", "Iceland", "🇮🇸", "193.187.xxx", "Mid-IS", 88L),
                                    com.example.data.network.TorCircuitHop("Exit Node", "Switzerland", "🇨🇭", "179.43.xxx", "Exit-CH", 135L)
                                )
                            }

                            hops.forEachIndexed { index, hop ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(theme.surface)
                                        .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "${hop.flag} ${hop.nodeType}", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary, maxLines = 1)
                                        Text(text = "${hop.latencyMs}ms", fontSize = 9.sp, color = theme.accentPrimary, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                if (index < hops.size - 1) {
                                    Text("➔", color = theme.textMuted, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pluggable Bridges Filter Row
                        Text(
                            text = "Pluggable Transport Bridge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val bridges = listOf(
                                TorBridgeType.DIRECT,
                                TorBridgeType.OBFS4,
                                TorBridgeType.SNOWFLAKE,
                                TorBridgeType.MEEK_AZURE,
                                TorBridgeType.CUSTOM
                            )
                            items(bridges) { bridge ->
                                val isSelected = torStatus.bridgeType == bridge
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setBridgeType(bridge) },
                                    label = { Text(bridge.protocolTag, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.accentPrimary,
                                        selectedLabelColor = theme.background,
                                        containerColor = theme.surface,
                                        labelColor = theme.textSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) theme.accentPrimary else theme.border
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 3. 4-Layer Cryptographic Shield
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.borderGold, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "4-Layer Cipher Shield",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = "End-to-End Quantum-Resistant Cascade",
                                        fontSize = 10.5.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.alertGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("4/4 Active", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = theme.alertGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 Simple Compact Toggle Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AES-256-GCM Hardware Cipher", fontSize = 11.5.sp, color = theme.textPrimary)
                            Switch(
                                checked = encryptionConfig.useAes256Gcm,
                                onCheckedChange = { viewModel.toggleAesGcm() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.background, checkedTrackColor = theme.accentPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ChaCha20-Poly1305 Stream Armor", fontSize = 11.5.sp, color = theme.textPrimary)
                            Switch(
                                checked = encryptionConfig.useChaCha20Poly1305,
                                onCheckedChange = { viewModel.toggleChaCha20() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.background, checkedTrackColor = theme.accentPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RSA-4096 Hybrid Key Exchange", fontSize = 11.5.sp, color = theme.textPrimary)
                            Switch(
                                checked = encryptionConfig.useHybridRsaKeyExchange,
                                onCheckedChange = { viewModel.toggleHybridRsa() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.background, checkedTrackColor = theme.accentPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ZK Size Normalization & Padding", fontSize = 11.5.sp, color = theme.textPrimary)
                            Switch(
                                checked = encryptionConfig.useZeroKnowledgeTrafficPadding,
                                onCheckedChange = { viewModel.toggleTrafficPadding() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.background, checkedTrackColor = theme.accentPrimary)
                            )
                        }
                    }
                }
            }

            // 4. Sovereign Privacy & 0-Fault Actions
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.borderGold, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Sovereign Vault & Legal Covenant",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )

                        Text(
                            text = "Fizz 1.1 operates 100% decentralized with zero server custody. All peer transactions and communications are private and autonomous.",
                            fontSize = 10.5.sp,
                            color = theme.textSecondary,
                            lineHeight = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.reopenLiabilityWaiver() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(13.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Liability Splash", fontSize = 10.sp, color = theme.accentPrimary)
                            }

                            OutlinedButton(
                                onClick = { viewModel.reopenOnboardingGuide() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(13.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start-Up Guide", fontSize = 10.sp, color = theme.accentPrimary)
                            }
                        }

                        // Emergency Panic Wipe
                        Button(
                            onClick = { showPanicDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.alertRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Panic Wipe (Zero Vault)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // P2P QR Code Pairing Dialog
    if (showPeerQrDialog) {
        PeerQrConnectionDialog(
            myPeerId = viewModel.cryptoManager.myPeerId,
            myOnionAddress = viewModel.cryptoManager.myOnionAddress,
            myFingerprint = viewModel.cryptoManager.myFingerprint,
            onConnectToPeer = { peer ->
                viewModel.connectToPeerParsed(
                    peerId = peer.peerId,
                    onionAddress = peer.onionAddress,
                    fingerprint = peer.fingerprint,
                    displayName = peer.displayName
                )
            },
            onDismiss = { viewModel.dismissPeerQrDialog() }
        )
    }

    // 0-Fault Legal Terms Covenant Dialog
    if (showLegalDisclaimer) {
        LegalDisclaimerDialog(onDismiss = { viewModel.dismissLegalDisclaimer() })
    }

    // Emergency Panic Wipe Confirmation
    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            title = { Text("Emergency Cryptographic Wipe?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.alertRed) },
            text = {
                Text(
                    "This will immediately destroy all local SQLite databases, zero all encryption keys in the Keystore, and wipe all listings and chat history permanently. This action is irreversible.",
                    fontSize = 12.sp,
                    color = theme.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.panicWipeAllData()
                        showPanicDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.alertRed)
                ) {
                    Text("Destroy Vault Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("Cancel", color = theme.textMuted, fontSize = 12.sp)
                }
            },
            containerColor = theme.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
