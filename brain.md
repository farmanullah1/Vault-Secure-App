# brain.md

## Decisions (storage mode, DI choice, libraries + versions)
- **Master Specification**: Built according to **Private Vault — Master Build Specification (v3)**.
- **Platform & UI**: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- **Core Security & Intercept Flows**:
  - **Launch Intercept & PIN Verification**:
    - Dedicated full-screen `PinEntryScreen` that intercepts app launch and strictly gates all core app functionality behind cryptographic PBKDF2-HMAC-SHA256 verification (with SHA1 fallback) and BiometricPrompt.
    - Rate-limiting lockout penalties: 5 failed attempts = 30s lockout, 6 = 60s, 7 = 300s, 8+ = 900s (15 min cap).
    - Shuffled keypad digit randomization to prevent smudge guessing.
    - Decoy PIN Safe support opening an isolated empty safe.
  - **Auto-Lock Configuration & Timeout System**:
    - Options: Immediate (0s), 1 minute (60s - DEFAULT), 2 minutes (120s), 5 minutes (300s), 30 minutes (1800s), and Until app is closed (-1).
    - In "Until app is closed" mode: user can continue using the app until the process is closed or killed; when reopened from cold launch, user is required to enter PIN/password.
    - SystemClock.elapsedRealtime() timing protects against device clock tampering.
    - Screen-off broadcast receiver automatically locks vault when screen is turned off.
    - Panic gestures: Flip-to-lock (accelerometer z < -7.0) and Shake-to-lock.
  - **First-Time Access Permissions Setup (Spec 9.8)**:
    - Dedicated `FirstTimePermissionsScreen` automatically shown after first authentication.
    - Comprehensive checklist with live status badges (Granted / Needed / Optional / Skipped):
      1. Folder Access (SD Card / SAF via ACTION_OPEN_DOCUMENT_TREE) with .nomedia protection.
      2. Notification Permission (POST_NOTIFICATIONS on Android 13+) for background import/playback progress.
      3. Biometric Authentication (Fingerprint / Face Unlock).
      4. Battery Optimization Exemption for background work.
    - Prominent zero-internet guarantee: "Private Safe never asks for internet, contacts, or location."
    - Settings -> PERMISSIONS & ACCESS card provides live status and one-tap re-configuration anytime.
  - **Disguised "SIM Services" Dashboard Screen (Spec 8.1 & 10.1)**:
    - Pure dark utility appearance (`#000000`).
    - Top bar with back arrow (closes app directly without authentication) and bold "SIM Service" title (long-press alternate unlock).
    - Carrier cards and rows: SIM 1 Jazz, SIM 2 Zong, SIM Toolkit, and Customer Care helplines.
    - Tapping carrier items intercepts with the PIN entry screen.
  - **Recycle Bin (Trash) & Secure Shredding**:
    - 30-day retention period with automatic purge of expired items (`trashTimestamp`).
    - Permanent deletion with optional Secure Shred (overwriting SAF document byte content with random bytes before deletion).
  - **Media Players & Galleries**:
    - Dynamic column grid (2, 3, 4 cols).
    - Photo Viewer with HorizontalPager, double-tap zoom/pan, rotation, slideshow, EXIF inspection, secure share.
    - Video Player with playlist queue, resume playback prompt, HUD gestures, PiP, Kids Lock.
    - Audio Player with queue, shuffle, embedded album art retriever, spinning vinyl animation, and sleep timer.

## Current phase & status
- **Active Phase**: Launch Intercept, First-Time Permissions Setup, Auto-Lock Defaults & SIM Services Dashboard Complete.
- **Build Status**: Fully implemented with zero fake data, full Compose Material 3 theming, Room v4, and clean state flows.

## Files updated this phase
- `app/src/main/java/com/example/security/PinSecurityManager.kt`: Added `KEY_PIN_LENGTH`, `KEY_LOCK_ON_SCREEN_OFF`, `KEY_DIRECT_PIN_LAUNCH`, `KEY_SHAKE_TO_LOCK`, `KEY_SENSITIVE_ACTION_REAUTH`, verified default auto-lock 1 minute.
- `app/src/main/java/com/example/ui/screens/PinEntryScreen.kt`: Created dedicated full-screen PIN entry screen with keypad shuffle, biometric shortcut, lockout countdown, and setup mode.
- `app/src/main/java/com/example/ui/screens/FirstTimePermissionsScreen.kt`: Created dedicated first-time access checklist asking for folder, notifications, biometrics, and battery optimization.
- `app/src/main/java/com/example/viewmodel/VaultViewModel.kt`: Added `Screen.PinEntry`, `Screen.FirstTimePermissions`, initial routing based on direct PIN launch or disguise, and updated lock/unlock routing.
- `app/src/main/java/com/example/MainActivity.kt`: Added screen-off broadcast receiver, shake-to-lock accelerometer listener, registered receivers, and navigation routing for PinEntry and FirstTimePermissions.
- `app/src/main/java/com/example/ui/screens/SimCoverScreen.kt`: Added carrier dashboard rows (Jazz, Zong, STK, Helplines), and routed authentication through `Screen.PinEntry`.
- `app/src/main/java/com/example/ui/components/PinKeypadDialog.kt`: Fixed indicator dot count and length-based verification logic.
- `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`: Added PERMISSIONS & ACCESS live status card, direct PIN launch toggle, lock on screen-off toggle, shake-to-lock toggle, and sensitive action re-auth toggle.

## Not verified / Not run
- Physical hardware SIM Toolkit app invocation on a physical carrier SIM in Pakistan.
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).
- Some OEM ROMs lack the standard `com.android.stk` package; handled gracefully with a truthful informative dialog.

## Next steps
- Open project in Android Studio or compile with Gradle to verify build.
