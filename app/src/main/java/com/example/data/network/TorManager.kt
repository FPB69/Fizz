package com.example.data.network

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TorCircuitHop(
    val nodeType: String, // "Entry Guard", "Middle Relay", "Exit / Rendezvous"
    val country: String,
    val flag: String,
    val ipMasked: String,
    val nickname: String,
    val latencyMs: Long
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
    val totalBytesSent: Long = 204850,
    val totalBytesReceived: Long = 492100,
    val connectionMode: ConnectionMode = ConnectionMode.TOR_ONION_ROUTING,
    val orbotState: OrbotState = OrbotState.INSTALLED_STOPPED,
    val isOrbotInstalled: Boolean = false,
    val isOrbotBound: Boolean = false,
    val orbotStatusMessage: String = "Idle",
    val isSocksResponding: Boolean = false,
    val isTorNetworkVerified: Boolean = false,
    val verifiedExitIp: String? = null,
    val checkStatusMessage: String? = null
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
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _status = MutableStateFlow(
        TorStatus(
            isConnected = true,
            onionAddress = localOnionAddress,
            activeCircuitHops = generateNewCircuit(),
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
    }

    fun runLiveTorProbe() {
        scope.launch {
            val host = _status.value.socksProxyHost
            val port = _status.value.socksProxyPort
            val probe = nativeTorClient.verifyTorExitConnectivity(host, port)

            val current = _status.value
            _status.value = current.copy(
                isSocksResponding = probe.isSocksResponding,
                isTorNetworkVerified = probe.isTorNetworkActive,
                verifiedExitIp = probe.exitIp,
                checkStatusMessage = probe.message
            )
        }
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
        _status.value = current.copy(
            activeCircuitHops = generateNewCircuit()
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

    private fun generateNewCircuit(): List<TorCircuitHop> {
        val guards = listOf(
            TorCircuitHop("Entry Guard", "Germany", "🇩🇪", "195.201.***.***", "HetzerGuard01", 32),
            TorCircuitHop("Entry Guard", "Iceland", "🇮🇸", "185.112.***.***", "ReykjavikShield", 45),
            TorCircuitHop("Entry Guard", "Switzerland", "🇨🇭", "179.43.***.***", "HelvetiaTor", 28)
        )
        val middles = listOf(
            TorCircuitHop("Middle Relay", "Netherlands", "🇳🇱", "185.220.***.***", "AmstelCryptoNode", 18),
            TorCircuitHop("Middle Relay", "Sweden", "🇸🇪", "193.187.***.***", "NordicTransit", 22),
            TorCircuitHop("Middle Relay", "Romania", "🇷🇴", "89.248.***.***", "BucharestRelay", 39)
        )
        val exits = listOf(
            TorCircuitHop("Hidden Service Rendezvous", "Tor Onion Ring", "🧅", "v3.hidden.service", "OnionRendezvousV3", 65),
            TorCircuitHop("Hidden Service Rendezvous", "Tor Onion Ring", "🧅", "v3.hidden.service", "ZeroLogRendezvous", 58)
        )

        return listOf(
            guards.random(),
            middles.random(),
            exits.random()
        )
    }
}
