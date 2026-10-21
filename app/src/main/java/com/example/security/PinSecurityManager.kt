package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class DisguiseMode {
    SIM_SERVICE,
    CALCULATOR,
    NOTES
}

data class IntruderLog(
    val timestamp: Long,
    val attemptedPinLength: Int,
    val failedCount: Int
)

/**
 * PinSecurityManager handles PIN setup, cryptographic hashing (PBKDF2-HMAC-SHA256),
 * decoy PIN verification, lockout rate-limiting, and auto-lock timeouts using SystemClock.elapsedRealtime().
 * Conforms to Master Build Spec v3 (Sections 9.1-9.4, Acceptance Criteria 15-17).
 */
class PinSecurityManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vault_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_SALT = "key_pin_salt"
        private const val KEY_DECOY_PIN_HASH = "key_decoy_pin_hash"
        private const val KEY_DECOY_PIN_SALT = "key_decoy_pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "key_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL_ELAPSED_MS = "key_lockout_until_elapsed_ms"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_FLAG_SECURE = "key_flag_secure"
        private const val KEY_GO_STRAIGHT_TO_VAULT = "key_go_straight_to_vault"
        private const val KEY_AUTO_LOCK_SECONDS = "key_auto_lock_seconds"
        private const val KEY_LAST_BACKGROUND_ELAPSED_MS = "key_last_background_elapsed_ms"
        private const val KEY_FIRST_TIME_SETUP_DONE = "key_first_time_setup_done"
        private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"
        private const val KEY_SHUFFLE_KEYPAD = "key_shuffle_keypad"
        private const val KEY_JAZZ_HELPLINE = "key_jazz_helpline"
        private const val KEY_ZONG_HELPLINE = "key_zong_helpline"
        private const val KEY_VAULT_FOLDER_URI = "key_vault_folder_uri"
        private const val KEY_DISGUISE_MODE = "key_disguise_mode"
        private const val KEY_FLIP_TO_LOCK = "key_flip_to_lock"
        private const val KEY_INTRUDER_LOGS = "key_intruder_logs"
        private const val KEY_PIN_LENGTH = "key_pin_length"
        private const val KEY_LOCK_ON_SCREEN_OFF = "key_lock_on_screen_off"
        private const val KEY_DIRECT_PIN_LAUNCH = "key_direct_pin_launch"
        private const val KEY_SHAKE_TO_LOCK = "key_shake_to_lock"
        private const val KEY_SENSITIVE_ACTION_REAUTH = "key_sensitive_action_reauth"

        // Master Spec v3 Section 32: Intruder Photo Settings
        private const val KEY_INTRUDER_PHOTO_ENABLED = "key_intruder_photo_enabled"
        private const val KEY_INTRUDER_THRESHOLD = "key_intruder_threshold"
        private const val KEY_INTRUDER_CAPTURE_ON_BIOMETRIC = "key_intruder_capture_on_biometric"
        private const val KEY_INTRUDER_RETENTION_COUNT = "key_intruder_retention_count"
        private const val KEY_INTRUDER_IMMEDIATE_NOTIFICATION = "key_intruder_immediate_notification"

        // Master Spec v3 Section 33: Playback Privacy & Loop Settings
        private const val KEY_STOP_ON_HEADPHONE_DISCONNECT = "key_stop_on_headphone_disconnect"
        private const val KEY_SET_VOLUME_ZERO_ON_DISCONNECT = "key_set_volume_zero_on_disconnect"
        private const val KEY_SHOW_RESUME_BANNER = "key_show_resume_banner"
        private const val KEY_PAUSE_AUDIO_ON_LOCK = "key_pause_audio_on_lock"
        private const val KEY_AUTO_RESUME_AFTER_UNLOCK = "key_auto_resume_after_unlock"
        private const val KEY_DEFAULT_LOOP_MODE = "key_default_loop_mode"

        private const val ITERATIONS = 12000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16

        // Auto-lock timing constants (Master Spec v3 Section 9.4 & Acceptance Criterion 15)
        const val AUTO_LOCK_IMMEDIATE = 0
        const val AUTO_LOCK_1_MIN = 60      // DEFAULT: 1 minute
        const val AUTO_LOCK_2_MIN = 120
        const val AUTO_LOCK_5_MIN = 300
        const val AUTO_LOCK_30_MIN = 1800
        const val AUTO_LOCK_ON_APP_CLOSE = -1

        // Verified Pakistani Telecom Customer Care Helplines (Section 10.2)
        const val DEFAULT_JAZZ_HELPLINE = "111"
        const val DEFAULT_ZONG_HELPLINE = "310"
    }

    var disguiseMode: DisguiseMode
        get() {
            val name = prefs.getString(KEY_DISGUISE_MODE, DisguiseMode.SIM_SERVICE.name)
            return try { DisguiseMode.valueOf(name ?: DisguiseMode.SIM_SERVICE.name) } catch (e: Exception) { DisguiseMode.SIM_SERVICE }
        }
        set(value) = prefs.edit().putString(KEY_DISGUISE_MODE, value.name).apply()

    var isFlipToLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLIP_TO_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_FLIP_TO_LOCK, value).apply()

    fun recordIntruderAttempt(pinLength: Int, failedCount: Int) {
        val entry = "${System.currentTimeMillis()}:$pinLength:$failedCount"
        val existing = prefs.getStringSet(KEY_INTRUDER_LOGS, emptySet()) ?: emptySet()
        val updated = (existing + entry).toList().takeLast(30).toSet()
        prefs.edit().putStringSet(KEY_INTRUDER_LOGS, updated).apply()
    }

    fun getIntruderLogs(): List<IntruderLog> {
        val entries = prefs.getStringSet(KEY_INTRUDER_LOGS, emptySet()) ?: emptySet()
        return entries.mapNotNull { str ->
            val parts = str.split(":")
            if (parts.size >= 3) {
                val time = parts[0].toLongOrNull() ?: return@mapNotNull null
                val len = parts[1].toIntOrNull() ?: 0
                val count = parts[2].toIntOrNull() ?: 1
                IntruderLog(time, len, count)
            } else null
        }.sortedByDescending { it.timestamp }
    }

    fun clearIntruderLogs() {
        prefs.edit().remove(KEY_INTRUDER_LOGS).apply()
    }

    val isPinSetup: Boolean
        get() = prefs.getString(KEY_PIN_HASH, null) != null

    val isDecoyPinSetup: Boolean
        get() = prefs.getString(KEY_DECOY_PIN_HASH, null) != null

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isFlagSecureEnabled: Boolean
        get() = prefs.getBoolean(KEY_FLAG_SECURE, true)
        set(value) = prefs.edit().putBoolean(KEY_FLAG_SECURE, value).apply()

    var goStraightToVault: Boolean
        get() = prefs.getBoolean(KEY_GO_STRAIGHT_TO_VAULT, false)
        set(value) = prefs.edit().putBoolean(KEY_GO_STRAIGHT_TO_VAULT, value).apply()

    // Default auto-lock is 1 minute (Section 9.4 & Acceptance Criterion 21)
    var autoLockSeconds: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_SECONDS, AUTO_LOCK_1_MIN)
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_SECONDS, value).apply()

    // Track background timing via SystemClock.elapsedRealtime() (Criterion 17)
    var lastBackgroundElapsedRealtime: Long
        get() = prefs.getLong(KEY_LAST_BACKGROUND_ELAPSED_MS, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_BACKGROUND_ELAPSED_MS, value).apply()

    var isFirstTimeSetupDone: Boolean
        get() = prefs.getBoolean(KEY_FIRST_TIME_SETUP_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_TIME_SETUP_DONE, value).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var isShuffleKeypadEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHUFFLE_KEYPAD, false)
        set(value) = prefs.edit().putBoolean(KEY_SHUFFLE_KEYPAD, value).apply()

    var jazzHelpline: String
        get() = prefs.getString(KEY_JAZZ_HELPLINE, DEFAULT_JAZZ_HELPLINE) ?: DEFAULT_JAZZ_HELPLINE
        set(value) = prefs.edit().putString(KEY_JAZZ_HELPLINE, value.trim()).apply()

    var zongHelpline: String
        get() = prefs.getString(KEY_ZONG_HELPLINE, DEFAULT_ZONG_HELPLINE) ?: DEFAULT_ZONG_HELPLINE
        set(value) = prefs.edit().putString(KEY_ZONG_HELPLINE, value.trim()).apply()

    var vaultFolderUri: String?
        get() = prefs.getString(KEY_VAULT_FOLDER_URI, null)
        set(value) = prefs.edit().putString(KEY_VAULT_FOLDER_URI, value).apply()

    var isDecoySessionActive: Boolean = false

    // Sensitive action re-auth timestamp (valid for 2 minutes per Section 9.4)
    private var lastSensitiveActionAuthTime: Long = 0L

    fun recordSensitiveActionAuth() {
        lastSensitiveActionAuthTime = SystemClock.elapsedRealtime()
    }

    fun isSensitiveActionAuthValid(): Boolean {
        if (lastSensitiveActionAuthTime == 0L) return false
        val diff = SystemClock.elapsedRealtime() - lastSensitiveActionAuthTime
        return diff < 120_000L // 2 minutes
    }

    var pinLength: Int
        get() = prefs.getInt(KEY_PIN_LENGTH, 4)
        set(value) = prefs.edit().putInt(KEY_PIN_LENGTH, value.coerceIn(4, 8)).apply()

    var isLockOnScreenOffEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ON_SCREEN_OFF, true)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_ON_SCREEN_OFF, value).apply()

    var isDirectPinLaunchEnabled: Boolean
        get() = prefs.getBoolean(KEY_DIRECT_PIN_LAUNCH, false)
        set(value) = prefs.edit().putBoolean(KEY_DIRECT_PIN_LAUNCH, value).apply()

    var isShakeToLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHAKE_TO_LOCK, false)
        set(value) = prefs.edit().putBoolean(KEY_SHAKE_TO_LOCK, value).apply()

    var isSensitiveActionReauthRequired: Boolean
        get() = prefs.getBoolean(KEY_SENSITIVE_ACTION_REAUTH, true)
        set(value) = prefs.edit().putBoolean(KEY_SENSITIVE_ACTION_REAUTH, value).apply()

    // --- Master Spec v3 Section 32: Intruder Photo Settings ---
    var isIntruderPhotoEnabled: Boolean
        get() = prefs.getBoolean(KEY_INTRUDER_PHOTO_ENABLED, false) // Default OFF per Spec 32.1
        set(value) = prefs.edit().putBoolean(KEY_INTRUDER_PHOTO_ENABLED, value).apply()

    var intruderThreshold: Int
        get() = prefs.getInt(KEY_INTRUDER_THRESHOLD, 1) // Default 1 attempt per Spec 32.1
        set(value) = prefs.edit().putInt(KEY_INTRUDER_THRESHOLD, value.coerceIn(1, 3)).apply()

    var isCaptureOnBiometricFailureEnabled: Boolean
        get() = prefs.getBoolean(KEY_INTRUDER_CAPTURE_ON_BIOMETRIC, false) // Default OFF per Spec 32.1
        set(value) = prefs.edit().putBoolean(KEY_INTRUDER_CAPTURE_ON_BIOMETRIC, value).apply()

    var intruderRetentionCount: Int
        get() = prefs.getInt(KEY_INTRUDER_RETENTION_COUNT, 50) // Default 50 per Spec 32.3
        set(value) = prefs.edit().putInt(KEY_INTRUDER_RETENTION_COUNT, value).apply()

    var isIntruderImmediateNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_INTRUDER_IMMEDIATE_NOTIFICATION, false) // Default OFF per Spec 32.4
        set(value) = prefs.edit().putBoolean(KEY_INTRUDER_IMMEDIATE_NOTIFICATION, value).apply()

    // --- Master Spec v3 Section 33: Playback Privacy Settings ---
    var isStopOnHeadphoneDisconnectEnabled: Boolean
        get() = prefs.getBoolean(KEY_STOP_ON_HEADPHONE_DISCONNECT, true) // Default ON per Spec 33.2
        set(value) = prefs.edit().putBoolean(KEY_STOP_ON_HEADPHONE_DISCONNECT, value).apply()

    var isSetVolumeZeroOnDisconnectEnabled: Boolean
        get() = prefs.getBoolean(KEY_SET_VOLUME_ZERO_ON_DISCONNECT, true) // Default ON per Spec 33.2
        set(value) = prefs.edit().putBoolean(KEY_SET_VOLUME_ZERO_ON_DISCONNECT, value).apply()

    var isShowResumeBannerEnabled: Boolean
        get() = prefs.getBoolean(KEY_SHOW_RESUME_BANNER, true) // Default ON per Spec 33.2
        set(value) = prefs.edit().putBoolean(KEY_SHOW_RESUME_BANNER, value).apply()

    var isPauseAudioOnLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_PAUSE_AUDIO_ON_LOCK, false) // Default OFF per Spec 33.3
        set(value) = prefs.edit().putBoolean(KEY_PAUSE_AUDIO_ON_LOCK, value).apply()

    var isAutoResumeAfterUnlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RESUME_AFTER_UNLOCK, false) // Default OFF per Spec 33.3
        set(value) = prefs.edit().putBoolean(KEY_AUTO_RESUME_AFTER_UNLOCK, value).apply()

    var defaultLoopMode: Int
        get() = prefs.getInt(KEY_DEFAULT_LOOP_MODE, 0) // 0: OFF, 1: LOOP_ONE, 2: LOOP_ALL
        set(value) = prefs.edit().putInt(KEY_DEFAULT_LOOP_MODE, value.coerceIn(0, 2)).apply()

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val hash = hashPin(pin, salt) ?: return false
        prefs.edit()
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt(KEY_PIN_LENGTH, pin.length)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL_ELAPSED_MS, 0L)
            .apply()
        return true
    }

    fun setDecoyPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)

        val hash = hashPin(pin, salt) ?: return false
        prefs.edit()
            .putString(KEY_DECOY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putString(KEY_DECOY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
        return true
    }

    fun removeDecoyPin() {
        prefs.edit()
            .remove(KEY_DECOY_PIN_HASH)
            .remove(KEY_DECOY_PIN_SALT)
            .apply()
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val verification = verifyPin(oldPin)
        if (verification is VerificationResult.Success) {
            return setPin(newPin)
        }
        return false
    }

    fun verifyPin(pin: String): VerificationResult {
        val lockoutRemainingMs = getLockoutRemainingMs()
        if (lockoutRemainingMs > 0) {
            return VerificationResult.LockedOut(lockoutRemainingMs)
        }

        // Check if Decoy PIN matches first
        val storedDecoyHash = prefs.getString(KEY_DECOY_PIN_HASH, null)
        val storedDecoySalt = prefs.getString(KEY_DECOY_PIN_SALT, null)
        if (storedDecoyHash != null && storedDecoySalt != null) {
            val decoySaltBytes = Base64.decode(storedDecoySalt, Base64.NO_WRAP)
            val computedDecoyHash = hashPin(pin, decoySaltBytes)
            if (computedDecoyHash != null && MessageDigest.isEqual(Base64.decode(storedDecoyHash, Base64.NO_WRAP), computedDecoyHash)) {
                isDecoySessionActive = true
                prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
                recordSensitiveActionAuth()
                return VerificationResult.DecoySuccess
            }
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
            isDecoySessionActive = false
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL_ELAPSED_MS, 0L)
                .apply()
            recordSensitiveActionAuth()
            return VerificationResult.Success
        } else {
            val failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, failedAttempts)
            recordIntruderAttempt(pin.length, failedAttempts)

            // Rate limiting per Spec 9.3:
            // 5 wrong attempts: 30s, 6 wrong: 60s, 7 wrong: 300s, 8+ wrong: 900s (15 min cap)
            val lockoutSeconds = when {
                failedAttempts >= 8 -> 900
                failedAttempts == 7 -> 300
                failedAttempts == 6 -> 60
                failedAttempts == 5 -> 30
                else -> 0
            }

            if (lockoutSeconds > 0) {
                val lockoutUntil = SystemClock.elapsedRealtime() + (lockoutSeconds * 1000L)
                editor.putLong(KEY_LOCKOUT_UNTIL_ELAPSED_MS, lockoutUntil)
                editor.apply()
                return VerificationResult.LockedOut(lockoutSeconds * 1000L)
            } else {
                editor.apply()
                return VerificationResult.Failed(5 - failedAttempts)
            }
        }
    }

    fun getLockoutRemainingMs(): Long {
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL_ELAPSED_MS, 0L)
        val diff = lockoutUntil - SystemClock.elapsedRealtime()
        return if (diff > 0) diff else 0L
    }

    fun safeResetAuth() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_DECOY_PIN_HASH)
            .remove(KEY_DECOY_PIN_SALT)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKOUT_UNTIL_ELAPSED_MS)
            .remove(KEY_FIRST_TIME_SETUP_DONE)
            .putInt(KEY_AUTO_LOCK_SECONDS, AUTO_LOCK_1_MIN) // Reset to 1 minute default
            .apply()
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray? {
        return try {
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } catch (e: NoSuchAlgorithmException) {
            try {
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
        object DecoySuccess : VerificationResult()
        data class Failed(val attemptsRemainingBeforeLockout: Int) : VerificationResult()
        data class LockedOut(val remainingMs: Long) : VerificationResult()
        object NoPinConfigured : VerificationResult()
        data class Error(val message: String) : VerificationResult()
    }
}
