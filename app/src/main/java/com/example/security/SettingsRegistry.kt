package com.example.security

/**
 * Settings Registry — Single Source of Truth for all application settings.
 * Fulfills Private Vault Master Build Specification (v4, merged) Section 36 & PV-UI-001.
 *
 * Consolidates all settings across Sections 9.4, 9.9, 16, 31, 32, 33, and 34.
 * Prevents decorative or contradictory toggles.
 */
data class SettingDefinition<T>(
    val key: String,
    val type: SettingType,
    val defaultValue: T,
    val allowedValues: List<T>? = null,
    val title: String,
    val description: String,
    val effect: String,
    val securityImpact: Boolean,
    val requiredPermission: String? = null,
    val testVerification: String
)

enum class SettingType {
    BOOLEAN,
    INT,
    STRING,
    ENUM
}

object SettingsRegistry {

    // --- Authentication & Lock Settings (Spec 9.4 & 36) ---
    val AUTO_LOCK_MODE = SettingDefinition(
        key = "pref_auto_lock_seconds",
        type = SettingType.INT,
        defaultValue = PinSecurityManager.AUTO_LOCK_1_MIN,
        allowedValues = listOf(
            PinSecurityManager.AUTO_LOCK_IMMEDIATE,
            PinSecurityManager.AUTO_LOCK_1_MIN,
            PinSecurityManager.AUTO_LOCK_2_MIN,
            PinSecurityManager.AUTO_LOCK_5_MIN,
            PinSecurityManager.AUTO_LOCK_30_MIN,
            PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE
        ),
        title = "Auto-Lock Timeout",
        description = "Locks the vault after background inactivity using tamper-proof SystemClock timing.",
        effect = "Automatically locks vault and requires PIN/biometric when timeout expires.",
        securityImpact = true,
        testVerification = "AutoLockTimingTest"
    )

    val FLAG_SECURE = SettingDefinition(
        key = "pref_flag_secure",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Screen & Recents Privacy (FLAG_SECURE)",
        description = "Blocks screenshots and blurs/hides app contents in recent apps switcher.",
        effect = "Applies WindowManager.LayoutParams.FLAG_SECURE to Activity window.",
        securityImpact = true,
        testVerification = "ScreenPrivacyVerification"
    )

    val BIOMETRICS_ENABLED = SettingDefinition(
        key = "pref_biometrics_enabled",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Fingerprint / Biometric Unlock",
        description = "Allows unlocking vault via device biometric hardware.",
        effect = "Enables BiometricPrompt on PIN screen and cover screen.",
        securityImpact = true,
        requiredPermission = "android.permission.USE_BIOMETRIC",
        testVerification = "BiometricHelperTest"
    )

    val SHUFFLE_KEYPAD = SettingDefinition(
        key = "pref_shuffle_keypad",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Randomize Keypad Digits",
        description = "Shuffles keypad digits on each attempt to prevent smudge guessing.",
        effect = "Randomizes keypad digit order in PinEntryScreen.",
        securityImpact = true,
        testVerification = "KeypadShuffleTest"
    )

    val GO_STRAIGHT_TO_VAULT = SettingDefinition(
        key = "pref_direct_pin_launch",
        type = SettingType.BOOLEAN,
        defaultValue = false,
        title = "Direct Launch Mode",
        description = "Opens PIN screen immediately on launch instead of the SIM disguise cover.",
        effect = "Bypasses cover screen if enabled.",
        securityImpact = false,
        testVerification = "DirectLaunchTest"
    )

    val POST_AUTH_PANEL_ENABLED = SettingDefinition(
        key = "pref_show_post_auth_panel",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Show Post-Auth Carrier Panel",
        description = "Shows carrier action sheet with Helpline, STK, and Vault buttons after unlock.",
        effect = "Displays post-auth modal bottom sheet when authenticating from cover screen.",
        securityImpact = false,
        testVerification = "PostAuthPanelTest"
    )

    // --- Panic & Motion Gestures (Spec 9.10 & 36 - P3 Priority) ---
    val FLIP_TO_LOCK = SettingDefinition(
        key = "pref_flip_to_lock",
        type = SettingType.BOOLEAN,
        defaultValue = false,
        title = "Flip Phone Face Down to Lock",
        description = "Locks vault immediately when the device is placed face down (accelerometer z < -7).",
        effect = "SensorEventListener monitors accelerometer orientation and triggers lockVault().",
        securityImpact = true,
        testVerification = "SensorGestureTest"
    )

    val SHAKE_TO_LOCK = SettingDefinition(
        key = "pref_shake_to_lock",
        type = SettingType.BOOLEAN,
        defaultValue = false,
        title = "Shake to Lock",
        description = "Locks vault immediately when vigorous motion is detected.",
        effect = "Triggers lockVault() on acceleration delta > 12 m/s².",
        securityImpact = true,
        testVerification = "SensorGestureTest"
    )

    val LOCK_ON_SCREEN_OFF = SettingDefinition(
        key = "pref_lock_on_screen_off",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Lock on Screen Off",
        description = "Locks vault whenever the phone power button is pressed or screen turns off.",
        effect = "BroadcastReceiver listens for ACTION_SCREEN_OFF and calls lockVault().",
        securityImpact = true,
        testVerification = "ScreenOffLockTest"
    )

