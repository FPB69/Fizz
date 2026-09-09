package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalMedicalTheme

@Composable
fun LegalDisclaimerDialog(
    onDismiss: () -> Unit,
    onAcknowledge: () -> Unit = onDismiss
) {
    val theme = LocalMedicalTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, theme.borderGold, RoundedCornerShape(22.dp))
                .testTag("legal_disclaimer_dialog"),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.accentPrimary.copy(alpha = 0.15f))
                                .border(1.dp, theme.borderGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LEGAL & PROTOCOL DISCLAIMER",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Zero-Fault • Zero-Liability • Autonomous P2P Protocol",
                                fontSize = 10.5.sp,
                                color = theme.accentPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_legal_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Critical 0-Fault Notice
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "0 FAULT & COMPLETE ZERO LIABILITY CLAUSE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = theme.accentPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "The developers, maintainers, and open-source contributors of Fizz.05 hold ZERO FAULT and ZERO LEGAL LIABILITY for any user conduct, transactions, messages, items listed, or regulatory non-compliance. You operate this software entirely at your own discretion and risk.",
                                        fontSize = 11.sp,
                                        color = theme.textPrimary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Clause 1: Decentralized Non-Custodial Architecture
                    item {
                        LegalSectionCard(
                            title = "1. Decentralized, Zero-Cloud Architecture",
                            content = "Fizz.05 contains ZERO central databases, ZERO intermediary servers, and ZERO cloud backends. All listings, photos, and messages are stored exclusively in your device's internal SQLite sandbox and transmitted peer-to-peer via Tor Onion v3 tunnels. The authors possess NO custody, control, or visibility over any data.",
                            tag = "NON-CUSTODIAL"
                        )
                    }

                    // Clause 2: Peer Autonomy & Local Jurisdiction Compliance
                    item {
                        LegalSectionCard(
                            title = "2. Autonomous Peer Sovereignty & Compliance",
                            content = "Every node operator and user is solely responsible for ensuring that their use of this software, communications, and any commercial or barter exchange complies with all applicable local, regional, national, and international laws, export controls, and regulations.",
                            tag = "PEER RESPONSIBILITY"
                        )
                    }

                    // Clause 3: Cryptographic Tooling & Open Protocol
                    item {
                        LegalSectionCard(
                            title = "3. Pure Cryptographic & Network Tooling",
                            content = "This software is published strictly as an informational and cryptographic communication tool implementing AES-256-GCM, ChaCha20-Poly1305, and Tor Onion Routing. Like mathematical algorithms and network protocols, it is agnostic to user content and cannot censor, alter, or moderate peer traffic.",
                            tag = "CRYPTOGRAPHIC PROTOCOL"
                        )
                    }

                    // Clause 4: Total Warranty Disclaimer (AS-IS)
                    item {
                        LegalSectionCard(
                            title = "4. \"AS-IS\" No Warranty & Indemnification",
                            content = "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY.",
                            tag = "NO WARRANTY"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Acknowledgement Button
                Button(
                    onClick = {
                        onAcknowledge()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("acknowledge_legal_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I Acknowledge & Accept 0-Fault Terms",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LegalSectionCard(
    title: String,
    content: String,
    tag: String
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.borderGold, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accentPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = content,
                fontSize = 10.5.sp,
                color = theme.textSecondary,
                lineHeight = 14.sp
            )
        }
    }
}
