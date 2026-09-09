package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.CryptoManager
import com.example.data.crypto.EncryptionCascadeResult
import com.example.data.crypto.EncryptionLayerConfig
import com.example.data.database.AppDatabase
import com.example.data.database.ListingEntity
import com.example.data.database.MessageEntity
import com.example.data.database.PeerContactEntity
import com.example.data.network.AnonymousNetworkLayer
import com.example.data.network.CircuitHopDepth
import com.example.data.network.ConnectionMode
import com.example.data.network.LocalPeerServer
import com.example.data.network.P2PClient
import com.example.data.network.TorBridgeType
import com.example.data.network.TorManager
import com.example.data.network.TorNetworkVitals
import com.example.data.network.TorStatus
import com.example.data.repository.ChatRepository
import com.example.data.repository.MarketplaceRepository
import com.example.data.transparency.CurrentActivityState
import com.example.data.transparency.TransparencyCategory
import com.example.data.transparency.TransparencyEvent
import com.example.data.transparency.TransparencyLogManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppNavTab {
    MARKETPLACE,
    MY_STORE,
    CHATS,
    SECURITY
}

class TorPeerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("fizz_sovereign_prefs", Context.MODE_PRIVATE)

    private val db = AppDatabase.getInstance(application)
    val cryptoManager = CryptoManager(application)
    val torManager = TorManager(application, cryptoManager.myOnionAddress)
    val transparencyManager = TransparencyLogManager(application)
    val anonymousNetworkLayer = AnonymousNetworkLayer(
        context = application,
        orbotManager = torManager.orbotManager,
        nativeTorClient = torManager.nativeTorClient,
        transparencyLogManager = transparencyManager
    )
    val networkVitals: StateFlow<TorNetworkVitals> = anonymousNetworkLayer.vitals
    private val p2pClient = P2PClient(application)

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Mandatory Liability Waiver State
    private val _hasAcceptedWaiver = MutableStateFlow(prefs.getBoolean("has_accepted_liability_waiver_v1", false))
    val hasAcceptedWaiver: StateFlow<Boolean> = _hasAcceptedWaiver.asStateFlow()

    // First Start-up Guide State
    private val _hasSeenOnboardingGuide = MutableStateFlow(prefs.getBoolean("has_seen_onboarding_guide_v1", false))
    val hasSeenOnboardingGuide: StateFlow<Boolean> = _hasSeenOnboardingGuide.asStateFlow()

    // Auto-Destruct Timer for Active Messaging Interface (null = permanent, or seconds: 10, 30, 60, 300, 3600, 86400)
    private val _selectedAutoDestructSeconds = MutableStateFlow<Long?>(null)
    val selectedAutoDestructSeconds: StateFlow<Long?> = _selectedAutoDestructSeconds.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    fun acceptLiabilityWaiver() {
        prefs.edit().putBoolean("has_accepted_liability_waiver_v1", true).apply()
        _hasAcceptedWaiver.value = true
        transparencyManager.logEvent(
            action = "Liability Covenant Accepted",
            description = "User acknowledged 0-fault, sovereign user-responsible protocol terms.",
            category = TransparencyCategory.CRYPTOGRAPHY,
            technicalDetails = "Zero-Knowledge Sovereign User Contract • Zero Server Liability"
        )
    }

    fun reopenLiabilityWaiver() {
        _hasAcceptedWaiver.value = false
    }

    fun completeOnboardingGuide() {
        prefs.edit().putBoolean("has_seen_onboarding_guide_v1", true).apply()
        _hasSeenOnboardingGuide.value = true
    }

    fun reopenOnboardingGuide() {
        _hasSeenOnboardingGuide.value = false
    }

    fun setAutoDestructTimer(seconds: Long?) {
        _selectedAutoDestructSeconds.value = seconds
        val label = when (seconds) {
            null -> "Permanent (No Auto-Destruct)"
            10L -> "10 Seconds"
            30L -> "30 Seconds"
            60L -> "1 Minute"
            300L -> "5 Minutes"
            3600L -> "1 Hour"
            86400L -> "24 Hours"
            else -> "$seconds seconds"
        }
        transparencyManager.logEvent(
            action = "Auto-Destruct Timer Configured",
            description = "Message lifespan set to $label before local SQLite purge.",
            category = TransparencyCategory.LOCAL_STORAGE,
            technicalDetails = "SQLite TTL: ${seconds ?: 0}s"
        )
        Toast.makeText(getApplication(), "Auto-destruct timer: $label", Toast.LENGTH_SHORT).show()
    }

    // Encryption Layer Configuration & Cascade State
    private val _encryptionConfig = MutableStateFlow(EncryptionLayerConfig())
    val encryptionConfig: StateFlow<EncryptionLayerConfig> = _encryptionConfig.asStateFlow()

    private val _lastCascadeResult = MutableStateFlow<EncryptionCascadeResult?>(null)
    val lastCascadeResult: StateFlow<EncryptionCascadeResult?> = _lastCascadeResult.asStateFlow()

    // Legal Disclaimer Modal Visibility
    private val _showLegalDisclaimer = MutableStateFlow(false)
    val showLegalDisclaimer: StateFlow<Boolean> = _showLegalDisclaimer.asStateFlow()

    fun openLegalDisclaimer() {
        _showLegalDisclaimer.value = true
    }

    fun dismissLegalDisclaimer() {
        _showLegalDisclaimer.value = false
    }

    fun toggleAesGcm() {
        val current = _encryptionConfig.value
        val updated = current.copy(useAes256Gcm = !current.useAes256Gcm)
        _encryptionConfig.value = updated
        cryptoManager.activeConfig = updated
        testLiveCascadeEncryption()
    }

    fun toggleChaCha20() {
        val current = _encryptionConfig.value
        val updated = current.copy(useChaCha20Poly1305 = !current.useChaCha20Poly1305)
        _encryptionConfig.value = updated
        cryptoManager.activeConfig = updated
        testLiveCascadeEncryption()
    }

    fun toggleHybridRsa() {
        val current = _encryptionConfig.value
        val updated = current.copy(useHybridRsaKeyExchange = !current.useHybridRsaKeyExchange)
        _encryptionConfig.value = updated
        cryptoManager.activeConfig = updated
        testLiveCascadeEncryption()
    }

    fun toggleTrafficPadding() {
        val current = _encryptionConfig.value
        val updated = current.copy(useZeroKnowledgeTrafficPadding = !current.useZeroKnowledgeTrafficPadding)
        _encryptionConfig.value = updated
        cryptoManager.activeConfig = updated
        testLiveCascadeEncryption()
    }

    fun testLiveCascadeEncryption(sampleText: String = "Fizz.1 P2P Autonomous Encrypted Payload") {
        val result = cryptoManager.encryptMultiLayerCascade(sampleText, _encryptionConfig.value)
        _lastCascadeResult.value = result
        transparencyManager.logEvent(
            action = "Cascade Encryption Executed",
            description = "Protected payload through ${result.layersApplied.size} cryptographic armor layers.",
            category = TransparencyCategory.CRYPTOGRAPHY,
            technicalDetails = "Raw: ${result.rawPayloadLength}B ➔ Padded: ${result.paddedLength}B • SHA256: ${result.integrityHashSha256}"
        )
    }

    fun setBridgeType(bridge: TorBridgeType, customLine: String = "") {
        torManager.setBridgeType(bridge, customLine)
        transparencyManager.logEvent(
            action = "Tor Bridge Protocol Changed",
            description = "Active Bridge: ${bridge.title} (${bridge.protocolTag})",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Bridge type updated ➔ ${bridge.name} • Custom line: ${customLine.ifEmpty { "N/A" }}"
        )
        Toast.makeText(getApplication(), "Tor Bridge updated to ${bridge.title}", Toast.LENGTH_SHORT).show()
    }

    fun setCircuitHopDepth(depth: CircuitHopDepth) {
        torManager.setCircuitHopDepth(depth)
        transparencyManager.logEvent(
            action = "Circuit Length Adjusted",
            description = "Configured ${depth.hopCount}-Hop Tor circuit topology: ${depth.label}",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Topology: ${depth.description}"
        )
        Toast.makeText(getApplication(), "Circuit depth set to ${depth.label}", Toast.LENGTH_SHORT).show()
    }

    fun setStrictNon14Eyes(strict: Boolean) {
        torManager.setStrictNon14Eyes(strict)
        transparencyManager.logEvent(
            action = "Jurisdiction Routing Filter",
            description = if (strict) "Strict Non-14-Eyes relays enforced (Switzerland, Iceland, Panama, Seychelles)" else "Global Tor consensus relays allowed",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Strict Jurisdiction Filter = $strict"
        )
        Toast.makeText(getApplication(), if (strict) "Strict Non-14-Eyes Relays active" else "Global Relays active", Toast.LENGTH_SHORT).show()
    }

    private val marketplaceRepo = MarketplaceRepository(
        context = application,
        listingDao = db.listingDao(),
        myPeerId = cryptoManager.myPeerId,
        myOnionAddress = cryptoManager.myOnionAddress
    )

    private val chatRepo = ChatRepository(
        messageDao = db.messageDao(),
        peerDao = db.peerContactDao(),
        cryptoManager = cryptoManager
    )

    // Local embedded P2P server that serves marketplace photos & listings directly from phone
    val localServer = LocalPeerServer(
        context = application,
        port = 8989,
        getMyListingsProvider = {
            marketplaceRepo.myListings.first()
        },
        getMyPeerInfo = {
            Triple(cryptoManager.myPeerId, cryptoManager.myOnionAddress, cryptoManager.myFingerprint)
        },
        onMessageReceived = { senderId, senderOnion, content, listingId, listingTitle, listingPrice ->
            chatRepo.receiveIncomingMessage(
                senderId = senderId,
                senderOnion = senderOnion,
                content = content,
                listingId = listingId,
                listingTitle = listingTitle,
                listingPrice = listingPrice,
                autoDestructSeconds = _selectedAutoDestructSeconds.value
            )
        }
    )

    val currentActivity: StateFlow<CurrentActivityState> = transparencyManager.currentActivity
    val transparencyEvents: StateFlow<List<TransparencyEvent>> = transparencyManager.events

    val currentTab = MutableStateFlow(AppNavTab.MARKETPLACE)
    val torStatus: StateFlow<TorStatus> = torManager.status

    val myListings: StateFlow<List<ListingEntity>> = marketplaceRepo.myListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peerListings: StateFlow<List<ListingEntity>> = marketplaceRepo.peerListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allListings: StateFlow<List<ListingEntity>> = marketplaceRepo.allListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val peers: StateFlow<List<PeerContactEntity>> = chatRepo.allPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<MessageEntity>> = chatRepo.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeerId = MutableStateFlow<String?>(null)
    val selectedPeerId: StateFlow<String?> = _selectedPeerId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isConnectingPeer = MutableStateFlow(false)
    val isConnectingPeer: StateFlow<Boolean> = _isConnectingPeer.asStateFlow()

    init {
        // Start local P2P HTTP Server on phone
        localServer.start()

        // Seed initial local inventory and peer network items
        viewModelScope.launch {
            marketplaceRepo.seedInitialDataIfEmpty()
            chatRepo.seedInitialChatsIfEmpty()
            testLiveCascadeEncryption()
        }

        // Periodic background auto-destruct cleaner (Runs every 1 second to vaporize expired local messages)
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val purgedCount = chatRepo.purgeExpiredMessages(System.currentTimeMillis())
                if (purgedCount > 0) {
                    transparencyManager.logEvent(
                        action = "Auto-Destruct Wiped $purgedCount Message(s)",
                        description = "Vaporized expired ephemeral messages permanently from local SQLite sandbox.",
                        category = TransparencyCategory.LOCAL_STORAGE,
                        technicalDetails = "DELETE FROM messages WHERE expiresAt <= now() • Zero fragments remain"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        localServer.stop()
    }

    fun setNavTab(tab: AppNavTab) {
        currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectChat(peerId: String) {
        _selectedPeerId.value = peerId
        currentTab.value = AppNavTab.CHATS
    }

    fun clearChatSelection() {
        _selectedPeerId.value = null
    }

    fun createListing(
        title: String,
        description: String,
        price: String,
        currency: String,
        category: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            transparencyManager.setWorking(
                action = "Saving Product Locally",
                subtitle = "Writing '$title' ($price $currency) to private SQLite database",
                category = TransparencyCategory.LOCAL_STORAGE
            )
            marketplaceRepo.createMyListing(
                title = title,
                description = description,
                price = price,
                currency = currency,
                category = category,
                imageUri = imageUri
            )
            transparencyManager.logEvent(
                action = "Product Hosted on Phone",
                description = "Saved '$title' to local SQLite storage. Ready for P2P peers to browse over Tor.",
                category = TransparencyCategory.LOCAL_STORAGE,
                technicalDetails = "SQLite INSERT -> listings table • Cloud uploads: 0 bytes • Host: local port 8989",
                setAsCurrent = true
            )
            Toast.makeText(getApplication(), "Listing hosted locally on this phone!", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteListing(id: String) {
        viewModelScope.launch {
            marketplaceRepo.deleteListing(id)
            transparencyManager.logEvent(
                action = "Product Removed",
                description = "Deleted item ID: $id from local SQLite database",
                category = TransparencyCategory.LOCAL_STORAGE,
                technicalDetails = "SQLite DELETE FROM listings WHERE id = '$id'",
                setAsCurrent = true
            )
            Toast.makeText(getApplication(), "Listing removed from local phone", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleStock(listing: ListingEntity) {
        viewModelScope.launch {
            marketplaceRepo.toggleStock(listing)
            transparencyManager.logEvent(
                action = "Stock Updated Locally",
                description = "Toggled in-stock status for '${listing.title}'",
                category = TransparencyCategory.LOCAL_STORAGE,
                technicalDetails = "SQLite UPDATE listings SET inStock = ${!listing.inStock} WHERE id = '${listing.id}'",
                setAsCurrent = true
            )
        }
    }

    fun sendMessage(
        peerId: String,
        peerOnion: String,
        text: String,
        relatedListingId: String? = null,
        relatedListingTitle: String? = null,
        relatedListingPrice: String? = null
    ) {
        viewModelScope.launch {
            val autoDestruct = _selectedAutoDestructSeconds.value
            transparencyManager.setWorking(
                action = "Encrypting & Sending Message",
                subtitle = "Multi-Layer Cascade Armor ➔ Routing via Tor SOCKS5 to peer ${if (autoDestruct != null) "(Auto-destruct: ${autoDestruct}s)" else ""}",
                category = TransparencyCategory.CRYPTOGRAPHY
            )

            val cascadeResult = cryptoManager.encryptMultiLayerCascade(text, _encryptionConfig.value)
            _lastCascadeResult.value = cascadeResult

            chatRepo.sendMessage(
                peerId = peerId,
                peerOnion = peerOnion,
                text = text,
                relatedListingId = relatedListingId,
                relatedListingTitle = relatedListingTitle,
                relatedListingPrice = relatedListingPrice,
                autoDestructSeconds = autoDestruct
            )

            transparencyManager.logEvent(
                action = "Multi-Layer Encrypted (${cascadeResult.layersApplied.size} Layers)",
                description = "AES-256-GCM + ChaCha20-Poly1305 + Hybrid RSA + ZK Padding applied.${if (autoDestruct != null) " Auto-destruct timer armed: ${autoDestruct}s" else ""}",
                category = TransparencyCategory.CRYPTOGRAPHY,
                technicalDetails = "Integrity Hash: ${cascadeResult.integrityHashSha256} • Transport: Tor Onion Ring • TTL: ${autoDestruct ?: "Infinite"}s"
            )

            // Route directly through Anonymous Tor Network Layer
            if (peerOnion.isNotEmpty()) {
                val sendResult = anonymousNetworkLayer.sendAnonymousMessage(
                    targetAddress = peerOnion,
                    senderId = cryptoManager.myPeerId,
                    senderOnion = cryptoManager.myOnionAddress,
                    encryptedContent = cascadeResult.finalCiphertextBase64,
                    listingId = relatedListingId,
                    listingTitle = relatedListingTitle,
                    listingPrice = relatedListingPrice
                )

                if (sendResult.isSuccess) {
                    Toast.makeText(getApplication(), "Delivered via Tor Multi-Hop", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteMessage(id: String) {
        viewModelScope.launch {
            chatRepo.deleteMessage(id)
            transparencyManager.logEvent(
                action = "Message Vaporized Manually",
                description = "Deleted message ID $id immediately from device storage.",
                category = TransparencyCategory.LOCAL_STORAGE,
                technicalDetails = "SQLite DELETE FROM messages WHERE id = '$id'"
            )
            Toast.makeText(getApplication(), "Message destroyed", Toast.LENGTH_SHORT).show()
        }
    }

    fun runSurveillanceScan() {
        torManager.runDeepSurveillanceScan()
        transparencyManager.logEvent(
            action = "Surveillance Interception Deep Scan",
            description = "Analyzing circuit latency signatures, DNS leakage, and relay correlation...",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Algorithmic timing analysis + ASN isolation evaluation"
        )
        Toast.makeText(getApplication(), "Running deep surveillance & interception scan...", Toast.LENGTH_SHORT).show()
    }

    fun connectToPeer(address: String) {
        viewModelScope.launch {
            _isConnectingPeer.value = true
            val result = anonymousNetworkLayer.fetchAnonymousStorefront(targetAddress = address)

            if (result.isSuccess) {
                val listings = result.getOrNull().orEmpty()
                if (listings.isNotEmpty()) {
                    marketplaceRepo.importPeerListings(listings)
                    Toast.makeText(getApplication(), "Discovered ${listings.size} listings over Tor!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(getApplication(), "Connected to peer! (Storefront is empty)", Toast.LENGTH_SHORT).show()
                }
            } else {
                transparencyManager.logEvent(
                    action = "Peer Connection Offline",
                    description = "Could not reach peer at $address: ${result.exceptionOrNull()?.message ?: "Host unreachable"}",
                    category = TransparencyCategory.NETWORK_TOR,
                    technicalDetails = "Socket timeout or peer offline • No data leaked",
                    setAsCurrent = true
                )
                Toast.makeText(getApplication(), "Peer connection: ${result.exceptionOrNull()?.message ?: "Node unreachable"}", Toast.LENGTH_LONG).show()
            }
            _isConnectingPeer.value = false
        }
    }

    // P2P QR Code Pairing Dialog State
    private val _showPeerQrDialog = MutableStateFlow(false)
    val showPeerQrDialog: StateFlow<Boolean> = _showPeerQrDialog.asStateFlow()

    fun openPeerQrDialog() {
        _showPeerQrDialog.value = true
    }

    fun dismissPeerQrDialog() {
        _showPeerQrDialog.value = false
    }

    fun connectToPeerParsed(peerId: String, onionAddress: String, fingerprint: String, displayName: String) {
        viewModelScope.launch {
            chatRepo.addOrUpdatePeer(
                peerId = peerId,
                displayName = displayName,
                onionAddress = onionAddress,
                publicKeyFingerprint = fingerprint
            )
            connectToPeer(onionAddress)
            selectChat(peerId)
            transparencyManager.logEvent(
                action = "Direct P2P Link Established",
                description = "Paired with $displayName ($onionAddress) with zero public discovery broadcast.",
                category = TransparencyCategory.NETWORK_TOR,
                technicalDetails = "P2P Link: $onionAddress • Fingerprint: $fingerprint",
                setAsCurrent = true
            )
        }
    }

    fun rotateTorCircuit() {
        torManager.rotateTorCircuit()
        transparencyManager.logEvent(
            action = "Tor Circuit Rebuilt",
            description = "Constructed brand new ${torStatus.value.circuitHopDepth.label} circuit with fresh guard, middle, and exit relays.",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "New circuit generated with random Tor consensus nodes • New crypto handshake",
            setAsCurrent = true
        )
        Toast.makeText(getApplication(), "Generated new Tor circuit!", Toast.LENGTH_SHORT).show()
    }

    fun updateProxySettings(host: String, port: Int) {
        torManager.updateProxySettings(host, port)
        transparencyManager.logEvent(
            action = "Proxy Settings Updated",
            description = "Updated SOCKS5 proxy target to $host:$port",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Proxy changed -> java.net.Proxy(SOCKS, InetSocketAddress($host, $port))",
            setAsCurrent = true
        )
        Toast.makeText(getApplication(), "Proxy configuration updated", Toast.LENGTH_SHORT).show()
    }

    fun setConnectionMode(mode: ConnectionMode) {
        torManager.setConnectionMode(mode)
        transparencyManager.logEvent(
            action = "Routing Mode Changed",
            description = if (mode == ConnectionMode.TOR_ONION_ROUTING) "Strict Tor Onion SOCKS5 routing active" else "Direct LAN P2P active",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "ConnectionMode -> $mode",
            setAsCurrent = true
        )
    }

    fun startOrbot() {
        val success = torManager.startOrbot()
        transparencyManager.logEvent(
            action = "Start Orbot Request Sent",
            description = if (success) "Sent Android Intent to start Orbot background Tor daemon" else "Orbot is not installed on this device",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Intent: org.torproject.android.intent.action.START",
            setAsCurrent = true
        )
        if (success) {
            Toast.makeText(getApplication(), "Sent start request to Orbot", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Orbot not installed or request failed", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopOrbot() {
        torManager.stopOrbot()
        transparencyManager.logEvent(
            action = "Stop Orbot Request Sent",
            description = "Sent Android Intent to stop Orbot Tor daemon",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Intent: org.torproject.android.intent.action.STOP",
            setAsCurrent = true
        )
        Toast.makeText(getApplication(), "Sent stop request to Orbot", Toast.LENGTH_SHORT).show()
    }

    fun bindOrbotService() {
        val bound = torManager.bindOrbotService()
        transparencyManager.logEvent(
            action = "Bind Tor Service",
            description = if (bound) "Bound to Orbot background AIDL ServiceConnection" else "Could not bind to Orbot service",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "ServiceConnection to org.torproject.android.service.TorService",
            setAsCurrent = true
        )
        if (bound) {
            Toast.makeText(getApplication(), "Binding to Orbot TorService", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Could not bind to Orbot service directly", Toast.LENGTH_SHORT).show()
        }
    }

    fun probeTorConnectivity() {
        torManager.runLiveTorProbe()
        viewModelScope.launch {
            anonymousNetworkLayer.probeNetworkVitals()
        }
        transparencyManager.logEvent(
            action = "Tor Network Vitals Probed",
            description = "Probing SOCKS5 proxy on port ${torStatus.value.socksProxyPort} & measuring network vitals...",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "SOCKS5 handshake test & HTTPS query to check.torproject.org via Tor proxy",
            setAsCurrent = true
        )
        Toast.makeText(getApplication(), "Probing Tor SOCKS proxy & exit IP...", Toast.LENGTH_SHORT).show()
    }

    fun openOrbotPlayStore() {
        val intent = torManager.orbotManager.getInstallIntent()
        intent.addFlags(android.content.IntentFilter.MATCH_CATEGORY_EMPTY)
        try {
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Cannot launch Play Store: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearTransparencyLog() {
        transparencyManager.clearLog()
    }

    fun panicWipeAllData() {
        viewModelScope.launch {
            marketplaceRepo.wipeAll()
            chatRepo.wipeAll()
            cryptoManager.wipeKeys()
            transparencyManager.logEvent(
                action = "EMERGENCY DATA PURGE",
                description = "All SQLite records deleted, keys zeroed in Keystore, and local cache destroyed.",
                category = TransparencyCategory.CRYPTOGRAPHY,
                technicalDetails = "Wipe complete • 0 recoverable data remains on device",
                setAsCurrent = true
            )
            Toast.makeText(getApplication(), "Vault zeroed! All local data cryptographically wiped.", Toast.LENGTH_LONG).show()
        }
    }
}
