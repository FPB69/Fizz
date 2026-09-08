package com.example.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.concurrent.TimeUnit

data class TorProxyVerificationResult(
    val isSocksResponding: Boolean,
    val isTorNetworkActive: Boolean,
    val exitIp: String? = null,
    val latencyMs: Long = 0,
    val message: String
)

class NativeTorProxyClient {

    companion object {
        const val DEFAULT_SOCKS_HOST = "127.0.0.1"
        const val DEFAULT_SOCKS_PORT = 9050
        const val TOR_CHECK_URL = "https://check.torproject.org/api/ip"
    }

    /**
     * Creates an OkHttpClient strictly configured to route 100% of network traffic
     * through the specified Tor SOCKS5 proxy.
     * Prevents DNS leaks by letting the SOCKS5 proxy resolve hostnames.
     */
    fun createTorEnforcedClient(
        socksHost: String = DEFAULT_SOCKS_HOST,
        socksPort: Int = DEFAULT_SOCKS_PORT,
        connectTimeoutSec: Long = 20,
        readTimeoutSec: Long = 25
    ): OkHttpClient {
        val proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(socksHost, socksPort))

        return OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(connectTimeoutSec, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .writeTimeout(readTimeoutSec, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Probes the local SOCKS5 port with an RFC 1928 SOCKS handshake to verify
     * that a SOCKS proxy is listening and ready to accept connections.
     */
    suspend fun probeSocks5Handshake(
        host: String = DEFAULT_SOCKS_HOST,
        port: Int = DEFAULT_SOCKS_PORT
    ): Boolean = withContext(Dispatchers.IO) {
        var socket: Socket? = null
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(host, port), 2500)
            socket.soTimeout = 2500

            val out: OutputStream = socket.getOutputStream()
            val inp: InputStream = socket.getInputStream()

            // SOCKS5 greeting: VER=0x05, NMETHODS=1, METHOD=0x00 (NO AUTH)
            out.write(byteArrayOf(0x05, 0x01, 0x00))
            out.flush()

            val response = ByteArray(2)
            val bytesRead = inp.read(response)

            // SOCKS5 response should be [0x05, 0x00]
            val success = bytesRead == 2 && response[0] == 0x05.toByte() && response[1] == 0x00.toByte()
            Log.d("NativeTorProxyClient", "SOCKS5 probe on $host:$port result: $success")
            success
        } catch (e: Exception) {
            Log.d("NativeTorProxyClient", "SOCKS5 probe failed on $host:$port: ${e.message}")
            false
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Checks actual Tor network connectivity through the SOCKS5 proxy by querying
     * check.torproject.org. Confirms exit IP and verifies if the node is a Tor relay.
     */
    suspend fun verifyTorExitConnectivity(
        host: String = DEFAULT_SOCKS_HOST,
        port: Int = DEFAULT_SOCKS_PORT
    ): TorProxyVerificationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val socksReachable = probeSocks5Handshake(host, port)

        if (!socksReachable) {
            return@withContext TorProxyVerificationResult(
                isSocksResponding = false,
                isTorNetworkActive = false,
                latencyMs = System.currentTimeMillis() - startTime,
                message = "Local SOCKS5 proxy port $port is not responding (ensure Orbot is running)"
            )
        }

        try {
            val client = createTorEnforcedClient(host, port, connectTimeoutSec = 10, readTimeoutSec = 10)
            val request = Request.Builder()
                .url(TOR_CHECK_URL)
                .header("User-Agent", "Fizz-Tor-Client/1.0")
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val isTor = json.optBoolean("IsTor", true)
                val exitIp = json.optString("IP", "Masked via Tor")

                TorProxyVerificationResult(
                    isSocksResponding = true,
                    isTorNetworkActive = isTor,
                    exitIp = exitIp,
                    latencyMs = latency,
                    message = if (isTor) "Verified Tor Circuit Active (Exit IP: $exitIp)" else "Proxy active (Non-Tor exit: $exitIp)"
                )
            } else {
                TorProxyVerificationResult(
                    isSocksResponding = true,
                    isTorNetworkActive = false,
                    latencyMs = latency,
                    message = "Tor check returned HTTP ${response.code}"
                )
            }
        } catch (e: Exception) {
            // SOCKS is open, but external internet / Tor circuit may be building
            val latency = System.currentTimeMillis() - startTime
            TorProxyVerificationResult(
                isSocksResponding = true,
                isTorNetworkActive = false,
                latencyMs = latency,
                message = "SOCKS5 active on $port (Establishing circuit: ${e.message})"
            )
        }
    }
}
