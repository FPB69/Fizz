package com.example.data.network

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OrbotState {
    NOT_INSTALLED,
    INSTALLED_STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
    BOUND
}

data class OrbotConnectionInfo(
    val state: OrbotState = OrbotState.INSTALLED_STOPPED,
    val socksPort: Int = 9050,
    val httpPort: Int = 8118,
    val isInstalled: Boolean = false,
    val isBound: Boolean = false,
    val statusMessage: String = "Idle"
)

class OrbotManager(private val context: Context) {

    companion object {
        const val ORBOT_PACKAGE = "org.torproject.android"
        const val ACTION_START = "org.torproject.android.intent.action.START"
        const val ACTION_STOP = "org.torproject.android.intent.action.STOP"
        const val ACTION_STATUS = "org.torproject.android.intent.action.STATUS"

        const val EXTRA_STATUS = "org.torproject.android.intent.extra.STATUS"
        const val EXTRA_SOCKS_PROXY_PORT = "org.torproject.android.intent.extra.SOCKS_PROXY_PORT"
        const val EXTRA_HTTP_PROXY_PORT = "org.torproject.android.intent.extra.HTTP_PROXY_PORT"
        const val EXTRA_PACKAGE_NAME = "org.torproject.android.intent.extra.PACKAGE_NAME"

        const val STATUS_ON = "ON"
        const val STATUS_OFF = "OFF"
        const val STATUS_STARTING = "STARTING"
        const val STATUS_STOPPING = "STOPPING"
    }

    private val _connectionInfo = MutableStateFlow(OrbotConnectionInfo())
    val connectionInfo: StateFlow<OrbotConnectionInfo> = _connectionInfo.asStateFlow()

    private var isReceiverRegistered = false
    private var isServiceBound = false

    private val orbotReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == ACTION_STATUS) {
                val statusStr = intent.getStringExtra(EXTRA_STATUS) ?: ""
                val socksPort = intent.getIntExtra(EXTRA_SOCKS_PROXY_PORT, 9050)
                val httpPort = intent.getIntExtra(EXTRA_HTTP_PROXY_PORT, 8118)

                Log.d("OrbotManager", "Received Orbot broadcast: status=$statusStr, socksPort=$socksPort")

                val state = when (statusStr) {
                    STATUS_ON -> OrbotState.RUNNING
                    STATUS_STARTING -> OrbotState.STARTING
                    STATUS_STOPPING -> OrbotState.STOPPING
                    STATUS_OFF -> OrbotState.INSTALLED_STOPPED
                    else -> OrbotState.RUNNING
                }

                _connectionInfo.value = _connectionInfo.value.copy(
                    state = state,
                    socksPort = socksPort,
                    httpPort = httpPort,
                    statusMessage = "Orbot Status: $statusStr ($socksPort)"
                )
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("OrbotManager", "Connected to Orbot TorService: $name")
            isServiceBound = true
            _connectionInfo.value = _connectionInfo.value.copy(
                isBound = true,
                state = OrbotState.RUNNING,
                statusMessage = "Orbot Service Bound & Active"
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("OrbotManager", "Disconnected from Orbot TorService")
            isServiceBound = false
            _connectionInfo.value = _connectionInfo.value.copy(
                isBound = false,
                statusMessage = "Orbot Service Disconnected"
            )
        }
    }

    init {
        checkOrbotInstalled()
        registerStatusReceiver()
    }

    fun isInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    ORBOT_PACKAGE,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(ORBOT_PACKAGE, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun checkOrbotInstalled() {
        val installed = isInstalled()
        _connectionInfo.value = _connectionInfo.value.copy(
            isInstalled = installed,
            state = if (installed) OrbotState.INSTALLED_STOPPED else OrbotState.NOT_INSTALLED,
            statusMessage = if (installed) "Orbot Installed (Ready to Start)" else "Orbot not installed on device"
        )
    }

    fun registerStatusReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(ACTION_STATUS)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(orbotReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(orbotReceiver, filter)
                }
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.w("OrbotManager", "Could not register Orbot broadcast receiver", e)
            }
        }
    }

    fun unregisterStatusReceiver() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(orbotReceiver)
                isReceiverRegistered = false
            } catch (_: Exception) {}
        }
    }

    fun startOrbot(): Boolean {
        _connectionInfo.value = _connectionInfo.value.copy(
            state = OrbotState.STARTING,
            statusMessage = "Requesting Orbot Tor start..."
        )

        val intent = Intent(ACTION_START).apply {
            setPackage(ORBOT_PACKAGE)
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
        }

        return try {
            context.sendBroadcast(intent)
            bindTorService()
            true
        } catch (e: Exception) {
            Log.e("OrbotManager", "Error broadcasting start to Orbot", e)
            false
        }
    }

    fun stopOrbot(): Boolean {
        _connectionInfo.value = _connectionInfo.value.copy(
            state = OrbotState.STOPPING,
            statusMessage = "Requesting Orbot Tor stop..."
        )

        val intent = Intent(ACTION_STOP).apply {
            setPackage(ORBOT_PACKAGE)
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
        }

        return try {
            context.sendBroadcast(intent)
            unbindTorService()
            _connectionInfo.value = _connectionInfo.value.copy(
                state = OrbotState.INSTALLED_STOPPED,
                statusMessage = "Orbot Stopped"
            )
            true
        } catch (e: Exception) {
            Log.e("OrbotManager", "Error stopping Orbot", e)
            false
        }
    }

    fun bindTorService(): Boolean {
        val serviceIntent = Intent().apply {
            component = ComponentName(ORBOT_PACKAGE, "org.torproject.android.service.TorService")
            putExtra(EXTRA_PACKAGE_NAME, context.packageName)
        }

        return try {
            val bound = context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                // Try fallback to TorVpnService
                val vpnIntent = Intent().apply {
                    component = ComponentName(ORBOT_PACKAGE, "org.torproject.android.service.vpn.TorVpnService")
                }
                context.bindService(vpnIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                true
            }
        } catch (e: Exception) {
            Log.w("OrbotManager", "Could not bind to Orbot service directly", e)
            false
        }
    }

    fun unbindTorService() {
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
                isServiceBound = false
            } catch (_: Exception) {}
        }
    }

    fun getInstallIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=$ORBOT_PACKAGE")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
