package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LocalMedicalTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Utility function to generate a ZXing QR Code Bitmap.
 */
fun generateQrBitmap(
    content: String,
    sizePx: Int = 512,
    darkColor: Int = android.graphics.Color.BLACK,
    lightColor: Int = android.graphics.Color.WHITE
): Bitmap? {
    return try {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) darkColor else lightColor)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * Encodes peer parameters into a standard Fizz P2P connection URI.
 */
fun buildP2PConnectionString(peerId: String, onionAddress: String, fingerprint: String, displayName: String): String {
    val encodedName = URLEncoder.encode(displayName, StandardCharsets.UTF_8.toString())
    return "fizz://peer?id=$peerId&onion=$onionAddress&fp=$fingerprint&name=$encodedName"
}

/**
 * Parsed P2P connection data.
 */
data class ParsedPeerConnection(
    val peerId: String,
    val onionAddress: String,
    val fingerprint: String,
    val displayName: String
)

/**
 * Parses a Fizz P2P connection URI or raw onion address.
 */
fun parseP2PConnectionString(raw: String): ParsedPeerConnection? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null

    return try {
        if (trimmed.startsWith("fizz://peer?")) {
            val query = trimmed.removePrefix("fizz://peer?")
            val params = query.split("&").associate {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to URLDecoder.decode(parts[1], StandardCharsets.UTF_8.toString())
                else parts[0] to ""
            }
            val id = params["id"] ?: "peer_${System.currentTimeMillis().toString().takeLast(6)}"
            val onion = params["onion"] ?: "unknown.onion"
            val fp = params["fp"] ?: "FP_UNKNOWN"
            val name = params["name"] ?: "Anonymous Peer"
            ParsedPeerConnection(peerId = id, onionAddress = onion, fingerprint = fp, displayName = name)
        } else if (trimmed.endsWith(".onion")) {
            val hash = trimmed.take(8)
            ParsedPeerConnection(
                peerId = "peer_$hash",
                onionAddress = trimmed,
                fingerprint = "FP_${trimmed.take(12).uppercase()}",
                displayName = "Onion Node $hash"
            )
        } else {
            // General connection token
            ParsedPeerConnection(
                peerId = "peer_${trimmed.take(8)}",
                onionAddress = "$trimmed.onion",
                fingerprint = "FP_${trimmed.takeLast(8).uppercase()}",
                displayName = "Direct Link Peer"
            )
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Elegant, uncluttered P2P QR Code generator & pairing dialog.
 */
@Composable
fun PeerQrConnectionDialog(
    myPeerId: String,
    myOnionAddress: String,
    myFingerprint: String,
    myDisplayName: String = "My Fizz Node",
    onConnectToPeer: (ParsedPeerConnection) -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalMedicalTheme.current
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: My QR Code, 1: Import / Connect

    val connectionString = remember(myPeerId, myOnionAddress, myFingerprint, myDisplayName) {
        buildP2PConnectionString(myPeerId, myOnionAddress, myFingerprint, myDisplayName)
    }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(connectionString) {
        qrBitmap = generateQrBitmap(
            content = connectionString,
            sizePx = 480,
            darkColor = Color(0xFF040D1A).toArgb(),
            lightColor = Color.White.toArgb()
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, theme.borderGold, RoundedCornerShape(22.dp))
                .testTag("peer_qr_dialog"),
            colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close
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
                                .background(theme.accentPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Direct P2P Link",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Zero-Discovery Encrypted Channel",
                                fontSize = 10.5.sp,
                                color = theme.accentPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text("✕", color = theme.textMuted, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = theme.surface,
                    contentColor = theme.accentPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = theme.accentPrimary,
                            height = 2.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "My QR Code",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) theme.accentPrimary else theme.textMuted
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Connect to Peer",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) theme.accentPrimary else theme.textMuted
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 0: Show My QR Code
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // QR Code Display Card
                        Card(
                            modifier = Modifier
                                .size(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, theme.borderGold, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "P2P Connection QR Code",
                                        modifier = Modifier.size(186.dp)
                                    )
                                } else {
                                    Text("Generating QR...", color = Color.Black, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact Connection Link Pill
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
                                        text = "Onion Address",
                                        fontSize = 9.sp,
                                        color = theme.textMuted
                                    )
                                    Text(
                                        text = myOnionAddress,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = theme.textPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Fizz P2P Link", connectionString)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "P2P Connection Link Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Link",
                                        tint = theme.accentPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Fizz P2P Link", connectionString)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied P2P Link to Clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = theme.accentPrimary,
                                    contentColor = theme.background
                                )
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Link", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Onion Address", myOnionAddress)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied Onion Address", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accentPrimary)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp), tint = theme.accentPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Onion", fontSize = 11.5.sp, color = theme.accentPrimary)
                            }
                        }
                    }
                } else {
                    // TAB 1: Connect to Peer via String / QR
                    var inputString by remember { mutableStateOf("") }
                    var parsedPeer by remember { mutableStateOf<ParsedPeerConnection?>(null) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Paste a friend's Fizz link or Onion address to establish a private direct connection.",
                            fontSize = 11.sp,
                            color = theme.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputString,
                            onValueChange = {
                                inputString = it
                                parsedPeer = parseP2PConnectionString(it)
                            },
                            label = { Text("Paste P2P URI or .onion Address", fontSize = 11.sp) },
                            placeholder = { Text("fizz://peer?id=... or abc123xyz.onion", fontSize = 10.5.sp) },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("peer_link_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.accentPrimary,
                                unfocusedBorderColor = theme.border,
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedContainerColor = theme.surface,
                                unfocusedContainerColor = theme.surface
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Paste from Clipboard button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Paste from Clipboard",
                                fontSize = 11.sp,
                                color = theme.accentPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val item = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                        if (item.isNotEmpty()) {
                                            inputString = item
                                            parsedPeer = parseP2PConnectionString(item)
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }

                        // Parsed Peer Preview Card
                        AnimatedVisibility(
                            visible = parsedPeer != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            parsedPeer?.let { peer ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(1.dp, theme.alertGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = theme.alertGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = peer.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary
                                            )
                                            Text(
                                                text = peer.onionAddress,
                                                fontSize = 10.sp,
                                                color = theme.textSecondary,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                parsedPeer?.let {
                                    onConnectToPeer(it)
                                    onDismiss()
                                }
                            },
                            enabled = parsedPeer != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connect_peer_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.accentPrimary,
                                contentColor = theme.background,
                                disabledContainerColor = theme.surfaceHigh,
                                disabledContentColor = theme.textMuted
                            )
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Establish Encrypted Link", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
