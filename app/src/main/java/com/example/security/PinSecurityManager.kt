package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PinSecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vault_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL_MS = "key_lockout_until_ms"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_AUTO_LOCK_SECONDS = "key_auto_lock_seconds"
        private const val KEY_JAZZ_HELPLINE = "key_jazz_helpline"
        private const val KEY_ZONG_HELPLINE = "key_zong_helpline"
        private const val KEY_VAULT_FOLDER_URI = "key_vault_folder_uri"

        private const val ITERATIONS = 12000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16

        const val DEFAULT_JAZZ_HELPLINE = "111"
        const val DEFAULT_ZONG_HELPLINE = "310"
    }

    val isPinSetup: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_SECONDS, 0) // 0 = Immediate
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_SECONDS, value).apply()

    var jazzHelpline: String
        get() = prefs.getString(KEY_JAZZ_HELPLINE, DEFAULT_JAZZ_HELPLINE) ?: DEFAULT_JAZZ_HELPLINE
        set(value) = prefs.edit().putString(KEY_JAZZ_HELPLINE, value.trim()).apply()

    var zongHelpline: String
        get() = prefs.getString(KEY_ZONG_HELPLINE, DEFAULT_ZONG_HELPLINE) ?: DEFAULT_ZONG_HELPLINE
        set(value) = prefs.edit().putString(KEY_ZONG_HELPLINE, value.trim()).apply()

    var vaultFolderUri: String?
        get() = prefs.getString(KEY_VAULT_FOLDER_URI, null)
        set(value) = prefs.edit().putString(KEY_VAULT_FOLDER_URI, value).apply()

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val hash = hashPin(pin, salt) ?: return false
        prefs.edit()
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
            .apply()
        return true
    }

    fun verifyPin(pin: String): VerificationResult {
        val lockoutRemainingMs = getLockoutRemainingMs()
        if (lockoutRemainingMs > 0) {
            return VerificationResult.LockedOut(lockoutRemainingMs)
        }

        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null)
        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null)
        if (storedHashBase64 == null || storedSaltBase64 == null) {
            return VerificationResult.NoPinConfigured
        }

        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)

        val computedHash = hashPin(pin, storedSalt)
        if (computedHash == null) {
            return VerificationResult.Error("Hashing algorithm error")
        }

        val matches = MessageDigest.isEqual(storedHash, computedHash)
        if (matches) {
            // Reset failure counters
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL_MS, 0L)
                .apply()
            return VerificationResult.Success
        } else {
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedAttempts)

            val lockoutSeconds = when {
                failedAttempts >= 7 -> 300 // 5 minutes
                failedAttempts == 6 -> 60  // 1 minute
                failedAttempts == 5 -> 30  // 30 seconds
                else -> 0
            }

            if (lockoutSeconds > 0) {
                val lockoutUntil = System.currentTimeMillis() + (lockoutSeconds * 1000L)
                editor.putLong(KEY_LOCKOUT_UNTIL_MS, lockoutUntil)
                editor.apply()
                return VerificationResult.LockedOut(lockoutSeconds * 1000L)
            } else {
                editor.apply()
                return VerificationResult.Failed(5 - failedAttempts)
            }
        }
    }

    fun getLockoutRemainingMs(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_MS, 0L)
        val diff = lockoutUntil - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    /**
     * Resets app authentication, PIN, and preferences.
     * Note: This does NOT delete or alter user media on storage.
     */
    fun safeResetAuth() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL_MS)
            .apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray? {
        return try {
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } catch (e: NoSuchAlgorithmException) {
            try {
                // Fallback for older API runtimes
                val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                factory.generateSecret(spec).encoded
            } catch (e2: Exception) {
                null
            }
        } catch (e: InvalidKeySpecException) {
            null
        }
    }

    sealed class VerificationResult {
        object Success : VerificationResult()
        data class Failed(val attemptsRemainingBeforeLockout: Int) : VerificationResult()
        data class LockedOut(val remainingMs: Long) : VerificationResult()
        object NoPinConfigured : VerificationResult()
        data class Error(val message: String) : VerificationResult()
    }
}
