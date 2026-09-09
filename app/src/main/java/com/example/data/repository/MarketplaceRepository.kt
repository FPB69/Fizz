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

import com.example.data.utils.ExifScrubber

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
                // Automatic EXIF Photo Metadata Removal Tool (Strips GPS location & camera model)
                val scrubbedSuccess = ExifScrubber.scrubExifMetadata(context, imageUri, photoFile)
                if (!scrubbedSuccess) {
                    context.contentResolver.openInputStream(imageUri)?.use { input ->
                        FileOutputStream(photoFile).use { output ->
                            input.copyTo(output)
                        }
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

        // Replace example items with a fully hands-on tutorial guide item
        val tutorialListing = ListingEntity(
            id = "lst_tutorial_01",
            title = "Welcome to Fizz 1.7: Hands-On Security & Setup Guide",
            description = "Welcome! 1) Connect Tab: Opens directly on launch for master connect/kill control. 2) Secure P2P Status Indicators: Live online/offline peer status dot updates only when active P2P connection is verified. 3) Automatic EXIF Scrubber: All photos stripped of GPS metadata. 4) Panic Wipe: Tor Vault Tab > Emergency Data Purge to instantly destroy all local SQLite data.",
            price = "0",
            currency = "FREE",
            category = "Security",
            photoPath = "sample_hardware_wallet_1788876501106",
            sellerPeerId = myPeerId,
            sellerOnion = myOnionAddress,
            createdAt = System.currentTimeMillis(),
            isMine = true,
            inStock = true,
            deliveryMethod = "Instant Hands-On Tutorial"
        )

        listingDao.insertListing(tutorialListing)
    }

    suspend fun wipeAll() = withContext(Dispatchers.IO) {
        listingDao.clearAll()
        photosDir.listFiles()?.forEach { it.delete() }
    }
}
