package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TorPeerViewModel

@Composable
fun ConnectScreen(viewModel: TorPeerViewModel) {
    val theme = LocalMedicalTheme.current
    val isConnected by viewModel.isNetworkConnected.collectAsStateWithLifecycle()
    val isOtrEnabled by viewModel.isOtrModeEnabled.collectAsStateWithLifecycle()
    val isStorageGranted by viewModel.isPersistentStorageGranted.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Fizz 1.7 Control Center",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentPrimary
                    )
                    Text(
                        text = "Manual Boot, Master Process Kill Switch & Storage Access",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )
                }
            }

            // Master Network Boot & Kill Switch Main Button
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            if (isConnected) theme.alertRed else theme.accentPrimary,
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isConnected) theme.alertRed.copy(alpha = 0.15f)
                                    else theme.accentPrimary.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Master Power Control",
                                tint = if (isConnected) theme.alertRed else theme.accentPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = if (isConnected) "P2P Network ACTIVE" else "Network Offline (Default)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) theme.alertGreen else theme.textSecondary
                        )

                        Text(
                            text = if (isConnected)
                                "Pressing the red button below will completely terminate all background threads, close sockets, and shut down the app."
                            else
                                "App boots completely offline. Tap the button below to manually start the Tor SOCKS5 proxy and local P2P listeners.",
                            fontSize = 12.sp,
                            color = theme.textMuted,
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = { viewModel.toggleManualNetworkConnect() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) theme.alertRed else theme.accentPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_master_connect_kill")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isConnected) "KILL ALL PROCESSES & CLOSE APP" else "MANUALLY CONNECT TOR / P2P NETWORK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Persistent Local Storage Permission Request
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdCard,
                                contentDescription = "Persistent Storage",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Persistent Storage Request",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        Text(
                            text = "To preserve encrypted chats, offline drafts, and local store listings when navigating away or restarting the device, enable persistent local SQLite sandbox storage.",
                            fontSize = 12.sp,
                            color = theme.textSecondary,
                            lineHeight = 16.sp
                        )

                        if (!isStorageGranted) {
                            Button(
                                onClick = { viewModel.requestPersistentStorage() },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_grant_persistent_storage")
                            ) {
                                Text("Grant Persistent Local Storage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.alertGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✅ Persistent SQLite Storage Active",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.alertGreen
                                )
                            }
                        }
                    }
                }
            }

            // Emergency Panic Wipe Button Location Box
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.alertRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Panic Wipe Location",
                                tint = theme.alertRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panic Wipe Button Location Guide",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.alertRed
                            )
                        }

                        Text(
                            text = "If device physical security is compromised, navigate to the 'Tor Vault' tab (bottom right) and scroll down to 'Emergency Data Purge'. Tap the red 'PANIC WIPE ALL DATA' button to zero out all SQLite records and cryptographic keys instantly.",
                            fontSize = 12.sp,
                            color = theme.textSecondary,
                            lineHeight = 16.sp
                        )

                        OutlinedButton(
                            onClick = { viewModel.setNavTab(AppNavTab.SECURITY) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_go_to_panic_wipe")
                        ) {
                            Text("Go to Tor Vault Tab (Panic Wipe)", fontSize = 11.5.sp, color = theme.textPrimary)
                        }
                    }
                }
            }

            // Cryptographic Protocols Overview (PGP, OTR, EXIF Scrubber)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Security Protocols Active in Fizz 1.7",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )

                        // OTR Protocol Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Off-The-Record (OTR v3) Forward Secrecy",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = "Ephemeral Diffie-Hellman key rotation per message session",
                                    fontSize = 11.sp,
                                    color = theme.textMuted
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.toggleOtrMode() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_toggle_otr")
                            ) {
                                Text(
                                    text = if (isOtrEnabled) "OTR ON" else "PGP ONLY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOtrEnabled) theme.accentPrimary else theme.textMuted
                                )
                            }
                        }

                        // PGP ASCII Armor
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PGP ASCII Armor wrapping enabled for all message payloads",
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }

                        // Automatic EXIF Photo Scrubber
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automatic EXIF Scrubber removes GPS location & camera model metadata",
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
