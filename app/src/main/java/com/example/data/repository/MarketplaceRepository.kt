package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.ListingDao
import com.example.data.database.ListingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MarketplaceRepository(
    private val context: Context,
    private val listingDao: ListingDao,
    private val myPeerId: String,
    private val myOnionAddress: String
) {
    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val myListings: Flow<List<ListingEntity>> = listingDao.getMyListings()
    val peerListings: Flow<List<ListingEntity>> = listingDao.getPeerListings()

    private val photosDir = File(context.filesDir, "listings_photos").apply {
        if (!exists()) mkdirs()
    }

    suspend fun createMyListing(
        title: String,
        description: String,
        price: String,
        currency: String,
        category: String,
        imageUri: Uri?,
        fallbackDrawable: String = ""
    ): ListingEntity = withContext(Dispatchers.IO) {
        val id = "lst_${UUID.randomUUID().toString().take(8)}"
        var photoPath = fallbackDrawable

        if (imageUri != null) {
            try {
                val photoFile = File(photosDir, "$id.jpg")
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(photoFile).use { output ->
                        input.copyTo(output)
                    }
                }
                photoPath = photoFile.absolutePath
            } catch (_: Exception) {}
        }

        val listing = ListingEntity(
            id = id,
            title = title,
            description = description,
            price = price,
            currency = currency,
            category = category,
            photoPath = photoPath,
            sellerPeerId = myPeerId,
            sellerOnion = myOnionAddress,
            createdAt = System.currentTimeMillis(),
            isMine = true,
            inStock = true,
            deliveryMethod = "P2P Encrypted Handshake"
        )
        listingDao.insertListing(listing)
        listing
    }

    suspend fun importPeerListings(listings: List<ListingEntity>) {
        listingDao.insertListings(listings)
    }

    suspend fun deleteListing(id: String) {
        listingDao.deleteListing(id)
    }

    suspend fun toggleStock(listing: ListingEntity) {
        listingDao.updateListing(listing.copy(inStock = !listing.inStock))
    }

    suspend fun getListing(id: String): ListingEntity? {
        return listingDao.getListingById(id)
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentListings = listingDao.getAllListings().first()
        if (currentListings.isNotEmpty()) return@withContext

        // Pre-seed owner's initial local item
        val myInitialListing = ListingEntity(
            id = "lst_local_01",
            title = "Encrypted LoRa Mesh Communicator",
            description = "Custom firmware off-grid long-range encrypted text radio. Zero internet required. Direct point-to-point 868/915MHz with local AES-256 keys.",
            price = "0.045",
            currency = "XMR",
            category = "Hardware",
            photoPath = "sample_lora_radio_1788876517455",
            sellerPeerId = myPeerId,
            sellerOnion = myOnionAddress,
            createdAt = System.currentTimeMillis() - 3600000,
            isMine = true,
            inStock = true,
            deliveryMethod = "P2P Drop / Secure Mail"
        )

        // Pre-seed peer listings discovered over Tor network from decentralized nodes
        val peer1Onion = "torpeer4kx92am7z6qp31b.onion"
        val peer2Onion = "privacyvault89vckl12w.onion"
        val peer3Onion = "cypherpunknode990x1a.onion"

        val peerListings = listOf(
            ListingEntity(
                id = "lst_peer_01",
                title = "Air-Gapped Hardware Crypto Vault",
                description = "Dedicated secure element signer with camera QR verification. No Bluetooth, no Wi-Fi, zero radio emission. Pure airgap security.",
                price = "0.028",
                currency = "XMR",
                category = "Hardware",
                photoPath = "sample_hardware_wallet_1788876501106",
                sellerPeerId = "peer_cypher99",
                sellerOnion = peer1Onion,
                createdAt = System.currentTimeMillis() - 7200000,
                isMine = false,
                inStock = true,
                deliveryMethod = "Dead-Drop or Tor Mail"
            ),
            ListingEntity(
                id = "lst_peer_02",
                title = "Faraday Signal-Blocking Go-Bag",
                description = "Military grade dual-layer RF shielding. Blocks 5G, GPS, RFID, WiFi, and Bluetooth. Tested -90dB attenuation across 100MHz-10GHz.",
                price = "45",
                currency = "USD Cash",
                category = "Physical Goods",
                photoPath = "sample_lora_radio_1788876517455",
                sellerPeerId = "peer_shield_vault",
                sellerOnion = peer2Onion,
                createdAt = System.currentTimeMillis() - 14400000,
                isMine = false,
                inStock = true,
                deliveryMethod = "In-Person P2P Handshake"
            ),
            ListingEntity(
                id = "lst_peer_03",
                title = "Encrypted IronKey Vault Drive 128GB",
                description = "FIPS 140-2 Level 3 hardware encrypted flash drive. Self-destruct PIN mechanism after 10 failed attempts. Waterproof epoxy coated.",
                price = "250,000",
                currency = "Sats",
                category = "Privacy Tools",
                photoPath = "sample_hardware_wallet_1788876501106",
                sellerPeerId = "peer_iron_node",
                sellerOnion = peer3Onion,
                createdAt = System.currentTimeMillis() - 28800000,
                isMine = false,
                inStock = true,
                deliveryMethod = "P2P Postal Drop"
            )
        )

        listingDao.insertListing(myInitialListing)
        listingDao.insertListings(peerListings)
    }

    suspend fun wipeAll() = withContext(Dispatchers.IO) {
        listingDao.clearAll()
        photosDir.listFiles()?.forEach { it.delete() }
    }
}
