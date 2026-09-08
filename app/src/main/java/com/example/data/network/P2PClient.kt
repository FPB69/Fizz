package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.database.ListingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class P2PClient(
    private val context: Context,
    private val nativeTorClient: NativeTorProxyClient = NativeTorProxyClient()
) {

    private fun getClient(
        useTorProxy: Boolean = false,
        proxyHost: String = NativeTorProxyClient.DEFAULT_SOCKS_HOST,
        proxyPort: Int = NativeTorProxyClient.DEFAULT_SOCKS_PORT
    ): OkHttpClient {
        return if (useTorProxy) {
            nativeTorClient.createTorEnforcedClient(
                socksHost = proxyHost,
                socksPort = proxyPort,
                connectTimeoutSec = 20,
                readTimeoutSec = 25
            )
        } else {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        }
    }

    suspend fun fetchPeerStore(
        targetAddress: String, // e.g., "192.168.1.50:8989" or "xyz.onion"
        useTorProxy: Boolean = true,
        proxyHost: String = NativeTorProxyClient.DEFAULT_SOCKS_HOST,
        proxyPort: Int = NativeTorProxyClient.DEFAULT_SOCKS_PORT
    ): Result<List<ListingEntity>> = withContext(Dispatchers.IO) {
        try {
            val url = if (targetAddress.startsWith("http://") || targetAddress.startsWith("https://")) {
                "$targetAddress/api/v1/store"
            } else {
                "http://$targetAddress/api/v1/store"
            }

            val client = getClient(useTorProxy, proxyHost, proxyPort)
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Peer returned HTTP ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty peer response"))
            val json = JSONObject(body)
            val sellerPeerId = json.optString("sellerPeerId", "peer_remote")
            val sellerOnion = json.optString("sellerOnion", targetAddress)
            val listingsArray = json.getJSONArray("listings")

            val list = mutableListOf<ListingEntity>()
            for (i in 0 until listingsArray.length()) {
                val item = listingsArray.getJSONObject(i)
                list.add(
                    ListingEntity(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        description = item.optString("description"),
                        price = item.optString("price"),
                        currency = item.optString("currency", "XMR"),
                        category = item.optString("category", "General"),
                        photoPath = item.optString("photoPath"),
                        sellerPeerId = sellerPeerId,
                        sellerOnion = sellerOnion,
                        createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                        isMine = false,
                        inStock = item.optBoolean("inStock", true),
                        deliveryMethod = item.optString("deliveryMethod", "P2P Encrypted Handshake")
                    )
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPeerMessage(
        targetAddress: String,
        senderId: String,
        senderOnion: String,
        encryptedContent: String,
        listingId: String? = null,
        listingTitle: String? = null,
        listingPrice: String? = null,
        useTorProxy: Boolean = true,
        proxyHost: String = NativeTorProxyClient.DEFAULT_SOCKS_HOST,
        proxyPort: Int = NativeTorProxyClient.DEFAULT_SOCKS_PORT
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val url = if (targetAddress.startsWith("http://") || targetAddress.startsWith("https://")) {
                "$targetAddress/api/v1/message"
            } else {
                "http://$targetAddress/api/v1/message"
            }

            val json = JSONObject().apply {
                put("senderId", senderId)
                put("senderOnion", senderOnion)
                put("content", encryptedContent)
                listingId?.let { put("listingId", it) }
                listingTitle?.let { put("listingTitle", it) }
                listingPrice?.let { put("listingPrice", it) }
            }

            val client = getClient(useTorProxy, proxyHost, proxyPort)
            val reqBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(reqBody).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Peer rejected: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
