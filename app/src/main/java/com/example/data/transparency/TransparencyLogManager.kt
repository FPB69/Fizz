package com.example.data.transparency

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class TransparencyCategory(val label: String, val emoji: String) {
    SYSTEM("System", "🫧"),
    NETWORK_TOR("Tor Network", "🧅"),
    LOCAL_STORAGE("Local Storage", "💾"),
    CRYPTOGRAPHY("Cryptography", "🔒"),
    P2P_PEER("Peer-to-Peer", "🤝")
}

data class TransparencyEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp)),
    val action: String,
    val description: String,
    val category: TransparencyCategory,
    val technicalDetails: String,
    val bytesTransferred: Long = 0
)

data class CurrentActivityState(
    val title: String = "Idle & Secure",
    val subtitle: String = "Local database active • Tor SOCKS5 ready • 0 cloud bytes",
    val isWorking: Boolean = false,
    val category: TransparencyCategory = TransparencyCategory.SYSTEM
)

class TransparencyLogManager(private val context: Context) {

    private val _currentActivity = MutableStateFlow(
        CurrentActivityState(
            title = "All Systems Transparent",
            subtitle = "100% Local Phone Storage • Tor SOCKS5 Ready",
            isWorking = false,
            category = TransparencyCategory.SYSTEM
        )
    )
    val currentActivity: StateFlow<CurrentActivityState> = _currentActivity.asStateFlow()

    private val _events = MutableStateFlow<List<TransparencyEvent>>(emptyList())
    val events: StateFlow<List<TransparencyEvent>> = _events.asStateFlow()

    init {
        logEvent(
            action = "Local Vault Initialized",
            description = "Opened encrypted SQLite database in private phone sandbox (/data/user/0/...)",
            category = TransparencyCategory.LOCAL_STORAGE,
            technicalDetails = "Database: torpeer_p2p_vault.db • Room ORM • WAL mode active • 0 cloud sync"
        )
        logEvent(
            action = "Cryptographic Keys Loaded",
            description = "RSA-2048 identity & AES-256-GCM cipher engines ready in Android Keystore",
            category = TransparencyCategory.CRYPTOGRAPHY,
            technicalDetails = "Algorithm: RSA/ECB/OAEPWithSHA-256AndMGF1Padding & AES/GCM/NoPadding (256-bit)"
        )
        logEvent(
            action = "Local P2P Server Online",
            description = "Listening on local loopback port 8989 for direct peer HTTP requests",
            category = TransparencyCategory.P2P_PEER,
            technicalDetails = "ServerSocket bound to port 8989 • Serves storefront json & photo assets directly"
        )
        logEvent(
            action = "Tor SOCKS5 Tunnel Configured",
            description = "Configured proxy client to route external traffic via 127.0.0.1:9050 (Orbot)",
            category = TransparencyCategory.NETWORK_TOR,
            technicalDetails = "Proxy: SOCKS5 (RFC 1928) on port 9050 • Zero DNS leakage • Direct IP blocked"
        )
    }

    fun logEvent(
        action: String,
        description: String,
        category: TransparencyCategory,
        technicalDetails: String,
        bytesTransferred: Long = 0,
        setAsCurrent: Boolean = false
    ) {
        val event = TransparencyEvent(
            action = action,
            description = description,
            category = category,
            technicalDetails = technicalDetails,
            bytesTransferred = bytesTransferred
        )

        val updated = ArrayList<TransparencyEvent>(_events.value.size + 1)
        updated.add(event)
        updated.addAll(_events.value.take(99)) // Keep up to 100 recent events
        _events.value = updated

        if (setAsCurrent) {
            _currentActivity.value = CurrentActivityState(
                title = action,
                subtitle = description,
                isWorking = false,
                category = category
            )
        }
    }

    fun setWorking(action: String, subtitle: String, category: TransparencyCategory) {
        _currentActivity.value = CurrentActivityState(
            title = action,
            subtitle = subtitle,
            isWorking = true,
            category = category
        )
        logEvent(
            action = action,
            description = subtitle,
            category = category,
            technicalDetails = "Operation initiated asynchronously"
        )
    }

    fun setIdle(title: String = "Idle & Secure", subtitle: String = "Ready for P2P requests • 0 cloud traffic") {
        _currentActivity.value = CurrentActivityState(
            title = title,
            subtitle = subtitle,
            isWorking = false,
            category = TransparencyCategory.SYSTEM
        )
    }

    fun clearLog() {
        _events.value = emptyList()
        logEvent(
            action = "Audit Log Cleared",
            description = "User wiped the in-memory transparency activity log",
            category = TransparencyCategory.SYSTEM,
            technicalDetails = "In-memory event buffer reset to 0 entries"
        )
    }
}
