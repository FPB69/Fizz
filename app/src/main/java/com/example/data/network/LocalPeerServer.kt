package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.database.ListingEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class LocalPeerServer(
    private val context: Context,
    private val port: Int = 8989,
    private val getMyListingsProvider: suspend () -> List<ListingEntity>,
    private val getMyPeerInfo: () -> Triple<String, String, String>, // peerId, onion, fingerprint
    private val onMessageReceived: suspend (senderId: String, senderOnion: String, content: String, listingId: String?, listingTitle: String?, listingPrice: String?) -> Unit
) {
    private var serverJob: Job? = null
    private var serverSocket: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    var isRunning = false
        private set

    fun start() {
        if (isRunning) return
        isRunning = true
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                Log.d("TorPeerServer", "P2P Local Server started on port $port")
                while (isActive && isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        launch {
                            handleClient(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!isRunning) break
                    }
                }
            } catch (e: Exception) {
                Log.e("TorPeerServer", "Server error", e)
            } finally {
                isRunning = false
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverJob?.cancel()
    }

    private suspend fun handleClient(socket: Socket) {
        try {
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = input.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]

            // Read headers
            var line: String?
            var contentLength = 0
            while (input.readLine().also { line = it } != null && !line.isNullOrEmpty()) {
                if (line!!.lowercase().startsWith("content-length:")) {
                    contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            when {
                path == "/api/v1/ping" -> {
                    val (peerId, onion, fp) = getMyPeerInfo()
                    val json = JSONObject().apply {
                        put("status", "ONLINE")
                        put("peerId", peerId)
                        put("onion", onion)
                        put("fingerprint", fp)
                    }
                    sendJsonResponse(output, 200, json.toString())
                }

                path == "/api/v1/manifest" || path == "/api/v1/store" -> {
                    val (peerId, onion, fp) = getMyPeerInfo()
                    val listings = getMyListingsProvider()
                    val jsonArray = JSONArray()
                    for (item in listings) {
                        val itemJson = JSONObject().apply {
                            put("id", item.id)
                            put("title", item.title)
                            put("description", item.description)
                            put("price", item.price)
                            put("currency", item.currency)
                            put("category", item.category)
                            put("photoPath", item.photoPath)
                            put("sellerPeerId", peerId)
                            put("sellerOnion", onion)
                            put("createdAt", item.createdAt)
                            put("inStock", item.inStock)
                            put("deliveryMethod", item.deliveryMethod)
                        }
                        jsonArray.put(itemJson)
                    }

                    val responseJson = JSONObject().apply {
                        put("sellerPeerId", peerId)
                        put("sellerOnion", onion)
                        put("fingerprint", fp)
                        put("listings", jsonArray)
                    }
                    sendJsonResponse(output, 200, responseJson.toString())
                }

                path.startsWith("/api/v1/photo/") -> {
                    val filename = path.removePrefix("/api/v1/photo/")
                    servePhoto(filename, output)
                }

                method == "POST" && path == "/api/v1/message" -> {
                    val bodyChars = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val r = input.read(bodyChars, read, contentLength - read)
                        if (r == -1) break
                        read += r
                    }
                    val bodyStr = String(bodyChars, 0, read)
                    val json = JSONObject(bodyStr)
                    val senderId = json.optString("senderId", "unknown_peer")
                    val senderOnion = json.optString("senderOnion", "torpeer_node.onion")
                    val content = json.optString("content", "")
                    val listingId = json.optString("listingId", "").takeIf { it.isNotEmpty() }
                    val listingTitle = json.optString("listingTitle", "").takeIf { it.isNotEmpty() }
                    val listingPrice = json.optString("listingPrice", "").takeIf { it.isNotEmpty() }

                    onMessageReceived(senderId, senderOnion, content, listingId, listingTitle, listingPrice)
                    sendJsonResponse(output, 200, """{"status":"DELIVERED"}""")
                }

                else -> {
                    sendTextResponse(output, 404, "Not Found")
                }
            }
        } catch (e: Exception) {
            Log.e("TorPeerServer", "Handle client failed", e)
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun servePhoto(filename: String, output: OutputStream) {
        val photosDir = File(context.filesDir, "listings_photos")
        val file = File(photosDir, filename)
        if (file.exists() && file.canRead()) {
            val bytes = file.readBytes()
            val header = "HTTP/1.1 200 OK\r\nContent-Type: image/jpeg\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
            output.write(header.toByteArray())
            output.write(bytes)
            output.flush()
        } else {
            sendTextResponse(output, 404, "Photo not found on local device storage")
        }
    }

    private fun sendJsonResponse(output: OutputStream, code: Int, json: String) {
        val bytes = json.toByteArray(Charsets.UTF_8)
        val response = "HTTP/1.1 $code OK\r\nContent-Type: application/json; charset=utf-8\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n"
        output.write(response.toByteArray(Charsets.UTF_8))
        output.write(bytes)
        output.flush()
    }

    private fun sendTextResponse(output: OutputStream, code: Int, text: String) {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val response = "HTTP/1.1 $code Status\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n$text"
        output.write(response.toByteArray(Charsets.UTF_8))
        output.flush()
    }
}
