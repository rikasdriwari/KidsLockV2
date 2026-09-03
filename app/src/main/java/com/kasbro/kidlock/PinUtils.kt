package com.kasbro.kidlock

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

object PinUtils {
    private const val PREFS = "kidlock_prefs"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isPinSet(context: Context): Boolean =
        prefs(context).contains(KEY_HASH)

    fun savePin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)
        prefs(context).edit()
            .putString(KEY_SALT, salt.joinToString(",") { it.toString() })
            .putString(KEY_HASH, hash)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val saltStr = prefs(context).getString(KEY_SALT, null) ?: return false
        val salt = saltStr.split(",").map { it.toByte() }.toByteArray()
        val expected = prefs(context).getString(KEY_HASH, null) ?: return false
        return hash(pin, salt) == expected
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
