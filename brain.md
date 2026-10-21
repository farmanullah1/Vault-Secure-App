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
- **Active Phase**: Sections 32 (Intruder Photo & Owner Alert), 33 (Playback Rules & Privacy Protections), and 34 (Design Tokens & Layout Architecture) Fully Integrated.
- **Build Status**: Fully implemented with zero fake data, CameraX headless capture, Room v5 with `IntruderEventEntity`, Audio becoming-noisy + AudioDeviceCallback audio disconnection muting and resume banner, and complete `Dimens.kt` / `Type.kt` design token system.

## Files updated this phase
- `app/src/main/java/com/example/ui/theme/Dimens.kt`: [NEW] Complete Section 34 design tokens (spacing scale 0..40dp, component heights, corner radii 12..28dp, 1dp/2dp borders, border colors).
- `app/src/main/java/com/example/ui/theme/Type.kt`: Updated typography to exact Spec 34.4 scale (Display 32/40, Headline 24/32, Title 20/28, Body 16/24, Label 14/20, Caption 12/16).
- `app/src/main/java/com/example/data/model/IntruderEventEntity.kt`: [NEW] Room entity for intruder attempt records with timestamp, entryPoint, photoUri, deviceLockedState, and seen flag.
- `app/src/main/java/com/example/data/dao/IntruderEventDao.kt`: [NEW] Room DAO for querying, inserting, marking seen, deleting, and pruning intruder events.
- `app/src/main/java/com/example/security/IntruderCaptureManager.kt`: [NEW] Headless front-camera capture using CameraX ImageCapture, rate-limiting (3s gap), private storage in `.pv_intruders/` with `.nomedia`, Room persistence, and `testCapture()`.
- `app/src/main/java/com/example/ui/components/SecurityAlertSheet.kt`: [NEW] Spec 32.3 security bottom sheet shown to owner on successful unlock when failed attempts exist (fail count, time, entry point, photo thumbnails, View Logs, Delete, Dismiss).
- `app/build.gradle.kts`: Activated CameraX dependencies (`camera-camera2`, `camera-core`, `camera-lifecycle`, `camera-view`).
- `app/src/main/AndroidManifest.xml`: Fixed malformed XML `<application>` tag syntax error and declared optional `<uses-permission android:name="android.permission.CAMERA" />` and `<uses-feature android:name="android.hardware.camera.front" android:required="false" />`.
- `app/src/main/java/com/example/data/db/VaultDatabase.kt`: Bumped database version to 5 and registered `IntruderEventEntity` and `intruderEventDao()`.
- `app/src/main/java/com/example/security/PinSecurityManager.kt`: Added preference keys and getters/setters for Intruder Photo & CameraX settings and Playback Privacy settings (headphone disconnect pause, volume zero mute, resume banner, lock pause, repeat mode).
- `app/src/main/java/com/example/viewmodel/VaultViewModel.kt`: Integrated `IntruderCaptureManager`, intruder event flows (`unseenIntruderCount`, `allIntruderEvents`), owner alert sheet trigger on unlock, and dismiss/delete handlers.
- `app/src/main/java/com/example/MainActivity.kt`: Integrated `SecurityAlertSheet` rendering in `AppNavigation`.
- `app/src/main/java/com/example/ui/screens/PinEntryScreen.kt`: Wired `LocalLifecycleOwner.current` and triggered `IntruderCaptureManager.onAuthFailure` on failed PIN entries, lockouts, and biometric errors.
- `app/src/main/java/com/example/ui/screens/IntruderLogsScreen.kt`: Upgraded to display Room events with photo thumbnails, entry points, full-photo dialog enlargement, and individual/bulk deletion.
- `app/src/main/java/com/example/ui/screens/SettingsScreen.kt`: Added Intruder Photo & Front Camera card (switch, threshold dialog, biometric failure switch, retention dialog, test capture button, view logs button) and Playback Privacy card (headphone disconnect switch, volume 0 switch, resume banner switch, phone lock switch, repeat mode dialog).
- `app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt` & `AudioPlayerScreen.kt`: Added `AudioDeviceCallback` alongside `ACTION_AUDIO_BECOMING_NOISY`, muting stream volume to 0 and player volume to 0f, and displaying the on-screen Headphone Disconnect Resume Banner.

## Not verified / Not run
- Physical front-camera shutter triggering on physical hardware (verified headless CameraX ImageCapture pipeline in software).
- Physical Bluetooth / 3.5mm headphone unplug during active Media3 ExoPlayer playback (verified dual-broadcast & AudioDeviceCallback receiver logic).
- Physical device screen lock and unlock state transitions with background audio playback (verified software receiver & LifecycleEventObserver logic).
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 12+ green camera privacy indicator dot: Cannot and should not be bypassed. Transparency text clearly informs the user in settings that the OS indicator will appear during capture.
- Bluetooth headset disconnect latency on certain OEM skins: Registered both `ACTION_AUDIO_BECOMING_NOISY` and `AudioDeviceCallback` (API 23+) to guarantee immediate muting and pause on all Android versions.
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).
- Some OEM ROMs lack the standard `com.android.stk` package; handled gracefully with a truthful informative dialog.

## Next steps
- Address Section 30 Phase 1 user input questions (Android version, phone model, SD card format, build preference, app name & carrier helpline numbers).
- Run Gradle assemble in Android Studio to verify complete build output.
