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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.ConnectionMode
import com.example.data.network.OrbotState
import com.example.ui.components.NetworkVitalsCard
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.TorPeerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacySecurityScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalMedicalTheme.current
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val networkVitals by viewModel.networkVitals.collectAsStateWithLifecycle()
    val myListings by viewModel.myListings.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val transparencyLogs by viewModel.transparencyLogs.collectAsStateWithLifecycle()

    var showPanicDialog by remember { mutableStateOf(false) }
    var proxyHostInput by remember { mutableStateOf(torStatus.socksProxyHost) }
    var proxyPortInput by remember { mutableStateOf(torStatus.socksProxyPort.toString()) }

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
                        Text(
                            text = "SECURITY & PRIVACY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Orbot Tor Routing • 100% Local Sandbox Storage",
                            fontSize = 11.5.sp,
                            color = theme.accentPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.12f))
                            .border(1.dp, theme.accentPrimary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Real-time Medical Network Vitals Card
            item {
                NetworkVitalsCard(
                    vitals = networkVitals,
                    torStatus = torStatus,
                    onProbe = { viewModel.probeTorConnectivity() }
                )
            }

            // Tor Circuit Visualizer Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Router, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Active Tor Circuit (3 Hops)", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.textPrimary)
                            }

                            OutlinedButton(
                                onClick = { viewModel.rotateTorCircuit() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("rotate_circuit_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Circuit", fontSize = 11.sp, color = theme.accentPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hop steps
                        torStatus.activeCircuitHops.forEachIndexed { index, hop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.surfaceElevated)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accentPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = hop.flag, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = hop.nodeType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                                    }
                                    Text(
                                        text = "${hop.nickname} • ${hop.ipMasked}",
                                        fontSize = 10.5.sp,
                                        color = theme.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = "${hop.latencyMs}ms",
                                    fontSize = 11.sp,
                                    color = theme.accentSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (index < torStatus.activeCircuitHops.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 22.dp)
                                        .width(2.dp)
                                        .height(6.dp)
                                        .background(theme.border)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connection Mode Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tor Onion Routing",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
                                )
                                Text(
                                    text = if (torStatus.connectionMode == ConnectionMode.TOR_ONION_ROUTING) "All traffic routed over Tor" else "Direct LAN P2P Mode",
                                    fontSize = 10.5.sp,
                                    color = theme.textSecondary
                                )
                            }

                            Switch(
                                checked = torStatus.connectionMode == ConnectionMode.TOR_ONION_ROUTING,
                                onCheckedChange = { isTor ->
                                    viewModel.setConnectionMode(
                                        if (isTor) ConnectionMode.TOR_ONION_ROUTING else ConnectionMode.DIRECT_P2P_WIFI_LAN
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = theme.accentPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Cryptographic Identity Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cryptographic Node Identity", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.textPrimary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Onion Address
                        Text("Permanent Onion v3 Address:", fontSize = 11.sp, color = theme.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceElevated)
                                .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.cryptoManager.myOnionAddress,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = theme.accentPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cb.setPrimaryClip(ClipData.newPlainText("Onion", viewModel.cryptoManager.myOnionAddress))
                                    Toast.makeText(context, "Copied Onion Address", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(15.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Public Key Fingerprint
                        Text("RSA-2048 / SHA-256 Fingerprint:", fontSize = 11.sp, color = theme.textSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surfaceElevated)
                                .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = viewModel.cryptoManager.myFingerprint,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = theme.alertGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Zero Cloud Local Storage Proof Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = theme.alertGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Local Storage & Zero Cloud Audit", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.textPrimary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Every byte of this application is stored strictly in your phone's internal storage sandbox. No Firebase, AWS, or third-party database is connected.",
                            fontSize = 11.5.sp,
                            color = theme.textSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StorageStatBox(
                                label = "Phone Listings",
                                value = "${myListings.size}",
                                modifier = Modifier.weight(1f)
                            )
                            StorageStatBox(
                                label = "Encrypted Msgs",
                                value = "${allMessages.size}",
                                modifier = Modifier.weight(1f)
                            )
                            StorageStatBox(
                                label = "Cloud Uploads",
                                value = "0 Bytes",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Orbot Service & Tor Proxy Configuration Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Orbot Service & SOCKS5 Proxy", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.textPrimary)
                            }

                            // Status badge
                            val badgeColor = when (torStatus.orbotState) {
                                OrbotState.RUNNING, OrbotState.BOUND -> theme.alertGreen
                                OrbotState.STARTING -> theme.alertAmber
                                OrbotState.STOPPING -> theme.alertRed
                                else -> if (torStatus.isSocksResponding) theme.alertGreen else theme.textMuted
                            }
                            val badgeLabel = when (torStatus.orbotState) {
                                OrbotState.RUNNING -> "ORBOT ACTIVE"
                                OrbotState.BOUND -> "TOR BOUND"
                                OrbotState.STARTING -> "STARTING..."
                                OrbotState.STOPPING -> "STOPPING..."
                                OrbotState.NOT_INSTALLED -> "ORBOT ABSENT"
                                else -> if (torStatus.isSocksResponding) "SOCKS5 ONLINE" else "ORBOT IDLE"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.12f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Routes all P2P marketplace and messaging traffic strictly through Orbot's Tor proxy on port ${torStatus.socksProxyPort} to prevent any external IP or DNS leaks.",
                            fontSize = 11.5.sp,
                            color = theme.textSecondary,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Orbot Service Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startOrbot() },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Orbot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.stopOrbot() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(15.dp), tint = theme.textPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Orbot", fontSize = 11.sp, color = theme.textPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.bindOrbotService() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bind Service", fontSize = 11.sp, color = theme.accentPrimary)
                            }

                            if (!torStatus.isOrbotInstalled) {
                                Button(
                                    onClick = { viewModel.openOrbotPlayStore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = theme.accentSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Orbot", fontSize = 11.sp, color = theme.accentSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Proxy Socket Test Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(theme.surfaceElevated)
                                .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Proxy Socket Verification",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.probeTorConnectivity() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(11.dp), tint = theme.accentPrimary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Socket", fontSize = 10.sp, color = theme.accentPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("SOCKS5 Socket Status:", fontSize = 10.5.sp, color = theme.textSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (torStatus.isSocksResponding) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (torStatus.isSocksResponding) theme.alertGreen else theme.textMuted,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (torStatus.isSocksResponding) "Listening (Port ${torStatus.socksProxyPort})" else "Not Open (${torStatus.socksProxyPort})",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (torStatus.isSocksResponding) theme.alertGreen else theme.textMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Exit Verification:", fontSize = 10.5.sp, color = theme.textSecondary)
                                        Text(
                                            text = torStatus.verifiedExitIp ?: if (torStatus.isTorNetworkVerified) "Verified Onion" else "Pending Check",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accentSecondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (torStatus.checkStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = torStatus.checkStatusMessage ?: "",
                                        fontSize = 10.sp,
                                        color = theme.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SOCKS Host & Port manual override
                        Text("Proxy Configuration Override:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = proxyHostInput,
                                onValueChange = { proxyHostInput = it },
                                label = { Text("Host", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = theme.surfaceElevated,
                                    unfocusedContainerColor = theme.surfaceElevated,
                                    focusedBorderColor = theme.accentPrimary,
                                    unfocusedBorderColor = theme.border,
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(2f)
                            )

                            OutlinedTextField(
                                value = proxyPortInput,
                                onValueChange = { proxyPortInput = it },
                                label = { Text("Port", fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = theme.surfaceElevated,
                                    unfocusedContainerColor = theme.surfaceElevated,
                                    focusedBorderColor = theme.accentPrimary,
                                    unfocusedBorderColor = theme.border,
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val port = proxyPortInput.toIntOrNull() ?: 9050
                                viewModel.updateProxySettings(proxyHostInput.trim(), port)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply Proxy Settings", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Real-time Transparency Logs Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Live Transparency Log",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = theme.textPrimary
                            )
                            Text(
                                "${transparencyLogs.size} events",
                                fontSize = 11.sp,
                                color = theme.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (transparencyLogs.isEmpty()) {
                            Text(
                                "Zero background operations currently recorded.",
                                fontSize = 11.5.sp,
                                color = theme.textSecondary
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                transparencyLogs.takeLast(5).reversed().forEach { event ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.surfaceElevated)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (event.isLocalOnly) theme.alertGreen else theme.accentSecondary
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = event.operation,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = theme.textPrimary
                                            )
                                            Text(
                                                text = event.details,
                                                fontSize = 10.sp,
                                                color = theme.textSecondary
                                            )
                                        }
                                        Text(
                                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp)),
                                            fontSize = 9.5.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = theme.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Panic Button: Emergency Wipe
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, theme.alertRed.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = theme.alertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Vault Panic Wipe", fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = theme.alertRed)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Instantly and irrecoverably zero out and purge all local listings, photos, encrypted chat logs, and cryptographic keys from this device.",
                            fontSize = 11.5.sp,
                            color = theme.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showPanicDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.alertRed, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("panic_wipe_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zero Out & Wipe All Local Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Panic Confirmation Dialog
    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            containerColor = theme.surface,
            title = {
                Text("Confirm Cryptographic Vault Purge?", color = theme.alertRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Text(
                    text = "Are you absolutely sure? All marketplace photos on your phone, local descriptions, prices, RSA keys, and P2P chat histories will be destroyed forever.",
                    color = theme.textPrimary,
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.panicWipeAllData()
                        showPanicDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.alertRed, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Purge Everything", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun StorageStatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 9.5.sp, color = theme.textMuted)
        }
    }
}