    // --- Intruder Photo & Alert (Spec 32 & 36 - P2 Priority) ---
    val INTRUDER_PHOTO_ENABLED = SettingDefinition(
        key = "pref_intruder_photo_enabled",
        type = SettingType.BOOLEAN,
        defaultValue = false,
        title = "Intruder Front Camera Capture",
        description = "Captures a front-camera snapshot when wrong PIN is entered.",
        effect = "Invokes CameraX headless capture on auth failures when enabled and permission granted.",
        securityImpact = true,
        requiredPermission = "android.permission.CAMERA",
        testVerification = "IntruderCaptureManagerTest"
    )

    val INTRUDER_THRESHOLD = SettingDefinition(
        key = "pref_intruder_threshold",
        type = SettingType.INT,
        defaultValue = 1,
        allowedValues = listOf(1, 2, 3),
        title = "Intruder Snapshot Threshold",
        description = "Number of failed attempts before front camera snapshot is triggered.",
        effect = "Captures photo when failed attempts reach or exceed this threshold.",
        securityImpact = true,
        testVerification = "IntruderThresholdTest"
    )

    val INTRUDER_BIOMETRIC_FAIL = SettingDefinition(
        key = "pref_intruder_biometric_fail",
        type = SettingType.BOOLEAN,
        defaultValue = false,
        title = "Capture on Biometric Failure",
        description = "Also triggers front camera capture on unrecognised biometric attempts.",
        effect = "Captures photo when BiometricPrompt returns onAuthenticationFailed().",
        securityImpact = true,
        requiredPermission = "android.permission.CAMERA",
        testVerification = "IntruderBiometricTest"
    )

    val INTRUDER_RETENTION_DAYS = SettingDefinition(
        key = "pref_intruder_retention_days",
        type = SettingType.INT,
        defaultValue = 30,
        allowedValues = listOf(7, 14, 30, 90),
        title = "Intruder Photo Retention Period",
        description = "Days before old intruder events and photos are automatically pruned.",
        effect = "Prunes IntruderEventEntity records older than configured days.",
        securityImpact = true,
        testVerification = "IntruderRetentionTest"
    )

    // --- Playback Privacy & Media Rules (Spec 33 & 36 - P1 Priority) ---
    val STOP_ON_HEADPHONE_DISCONNECT = SettingDefinition(
        key = "pref_stop_on_headphone_disconnect",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Pause on Headphone Disconnect",
        description = "Immediately pauses media playback when headphones or Bluetooth audio are unplugged.",
        effect = "ACTION_AUDIO_BECOMING_NOISY and AudioDeviceCallback pause player instantly.",
        securityImpact = true,
        testVerification = "HeadphoneDisconnectTest"
    )

    val VOLUME_ZERO_ON_HEADPHONE_DISCONNECT = SettingDefinition(
        key = "pref_volume_zero_on_headphone_disconnect",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Mute Volume to 0 on Disconnect",
        description = "Mutes device media stream to 0 to prevent loud speaker burst if playback resumes.",
        effect = "AudioManager.setStreamVolume(STREAM_MUSIC, 0, 0) and player.volume = 0f.",
        securityImpact = true,
        testVerification = "HeadphoneMuteTest"
    )

    val SHOW_DISCONNECT_RESUME_BANNER = SettingDefinition(
        key = "pref_show_disconnect_resume_banner",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Headphone Disconnect Banner",
        description = "Shows an on-screen confirmation banner before resuming playback after disconnect.",
        effect = "Renders resume confirmation banner in VideoPlayerScreen and AudioPlayerScreen.",
        securityImpact = false,
        testVerification = "ResumeBannerTest"
    )

    val PAUSE_ON_PHONE_LOCK = SettingDefinition(
        key = "pref_pause_on_phone_lock",
        type = SettingType.BOOLEAN,
        defaultValue = true,
        title = "Phone Lock Playback Rule",
        description = "Pauses video playback on phone lock while allowing background music to continue.",
        effect = "Pauses ExoPlayer in video mode when screen turns off or app is locked.",
        securityImpact = true,
        testVerification = "PhoneLockPlaybackTest"
    )

    val REPEAT_MODE = SettingDefinition(
        key = "pref_repeat_mode",
        type = SettingType.STRING,
        defaultValue = "ALL",
        allowedValues = listOf("OFF", "ONE", "ALL"),
        title = "Default Playback Loop Mode",
        description = "Configures media player repeat behavior (Off, Loop Current, Loop All).",
        effect = "Applies Player.REPEAT_MODE_OFF, REPEAT_MODE_ONE, or REPEAT_MODE_ALL.",
        securityImpact = false,
        testVerification = "RepeatModeTest"
    )

    // Complete list of registered settings for inspection and validation
    val ALL_SETTINGS: List<SettingDefinition<*>> = listOf(
        AUTO_LOCK_MODE,
        FLAG_SECURE,
        BIOMETRICS_ENABLED,
        SHUFFLE_KEYPAD,
        GO_STRAIGHT_TO_VAULT,
        POST_AUTH_PANEL_ENABLED,
        FLIP_TO_LOCK,
        SHAKE_TO_LOCK,
        LOCK_ON_SCREEN_OFF,
        INTRUDER_PHOTO_ENABLED,
        INTRUDER_THRESHOLD,
        INTRUDER_BIOMETRIC_FAIL,
        INTRUDER_RETENTION_DAYS,
        STOP_ON_HEADPHONE_DISCONNECT,
        VOLUME_ZERO_ON_HEADPHONE_DISCONNECT,
        SHOW_DISCONNECT_RESUME_BANNER,
        PAUSE_ON_PHONE_LOCK,
        REPEAT_MODE
    )
}
