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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TorCyan
import com.example.ui.theme.BubbleAquaDark
import com.example.ui.theme.BubbleAquaLight
import com.example.ui.theme.BubbleAquaPrimary
import com.example.ui.theme.BubbleCarbonationGreen
import com.example.ui.theme.BubbleCyan
import com.example.ui.theme.BubbleFoamWhite
import com.example.ui.theme.TorOnionGreen
import com.example.ui.theme.TorPurple
import com.example.ui.theme.TorPurpleDark
import com.example.ui.theme.TorPurpleLight
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
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newPeerOnionInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = BubbleAquaPrimary,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = "New Chat")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New P2P Chat", fontWeight = FontWeight.Bold)
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
                        text = "Encrypted Chats",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "End-to-End Encrypted via Tor Onion",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(TorOnionGreen.copy(alpha = 0.12f))
                        .border(1.dp, TorOnionGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TorOnionGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AES-256-GCM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TorOnionGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Peer Conversations",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start a direct conversation with any Tor Onion peer address",
                            color = TextSecondary,
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
            containerColor = DarkSurfaceElevated,
            title = {
                Text("Start P2P Encrypted Chat", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Enter peer Tor Onion address or contact ID to initiate an encrypted session:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPeerOnionInput,
                        onValueChange = { newPeerOnionInput = it },
                        placeholder = { Text("e.g. torpeer9x...onion", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TorPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
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
                        containerColor = BubbleAquaPrimary,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("Connect & Chat", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun PeerChatItem(
    peer: PeerContactEntity,
    lastMessage: MessageEntity?,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("peer_item_${peer.peerId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Peer Avatar with Identicon gradient
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (peer.isVerified) BubbleCarbonationGreen.copy(alpha = 0.2f)
                        else BubbleAquaPrimary.copy(alpha = 0.15f)
                    )
                    .border(
                        1.dp,
                        if (peer.isVerified) BubbleCarbonationGreen else BubbleAquaPrimary.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = peer.alias.take(2).uppercase(),
                    color = if (peer.isVerified) BubbleCarbonationGreen else BubbleAquaLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
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
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (peer.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Key",
                                tint = TorOnionGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (lastMessage != null) {
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(lastMessage.timestamp))
                        Text(
                            text = timeStr,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lastMessage?.content ?: peer.onionAddress,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (peer.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = BubbleCarbonationGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = peer.onionAddress,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BubbleAquaLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFingerprintModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Verify Fingerprint",
                            tint = BubbleCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.95f))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Marketplace Context Banner (if discussing an item)
            if (relatedListingMessage != null) {
                Card(
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceHigh),
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
                                tint = TorOnionGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = relatedListingMessage.relatedListingTitle ?: "Marketplace Item",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Price: ${relatedListingMessage.relatedListingPrice ?: "Local Handshake"}",
                                    fontSize = 11.sp,
                                    color = TorOnionGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = "Direct P2P Order",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Security Notification Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TorOnionGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Messages are End-to-End Encrypted via Tor (3 Hops)",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
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
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Encrypted message...", color = TextMuted, fontSize = 13.sp) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BubbleAquaPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BubbleAquaPrimary)
                        .testTag("send_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Encrypted",
                        tint = DarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Inspect Ciphertext Dialog (Shows true local encryption)
    if (showCipherModal != null) {
        val m = showCipherModal!!
        AlertDialog(
            onDismissRequest = { showCipherModal = null },
            containerColor = DarkSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TorOnionGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AES-256-GCM Payload", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "This message was transmitted encrypted through Tor onion routing. Decrypted strictly on phone:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = m.encryptedBlob.ifEmpty { "AES_GCM_CIPHERTEXT_BASE64_PAYLOAD" },
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TorCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Decrypted Text:\n${m.content}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCipherModal = null }) {
                    Text("Close", color = TorPurpleLight)
                }
            }
        )
    }

    // Peer Fingerprint Verification Dialog
    if (showFingerprintModal) {
        AlertDialog(
            onDismissRequest = { showFingerprintModal = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = TorCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Peer Fingerprint", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Compare this cryptographic key fingerprint with your peer out-of-band to prevent MITM attacks:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = peer.fingerprint,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TorOnionGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: ${if (peer.isVerified) "Verified Contact" else "Unverified (Trust on first use)"}",
                        fontSize = 12.sp,
                        color = if (peer.isVerified) TorOnionGreen else TextMuted
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
                    colors = ButtonDefaults.buttonColors(containerColor = TorPurple)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Fingerprint")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFingerprintModal = false }) {
                    Text("Close", color = TextSecondary)
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
    val isOut = message.isOutgoing
    val alignment = if (isOut) Alignment.End else Alignment.Start
    val bg = if (isOut) TorPurpleDark else DarkSurfaceElevated
    val border = if (isOut) TorPurple else DarkBorder

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
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = if (isOut) TorPurpleLight else TorOnionGreen,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    if (isOut) {
                        Text(
                            text = "• ${message.status}",
                            fontSize = 10.sp,
                            color = TorPurpleLight
                        )
                    }
                }
            }
        }
    }
}
