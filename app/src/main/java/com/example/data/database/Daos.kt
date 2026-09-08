package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isMine = 1 ORDER BY createdAt DESC")
    fun getMyListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isMine = 0 ORDER BY createdAt DESC")
    fun getPeerListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)

    @Query("DELETE FROM listings WHERE isMine = 0")
    suspend fun clearPeerListings()

    @Query("DELETE FROM listings")
    suspend fun clearAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE peerId = :peerId ORDER BY timestamp ASC")
    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE peerId = :peerId")
    suspend fun deleteConversation(peerId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}

@Dao
interface PeerContactDao {
    @Query("SELECT * FROM peer_contacts ORDER BY lastSeen DESC")
    fun getAllPeers(): Flow<List<PeerContactEntity>>

    @Query("SELECT * FROM peer_contacts WHERE peerId = :peerId LIMIT 1")
    suspend fun getPeer(peerId: String): PeerContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeer(peer: PeerContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeers(peers: List<PeerContactEntity>)

    @Query("UPDATE peer_contacts SET isVerified = :verified WHERE peerId = :peerId")
    suspend fun updateVerification(peerId: String, verified: Boolean)

    @Query("DELETE FROM peer_contacts WHERE peerId = :peerId")
    suspend fun deletePeer(peerId: String)

    @Query("DELETE FROM peer_contacts")
    suspend fun clearAll()
}
