package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
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
    val isOpenDyslexic by viewModel.isOpenDyslexic.collectAsStateWithLifecycle()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Settings & Security",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Tor status, keys, and display",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceElevated)
                                .border(1.dp, theme.borderGold, CircleShape)
                        ) {
                            Text(
                                text = if (theme.isDark) "🌙" else "☀️",
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // 1. Accessibility & Font Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, theme.borderGold, RoundedCornerShape(14.dp))
                        .testTag("opendyslexic_font_card"),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
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
                                        imageVector = Icons.Default.TextFields,
                                        contentDescription = null,
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "OpenDyslexic Font",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                    Text(
                                        text = if (isOpenDyslexic) "Enabled (wider spacing & bottom weighting)" else "Standard font",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isOpenDyslexic,
                                onCheckedChange = { viewModel.toggleOpenDyslexic() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = theme.background,
                                    checkedTrackColor = theme.accentPrimary
                                ),
                                modifier = Modifier.testTag("switch_opendyslexic")
                            )
                        }
                    }
                }
            }

            // 2. Peer Identity & QR Pairing
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(14.dp))
                        .testTag("p2p_qr_card"),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Onion Identity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.alertGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Ready",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.alertGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surface)
                                .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = viewModel.cryptoManager.myOnionAddress,
                                    fontSize = 11.5.sp,
                                    color = theme.accentPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Tor Onion", viewModel.cryptoManager.myOnionAddress)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.openPeerQrDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_show_qr"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.accentPrimary,
                                    contentColor = theme.background
                                )
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("My QR Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openPeerQrDialog() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scan_qr"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentPrimary)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(14.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pair Peer", fontSize = 12.sp, color = theme.accentPrimary)
                            }
                        }
                    }
                }
            }

            // 3. Tor Relays & Bridges
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tor Circuit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "Port ${torStatus.socksProxyPort} • 3 Hops",
                                    fontSize = 11.sp,
                                    color = theme.textSecondary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.rotateTorCircuit() },
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(theme.surface)
                                    .border(1.dp, theme.borderGold, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "New Circuit",
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val hops = torStatus.activeCircuitHops.ifEmpty {
                                listOf(
                                    com.example.data.network.TorCircuitHop("Entry", "Germany", "🇩🇪", "185.220.xxx", "Guard-DE", 42L),
                                    com.example.data.network.TorCircuitHop("Middle", "Iceland", "🇮🇸", "193.187.xxx", "Mid-IS", 88L),
                                    com.example.data.network.TorCircuitHop("Exit", "Switzerland", "🇨🇭", "179.43.xxx", "Exit-CH", 135L)
                                )
                            }

                            hops.forEachIndexed { index, hop ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.surface)
                                        .border(1.dp, theme.border, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "${hop.flag} ${hop.nodeType}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary, maxLines = 1)
                                        Text(text = "${hop.latencyMs}ms", fontSize = 9.sp, color = theme.accentPrimary)
                                    }
                                }
                                if (index < hops.size - 1) {
                                    Text("→", color = theme.textMuted, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Bridge Type",
                            fontSize = 11.5.sp,
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
                                TorBridgeType.MEEK_AZURE
                            )
                            items(bridges) { bridge ->
                                val isSelected = torStatus.bridgeType == bridge
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setBridgeType(bridge) },
                                    label = { Text(bridge.protocolTag, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = theme.accentPrimary,
                                        selectedLabelColor = theme.background,
                                        containerColor = theme.surface,
                                        labelColor = theme.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Encryption
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Encryption",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text("Active", fontSize = 11.sp, color = theme.alertGreen, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AES-256-GCM", fontSize = 12.sp, color = theme.textPrimary)
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
                            Text("ChaCha20-Poly1305", fontSize = 12.sp, color = theme.textPrimary)
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
                            Text("RSA-4096 Key Exchange", fontSize = 12.sp, color = theme.textPrimary)
                            Switch(
                                checked = encryptionConfig.useHybridRsaKeyExchange,
                                onCheckedChange = { viewModel.toggleHybridRsa() },
                                colors = SwitchDefaults.colors(checkedThumbColor = theme.background, checkedTrackColor = theme.accentPrimary)
                            )
                        }
                    }
                }
            }

            // 5. App Data & Reset
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Data Management",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.reopenLiabilityWaiver() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Terms", fontSize = 11.sp, color = theme.accentPrimary)
                            }

                            OutlinedButton(
                                onClick = { viewModel.reopenOnboardingGuide() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Guide", fontSize = 11.sp, color = theme.accentPrimary)
                            }
                        }

                        Button(
                            onClick = { showPanicDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.alertRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Erase All Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

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

    if (showLegalDisclaimer) {
        LegalDisclaimerDialog(onDismiss = { viewModel.dismissLegalDisclaimer() })
    }

    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            title = { Text("Erase All Local Data?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.alertRed) },
            text = {
                Text(
                    "This permanently deletes all chats, listings, and encryption keys from this phone.",
                    fontSize = 13.sp,
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
                    Text("Delete Everything", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("Cancel", color = theme.textMuted, fontSize = 12.sp)
                }
            },
            containerColor = theme.surfaceElevated,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
