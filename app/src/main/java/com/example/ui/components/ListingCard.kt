package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.database.ListingEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHigh
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TorCyan
import com.example.ui.theme.TorOnionGreen
import com.example.ui.theme.TorPurple
import com.example.ui.theme.TorPurpleLight
import java.io.File

@Composable
fun ListingCard(
    listing: ListingEntity,
    onInquireOrBuy: (ListingEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onToggleStock: ((ListingEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("listing_card_${listing.id}")
    ) {
        Column {
            // Photo Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(DarkSurfaceHigh)
            ) {
                ListingPhotoViewer(listing = listing, context = context)

                // Top Badges Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Storage Location Badge (Mandatory distinction: local phone vs peer)
                    if (listing.isMine) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                                .border(1.dp, TorOnionGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = TorOnionGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "On Owner Phone (Local)",
                                color = TorOnionGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                                .border(1.dp, TorPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = TorPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Peer .onion",
                                color = TorPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Stock badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (listing.inStock) TorOnionGreen.copy(alpha = 0.15f)
                                else AccentAmber.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (listing.inStock) TorOnionGreen.copy(alpha = 0.4f)
                                else AccentAmber.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (listing.inStock) "In Stock" else "Reserved",
                            color = if (listing.inStock) TorOnionGreen else AccentAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Price pill at bottom corner of image
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TorPurple, TorCyan)
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${listing.price} ${listing.currency}",
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Body Area
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    SuggestionChip(
                        onClick = {},
                        label = { Text(listing.category, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = DarkSurfaceHigh,
                            labelColor = TextSecondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = DarkBorder
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata: Delivery & Peer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.deliveryMethod,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = if (listing.isMine) "Owner Node" else listing.sellerOnion.take(16) + "...",
                        fontSize = 11.sp,
                        color = TorPurple,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                if (listing.isMine) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onToggleStock?.invoke(listing) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (listing.inStock) "Mark Reserved" else "Mark In Stock", fontSize = 12.sp)
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = { onDelete(listing.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceHigh)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Local Listing",
                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { onInquireOrBuy(listing) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inquire_button_${listing.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TorPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("P2P Encrypted Order / Inquiry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ListingPhotoViewer(listing: ListingEntity, context: Context) {
    val path = listing.photoPath

    when {
        path.startsWith("/") -> {
            val file = File(path)
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                FallbackPhotoBanner(title = listing.title)
            }
        }
        path.contains("sample_hardware_wallet") -> {
            Image(
                painter = painterResource(id = R.drawable.sample_hardware_wallet_1788876501106),
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
        path.contains("sample_lora_radio") -> {
            Image(
                painter = painterResource(id = R.drawable.sample_lora_radio_1788876517455),
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
        path.contains("p2p_market_banner") -> {
            Image(
                painter = painterResource(id = R.drawable.p2p_market_banner_1788876384012),
                contentDescription = listing.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
        else -> {
            FallbackPhotoBanner(title = listing.title)
        }
    }
}

@Composable
fun FallbackPhotoBanner(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.linearGradient(
                    listOf(DarkSurfaceElevated, DarkSurfaceHigh)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = TorPurpleLight.copy(alpha = 0.5f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Local Encrypted Photo Storage",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}
