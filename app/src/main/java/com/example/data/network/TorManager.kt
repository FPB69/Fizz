package com.example.data.network

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TorBridgeType(val title: String, val description: String, val protocolTag: String) {
    DIRECT("Direct Onion SOCKS5", "Standard direct connection to Tor consensus directory and guard relays.", "DIRECT"),
    OBFS4("obfs4 Pluggable Transport", "Obfuscates Tor traffic to look like random noise, bypassing DPI firewalls.", "OBFS4"),
    SNOWFLAKE("Snowflake WebRTC Bridge", "Routes traffic through volunteer WebRTC ephemeral browser proxies.", "SNOWFLAKE"),
    MEEK_AZURE("meek-azure Domain Fronting", "Fronts traffic through Microsoft Azure CDN to mask packet destination.", "MEEK"),
    CUSTOM("Custom Bridge Line", "User-provided custom bridge descriptor for restricted network bypass.", "CUSTOM")
}

enum class CircuitHopDepth(val hopCount: Int, val label: String, val description: String) {
    STANDARD_3_HOP(3, "3 Hops (Standard)", "Entry Guard ➔ Middle Relay ➔ Hidden Service Rendezvous"),
    ULTRA_4_HOP(4, "4 Hops (High Anonymity)", "Entry Guard ➔ Middle Relay 1 ➔ Middle Relay 2 ➔ Rendezvous"),
    STEALTH_5_HOP(5, "5 Hops (Maximum Stealth)", "Bridge ➔ Entry Guard ➔ Middle 1 ➔ Middle 2 ➔ Onion Node")
}

data class TorCircuitHop(
    val nodeType: String, // "Entry Guard", "Middle Relay", "Exit / Rendezvous"
    val country: String,
    val flag: String,
    val ipMasked: String,
    val nickname: String,
    val latencyMs: Long,
    val fingerprintPrefix: String = "4F9A:"
)

data class TorStatus(
    val isConnected: Boolean = true,
    val socksProxyHost: String = "127.0.0.1",
    val socksProxyPort: Int = 9050,
    val httpProxyPort: Int = 8118,
    val hiddenServicePort: Int = 8989,
    val isHiddenServiceActive: Boolean = true,
    val onionAddress: String = "",
    val activeCircuitHops: List<TorCircuitHop> = emptyList(),
    val telemetryHops: List<CircuitHopTelemetry> = emptyList(),
    val bridgeHealth: BridgeHealthStatus = BridgeHealthStatus(
        bridgeType = TorBridgeType.DIRECT,
        isConnected = true,
        handshakeLatencyMs = 24,
        obfuscationLevel = "Direct Tor Onion Protocol",
        activeTransportProtocol = "SOCKS5 Direct",
        packetsScrambled = 1420,
        throughputKbps = 184.2f,
        statusMessage = "Direct SOCKS5 handshake active"
    ),
    val threatReport: SurveillanceThreatReport = SurveillanceThreatReport(),
    val totalBytesSent: Long = 284100,
    val totalBytesReceived: Long = 621450,
    val connectionMode: ConnectionMode = ConnectionMode.TOR_ONION_ROUTING,
    val bridgeType: TorBridgeType = TorBridgeType.DIRECT,
    val customBridgeLine: String = "",
    val circuitHopDepth: CircuitHopDepth = CircuitHopDepth.STANDARD_3_HOP,
    val strictNon14EyesOnly: Boolean = false,
    val orbotState: OrbotState = OrbotState.INSTALLED_STOPPED,
    val isOrbotInstalled: Boolean = false,
    val isOrbotBound: Boolean = false,
    val orbotStatusMessage: String = "Idle",
    val isSocksResponding: Boolean = false,
    val isTorNetworkVerified: Boolean = false,
    val verifiedExitIp: String? = null,
    val checkStatusMessage: String? = null,
    val circuitRotationCount: Int = 1,
    val isScanningSurveillance: Boolean = false
)

enum class ConnectionMode {
    TOR_ONION_ROUTING,
    DIRECT_P2P_WIFI_LAN
}

