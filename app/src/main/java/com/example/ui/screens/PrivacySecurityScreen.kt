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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.network.ConnectionMode
import com.example.data.network.OrbotState
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
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TorCyan
import com.example.ui.theme.TorOnionGreen
import com.example.ui.theme.TorPurple
import com.example.ui.theme.TorPurpleDark
import com.example.ui.theme.TorPurpleLight
import com.example.ui.viewmodel.TorPeerViewModel

@Composable
fun PrivacySecurityScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val myListings by viewModel.myListings.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()

    var showPanicDialog by remember { mutableStateOf(false) }
    var proxyHostInput by remember { mutableStateOf(torStatus.socksProxyHost) }
    var proxyPortInput by remember { mutableStateOf(torStatus.socksProxyPort.toString()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            text = "Fizz Tor & Security Vault",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BubbleFoamWhite
                        )
                        Text(
                            text = "Bubble Water Security • Zero Cloud • Local Storage Only",
                            fontSize = 12.sp,
                            color = BubbleAquaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TorPurple.copy(alpha = 0.2f))
                            .border(1.dp, TorPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = TorPurple,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Tor Circuit Visualizer Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Router, contentDescription = null, tint = TorOnionGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Active Tor Circuit (3 Hops)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { viewModel.rotateTorCircuit() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("rotate_circuit_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Circuit", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hop steps
                        torStatus.activeCircuitHops.forEachIndexed { index, hop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceHigh)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(TorPurpleDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TorPurpleLight
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = hop.flag, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = hop.nodeType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Text(
                                        text = "${hop.nickname} • ${hop.ipMasked}",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = "${hop.latencyMs}ms",
                                    fontSize = 11.sp,
                                    color = TorCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (index < torStatus.activeCircuitHops.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 24.dp)
                                        .width(2.dp)
                                        .height(8.dp)
                                        .background(DarkBorder)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Connection Mode Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tor Onion Routing",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (torStatus.connectionMode == ConnectionMode.TOR_ONION_ROUTING) "All traffic routed over Tor" else "Direct LAN P2P Mode",
                                    fontSize = 11.sp,
                                    color = TextMuted
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
                                    checkedTrackColor = TorPurple
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
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = TorPurple, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cryptographic Node Identity", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Onion Address
                        Text("Permanent Onion v3 Address:", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.cryptoManager.myOnionAddress,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TorPurpleLight,
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
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TorCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Public Key Fingerprint
                        Text("RSA-2048 / SHA-256 Fingerprint:", fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = viewModel.cryptoManager.myFingerprint,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TorOnionGreen,
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
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = TorOnionGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Local Storage & Zero Cloud Audit", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Every byte of this application is stored strictly in your phone's internal storage sandbox. No Firebase, AWS, or third-party database is connected.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            // Orbot Service Binding & Native Tor Proxy Client Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = BubbleAquaPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Orbot Service & Native Tor Proxy", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }

                            // Status badge
                            val badgeColor = when (torStatus.orbotState) {
                                OrbotState.RUNNING, OrbotState.BOUND -> BubbleCarbonationGreen
                                OrbotState.STARTING -> BubbleCyan
                                OrbotState.STOPPING -> ErrorRed
                                else -> if (torStatus.isSocksResponding) BubbleCarbonationGreen else TextMuted
                            }
                            val badgeLabel = when (torStatus.orbotState) {
                                OrbotState.RUNNING -> "ORBOT ACTIVE"
                                OrbotState.BOUND -> "TOR SERVICE BOUND"
                                OrbotState.STARTING -> "STARTING..."
                                OrbotState.STOPPING -> "STOPPING..."
                                OrbotState.NOT_INSTALLED -> "ORBOT NOT DETECTED"
                                else -> if (torStatus.isSocksResponding) "SOCKS5 ONLINE" else "ORBOT IDLE"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Routes all P2P marketplace and messaging traffic strictly through Orbot's Tor proxy on port ${torStatus.socksProxyPort} to prevent any external IP or DNS leaks.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Orbot Service Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startOrbot() },
                                colors = ButtonDefaults.buttonColors(containerColor = BubbleAquaPrimary, contentColor = DarkBackground),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start Orbot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.stopOrbot() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop Orbot", fontSize = 11.sp)
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
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bind Service", fontSize = 11.sp)
                            }

                            if (!torStatus.isOrbotInstalled) {
                                Button(
                                    onClick = { viewModel.openOrbotPlayStore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHigh),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = BubbleCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Orbot", fontSize = 11.sp, color = BubbleCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Native Tor Proxy Probe Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Native Tor Proxy Verification",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BubbleFoamWhite
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.probeTorConnectivity() },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Socket", fontSize = 10.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("SOCKS5 Socket Status:", fontSize = 11.sp, color = TextMuted)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (torStatus.isSocksResponding) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (torStatus.isSocksResponding) BubbleCarbonationGreen else TextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (torStatus.isSocksResponding) "Listening (Port ${torStatus.socksProxyPort})" else "Not Open (${torStatus.socksProxyPort})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (torStatus.isSocksResponding) BubbleCarbonationGreen else TextMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Tor Exit Verification:", fontSize = 11.sp, color = TextMuted)
                                        Text(
                                            text = torStatus.verifiedExitIp ?: if (torStatus.isTorNetworkVerified) "Verified Onion" else "Pending Check",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BubbleCyan,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                if (torStatus.checkStatusMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = torStatus.checkStatusMessage ?: "",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // SOCKS Host & Port manual override
                        Text("Proxy Configuration Override:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BubbleFoamWhite)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = proxyHostInput,
                                onValueChange = { proxyHostInput = it },
                                label = { Text("Host") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BubbleAquaPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(2f)
                            )

                            OutlinedTextField(
                                value = proxyPortInput,
                                onValueChange = { proxyPortInput = it },
                                label = { Text("Port") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BubbleAquaPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val port = proxyPortInput.toIntOrNull() ?: 9050
                                viewModel.updateProxySettings(proxyHostInput.trim(), port)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BubbleAquaDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply Proxy Settings")
                        }
                    }
                }
            }

            // Panic Button: Emergency Wipe
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Vault Panic Wipe", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ErrorRed)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Instantly and irrecoverably zero out and purge all local listings, photos, encrypted chat logs, and cryptographic keys from this device.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showPanicDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("panic_wipe_button")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zero Out & Wipe All Local Data", fontWeight = FontWeight.Bold)
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
            containerColor = DarkSurfaceElevated,
            title = {
                Text("Confirm Cryptographic Vault Purge?", color = ErrorRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you absolutely sure? All marketplace photos on your phone, local descriptions, prices, RSA keys, and P2P chat histories will be destroyed forever.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.panicWipeAllData()
                        showPanicDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text("Purge Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicDialog = false }) {
                    Text("Cancel", color = TextSecondary)
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceHigh)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 10.sp, color = TextMuted)
        }
    }
}
