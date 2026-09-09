package com.example.data.repository

import com.example.data.crypto.CryptoManager
import com.example.data.database.MessageDao
import com.example.data.database.MessageEntity
import com.example.data.database.PeerContactDao
import com.example.data.database.PeerContactEntity
import com.example.data.database.TalkRequestDao
import com.example.data.database.TalkRequestEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository(
    private val messageDao: MessageDao,
    private val peerDao: PeerContactDao,
    private val talkRequestDao: TalkRequestDao,
    private val cryptoManager: CryptoManager
) {
    val allMessages: Flow<List<MessageEntity>> = messageDao.getAllMessages()
    val allPeers: Flow<List<PeerContactEntity>> = peerDao.getAllPeers()
    val connectedPeers: Flow<List<PeerContactEntity>> = peerDao.getConnectedPeers()
    val allTalkRequests: Flow<List<TalkRequestEntity>> = talkRequestDao.getAllTalkRequests()
    val pendingIncomingRequests: Flow<List<TalkRequestEntity>> = talkRequestDao.getPendingIncomingRequests()

    fun getMessagesForPeer(peerId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForPeer(peerId)
    }

    suspend fun getPeer(peerId: String): PeerContactEntity? {
        return peerDao.getPeer(peerId)
    }

    suspend fun getLatestRequestForPeer(peerId: String): TalkRequestEntity? {
        return talkRequestDao.getLatestRequestForPeer(peerId)
    }

    suspend fun sendTalkRequest(
        peerId: String,
        peerAlias: String,
        peerOnion: String,
        listingId: String? = null,
        listingTitle: String? = null,
        listingPrice: String? = null,
        initialMessage: String = "Hi, I would like to connect regarding your listing."
    ): TalkRequestEntity = withContext(Dispatchers.IO) {
        val requestId = "req_${UUID.randomUUID().toString().take(8)}"
        val request = TalkRequestEntity(
            id = requestId,
            peerId = peerId,
            peerAlias = peerAlias.ifEmpty { peerOnion.take(14) + "..." },
            peerOnion = peerOnion,
            listingId = listingId,
            listingTitle = listingTitle,
            listingPrice = listingPrice,
            isIncoming = false,
            status = "PENDING",
            initialMessage = initialMessage,
            timestamp = System.currentTimeMillis()
        )
        talkRequestDao.insertRequest(request)

        val existing = peerDao.getPeer(peerId)
        if (existing == null) {
            peerDao.insertPeer(
                PeerContactEntity(
                    peerId = peerId,
                    alias = peerAlias.ifEmpty { peerOnion.take(14) + "..." },
                    onionAddress = peerOnion,
                    publicKey = "PUB_${peerId.take(12)}",
                    fingerprint = "FP_${peerOnion.take(8).uppercase()}",
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true,
                    connectionStatus = "PENDING_SENT"
                )
            )
        } else {
            peerDao.insertPeer(existing.copy(connectionStatus = "PENDING_SENT"))
        }

        request
    }

    suspend fun receiveIncomingTalkRequest(
        peerId: String,
        peerAlias: String,
        peerOnion: String,
        listingId: String? = null,
        listingTitle: String? = null,
        listingPrice: String? = null,
        initialMessage: String = "Hi, I'm interested in talking with you."
    ): TalkRequestEntity = withContext(Dispatchers.IO) {
        val requestId = "req_${UUID.randomUUID().toString().take(8)}"
        val request = TalkRequestEntity(
            id = requestId,
            peerId = peerId,
            peerAlias = peerAlias.ifEmpty { peerOnion.take(14) + "..." },
            peerOnion = peerOnion,
            listingId = listingId,
            listingTitle = listingTitle,
            listingPrice = listingPrice,
            isIncoming = true,
            status = "PENDING",
            initialMessage = initialMessage,
            timestamp = System.currentTimeMillis()
        )
        talkRequestDao.insertRequest(request)

        val existing = peerDao.getPeer(peerId)
        if (existing == null) {
            peerDao.insertPeer(
                PeerContactEntity(
                    peerId = peerId,
                    alias = peerAlias.ifEmpty { peerOnion.take(14) + "..." },
                    onionAddress = peerOnion,
                    publicKey = "PUB_${peerId.take(12)}",
                    fingerprint = "FP_${peerOnion.take(8).uppercase()}",
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true,
                    connectionStatus = "PENDING_RECEIVED"
                )
            )
        } else {
            peerDao.insertPeer(existing.copy(connectionStatus = "PENDING_RECEIVED"))
        }

        request
    }

    suspend fun acceptTalkRequest(requestId: String) = withContext(Dispatchers.IO) {
        val request = talkRequestDao.getRequestById(requestId) ?: return@withContext
        talkRequestDao.updateStatus(requestId, "ACCEPTED")
        peerDao.updateConnectionStatus(request.peerId, "CONNECTED")

        // Create initial handshake confirmation message
        val now = System.currentTimeMillis()
        val handshakeMsg = MessageEntity(
            id = "msg_hs_${UUID.randomUUID().toString().take(8)}",
            peerId = request.peerId,
            peerOnion = request.peerOnion,
            content = "Encrypted P2P connection established. Messages saved locally on device.",
            encryptedBlob = "UEVFUl9DT05ORUNUSU9OX0FDQ0VQVEVE",
            isOutgoing = false,
            timestamp = now,
            status = "VERIFIED",
            relatedListingId = request.listingId,
            relatedListingTitle = request.listingTitle,
            relatedListingPrice = request.listingPrice
        )
        messageDao.insertMessage(handshakeMsg)

        if (request.initialMessage.isNotBlank() && request.initialMessage != "Hi, I'm interested in talking with you.") {
            val userMsg = MessageEntity(
                id = "msg_req_${UUID.randomUUID().toString().take(8)}",
                peerId = request.peerId,
                peerOnion = request.peerOnion,
                content = request.initialMessage,
                encryptedBlob = "SU5JVElBTF9VU0VSX1JFUVVFU1RfTVNH",
                isOutgoing = false,
                timestamp = now + 50,
                status = "VERIFIED",
                relatedListingId = request.listingId,
                relatedListingTitle = request.listingTitle,
                relatedListingPrice = request.listingPrice
            )
            messageDao.insertMessage(userMsg)
        }
    }

    suspend fun declineTalkRequest(requestId: String) = withContext(Dispatchers.IO) {
        val request = talkRequestDao.getRequestById(requestId) ?: return@withContext
        talkRequestDao.updateStatus(requestId, "DECLINED")
        peerDao.updateConnectionStatus(request.peerId, "NOT_CONNECTED")
    }

    suspend fun sendMessage(
        peerId: String,
        peerOnion: String,
        text: String,
        relatedListingId: String? = null,
        relatedListingTitle: String? = null,
        relatedListingPrice: String? = null,
        autoDestructSeconds: Long? = null
    ): MessageEntity = withContext(Dispatchers.IO) {
        val cipherPayload = cryptoManager.encryptAesGcm(text)
        val now = System.currentTimeMillis()
        val expiresAt = if (autoDestructSeconds != null && autoDestructSeconds > 0) {
            now + (autoDestructSeconds * 1000L)
        } else null

        val msg = MessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            peerId = peerId,
            peerOnion = peerOnion,
            content = text,
            encryptedBlob = cipherPayload,
            isOutgoing = true,
            timestamp = now,
            status = "DELIVERED",
            relatedListingId = relatedListingId,
            relatedListingTitle = relatedListingTitle,
            relatedListingPrice = relatedListingPrice,
            autoDestructSeconds = autoDestructSeconds,
            expiresAt = expiresAt
        )
        messageDao.insertMessage(msg)

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
                    isOnline = true,
                    connectionStatus = "CONNECTED"
                )
            )
        } else {
            peerDao.insertPeer(existing.copy(lastSeen = System.currentTimeMillis(), connectionStatus = "CONNECTED"))
        }

        msg
    }

    suspend fun receiveIncomingMessage(
        senderId: String,
        senderOnion: String,
        content: String,
        listingId: String? = null,
        listingTitle: String? = null,
        listingPrice: String? = null,
        autoDestructSeconds: Long? = null
    ) = withContext(Dispatchers.IO) {
        val decrypted = cryptoManager.decryptAesGcm(content)
        val now = System.currentTimeMillis()
        val expiresAt = if (autoDestructSeconds != null && autoDestructSeconds > 0) {
            now + (autoDestructSeconds * 1000L)
        } else null

        val msg = MessageEntity(
            id = "msg_${UUID.randomUUID().toString().take(8)}",
            peerId = senderId,
            peerOnion = senderOnion,
            content = decrypted,
            encryptedBlob = content,
            isOutgoing = false,
            timestamp = now,
            status = "VERIFIED",
            relatedListingId = listingId,
            relatedListingTitle = listingTitle,
            relatedListingPrice = listingPrice,
            autoDestructSeconds = autoDestructSeconds,
            expiresAt = expiresAt
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
                    isOnline = true,
                    connectionStatus = "CONNECTED"
                )
            )
        } else {
            peerDao.insertPeer(existing.copy(lastSeen = System.currentTimeMillis(), connectionStatus = "CONNECTED"))
        }
    }

    suspend fun purgeExpiredMessages(currentTime: Long = System.currentTimeMillis()): Int = withContext(Dispatchers.IO) {
        messageDao.deleteExpiredMessages(currentTime)
    }

    suspend fun deleteMessage(id: String) = withContext(Dispatchers.IO) {
        messageDao.deleteMessageById(id)
    }

    suspend fun saveContact(peer: PeerContactEntity) {
        peerDao.insertPeer(peer)
    }

    suspend fun addOrUpdatePeer(
        peerId: String,
        displayName: String,
        onionAddress: String,
        publicKeyFingerprint: String
    ) = withContext(Dispatchers.IO) {
        val existing = peerDao.getPeer(peerId)
        val entity = PeerContactEntity(
            peerId = peerId,
            alias = displayName.ifEmpty { onionAddress.take(14) + "..." },
            onionAddress = onionAddress,
            publicKey = "PUB_${peerId.take(12)}",
            fingerprint = publicKeyFingerprint.ifEmpty { "FP_${onionAddress.take(8).uppercase()}" },
            lastSeen = System.currentTimeMillis(),
            isOnline = true,
            isVerified = true,
            connectionStatus = "CONNECTED"
        )
        peerDao.insertPeer(entity)
    }

    suspend fun verifyPeer(peerId: String, verified: Boolean) {
        peerDao.updateVerification(peerId, verified)
    }

    suspend fun saveDraft(peerId: String, draftText: String) = withContext(Dispatchers.IO) {
        val peer = peerDao.getPeer(peerId)
        if (peer != null) {
            peerDao.insertPeer(peer.copy(draftMessage = draftText))
        }
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
            isVerified = true,
            connectionStatus = "CONNECTED"
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
            isVerified = false,
            connectionStatus = "CONNECTED"
        )
        val peer3 = PeerContactEntity(
            peerId = "peer_aurora_mesh",
            alias = "Aurora Radio Labs",
            onionAddress = "auroramesh773xk91.onion",
            publicKey = "RSA_PUB_auroramesh",
            fingerprint = "C3:99:11:44:88:77:00:AA",
            lastSeen = System.currentTimeMillis() - 600000,
            isOnline = true,
            circuitHops = 3,
            isVerified = false,
            connectionStatus = "PENDING_RECEIVED"
        )

        peerDao.insertPeers(listOf(peer1, peer2, peer3))

        // Pre-seed an incoming talk request from Aurora Radio Labs
        val sampleIncomingRequest = TalkRequestEntity(
            id = "req_init_incoming_01",
            peerId = "peer_aurora_mesh",
            peerAlias = "Aurora Radio Labs",
            peerOnion = "auroramesh773xk91.onion",
            listingId = "lst_local_01",
            listingTitle = "Encrypted LoRa Mesh Communicator",
            listingPrice = "0.045 XMR",
            isIncoming = true,
            status = "PENDING",
            initialMessage = "Hello! I saw your LoRa Mesh Communicator listing and would like to establish an encrypted P2P talk channel.",
            timestamp = System.currentTimeMillis() - 600000
        )
        talkRequestDao.insertRequest(sampleIncomingRequest)

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
        talkRequestDao.clearAll()
    }
}
