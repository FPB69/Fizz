package com.example.ui.components

import android.content.Context
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.LocalMedicalTheme
import java.io.File

/**
 * Super Minimalist Listing Card
 * Styled strictly according to the Medical App Dark/Light Theme aesthetic.
 */
@Composable
fun ListingCard(
    listing: ListingEntity,
    onInquireOrBuy: (ListingEntity) -> Unit,
    onDelete: ((String) -> Unit)? = null,
    onToggleStock: ((ListingEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalMedicalTheme.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, theme.border, RoundedCornerShape(18.dp))
            .then(
                if (!listing.isMine) {
                    Modifier.clickable { onInquireOrBuy(listing) }
                } else Modifier
            )
            .testTag("listing_card_${listing.id}")
    ) {
        Column {
            // Photo Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(theme.surfaceElevated)
            ) {
                ListingPhotoViewer(listing = listing, context = context)

                // Status Badges Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (listing.isMine) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surface.copy(alpha = 0.9f))
                                .border(1.dp, theme.accentSecondary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = theme.accentSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Local Vault",
                                color = theme.accentSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.surface.copy(alpha = 0.9f))
                                .border(1.dp, theme.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = theme.accentPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Tor Peer",
                                color = theme.accentPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Stock badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (listing.inStock) theme.alertGreen.copy(alpha = 0.12f)
                                else theme.alertAmber.copy(alpha = 0.12f)
                            )
                            .border(
                                1.dp,
                                if (listing.inStock) theme.alertGreen.copy(alpha = 0.4f)
                                else theme.alertAmber.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (listing.inStock) "In Stock" else "Reserved",
                            color = if (listing.inStock) theme.alertGreen else theme.alertAmber,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Price Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.accentPrimary)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${listing.price} ${listing.currency}",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Card Body
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.surfaceElevated)
                            .border(1.dp, theme.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = listing.category,
                            fontSize = 10.sp,
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = listing.description,
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
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
                            tint = theme.textMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = listing.deliveryMethod,
                            fontSize = 11.sp,
                            color = theme.textMuted
                        )
                    }

                    Text(
                        text = if (listing.isMine) "Local Node" else listing.sellerOnion.take(14) + "...",
                        fontSize = 10.5.sp,
                        color = theme.accentPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                                tint = theme.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (listing.inStock) "Mark Reserved" else "Mark In Stock",
                                fontSize = 11.5.sp,
                                color = theme.textPrimary
                            )
                        }

                        if (onDelete != null) {
                            IconButton(
                                onClick = { onDelete(listing.id) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(theme.surfaceElevated)
                                    .border(1.dp, theme.border, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Local Listing",
                                    tint = theme.alertRed,
                                    modifier = Modifier.size(18.dp)
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
                            containerColor = theme.accentPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Contact Seller",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListingPhotoViewer(listing: ListingEntity, context: Context) {
    val path = listing.photoPath
    val theme = LocalMedicalTheme.current

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
                DefaultListingPlaceholder(title = listing.title, theme = theme)
            }
        }
        path.isNotEmpty() -> {
            val resId = context.resources.getIdentifier(path, "drawable", context.packageName)
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                DefaultListingPlaceholder(title = listing.title, theme = theme)
            }
        }
        else -> {
            DefaultListingPlaceholder(title = listing.title, theme = theme)
        }
    }
}

@Composable
private fun DefaultListingPlaceholder(title: String, theme: com.example.ui.theme.MedicalThemeColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(theme.surfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = theme.accentPrimary.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title.take(20),
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
