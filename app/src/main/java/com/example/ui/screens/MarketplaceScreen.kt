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

    // Dynamic category list combining defaults + all custom categories from listings
    val categories = remember(allListings) {
        val standardDefaults = listOf("Hardware", "Software", "Security", "Physical Goods", "Services")
        val customFromListings = allListings.map { it.category.trim() }.filter { it.isNotBlank() }
        (listOf("All") + standardDefaults + customFromListings).distinct()
    }

    val filteredListings = allListings.filter { listing ->
        val matchesCategory = selectedCategory == "All" || listing.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isEmpty() ||
                listing.title.contains(searchQuery, ignoreCase = true) ||
                listing.description.contains(searchQuery, ignoreCase = true) ||
                listing.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
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
                                text = "Fizz 1.2",
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
                        placeholder = { Text("Search listings, custom categories...", fontSize = 12.sp, color = theme.textMuted) },
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