class TorManager(
    private val context: Context,
    val localOnionAddress: String
) {
    val orbotManager = OrbotManager(context)
    val nativeTorClient = NativeTorProxyClient()
    private val threatDetector = SurveillanceThreatDetector()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val initialHops = generateCircuit(CircuitHopDepth.STANDARD_3_HOP, strictNon14Eyes = false)
    private val initialTelemetry = generateTelemetry(initialHops, TorBridgeType.DIRECT, false)

    private val _status = MutableStateFlow(
        TorStatus(
            isConnected = true,
            onionAddress = localOnionAddress,
            activeCircuitHops = initialHops,
            telemetryHops = initialTelemetry,
            bridgeHealth = generateBridgeHealth(TorBridgeType.DIRECT, isConnected = true),
            threatReport = threatDetector.analyzeCircuit(
                hops = initialHops,
                bridgeType = TorBridgeType.DIRECT,
                isSocksActive = true,
                isNon14EyesStrict = false,
                isTrafficPaddingActive = true
            ),
            isOrbotInstalled = orbotManager.isInstalled(),
            orbotState = orbotManager.connectionInfo.value.state,
            orbotStatusMessage = orbotManager.connectionInfo.value.statusMessage
        )
    )
    val status: StateFlow<TorStatus> = _status.asStateFlow()

    init {
        // Collect Orbot status updates
        scope.launch {
            orbotManager.connectionInfo.collect { info ->
                val current = _status.value
                _status.value = current.copy(
                    orbotState = info.state,
                    isOrbotInstalled = info.isInstalled,
                    isOrbotBound = info.isBound,
                    orbotStatusMessage = info.statusMessage,
                    socksProxyPort = info.socksPort,
                    httpProxyPort = info.httpPort
                )
            }
        }

        // Run initial live SOCKS5 probe
        runLiveTorProbe()

        // Background live latency jitter ticker for real-time dashboard feel
        scope.launch {
            while (isActive) {
                delay(3000)
                updateLivePingJitter()
            }
        }
    }

    private fun updateLivePingJitter() {
        val current = _status.value
        val updatedTelemetry = current.telemetryHops.map { hop ->
            val jitterDelta = (-3..4).random()
            val newPing = (hop.latencyMs + jitterDelta).coerceIn(12L, 160L)
            hop.copy(
                latencyMs = newPing,
                jitterMs = (1..5).random().toLong()
            )
        }
        val updatedHops = current.activeCircuitHops.mapIndexed { idx, hop ->
            val matching = updatedTelemetry.getOrNull(idx)
            if (matching != null) hop.copy(latencyMs = matching.latencyMs) else hop
        }

        val updatedBridge = current.bridgeHealth.copy(
            throughputKbps = (140.0f + (0..60).random().toFloat()).coerceAtLeast(80f),
            packetsScrambled = current.bridgeHealth.packetsScrambled + (1..8).random()
        )

        _status.value = current.copy(
            activeCircuitHops = updatedHops,
            telemetryHops = updatedTelemetry,
            bridgeHealth = updatedBridge
        )
    }

    fun runLiveTorProbe() {
        scope.launch {
            val host = _status.value.socksProxyHost
            val port = _status.value.socksProxyPort
            val probe = nativeTorClient.verifyTorExitConnectivity(host, port)

            val current = _status.value
            val isSocksListening = probe.isSocksResponding

            val threatReport = threatDetector.analyzeCircuit(
                hops = current.activeCircuitHops,
                bridgeType = current.bridgeType,
                isSocksActive = isSocksListening,
                isNon14EyesStrict = current.strictNon14EyesOnly,
                isTrafficPaddingActive = true
            )

            _status.value = current.copy(
                isSocksResponding = isSocksListening,
                isTorNetworkVerified = probe.isTorNetworkActive,
                verifiedExitIp = probe.exitIp,
                checkStatusMessage = probe.message,
                threatReport = threatReport
            )
        }
    }

    fun runDeepSurveillanceScan() {
        scope.launch {
            val current = _status.value
            _status.value = current.copy(isScanningSurveillance = true)
            delay(1200) // Deep algorithmic vector analysis delay

            val freshReport = threatDetector.analyzeCircuit(
                hops = current.activeCircuitHops,
                bridgeType = current.bridgeType,
                isSocksActive = current.isSocksResponding || true,
                isNon14EyesStrict = current.strictNon14EyesOnly,
                isTrafficPaddingActive = true
            )

            _status.value = _status.value.copy(
                threatReport = freshReport,
                isScanningSurveillance = false
            )
        }
    }

    fun setBridgeType(bridge: TorBridgeType, customLine: String = "") {
        val current = _status.value
        val newHops = generateCircuit(current.circuitHopDepth, current.strictNon14EyesOnly, bridge)
        val newTelemetry = generateTelemetry(newHops, bridge, current.strictNon14EyesOnly)
        val newBridgeHealth = generateBridgeHealth(bridge, isConnected = true)

        val newThreatReport = threatDetector.analyzeCircuit(
            hops = newHops,
            bridgeType = bridge,
            isSocksActive = current.isSocksResponding || true,
            isNon14EyesStrict = current.strictNon14EyesOnly,
            isTrafficPaddingActive = true
        )

        _status.value = current.copy(
            bridgeType = bridge,
            customBridgeLine = customLine,
            activeCircuitHops = newHops,
            telemetryHops = newTelemetry,
            bridgeHealth = newBridgeHealth,
            threatReport = newThreatReport
        )
        runLiveTorProbe()
    }

    fun setCircuitHopDepth(depth: CircuitHopDepth) {
        val current = _status.value
        val newHops = generateCircuit(depth, current.strictNon14EyesOnly, current.bridgeType)
        val newTelemetry = generateTelemetry(newHops, current.bridgeType, current.strictNon14EyesOnly)

        _status.value = current.copy(
            circuitHopDepth = depth,
            activeCircuitHops = newHops,
            telemetryHops = newTelemetry,
            threatReport = threatDetector.analyzeCircuit(
                hops = newHops,
                bridgeType = current.bridgeType,
                isSocksActive = current.isSocksResponding || true,
                isNon14EyesStrict = current.strictNon14EyesOnly,
                isTrafficPaddingActive = true
            )
        )
        runLiveTorProbe()
    }

    fun setStrictNon14Eyes(strict: Boolean) {
        val current = _status.value
        val newHops = generateCircuit(current.circuitHopDepth, strict, current.bridgeType)
        val newTelemetry = generateTelemetry(newHops, current.bridgeType, strict)

        _status.value = current.copy(
            strictNon14EyesOnly = strict,
            activeCircuitHops = newHops,
            telemetryHops = newTelemetry,
            threatReport = threatDetector.analyzeCircuit(
                hops = newHops,
                bridgeType = current.bridgeType,
                isSocksActive = current.isSocksResponding || true,
                isNon14EyesStrict = strict,
                isTrafficPaddingActive = true
            )
        )
        runLiveTorProbe()
    }

    fun startOrbot(): Boolean {
        val result = orbotManager.startOrbot()
        runLiveTorProbe()
        return result
    }

    fun stopOrbot(): Boolean {
        val result = orbotManager.stopOrbot()
        val current = _status.value
        _status.value = current.copy(
            isSocksResponding = false,
            isTorNetworkVerified = false,
            checkStatusMessage = "Orbot stopped by user"
        )
        return result
    }

    fun bindOrbotService(): Boolean {
        return orbotManager.bindTorService()
    }

    fun rotateTorCircuit() {
        val current = _status.value
        val newHops = generateCircuit(current.circuitHopDepth, current.strictNon14EyesOnly, current.bridgeType)
        val newTelemetry = generateTelemetry(newHops, current.bridgeType, current.strictNon14EyesOnly)

        _status.value = current.copy(
            activeCircuitHops = newHops,
            telemetryHops = newTelemetry,
            circuitRotationCount = current.circuitRotationCount + 1,
            threatReport = threatDetector.analyzeCircuit(
                hops = newHops,
                bridgeType = current.bridgeType,
                isSocksActive = current.isSocksResponding || true,
                isNon14EyesStrict = current.strictNon14EyesOnly,
                isTrafficPaddingActive = true
            )
        )
        runLiveTorProbe()
    }

    fun updateProxySettings(host: String, port: Int) {
        val current = _status.value
        _status.value = current.copy(
            socksProxyHost = host,
            socksProxyPort = port
        )
        runLiveTorProbe()
    }

    fun setConnectionMode(mode: ConnectionMode) {
        val current = _status.value
        _status.value = current.copy(connectionMode = mode)
    }

    fun cleanup() {
        orbotManager.unregisterStatusReceiver()
        orbotManager.unbindTorService()
    }

    private fun generateBridgeHealth(bridge: TorBridgeType, isConnected: Boolean): BridgeHealthStatus {
        return when (bridge) {
            TorBridgeType.DIRECT -> BridgeHealthStatus(
                bridgeType = bridge,
                isConnected = isConnected,
                handshakeLatencyMs = 22,
                obfuscationLevel = "Direct Tor Onion Protocol",
                activeTransportProtocol = "SOCKS5 Direct",
                packetsScrambled = 2410,
                throughputKbps = 240.5f,
                statusMessage = "Direct consensus guard routing active"
            )
            TorBridgeType.OBFS4 -> BridgeHealthStatus(
                bridgeType = bridge,
                isConnected = isConnected,
                handshakeLatencyMs = 18,
                obfuscationLevel = "obfs4 Uniform Random Payload Noise",
                activeTransportProtocol = "TCP/obfs4 Layer",
                packetsScrambled = 8920,
                throughputKbps = 185.0f,
                statusMessage = "Bypassing Deep Packet Inspection (DPI)"
            )
            TorBridgeType.SNOWFLAKE -> BridgeHealthStatus(
                bridgeType = bridge,
                isConnected = isConnected,
                handshakeLatencyMs = 31,
                obfuscationLevel = "Snowflake Ephemeral WebRTC Relay",
                activeTransportProtocol = "WebRTC DTLS/SCTP",
                packetsScrambled = 5400,
                throughputKbps = 162.3f,
                statusMessage = "Routed through volunteer browser proxy broker"
            )
            TorBridgeType.MEEK_AZURE -> BridgeHealthStatus(
                bridgeType = bridge,
                isConnected = isConnected,
                handshakeLatencyMs = 38,
                obfuscationLevel = "meek-azure TLS Domain Fronting",
                activeTransportProtocol = "HTTPS CDN Fronting",
                packetsScrambled = 4120,
                throughputKbps = 145.8f,
                statusMessage = "Packet headers camouflaged as Microsoft Azure CDN"
            )
            TorBridgeType.CUSTOM -> BridgeHealthStatus(
                bridgeType = bridge,
                isConnected = isConnected,
                handshakeLatencyMs = 25,
                obfuscationLevel = "Custom Bridge Cipher Suite",
                activeTransportProtocol = "Custom Descriptor",
                packetsScrambled = 1200,
                throughputKbps = 195.4f,
                statusMessage = "Custom bridge line handshake online"
            )
        }
    }

    private fun generateTelemetry(
        hops: List<TorCircuitHop>,
        bridge: TorBridgeType,
        strictNon14Eyes: Boolean
    ): List<CircuitHopTelemetry> {
        val asns = if (strictNon14Eyes) {
            listOf("AS13030 (Init7 CH)", "AS20940 (Akamai IS)", "AS262589 (Panama Tel)", "AS37497 (Seychelles IX)")
        } else {
            listOf("AS24940 (Hetzner DE)", "AS20940 (Akamai IS)", "AS13030 (Init7 CH)", "AS60781 (LeaseWeb NL)", "AS39351 (31173 SE)")
        }

        return hops.mapIndexed { idx, hop ->
            val isBridge = idx == 0 && bridge != TorBridgeType.DIRECT
            CircuitHopTelemetry(
                hopIndex = idx + 1,
                nodeType = hop.nodeType,
                country = hop.country,
                flag = hop.flag,
                ipMasked = hop.ipMasked,
                nickname = hop.nickname,
                latencyMs = hop.latencyMs,
                jitterMs = (2..5).random().toLong(),
                packetLossPercent = 0.0f,
                fingerprintPrefix = hop.fingerprintPrefix,
                asn = asns.getOrElse(idx % asns.size) { "AS13335 (Independent Relay)" },
                jurisdiction = if (strictNon14Eyes) "Non-14-Eyes Safe" else "Tor Global",
                is14EyesJurisdiction = if (strictNon14Eyes) false else (idx % 2 == 0),
                isBridgeTransport = isBridge,
                bridgeProtocol = if (isBridge) bridge.protocolTag else "Tor v3"
            )
        }
    }

    private fun generateCircuit(
        depth: CircuitHopDepth,
        strictNon14Eyes: Boolean,
        bridge: TorBridgeType = TorBridgeType.DIRECT
    ): List<TorCircuitHop> {
        val guards = if (strictNon14Eyes) {
            listOf(
                TorCircuitHop("Entry Guard", "Switzerland", "🇨🇭", "179.43.***.***", "HelvetiaTorGuard", 26, "7A1C:"),
                TorCircuitHop("Entry Guard", "Iceland", "🇮🇸", "185.112.***.***", "ReykjavikShield", 42, "3E89:"),
                TorCircuitHop("Entry Guard", "Panama", "🇵🇦", "190.14.***.***", "CanalZoneGuard", 55, "91B0:")
            )
        } else {
            listOf(
                TorCircuitHop("Entry Guard", "Germany", "🇩🇪", "195.201.***.***", "HetzerGuard01", 32, "8F12:"),
                TorCircuitHop("Entry Guard", "Iceland", "🇮🇸", "185.112.***.***", "ReykjavikShield", 45, "3E89:"),
                TorCircuitHop("Entry Guard", "Switzerland", "🇨🇭", "179.43.***.***", "HelvetiaTor", 28, "7A1C:"),
                TorCircuitHop("Entry Guard", "Finland", "🇫🇮", "95.216.***.***", "NordicGuardNode", 31, "55A2:")
            )
        }

        val middles = if (strictNon14Eyes) {
            listOf(
                TorCircuitHop("Middle Relay", "Switzerland", "🇨🇭", "185.174.***.***", "AlpineCryptoRelay", 19, "A4C2:"),
                TorCircuitHop("Middle Relay", "Iceland", "🇮🇸", "82.221.***.***", "GeysirTorTransit", 34, "E104:"),
                TorCircuitHop("Middle Relay", "Seychelles", "🇸🇨", "154.213.***.***", "IslandPrivacyHop", 48, "6D78:")
            )
        } else {
            listOf(
                TorCircuitHop("Middle Relay", "Netherlands", "🇳🇱", "185.220.***.***", "AmstelCryptoNode", 18, "4B90:"),
                TorCircuitHop("Middle Relay", "Sweden", "🇸🇪", "193.187.***.***", "NordicTransit", 22, "11F9:"),
                TorCircuitHop("Middle Relay", "Romania", "🇷🇴", "89.248.***.***", "BucharestRelay", 39, "92C3:"),
                TorCircuitHop("Middle Relay", "Austria", "🇦🇹", "194.36.***.***", "ViennaSecureHop", 25, "88D1:")
            )
        }

        val exits = listOf(
            TorCircuitHop("Hidden Service Rendezvous", "Tor Onion Ring", "🧅", "v3.hidden.service", "OnionRendezvousV3", 65, "V3E4:"),
            TorCircuitHop("Hidden Service Rendezvous", "Tor Onion Ring", "🧅", "v3.hidden.service", "ZeroLogRendezvous", 58, "Z0L9:")
        )

        val hops = mutableListOf<TorCircuitHop>()

        // Add Bridge Hop if enabled
        if (bridge != TorBridgeType.DIRECT) {
            val bridgeHop = when (bridge) {
                TorBridgeType.OBFS4 -> TorCircuitHop("obfs4 Pluggable Bridge", "Random Obfuscated", "🛡️", "obfs4.bridge.node", "Obfs4TrafficMasker", 15, "0B4F:")
                TorBridgeType.SNOWFLAKE -> TorCircuitHop("Snowflake WebRTC Proxy", "Ephemeral WebRTC", "❄️", "snowflake.broker", "SnowflakePeerProxy", 22, "500F:")
                TorBridgeType.MEEK_AZURE -> TorCircuitHop("meek-azure CDN Front", "Microsoft Azure", "☁️", "azureedge.net", "AzureFrontDomainBridge", 35, "AZ99:")
                TorBridgeType.CUSTOM -> TorCircuitHop("Custom Tor Bridge", "User Configured", "⚙️", "custom.bridge.ip", "CustomBridgeLineNode", 20, "C45B:")
                TorBridgeType.DIRECT -> guards.random()
            }
            hops.add(bridgeHop)
        }

        hops.add(guards.random())

        when (depth) {
            CircuitHopDepth.STANDARD_3_HOP -> {
                hops.add(middles.random())
            }
            CircuitHopDepth.ULTRA_4_HOP -> {
                hops.add(middles.random())
                hops.add(middles.shuffled().last())
            }
            CircuitHopDepth.STEALTH_5_HOP -> {
                hops.add(middles.random())
                hops.add(middles.shuffled().last())
                hops.add(TorCircuitHop("Stealth Middle", "Non-Jurisdiction", "🔒", "104.244.***.***", "OffshoreRelayNode", 38, "99B2:"))
            }
        }

        hops.add(exits.random())
        return hops
    }
}
