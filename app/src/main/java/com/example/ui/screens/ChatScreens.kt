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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.database.TalkRequestEntity
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
    val talkRequests by viewModel.talkRequests.collectAsStateWithLifecycle()
    val pendingIncomingRequests by viewModel.pendingIncomingRequests.collectAsStateWithLifecycle()
    val showPeerQrDialog by viewModel.showPeerQrDialog.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newPeerOnionInput by remember { mutableStateOf("") }

    val connectedPeers = peers.filter { it.connectionStatus == "CONNECTED" }

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
                        text = "Saved on your device & peer device only",
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

            // Tab Row: Direct Chats vs Talk Requests
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = theme.surfaceElevated,
                contentColor = theme.accentPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = theme.accentPrimary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, theme.border, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Active Chats (${connectedPeers.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == 0) theme.accentPrimary else theme.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Talk Requests",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == 1) theme.accentPrimary else theme.textSecondary
                            )
                            if (pendingIncomingRequests.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(theme.alertAmber)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pendingIncomingRequests.size}",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTabIndex == 0) {
                // Active Chats List
                if (connectedPeers.isEmpty()) {
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
                                text = "No Connected Peers Yet",
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Send a talk request to a seller in Market or connect via QR",
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
                        items(connectedPeers, key = { it.peerId }) { peer ->
                            val lastMessage = allMessages.firstOrNull { it.peerId == peer.peerId }
                            PeerChatItem(
                                peer = peer,
                                lastMessage = lastMessage,
                                onClick = { viewModel.selectChat(peer.peerId) }
                            )
                        }
                    }
                }
            } else {
                // Talk Requests List
                if (talkRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = theme.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Talk Requests",
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Contact sellers in the marketplace to initiate P2P handshakes",
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(talkRequests, key = { it.id }) { request ->
                            TalkRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptTalkRequest(request.id) },
                                onDecline = { viewModel.declineTalkRequest(request.id) },
                                onOpenChat = { viewModel.selectChat(request.peerId) }
                            )
                        }
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
                            viewModel.sendTalkRequest(
                                peerId = peerId,
                                peerAlias = input.take(14) + "...",
                                peerOnion = input,
                                initialMessage = "Hello! Initializing P2P encrypted session via Tor."
                            )
                            showNewChatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Send Talk Request", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            }
        )
    }
}

