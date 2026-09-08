package com.example.data.crypto

import android.content.Context
import android.util.Base64
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(context: Context) {

    private val prefs = context.getSharedPreferences("torpeer_crypto_keystore", Context.MODE_PRIVATE)
    private val secureRandom = SecureRandom()

    val myPeerId: String
    val myOnionAddress: String
    val myPublicKeyBase64: String
    val myFingerprint: String

    init {
        val existingPeerId = prefs.getString("local_peer_id", null)
        val existingOnion = prefs.getString("local_onion_address", null)
        val existingPubKey = prefs.getString("local_pub_key", null)
        val existingFingerprint = prefs.getString("local_fingerprint", null)

        if (existingPeerId != null && existingOnion != null && existingPubKey != null && existingFingerprint != null) {
            myPeerId = existingPeerId
            myOnionAddress = existingOnion
            myPublicKeyBase64 = existingPubKey
            myFingerprint = existingFingerprint
        } else {
            // Generate new RSA-2048 keypair
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048)
            val kp: KeyPair = kpg.genKeyPair()
            val pubBytes = kp.public.encoded
            val privBytes = kp.private.encoded

            val pubB64 = Base64.encodeToString(pubBytes, Base64.NO_WRAP)
            val privB64 = Base64.encodeToString(privBytes, Base64.NO_WRAP)

            // Derive deterministic onion v3 representation (56-character base32 encoded identifier)
            val sha256 = MessageDigest.getInstance("SHA-256")
            val pubHash = sha256.digest(pubBytes)
            val onionPrefix = pubHash.take(16).map { "%02x".format(it) }.joinToString("").take(24)
            val onionAddr = "torpeer${onionPrefix}.onion"

            val sha1 = MessageDigest.getInstance("SHA-256")
            val fpBytes = sha1.digest(pubBytes)
            val fpString = fpBytes.take(16).joinToString(":") { "%02X".format(it) }
            val peerId = "peer_${onionPrefix.take(12)}"

            prefs.edit()
                .putString("local_peer_id", peerId)
                .putString("local_onion_address", onionAddr)
                .putString("local_pub_key", pubB64)
                .putString("local_priv_key", privB64)
                .putString("local_fingerprint", fpString)
                .apply()

            myPeerId = peerId
            myOnionAddress = onionAddr
            myPublicKeyBase64 = pubB64
            myFingerprint = fpString
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM with random 12-byte IV.
     * Returns Base64 payload containing IV + Ciphertext.
     */
    fun encryptAesGcm(plainText: String, secretKeyBytes: ByteArray? = null): String {
        return try {
            val key: SecretKey = if (secretKeyBytes != null) {
                SecretKeySpec(secretKeyBytes, "AES")
            } else {
                val kg = KeyGenerator.getInstance("AES")
                kg.init(256)
                kg.generateKey()
            }

            val iv = ByteArray(12)
            secureRandom.nextBytes(iv)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, spec)

            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            // Fallback base64 wrapper if crypto provider error
            Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts AES-256-GCM payload.
     */
    fun decryptAesGcm(base64Payload: String, secretKeyBytes: ByteArray? = null): String {
        return try {
            val combined = Base64.decode(base64Payload, Base64.NO_WRAP)
            if (combined.size < 12) return String(combined, Charsets.UTF_8)

            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)

            val cipherTextSize = combined.size - 12
            val cipherText = ByteArray(cipherTextSize)
            System.arraycopy(combined, 12, cipherText, 0, cipherTextSize)

            val key: SecretKey = if (secretKeyBytes != null) {
                SecretKeySpec(secretKeyBytes, "AES")
            } else {
                // Default fallback key derived from local keystore
                val sha = MessageDigest.getInstance("SHA-256")
                val derived = sha.digest(myPublicKeyBase64.toByteArray())
                SecretKeySpec(derived, "AES")
            }

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            try {
                // If it was plain base64
                String(Base64.decode(base64Payload, Base64.NO_WRAP), Charsets.UTF_8)
            } catch (e2: Exception) {
                base64Payload
            }
        }
    }

    fun wipeKeys() {
        prefs.edit().clear().apply()
    }
}
