package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.database.MessageEntity
import com.example.data.database.PeerContactEntity
import com.example.ui.components.PeerQrConnectionDialog
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.TorPeerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val showPeerQrDialog by viewModel.showPeerQrDialog.collectAsStateWithLifecycle()
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newPeerOnionInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = theme.accentPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New P2P Chat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title & Encryption Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chats",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Encrypted direct messages",
                        fontSize = 11.5.sp,
                        color = theme.textSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.openPeerQrDialog() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.borderGold, CircleShape)
                            .testTag("chat_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "P2P QR Direct Link",
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(theme.alertGreen.copy(alpha = 0.12f))
                            .border(1.dp, theme.alertGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = theme.alertGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AES-256-GCM",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.alertGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (peers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = theme.textMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Peer Conversations",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start a direct conversation with any Tor Onion peer address",
                            color = theme.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(peers, key = { it.peerId }) { peer ->
                        val lastMessage = allMessages.firstOrNull { it.peerId == peer.peerId }
                        PeerChatItem(
                            peer = peer,
                            lastMessage = lastMessage,
                            onClick = { viewModel.selectChat(peer.peerId) }
                        )
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            containerColor = theme.surface,
            title = {
                Text("Start P2P Encrypted Chat", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter peer Tor Onion address or contact ID to initiate an encrypted session:",
                        color = theme.textSecondary,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPeerOnionInput,
                        onValueChange = { newPeerOnionInput = it },
                        placeholder = { Text("e.g. torpeer9x...onion", color = theme.textMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = theme.surfaceElevated,
                            unfocusedContainerColor = theme.surfaceElevated,
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.border,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("new_chat_peer_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = newPeerOnionInput.trim()
                        if (input.isNotBlank()) {
                            val peerId = "peer_" + input.take(10).replace(".", "_")
                            viewModel.sendMessage(
                                peerId = peerId,
                                peerOnion = input,
                                text = "Hello! Initializing P2P encrypted session via Tor."
                            )
                            viewModel.selectChat(peerId)
                            showNewChatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Connect & Chat", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            }
        )
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
}

@Composable
fun PeerChatItem(
    peer: PeerContactEntity,
    lastMessage: MessageEntity?,
    onClick: () -> Unit
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("peer_item_${peer.peerId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Peer Avatar with medical initial circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (peer.isVerified) theme.alertGreen.copy(alpha = 0.12f)
                        else theme.accentPrimary.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (peer.isVerified) theme.alertGreen else theme.accentPrimary.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = peer.alias.take(2).uppercase(),
                    color = if (peer.isVerified) theme.alertGreen else theme.accentPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = peer.alias,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = theme.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (peer.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Contact",
                                tint = theme.alertGreen,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    if (lastMessage != null) {
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastMessage.timestamp)),
                            fontSize = 10.5.sp,
                            color = theme.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lastMessage?.content ?: "Direct encrypted peer connection",
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    peerId: String,
    viewModel: TorPeerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalMedicalTheme.current
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val peer = peers.firstOrNull { it.peerId == peerId } ?: PeerContactEntity(
        peerId = peerId,
        alias = peerId,
        onionAddress = "$peerId.onion",
        publicKey = "",
        fingerprint = "E1:A2:3B:4C:5D:6E"
    )

    val conversationMessages = allMessages.filter { it.peerId == peerId }.sortedBy { it.timestamp }
    val relatedListingMessage = conversationMessages.lastOrNull { it.relatedListingId != null }

    var inputText by remember { mutableStateOf("") }
    var showCipherModal by remember { mutableStateOf<MessageEntity?>(null) }
    var showFingerprintModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = peer.alias,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            if (peer.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = theme.alertGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = peer.onionAddress,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.textPrimary
                        )
                    }
                },
                actions = {
                    var showTimerMenu by remember { mutableStateOf(false) }
                    val autoDestruct by viewModel.selectedAutoDestructSeconds.collectAsStateWithLifecycle()

                    Box {
                        IconButton(
                            onClick = { showTimerMenu = true },
                            modifier = Modifier.testTag("auto_destruct_timer_button")
                        ) {
                            Icon(
                                imageVector = if (autoDestruct != null) Icons.Default.LocalFireDepartment else Icons.Default.Timer,
                                contentDescription = "Auto-Destruct Timer",
                                tint = if (autoDestruct != null) theme.alertAmber else theme.textSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showTimerMenu,
                            onDismissRequest = { showTimerMenu = false },
                            modifier = Modifier.background(theme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Off (Permanent Storage)", color = theme.textPrimary, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(null)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 10 Seconds (Ultra-Ephemeral)", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(10)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 30 Seconds", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(30)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 1 Minute", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(60)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 5 Minutes", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(300)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 1 Hour", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(3600)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🔥 24 Hours", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(86400)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    IconButton(onClick = { showFingerprintModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Verify Fingerprint",
                            tint = theme.accentPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.surface,
                    titleContentColor = theme.textPrimary
                )
            )
        }
    ) { innerPadding ->
        val autoDestructSeconds by viewModel.selectedAutoDestructSeconds.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Marketplace Context Banner (if discussing an item)
            if (relatedListingMessage != null) {
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = relatedListingMessage.relatedListingTitle ?: "Marketplace Item",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = theme.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Price: ${relatedListingMessage.relatedListingPrice ?: "Local Handshake"}",
                                    fontSize = 11.sp,
                                    color = theme.alertGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "Direct P2P Order",
                            fontSize = 10.5.sp,
                            color = theme.textMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Security & Auto-Destruct Indicator Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = theme.alertGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tor Multi-Hop Encrypted",
                            fontSize = 10.sp,
                            color = theme.textSecondary
                        )
                    }
                }

                if (autoDestructSeconds != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.alertAmber.copy(alpha = 0.12f))
                            .border(1.dp, theme.alertAmber.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = theme.alertAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto-Destruct: ${autoDestructSeconds}s",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.alertAmber
                            )
                        }
                    }
                }
            }

            // Messages LazyColumn
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversationMessages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        onInspectCipher = { showCipherModal = msg }
                    )
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surface)
                    .border(1.dp, theme.border)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (autoDestructSeconds != null) "Vaporizing message (${autoDestructSeconds}s)..." else "Encrypted message...",
                            color = theme.textMuted,
                            fontSize = 12.5.sp
                        )
                    },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.surfaceElevated,
                        unfocusedContainerColor = theme.surfaceElevated,
                        focusedBorderColor = if (autoDestructSeconds != null) theme.alertAmber else theme.accentPrimary,
                        unfocusedBorderColor = theme.border,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val text = inputText.trim()
                        if (text.isNotEmpty()) {
                            viewModel.sendMessage(
                                peerId = peer.peerId,
                                peerOnion = peer.onionAddress,
                                text = text,
                                relatedListingId = relatedListingMessage?.relatedListingId,
                                relatedListingTitle = relatedListingMessage?.relatedListingTitle,
                                relatedListingPrice = relatedListingMessage?.relatedListingPrice
                            )
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (autoDestructSeconds != null) theme.alertAmber else theme.accentPrimary)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Encrypted",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Inspect Ciphertext Dialog with Burn / Vaporize Option
    if (showCipherModal != null) {
        val m = showCipherModal!!
        AlertDialog(
            onDismissRequest = { showCipherModal = null },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = theme.alertGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AES-256-GCM Payload", color = theme.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Transmitted encrypted through Tor SOCKS5 proxy. Decrypted strictly in-memory on this phone:",
                        fontSize = 11.5.sp,
                        color = theme.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = m.encryptedBlob.ifEmpty { "AES_GCM_CIPHERTEXT_BASE64_PAYLOAD" },
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decrypted Text:\n${m.content}",
                        fontSize = 12.sp,
                        color = theme.textPrimary
                    )

                    if (m.expiresAt != null) {
                        val remainingSec = ((m.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto-Destruct armed: ${remainingSec}s remaining until wiped",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.alertAmber
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.deleteMessage(m.id)
                            showCipherModal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.alertRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Burn Now", fontSize = 11.sp)
                    }
                    TextButton(onClick = { showCipherModal = null }) {
                        Text("Close", color = theme.accentPrimary)
                    }
                }
            }
        )
    }

    // Peer Fingerprint Verification Dialog
    if (showFingerprintModal) {
        AlertDialog(
            onDismissRequest = { showFingerprintModal = false },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = theme.accentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Peer Fingerprint", color = theme.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Compare this cryptographic key fingerprint out-of-band to prevent MITM attacks:",
                        color = theme.textSecondary,
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = peer.fingerprint,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = theme.alertGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${if (peer.isVerified) "Verified Contact" else "Unverified (Trust on first use)"}",
                        fontSize = 11.5.sp,
                        color = if (peer.isVerified) theme.alertGreen else theme.textMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Fingerprint", peer.fingerprint))
                        Toast.makeText(context, "Fingerprint copied!", Toast.LENGTH_SHORT).show()
                        showFingerprintModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Fingerprint", fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFingerprintModal = false }) {
                    Text("Close", color = theme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onInspectCipher: () -> Unit
) {
    val theme = LocalMedicalTheme.current
    val isOut = message.isOutgoing
    val alignment = if (isOut) Alignment.End else Alignment.Start
    val bg = if (isOut) theme.accentPrimary.copy(alpha = 0.15f) else theme.surface
    val border = if (message.expiresAt != null) theme.alertAmber.copy(alpha = 0.6f) else if (isOut) theme.accentPrimary.copy(alpha = 0.4f) else theme.border

    val remainingSeconds = if (message.expiresAt != null) {
        ((message.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
    } else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isOut) 14.dp else 2.dp,
                        bottomEnd = if (isOut) 2.dp else 14.dp
                    )
                )
                .background(bg)
                .border(
                    1.dp,
                    border,
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isOut) 14.dp else 2.dp,
                        bottomEnd = if (isOut) 2.dp else 14.dp
                    )
                )
                .clickable(onClick = onInspectCipher)
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = theme.textPrimary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (remainingSeconds != null) Icons.Default.LocalFireDepartment else Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = if (remainingSeconds != null) theme.alertAmber else if (isOut) theme.accentPrimary else theme.alertGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 9.5.sp,
                        color = theme.textMuted
                    )
                    if (remainingSeconds != null) {
                        Text(
                            text = "• 🔥 ${remainingSeconds}s",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.alertAmber
                        )
                    } else if (isOut) {
                        Text(
                            text = "• ${message.status}",
                            fontSize = 9.5.sp,
                            color = theme.accentPrimary
                        )
                    }
                }
            }
        }
    }
}
