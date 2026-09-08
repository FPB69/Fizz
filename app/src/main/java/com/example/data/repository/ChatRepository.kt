package com.example.data.repository

import com.example.data.crypto.CryptoManager
import com.example.data.database.MessageDao
import com.example.data.database.MessageEntity
import com.example.data.database.PeerContactDao
import com.example.data.database.PeerContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val messageDao: MessageDao,
    private val peerDao: PeerContactDao,
    private val cryptoManager: CryptoManager
) {
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val allPeers: Flow<List<PeerContactEntity>> = peerDao.getAllPeers()

    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForPeer(peerId)
    }

    suspend fun getPeer(peerId: String): PeerContactEntity? {
        return peerDao.getPeer(peerId)
    }

    suspend fun sendMessage(
        peerId: String,
        peerOnion: String,
        text: String,
        relatedListingId: String? = null,
        relatedListingTitle: String? = null,
        relatedListingPrice: String? = null
    ): MessageEntity = withContext(Dispatchers.IO) {
        val cipherPayload = cryptoManager.encryptAesGcm(text)
        val msg = MessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            peerId = peerId,
            peerOnion = peerOnion,
            content = text,
            encryptedBlob = cipherPayload,
            isOutgoing = true,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED",
            relatedListingId = relatedListingId,
            relatedListingTitle = relatedListingTitle,
            relatedListingPrice = relatedListingPrice
        )
        messageDao.insertMessage(msg)

        // Ensure peer contact exists
        val existing = peerDao.getPeer(peerId)
        if (existing == null) {
            peerDao.insertPeer(
                PeerContactEntity(
                    peerId = peerId,
                    alias = if (peerOnion.isNotEmpty()) peerOnion.take(14) + "..." else peerId,
                    onionAddress = peerOnion,
                    publicKey = "PUBKEY_${peerId}",
                    fingerprint = "E2:4B:91:0A:78:CD:1F:09",
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true
                )
            )
        } else {
            peerDao.insertPeer(existing.copy(lastSeen = System.currentTimeMillis()))
        }

        msg
    }

    suspend fun receiveIncomingMessage(
        senderId: String,
        senderOnion: String,
        content: String,
        listingId: String? = null,
        listingTitle: String? = null,
        listingPrice: String? = null
    ) = withContext(Dispatchers.IO) {
        val decrypted = cryptoManager.decryptAesGcm(content)
        val msg = MessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            peerId = senderId,
            peerOnion = senderOnion,
            content = decrypted,
            encryptedBlob = content,
            isOutgoing = false,
            timestamp = System.currentTimeMillis(),
            status = "VERIFIED",
            relatedListingId = listingId,
            relatedListingTitle = listingTitle,
            relatedListingPrice = listingPrice
        )
        messageDao.insertMessage(msg)

        val existing = peerDao.getPeer(senderId)
        if (existing == null) {
            peerDao.insertPeer(
                PeerContactEntity(
                    peerId = senderId,
                    alias = senderOnion.take(14) + "...",
                    onionAddress = senderOnion,
                    publicKey = "PUBKEY_${senderId}",
                    fingerprint = "8F:3A:C2:59:71:0D:E3:44",
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true
                )
            )
        }
    }

    suspend fun saveContact(peer: PeerContactEntity) {
        peerDao.insertPeer(peer)
    }

    suspend fun verifyPeer(peerId: String, verified: Boolean) {
        peerDao.updateVerification(peerId, verified)
    }

    suspend fun deleteConversation(peerId: String) {
        messageDao.deleteConversation(peerId)
    }

    suspend fun seedInitialChatsIfEmpty() = withContext(Dispatchers.IO) {
        val existingPeers = peerDao.getAllPeers().first()
        if (existingPeers.isNotEmpty()) return@withContext

        val peer1 = PeerContactEntity(
            peerId = "peer_cypher99",
            alias = "Cypherpunk Node #99",
            onionAddress = "torpeer4kx92am7z6qp31b.onion",
            publicKey = "RSA_PUB_4kx92am7z6qp31b",
            fingerprint = "A4:91:0E:5B:3C:77:2F:88",
            lastSeen = System.currentTimeMillis() - 120000,
            isOnline = true,
            circuitHops = 3,
            isVerified = true
        )
        val peer2 = PeerContactEntity(
            peerId = "peer_shield_vault",
            alias = "ShieldVault Dealer",
            onionAddress = "privacyvault89vckl12w.onion",
            publicKey = "RSA_PUB_89vckl12w",
            fingerprint = "B2:77:88:99:AA:BB:CC:DD",
            lastSeen = System.currentTimeMillis() - 1800000,
            isOnline = true,
            circuitHops = 3,
            isVerified = false
        )

        peerDao.insertPeers(listOf(peer1, peer2))

        val initialMessages = listOf(
            MessageEntity(
                id = "msg_init_01",
                peerId = "peer_cypher99",
                peerOnion = "torpeer4kx92am7z6qp31b.onion",
                content = "Greetings! My onion node is online and serving direct P2P listings.",
                encryptedBlob = "U2VjdXJlSW5pdGlhbFBheWxvYWQ=",
                isOutgoing = false,
                timestamp = System.currentTimeMillis() - 7200000,
                status = "VERIFIED"
            ),
            MessageEntity(
                id = "msg_init_02",
                peerId = "peer_cypher99",
                peerOnion = "torpeer4kx92am7z6qp31b.onion",
                content = "I have 2 units of the Air-Gapped Hardware Crypto Vault ready for local drop or encrypted parcel.",
                encryptedBlob = "VmVyaWZpZWRFbmNyeXB0ZWRNZXNzYWdl",
                isOutgoing = false,
                timestamp = System.currentTimeMillis() - 3600000,
                status = "VERIFIED",
                relatedListingId = "lst_peer_01",
                relatedListingTitle = "Air-Gapped Hardware Crypto Vault",
                relatedListingPrice = "0.028 XMR"
            ),
            MessageEntity(
                id = "msg_init_03",
                peerId = "peer_shield_vault",
                peerOnion = "privacyvault89vckl12w.onion",
                content = "Signal-blocking go-bags restocked. No cloud trace, pure peer-to-peer.",
                encryptedBlob = "U2hpZWxkVmF1bHRFbmNyeXB0ZWQ=",
                isOutgoing = false,
                timestamp = System.currentTimeMillis() - 14400000,
                status = "VERIFIED",
                relatedListingId = "lst_peer_02",
                relatedListingTitle = "Faraday Signal-Blocking Go-Bag",
                relatedListingPrice = "45 USD Cash"
            )
        )

        for (m in initialMessages) {
            messageDao.insertMessage(m)
        }
    }

    suspend fun wipeAll() = withContext(Dispatchers.IO) {
        messageDao.clearAll()
        peerDao.clearAll()
    }
}
