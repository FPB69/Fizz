package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.TorStatusBadge
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TorCyan
import com.example.ui.theme.TorOnionGreen
import com.example.ui.theme.TorPurple
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.TorPeerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: TorPeerViewModel,
    modifier: Modifier = Modifier
) {
    val allListings by viewModel.allListings.collectAsStateWithLifecycle()
    val torStatus by viewModel.torStatus.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isConnecting by viewModel.isConnectingPeer.collectAsStateWithLifecycle()

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
                containerColor = BubbleAquaPrimary,
                contentColor = DarkBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("connect_peer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AddLink, contentDescription = "Connect Peer")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect Peer Store", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with App Title & Tor Status Badge
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Fizz",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = BubbleAquaPrimary
                            )
                            Text(
                                text = " Market",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = BubbleFoamWhite
                            )
                        }
                        Text(
                            text = "Effervescent P2P Marketplace • 100% Local",
                            fontSize = 12.sp,
                            color = BubbleAquaLight
                        )
                    }

                    TorStatusBadge(
                        status = torStatus,
                        onClick = { viewModel.setNavTab(AppNavTab.SECURITY) }
                    )
                }
            }

            // Hero Banner & Zero Cloud Notice
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fizz_bubble_banner_1788879151068),
                        contentDescription = "Fizz Bubble Marketplace Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Dark gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DarkBackground.copy(alpha = 0.95f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = BubbleCarbonationGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% LOCAL & ZERO CLOUD STORAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BubbleCarbonationGreen,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Photos, descriptions & prices live strictly on the owner's phone",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search decentralized listings...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = BubbleAquaPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
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
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BubbleAquaPrimary,
                                selectedLabelColor = DarkBackground,
                                containerColor = DarkSurfaceElevated,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) BubbleAquaPrimary else DarkBorder
                            )
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
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No listings found in this category",
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Connect to a peer node via .onion or add your local items in My Store",
                                color = TextMuted,
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
            containerColor = DarkSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = BubbleAquaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect Peer Storefront", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter the peer's Tor Onion address (.onion) or direct P2P LAN host (e.g. 192.168.1.50:8989) to fetch their locally hosted catalog:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = peerAddressInput,
                        onValueChange = { peerAddressInput = it },
                        placeholder = { Text("e.g. torpeer3v9x...onion or 192.168.1.100:8989", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BubbleAquaPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("peer_address_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Example pre-seeded peer node:\ntorpeer4kx92am7z6qp31b.onion",
                        color = BubbleCyan,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHigh)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BubbleCarbonationGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transparent: Direct P2P over Tor. 0 central logging.",
                            fontSize = 10.sp,
                            color = BubbleAquaLight
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
                        containerColor = BubbleAquaPrimary,
                        contentColor = DarkBackground
                    ),
                    enabled = !isConnecting,
                    modifier = Modifier.testTag("connect_confirm_button")
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DarkBackground, strokeWidth = 2.dp)
                    } else {
                        Text("Connect & Fetch", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
