package com.quicksms.app.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

class KeyManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "quicksms_auth_prefs"
        private const val KEY_RAW_API_KEY = "raw_api_key"
        private const val KEY_HASHED_API_KEY = "hashed_api_key"

        fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    fun getOrCreateApiKey(): String {
        val existing = prefs.getString(KEY_RAW_API_KEY, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }
        return generateNewApiKey()
    }

    fun generateNewApiKey(): String {
        val randomBytes = ByteArray(24)
        SecureRandom().nextBytes(randomBytes)
        val token = android.util.Base64.encodeToString(
            randomBytes,
            android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP
        )
        val rawKey = "qsms_$token"
        val hashed = sha256(rawKey)

        prefs.edit()
            .putString(KEY_RAW_API_KEY, rawKey)
            .putString(KEY_HASHED_API_KEY, hashed)
            .apply()

        return rawKey
    }

    fun isValidToken(providedToken: String?): Boolean {
        if (providedToken.isNullOrBlank()) return false
        val cleanToken = if (providedToken.startsWith("Bearer ", ignoreCase = true)) {
            providedToken.substring(7).trim()
        } else {
            providedToken.trim()
        }

        val storedHash = prefs.getString(KEY_HASHED_API_KEY, null) ?: return false
        val providedHash = sha256(cleanToken)

        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            providedHash.toByteArray(Charsets.UTF_8)
        )
    }
}
