package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.database.ListingEntity
import com.example.ui.components.ListingCard
import com.example.ui.components.NetworkVitalsCard
import com.example.ui.components.TorStatusBadge
import com.example.ui.theme.LocalMedicalTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TorPeerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalMedicalTheme.current
    val allListings by viewModel.allListings.collectAsStateWithLifecycle()
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val networkVitals by viewModel.networkVitals.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnectingPeer.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var showConnectDialog by remember { mutableStateOf(false) }
    var peerAddressInput by remember { mutableStateOf("") }

    val categories = listOf("All", "Hardware", "Privacy Tools", "Physical Goods", "Digital")

    val filteredListings = allListings.filter { listing ->
        val matchesCategory = selectedCategory == "All" || listing.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isEmpty() ||
                listing.title.contains(searchQuery, ignoreCase = true) ||
                listing.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showConnectDialog = true },
                containerColor = theme.accentPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("connect_peer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AddLink, contentDescription = "Connect Peer")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect Peer Store", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
                                text = "FIZZ",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = theme.accentPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MARKET",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Light,
                                letterSpacing = 0.5.sp,
                                color = theme.textPrimary
                            )
                        }
                        Text(
                            text = "Decentralized P2P • 100% On-Device",
                            fontSize = 11.5.sp,
                            color = theme.textSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Theme Switcher Button (Dark / Light toggle)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(theme.surface)
                                .border(1.dp, theme.border, CircleShape)
                                .clickable { viewModel.toggleTheme() }
                                .testTag("theme_toggle_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark/Light Mode",
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

            // Real-time Anonymity & Tor Network Vitals Card
            item {
                NetworkVitalsCard(
                    vitals = networkVitals,
                    torStatus = torStatus,
                    onProbe = { viewModel.probeTorConnectivity() }
                )
            }

            // Zero Cloud Security Assurance Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.border, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.alertGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = theme.alertGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% LOCAL & ZERO CLOUD STORAGE",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.alertGreen,
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = "Photos, descriptions & database records reside strictly on your device.",
                                fontSize = 11.5.sp,
                                color = theme.textSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search listings, hardware, tools...", color = theme.textMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = theme.surface,
                        unfocusedContainerColor = theme.surface,
                        focusedBorderColor = theme.accentPrimary,
                        unfocusedBorderColor = theme.border,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marketplace_search_input")
                )
            }

            // Category Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedCategory(cat) },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.accentPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = theme.surface,
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

            // Listings List or Empty State
            if (filteredListings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = theme.textMuted,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No listings found in this category",
                                color = theme.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Connect to a peer node via .onion or add your local items in My Store",
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredListings, key = { it.id }) { listing ->
                    ListingCard(
                        listing = listing,
                        onInquireOrBuy = { targetListing ->
                            viewModel.selectChat(targetListing.sellerPeerId)
                            viewModel.sendMessage(
                                peerId = targetListing.sellerPeerId,
                                peerOnion = targetListing.sellerOnion,
                                text = "Hi! I am interested in purchasing your listing: ${targetListing.title} for ${targetListing.price} ${targetListing.currency}. Is this still available for P2P handshake?",
                                relatedListingId = targetListing.id,
                                relatedListingTitle = targetListing.title,
                                relatedListingPrice = "${targetListing.price} ${targetListing.currency}"
                            )
                        }
                    )
                }
            }

            // Footer padding for FAB
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Connect Peer Dialog
    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            containerColor = theme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = theme.accentPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Peer Storefront", color = theme.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter the peer's Tor Onion address (.onion) or direct P2P LAN host (e.g. 192.168.1.50:8989) to fetch their locally hosted catalog:",
                        color = theme.textSecondary,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = peerAddressInput,
                        onValueChange = { peerAddressInput = it },
                        placeholder = { Text("e.g. torpeer3v9x...onion or 192.168.1.100:8989", color = theme.textMuted, fontSize = 12.sp) },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("peer_address_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Example pre-seeded peer node:\ntorpeer4kx92am7z6qp31b.onion",
                        color = theme.accentPrimary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.surfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = theme.alertGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Direct SOCKS5 proxy routing. Zero central tracking.",
                            fontSize = 10.5.sp,
                            color = theme.textSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val addr = peerAddressInput.trim().ifEmpty { "torpeer4kx92am7z6qp31b.onion" }
                        viewModel.connectToPeer(addr)
                        showConnectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.accentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isConnecting,
                    modifier = Modifier.testTag("connect_confirm_button")
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Connect & Fetch", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            }
        )
    }
}
