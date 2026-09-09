package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.ListingEntity
import com.example.ui.components.ListingCard
import com.example.ui.components.PeerQrConnectionDialog
import com.example.ui.components.TorStatusBadge
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TorPeerViewModel

@Composable
fun MarketplaceScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    val allListings by viewModel.allListings.collectAsStateWithLifecycle()
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val showPeerQrDialog by viewModel.showPeerQrDialog.collectAsStateWithLifecycle()

    var contactTargetListing by remember { mutableStateOf<ListingEntity?>(null) }
    var talkRequestMessage by remember { mutableStateOf("") }

    // Popular suggested privacy keyword tags
    val suggestedKeywords = remember {
        listOf("Hardware", "Encrypted", "LoRa", "Vault", "Faraday", "XMR", "Air-Gapped", "Sats", "Security")
    }

    // Dynamic category list combining defaults + all custom categories from listings
    val categories = remember(allListings) {
        val standardDefaults = listOf("Hardware", "Software", "Security", "Physical Goods", "Services")
        val customFromListings = allListings.map { it.category.trim() }.filter { it.isNotBlank() }
        (listOf("All") + standardDefaults + customFromListings).distinct()
    }

    // Local-only tokenized search engine
    val filteredListings = remember(allListings, selectedCategory, searchQuery) {
        val tokens = searchQuery.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        allListings.filter { listing ->
            val matchesCategory = selectedCategory == "All" || listing.category.equals(selectedCategory, ignoreCase = true)
            if (tokens.isEmpty()) {
                matchesCategory
            } else {
                val searchableText = "${listing.title} ${listing.description} ${listing.category} ${listing.price} ${listing.currency} ${listing.deliveryMethod} ${listing.sellerOnion}".lowercase()
                val matchesKeywords = tokens.all { token -> searchableText.contains(token) }
                matchesCategory && matchesKeywords
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openPeerQrDialog() },
                containerColor = theme.accentPrimary,
                contentColor = theme.background,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("connect_peer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.QrCode2, contentDescription = "Pair via QR")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR Direct Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with App Title, Dark/Light Switcher, and Tor Status Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Fizz 1.4",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = theme.accentPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Market",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.5.sp,
                                color = theme.textPrimary
                            )
                        }
                        Text(
                            text = "Direct P2P listings over Tor",
                            fontSize = 11.sp,
                            color = theme.textSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Theme Switcher Button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceElevated)
                                .border(1.dp, theme.borderGold, CircleShape)
                                .clickable { viewModel.toggleTheme() }
                                .testTag("theme_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        TorStatusBadge(
                            status = torStatus,
                            onClick = { viewModel.setNavTab(AppNavTab.SECURITY) }
                        )
                    }
                }
            }

            // Search Bar & QR Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search listings, keywords, onion ID...", fontSize = 12.sp, color = theme.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = theme.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.border,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedContainerColor = theme.surfaceElevated.copy(alpha = 0.9f),
                            unfocusedContainerColor = theme.surfaceElevated.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("marketplace_search_input")
                    )

                    // Quick QR Button
                    IconButton(
                        onClick = { viewModel.openPeerQrDialog() },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.borderGold, RoundedCornerShape(12.dp))
                            .testTag("btn_market_qr")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "P2P QR Direct Link",
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // On-Device Search Privacy Banner & Results Summary
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceElevated)
                        .border(1.dp, if (searchQuery.isNotBlank()) theme.accentPrimary.copy(alpha = 0.5f) else theme.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(theme.accentPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "On-Device Search Privacy",
                                    tint = theme.accentPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (searchQuery.isBlank()) "100% On-Device Local Search Index" else "Found ${filteredListings.size} listing(s) matching '$searchQuery'",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    modifier = Modifier.testTag("privacy_search_badge")
                                )
                                Text(
                                    text = "Zero network transmission • Search queries remain isolated in phone memory",
                                    fontSize = 9.5.sp,
                                    color = theme.textMuted
                                )
                            }
                        }

                        if (searchQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.accentPrimary.copy(alpha = 0.12f))
                                    .clickable { viewModel.setSearchQuery("") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("clear_search_button")
                            ) {
                                Text(
                                    text = "Reset Search",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accentPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips (Standard + Custom Categories)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(category) },
                            label = { Text(category, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.accentPrimary,
                                selectedLabelColor = theme.background,
                                containerColor = theme.surfaceElevated,
                                labelColor = theme.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) theme.accentPrimary else theme.border
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            // Quick Keyword Tags Filter Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Tags:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMuted
                        )
                    }

                    suggestedKeywords.forEach { tag ->
                        val isActive = searchQuery.contains(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isActive) theme.accentPrimary else theme.surfaceElevated.copy(alpha = 0.6f))
                                .border(0.5.dp, if (isActive) theme.accentPrimary else theme.border, RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isActive) {
                                        viewModel.setSearchQuery("")
                                    } else {
                                        viewModel.setSearchQuery(tag)
                                    }
                                }
                                .padding(horizontal = 9.dp, vertical = 3.5.dp)
                                .testTag("keyword_chip_$tag")
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                color = if (isActive) theme.background else theme.textSecondary,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Listings List
            if (filteredListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "No listings found in this category",
                                fontSize = 13.sp,
                                color = theme.textSecondary
                            )
                            Button(
                                onClick = { viewModel.openPeerQrDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.accentPrimary, contentColor = theme.background),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect Peer via QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredListings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        onInquireOrBuy = { selectedListing ->
                            val peer = peers.find { it.peerId == selectedListing.sellerPeerId }
                            if (peer != null && peer.connectionStatus == "CONNECTED") {
                                viewModel.selectChat(selectedListing.sellerPeerId)
                            } else {
                                talkRequestMessage = "Hi, I'm interested in '${selectedListing.title}' (${selectedListing.price} ${selectedListing.currency})."
                                contactTargetListing = selectedListing
                            }
                        }
                    )
                }
            }
        }
    }

    // Contact Seller / Talk Request Dialog
    if (contactTargetListing != null) {
        val target = contactTargetListing!!
        AlertDialog(
            onDismissRequest = { contactTargetListing = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(theme.accentPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = theme.accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact Seller",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "To establish a private P2P channel, send a talk request. Once accepted by the seller, encrypted messaging activates.",
                        fontSize = 12.sp,
                        color = theme.textSecondary,
                        lineHeight = 16.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surface)
                            .border(1.dp, theme.border, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = target.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "${target.price} ${target.currency} • Category: ${target.category}",
                                fontSize = 11.5.sp,
                                color = theme.accentPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Seller: ${target.sellerOnion}",
                                fontSize = 10.sp,
                                color = theme.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    OutlinedTextField(
                        value = talkRequestMessage,
                        onValueChange = { talkRequestMessage = it },
                        label = { Text("Introductory Note", fontSize = 11.sp) },
                        placeholder = { Text("Write a message to seller...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.accentPrimary,
                            unfocusedBorderColor = theme.border,
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendTalkRequest(
                            peerId = target.sellerPeerId,
                            peerAlias = target.sellerOnion.take(14) + "...",
                            peerOnion = target.sellerOnion,
                            listingId = target.id,
                            listingTitle = target.title,
                            listingPrice = "${target.price} ${target.currency}",
                            initialMessage = talkRequestMessage
                        )
                        contactTargetListing = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Talk Request", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactTargetListing = null }) {
                    Text("Cancel", fontSize = 12.sp, color = theme.textMuted)
                }
            },
            containerColor = theme.surfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
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
}