@Composable
fun TalkRequestItem(
    request: TalkRequestEntity,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Type & Status
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
                            .background(if (request.isIncoming) theme.accentPrimary.copy(alpha = 0.15f) else theme.surfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (request.isIncoming) Icons.Default.Handshake else Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = if (request.isIncoming) theme.accentPrimary else theme.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (request.isIncoming) "Incoming Talk Request" else "Sent Talk Request",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = request.peerAlias,
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                    }
                }

                // Status Badge
                when (request.status) {
                    "PENDING" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.alertAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (request.isIncoming) "Awaiting Your Decision" else "Pending Acceptance",
                                color = theme.alertAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "ACCEPTED" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.alertGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Connected",
                                color = theme.alertGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.alertRed.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Declined",
                                color = theme.alertRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Attached Listing Details (if any)
            if (!request.listingTitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.surfaceElevated)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = theme.accentPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Regarding: ${request.listingTitle} (${request.listingPrice ?: ""})",
                        fontSize = 11.sp,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Initial Note
            if (request.initialMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${request.initialMessage}\"",
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            if (request.status == "PENDING" && request.isIncoming) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.alertGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept Talk", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (request.status == "ACCEPTED") {
                Button(
                    onClick = onOpenChat,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Encrypted Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PeerChatItem(
    peer: PeerContactEntity,
    lastMessage: MessageEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("peer_item_${peer.peerId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Node Avatar / Letter
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

    var inputText by remember(peerId) { mutableStateOf(peer.draftMessage) }
    var showCipherModal by remember { mutableStateOf<MessageEntity?>(null) }
    var showFingerprintModal by remember { mutableStateOf(false) }

    // Save draft when typing or leaving
    androidx.compose.runtime.DisposableEffect(peerId, inputText) {
        onDispose {
            viewModel.saveDraft(peerId, inputText)
        }
    }

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
                                text = { Text("10 Seconds (Ultra-Ephemeral)", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(10)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("30 Seconds", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(30)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("1 Minute", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(60)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("5 Minutes", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(300)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("1 Hour", color = theme.alertAmber, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAutoDestructTimer(3600)
                                    showTimerMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(16.dp)) }
                            )
                            DropdownMenuItem(
                                text = { Text("24 Hours", color = theme.alertAmber, fontSize = 12.sp) },
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

            // Peer Storage Clarity Banner
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
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = theme.alertGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "P2P: Saved on your phone & peer device only",
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
                                text = "${autoDestructSeconds}s TTL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.alertAmber
                            )
                        }
                    }
                }
            }

            // Message History
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(conversationMessages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onInspectCipher = { showCipherModal = message },
                        onDeleteMessage = { viewModel.deleteMessage(message.id) }
                    )
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Write encrypted message...", color = theme.textMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.surfaceElevated,
                        unfocusedContainerColor = theme.surfaceElevated,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.border,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    maxLines = 4
                )

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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(theme.accentPrimary)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Encrypted Message",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Modal to view Ciphertext & Cryptographic Proof
    if (showCipherModal != null) {
        val msg = showCipherModal!!
        AlertDialog(
            onDismissRequest = { showCipherModal = null },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cryptographic Payload", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Decrypted Content:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textPrimary)
                    Text(msg.content, fontSize = 12.sp, color = theme.textSecondary)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("AES-256-GCM Raw Ciphertext:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = theme.textPrimary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = msg.encryptedBlob.ifEmpty { "AES_GCM_ENCRYPTED_BLOB_V2" },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentPrimary
                        )
                    }

                    if (msg.expiresAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoDelete, contentDescription = null, tint = theme.alertAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val remainingSec = ((msg.expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
                            Text("Vaporizes in: ${remainingSec}s", fontSize = 11.sp, color = theme.alertAmber, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Ciphertext", msg.encryptedBlob))
                        Toast.makeText(context, "Ciphertext copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Ciphertext", fontSize = 11.5.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCipherModal = null }) {
                    Text("Close", color = theme.textSecondary)
                }
            }
        )
    }

    // Modal to verify Key Fingerprint
    if (showFingerprintModal) {
        AlertDialog(
            onDismissRequest = { showFingerprintModal = false },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = theme.accentPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Peer Cryptographic Identity", color = theme.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Compare this fingerprint with your peer over a trusted side-channel to prevent MITM attacks:", fontSize = 12.sp, color = theme.textSecondary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = peer.fingerprint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = theme.accentPrimary
                        )
                    }

                    Text("Tor Onion Address:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    Text(peer.onionAddress, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = theme.textMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyPeer(peer.peerId, !peer.isVerified)
                        showFingerprintModal = false
                        Toast.makeText(context, if (!peer.isVerified) "Marked peer as Verified!" else "Marked peer as Unverified", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (peer.isVerified) theme.alertAmber else theme.alertGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (peer.isVerified) "Unmark Verified" else "Mark Verified", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    onInspectCipher: () -> Unit,
    onDeleteMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    val isMine = message.isOutgoing

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(if (isMine) theme.accentPrimary else theme.surfaceElevated)
                .border(
                    1.dp,
                    if (isMine) theme.accentPrimary else theme.border,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .clickable { onInspectCipher() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = if (isMine) Color.White else theme.textPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = if (isMine) Color.White.copy(alpha = 0.7f) else theme.textMuted,
                        modifier = Modifier.size(10.dp)
                    )

                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.7f) else theme.textMuted
                    )

                    if (message.autoDestructSeconds != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Ephemeral",
                            tint = if (isMine) Color.White else theme.alertAmber,
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    if (isMine) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = message.status,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}
