package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val currency: String,
    val category: String,
    val photoPath: String,
    val sellerPeerId: String,
    val sellerOnion: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isMine: Boolean = false,
    val inStock: Boolean = true,
    val deliveryMethod: String = "P2P Encrypted Handshake"
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val peerId: String,
    val peerOnion: String,
    val content: String,
    val encryptedBlob: String = "",
    val isOutgoing: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED", // SENT, DELIVERED, RECEIVED, VERIFIED
    val relatedListingId: String? = null,
    val relatedListingTitle: String? = null,
    val relatedListingPrice: String? = null,
    val autoDestructSeconds: Long? = null, // e.g., 10, 30, 60, 300, 3600, 86400 or null (permanent)
    val expiresAt: Long? = null // epoch ms timestamp when message is permanently purged from SQLite
)

@Entity(tableName = "peer_contacts")
data class PeerContactEntity(
    @PrimaryKey
    val peerId: String,
    val alias: String,
    val onionAddress: String,
    val publicKey: String,
    val fingerprint: String,
    val lastSeen: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true,
    val circuitHops: Int = 3,
    val isVerified: Boolean = false
)
