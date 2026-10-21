# Private Vault (SIM Service)

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2024%2B)-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Offline Only](https://img.shields.io/badge/Network-100%25%20Offline%20(Zero%20Internet)-red.svg)](#important-disclosures--threat-model)
[![Spec Conformance](https://img.shields.io/badge/Spec-v4%20Merged%20Complete-blue.svg)](#requirements-traceability-matrix)

> **Architectural Standard:** Engineered strictly in accordance with **Private Vault — Master Build Specification (v4, merged)** and **V4 Update & Implementation Addendum**.

Private Vault is an ultra-secure, privacy-engineered native Android media safe designed for full storage flexibility, discretion, and data preservation. It keeps the user's high-volume **photos, videos, audio, and private notes** on external SD cards or internal storage, hides them from the Android gallery ecosystem via consent-driven `.nomedia` markers, and opens behind an authentic system utility disguise (**"SIM Service"** with Jazz and Zong carrier rows), with optional Calculator and Notes covers.

---

## Table of Contents
1. [Important Disclosures & Threat Model](#1-important-disclosures--threat-model)
2. [Disguised Cover System](#2-disguised-cover-system)
3. [Security & Cryptographic Architecture](#3-security--cryptographic-architecture)
4. [Storage Engine & Data Integrity (Copy-Verify-Delete)](#4-storage-engine--data-integrity-copy-verify-delete)
5. [Media Suite & High-Performance Viewers](#5-media-suite--high-performance-viewers)
6. [Intruder Capture & Owner Alerts](#6-intruder-capture--owner-alerts)
7. [Settings Registry (Zero Dead Switches)](#7-settings-registry-zero-dead-switches)
8. [Requirements Traceability Matrix (PV-AC-001 – PV-AC-036)](#8-requirements-traceability-matrix-pv-ac-001--pv-ac-036)
9. [Toolchain & Build Instructions](#9-toolchain--build-instructions)
10. [Automated Test Suite & Verification](#10-automated-test-suite--verification)

---

## 1. Important Disclosures & Threat Model

### Truth in Advertising & Security Boundaries
- **Hidden Mode vs. Disk Encryption:**
  - The application operates in **Hidden Mode** utilizing standard Android `.nomedia` markers and dot-prefixed directories.
  - This removes all media files from Android's MediaStore indexer, ensuring they never appear in Google Photos, OEM gallery apps, WhatsApp/Instagram media pickers, or browser file upload dialogs.
  - **This is NOT full-disk filesystem encryption**. Media files remain standard files on the SD card. Anyone who opens the folder directly in a file manager (with hidden files enabled) or connects the SD card to a PC can browse the files. This fulfills the user requirement that files remain accessible in external file managers.
- **100% Offline (Zero Network Footprint):**
  - `android.permission.INTERNET` is **NOT declared** in `AndroidManifest.xml`.
  - The application contains zero network request libraries, zero third-party telemetry, zero remote crash reporting, zero ads, and zero cloud sync. All cryptographic verification, thumbnail generation, and database indexing execute strictly on-device.
- **Read-Only Scans by Default:**
  - Initial folder selection and scanning are strictly read-only.
  - The app never auto-creates, modifies, or overwrites files without explicit user consent (Spec 2.1 Conflict Resolution #2).

### Media Format Support Matrix

| Category | Formats Supported | Engine / Player Stack | Capabilities |
|---|---|---|---|
| **Photos** | JPEG, PNG, WebP, GIF, BMP, HEIF | Coil Compose with hardware bitmaps | Smooth pinch-to-zoom (1x–5x), animated double-tap snap (1x ↔ 2.8x), pan, 90° clockwise rotation, slideshow with auto-advance, EXIF metadata inspection, and safe SAF export. |
| **Videos** | MP4, MKV, WebM, 3GP, TS | Media3 ExoPlayer 1.5.1 | Hardware-accelerated decoding, left-swipe brightness HUD, right-swipe volume HUD, double-tap 10s seek, aspect ratio toggle (Fit, Zoom, Stretch), playback speed (0.5x–2.0x), Picture-in-Picture (PiP), and instant "Open in External Player" intent chooser (VLC/MX Player). |
| **Audio** | MP3, AAC, FLAC, OGG, WAV, M4A | Media3 ExoPlayer 1.5.1 | Rotating vinyl disc animation, dynamic audio frequency spectrum bars, seek scrubber, repeat modes (Off, Current, All), sleep timer, docked mini-player, background lock-screen playback, and dual headphone-disconnect muting. |
| **Notes** | TXT, Markdown (.md) | Native Secure Text Engine | Private encrypted notes, folder categorization, full-text search, and origin mapping. |

---

## 2. Disguised Cover System

The app disguises its launcher entry point and interface to prevent casual observers from suspecting the existence of a private safe.

```
+-------------------------------------------------------------+
|                     DISGUISED ENTRY MODES                   |
+-------------------------------------------------------------+
|                                                             |
|  [Mode A: SIM Service]       [Mode B: Calculator]           |
|  - Pure black #000000        - Fully functional math engine |
|  - Back arrow (clean exit)   - Arithmetic (+, -, *, /, %)   |
|  - "Jazz" & "Zong" rows      - Secret "=" PIN unlock        |
|  - Post-auth carrier sheet   - Routes to PinEntryScreen     |
|                                                             |
|  [Mode C: Notes Disguise]                                   |
|  - Realistic decoy personal/work notes                      |
|  - Category filters & search                                |
|  - Long-press title secret trigger                          |
+-------------------------------------------------------------+
```

### 1. Primary Disguise: "SIM Service" (Spec Section 8.1 & PV-AC-001)
- **Visual Identity:** High-contrast pure black background (`#000000`), completely ignoring system dark/light modes.
- **Top Navigation:** 56 dp height with a 24 dp white back arrow (48 dp touch target). Tapping back cleanly executes `(context as? Activity)?.finish()` without requiring authentication (PV-AC-002).
- **Carrier Rows:** Upper-middle list with exactly two carrier rows:
  - **Jazz** (SIM 1): Outline vector SIM card icon (20 × 24 dp), bold text, light-gray chevron.
  - **Zong** (SIM 2): Outline vector SIM card icon (20 × 24 dp), bold text, light-gray chevron.
- **Post-Auth Carrier Action Sheet (Spec Section 8.2 & PV-AC-003):**
  - **Call Helpline:** Pre-fills official carrier customer care numbers (`111` for Jazz, `310` for Zong) in the Android system dialer using `ACTION_DIAL`. Never auto-calls.
  - **Open SIM Toolkit:** Inspects package manager for genuine device STK packages (`com.android.stk`, `com.sec.android.app.servicemodeapp`, etc.). Displays an honest dialog if no OEM STK app is present.
  - **Open Vault:** Primary gradient emerald button transitioning into the private vault.
- **Alternate Secret Trigger:** 2-second long-press on the "SIM Service" title text.

### 2. Disguise Option: Calculator Cover (Spec Section 8.3)
- Fully functioning calculator with standard arithmetic (`+`, `-`, `×`, `÷`, `%`, parentheses).
- **Unlock Trigger:** Entering master PIN and pressing `=` evaluates math or verifies PIN to unlock. Long-pressing the `( )` key activates biometric prompt or routes to the secure PIN entry screen.

### 3. Disguise Option: Notes Cover (Spec Section 8.4)
- Realistic decoy personal and work notes (groceries, home projects, meeting notes) with category chips and search.
- **Unlock Trigger:** Long-pressing the "My Notes" header icon triggers authentication.

---

## 3. Security & Cryptographic Architecture

### 1. Master Authentication (Spec Section 9.1 & PV-AC-013)
- **PBKDF2-HMAC-SHA256**: 12,000 iterations, 256-bit derived key, and a cryptographically secure 16-byte random salt generated via `java.security.SecureRandom`.
- **Zero Plaintext Storage**: Only the Base64-encoded PBKDF2 hash and salt are persisted in private SharedPreferences.
- **Biometric Integration**: Uses AndroidX `BiometricPrompt` supporting fingerprint and face unlock, falling back cleanly to master PIN on hardware absence or error.

### 2. Lockout Rate Limiting & Tamper Resistance (Spec Section 9.3 & PV-AC-015..017)
- **Exponential Penalty Progression**:
  - Attempts 1–4: Informative warning with remaining tries.
  - Attempt 5: 30-second lockout.
  - Attempt 6: 60-second lockout.
  - Attempt 7: 300-second (5 minutes) lockout.
  - Attempt 8+: 900-second (15 minutes) lockout cap.
- **Tamper-Proof Timing**: Lockout timestamps utilize `SystemClock.elapsedRealtime()`, measuring non-resettable CPU ticks since boot. Tampering with the phone's date, time, or timezone **cannot bypass lockouts**. Timestamps survive process restart.

### 3. Window Security & Sensor Gestures
- **`FLAG_SECURE` (Spec 9.5 & PV-AC-019)**: Applied dynamically on activity resume. Blocks screen capture, screenshots, screen recording, and obscures snapshots in Android's recent apps overview.
- **Flip-to-Lock**: Accelerometer orientation detection immediately locks the vault when placed face-down on a surface (`z < -7.0 m/s²`).
- **Shake-to-Lock**: Vigorous motion detection (`delta > 12.0 m/s²`) triggers instant vault lock.
- **Screen-Off Receiver**: Listens for `Intent.ACTION_SCREEN_OFF` to lock the vault immediately upon screen sleep.

### 4. Safe Reset (Spec Section 37.4 & PV-AC-033)
- In the event of a forgotten PIN or credential purge, "Safe Reset" clears master PIN hashes and the internal Room database index.
- **Media files stored on the SD card are NEVER deleted or modified**. After setting a new PIN, the user re-selects the folder to instantly re-index all media.

---

## 4. Storage Engine & Data Integrity (Copy-Verify-Delete)

Direct filesystem manipulation on modern Android (API 30+) is restricted by Scoped Storage. All storage access is engineered through the **Storage Access Framework (SAF)** with strict data safety protocols.

```
+---------------------------------------------------------------------------------+
|                     COPY-VERIFY-DELETE PROTOCOL (Spec 14.5)                     |
+---------------------------------------------------------------------------------+
|                                                                                 |
|  [Step 1: INITIALIZED]    -> Record operation in Room OperationJournalEntity    |
|                                                                                 |
|  [Step 2: COPYING]        -> Stream bytes from source to destination via SAF    |
|                                                                                 |
|  [Step 3: VERIFYING]      -> Verify destination byte count == source size       |
|                                 |                                               |
|               +-----------------+-----------------+                             |
|               | (Mismatch / Error)                | (Passed)                    |
|               v                                   v                             |
|  [Purge Dest & Preserve Source]       [Step 4: SOURCE_DELETED]                  |
|  - Destination partial deleted        - Safe to delete original source          |
|  - Source remains 100% intact         - Record operation COMPLETED              |
+---------------------------------------------------------------------------------+
```

### 1. Copy-Verify-Delete Protocol (PV-AC-029)
When moving files from device gallery folders into the vault:
1. An `OperationJournalEntity` audit record is inserted into the Room database (`stage = INITIALIZED`).
2. Streams bytes into a newly created `DocumentFile` in the vault folder using bounded 8 KB buffers.
3. Flushes the output stream and verifies that `destination.length() == source.length()`.
4. **Source file deletion occurs ONLY AFTER byte size verification passes**.
5. If verification fails (e.g., storage full, stream interrupted), the partial destination is deleted, and the source file is **preserved completely**.

### 2. Room Operation Journal & Crash Reconciliation (PV-AC-030 & PV-SEC-REC)
- An asynchronous Room database table (`operation_journal`) tracks every file transfer with unique UUIDs, source/destination URIs, byte counters, and stages.
- **Startup Reconciliation**: On application launch, `VaultRepository.reconcilePendingOperations()` checks for unfinished operations resulting from sudden process death or OS ejection:
  - Transfers interrupted prior to verification are marked failed, and source files remain untouched.
  - Verified transfers finalize catalog entries cleanly.

### 3. Recycle Bin (Trash) & Secure Shredding (PV-AC-031..032)
- **Soft Deletion**: Deleted items move to `.pv_trash/` with `.nomedia` and can be restored with original folder metadata.
- **30-Day Auto-Purge**: Background check purges trash entries older than 30 days.
- **Secure Shredder**: Optional multi-pass overwriting fills file contents with pseudo-random bytes from `SecureRandom` before unlinking the document.

---

## 5. Media Suite & High-Performance Viewers

### Photo Viewer Screen (`PhotoViewerScreen.kt`)
- Full-screen viewer using Compose Foundation `HorizontalPager`.
- Multi-touch transformable state supporting smooth pinch-to-zoom (1x up to 5x) with dynamic panning bounds.
- Double-tap animated snap gesture: zooms directly to 2.8x detail, or snaps back to 1x fit.
- 90° clockwise rotation button with orientation preservation.
- Slideshow mode with 3.5s automated transitions and on-screen indicator.
- File Metadata & EXIF Bottom Sheet displaying dimensions, ISO, aperture, camera model, date added, file size, and origin directory.
- Safe Export / Share intent chooser with security warning disclosures.

### Video Player Screen (`VideoPlayerScreen.kt`)
- Integrated **Media3 ExoPlayer 1.5.1** with surface view hardware acceleration.
- **Gesture HUD**:
  - Vertical swipe on the left half adjusts screen brightness (0.0 to 1.0).
  - Vertical swipe on the right half adjusts audio stream volume with HUD gauge.
  - Double-tap left skips backward 10 seconds; double-tap right skips forward 10 seconds.
- Aspect ratio switcher: cycling through `FIT` (letterbox), `ZOOM` (fill screen), and `STRETCH`.
- Playback speed controls: `0.5x`, `0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`.
- Picture-in-Picture (PiP) support with auto-entry on home press.
- "Open in External Player" intent fallback granting temporary read permissions to third-party players (VLC, MX Player).

### Audio Player Screen (`AudioPlayerScreen.kt`)
- Immersive audio playback UI with an animated spinning vinyl record and dynamic frequency spectrum visualizer.
- Scrubber slider with elapsed time and total duration display.
- Repeat modes: Loop Off, Loop Current Track (`ONE`), and Loop Playlist (`ALL`).
- Docked mini-player banner on `VaultMainScreen` allowing global pause/play while browsing folders.
- Sleep timer with configurable countdown presets (15m, 30m, 45m, 60m).
- Background lock-screen audio playback support.

### Playback Privacy Protections (Spec Section 33 & PV-AC-026..028)
- **Dual Headphone Disconnect Receivers**:
  - Registers `AudioManager.ACTION_AUDIO_BECOMING_NOISY`.
  - Registers API 23+ `AudioDeviceCallback` monitoring wired headsets, headphones, Bluetooth A2DP, and USB headsets.
  - On disconnect: immediately pauses ExoPlayer, mutes audio stream volume to 0 to prevent accidental speaker bursts, and displays an on-screen resume banner.
- **Phone Lock Video Rule**: When the phone screen locks, video playback pauses immediately for privacy; audio playback continues seamlessly.

---

## 6. Intruder Capture & Owner Alerts

```
+---------------------------------------------------------------------------------+
|                         INTRUDER CAPTURE PIPELINE                               |
+---------------------------------------------------------------------------------+
|                                                                                 |
|  [Failed Unlock Attempt] -> Exceeds threshold (1, 2, or 3 attempts)             |
|                                      |                                          |
|                                      v                                          |
|  [CameraX Headless Capture] -> Headless ImageCapture via Front Camera           |
|                                (Rate-limited: min 3 sec cooldown)               |
|                                      |                                          |
|                                      v                                          |
|  [Private Storage]         -> Saved in app-private .pv_intruders/               |
|                                Indexed in Room IntruderEventEntity              |
|                                      |                                          |
|                                      v                                          |
|  [Owner Unlock Alert]      -> On successful unlock, SecurityAlertSheet appears  |
|                                displaying photo thumbnail, attempt count, time  |
+---------------------------------------------------------------------------------+
```

- **Opt-In Guardrail (Spec 32.1)**: Intruder capture is disabled by default. Turning it on requires explicit user confirmation and runtime camera permission.
- **Headless CameraX**: Binds front camera lifecycle to capture a high-resolution snapshot silently without displaying a preview viewfinder.
- **Rate-Limiting**: Minimum 3-second throttle prevents burst captures.
- **Security Alert Sheet (PV-AC-035)**: Displayed immediately upon legitimate owner unlock if failed attempts occurred, showing photo thumbnails, timestamps, and log entries.

---

## 7. Settings Registry (Zero Dead Switches)

To prevent decorative or contradictory toggles (Spec 2.1 Conflict Resolution #7 & Section 36), all application configuration is governed by **`SettingsRegistry.kt`**:

| Key | Type | Default | Title & Effect | Security Impact |
|---|---|---|---|:---:|
| `pref_auto_lock_seconds` | `INT` | `60` (1 min) | Auto-lock timeout (`0`, `60`, `120`, `300`, `1800`, `-1`). Uses monotonic timer. | High |
| `pref_flag_secure` | `BOOLEAN` | `true` | Applies `FLAG_SECURE` to block screenshots and recents switcher previews. | High |
| `pref_biometrics_enabled` | `BOOLEAN` | `true` | Enables BiometricPrompt on PIN and cover screens. | High |
| `pref_shuffle_keypad` | `BOOLEAN` | `true` | Shuffles keypad digits (0–9) on each attempt to prevent smudge guessing. | Medium |
| `pref_direct_pin_launch` | `BOOLEAN` | `false` | Bypasses cover screen, launching directly into PIN entry. | Low |
| `pref_show_post_auth_panel` | `BOOLEAN` | `true` | Shows carrier action sheet (Helpline, STK, Vault) after carrier row unlock. | Low |
| `pref_flip_to_lock` | `BOOLEAN` | `false` | Locks vault when placed face-down (`z < -7.0 m/s²`). | Medium |
| `pref_shake_to_lock` | `BOOLEAN` | `false` | Locks vault on vigorous motion (`delta > 12.0 m/s²`). | Medium |
| `pref_lock_on_screen_off` | `BOOLEAN` | `true` | Locks vault when the power button is pressed or screen turns off. | High |
| `pref_intruder_photo_enabled`| `BOOLEAN` | `false` | Captures front-camera snapshot on failed PIN attempts. | High |
| `pref_intruder_threshold` | `INT` | `1` | Failed attempts before snapshot is taken (1, 2, or 3). | Medium |
| `pref_intruder_biometric_fail`| `BOOLEAN` | `false` | Also captures snapshot on unrecognized biometric scans. | Medium |
| `pref_intruder_retention_days`| `INT` | `30` | Auto-pruning period for intruder logs (7, 14, 30, or 90 days). | Low |
| `pref_stop_on_headphone_disconnect` | `BOOLEAN` | `true` | Immediately pauses playback when headphones/Bluetooth unplug. | High |
| `pref_volume_zero_on_headphone_disconnect` | `BOOLEAN` | `true` | Mutes media stream to 0 upon headphone disconnection. | High |
| `pref_show_disconnect_resume_banner` | `BOOLEAN` | `true` | Displays on-screen confirmation banner before resuming playback. | Low |
| `pref_pause_on_phone_lock` | `BOOLEAN` | `true` | Pauses video on screen lock while keeping audio playing. | Medium |
| `pref_repeat_mode` | `STRING` | `"ALL"` | Default playback repeat mode (`"OFF"`, `"ONE"`, `"ALL"`). | Low |

---

## 8. Requirements Traceability Matrix (PV-AC-001 – PV-AC-036)

| ID | Requirement | Priority | Module | Status |
|---|---|---|---|---|
| **PV-AC-001** | Disguised Cover Screen "SIM Service", Jazz & Zong rows, black background | P0 | UI/Cover | **Implemented** |
| **PV-AC-002** | Back arrow in top bar finishes activity without authentication | P0 | UI/Cover | **Implemented** |
| **PV-AC-003** | Post-auth sheet appears after carrier unlock unless direct launch enabled | P1 | UI/Cover | **Implemented** |
| **PV-AC-004** | "Call Helpline" opens system dialer with `ACTION_DIAL` (111 / 310) | P1 | UI/Cover | **Implemented** |
| **PV-AC-005** | "Open SIM Toolkit" detects installed STK packages or shows honest dialog | P1 | UI/Cover | **Implemented** |
| **PV-AC-006** | Adaptive launcher icon with metallic squircle, SIM card, and contacts | P0 | Res/Icon | **Implemented** |
| **PV-AC-007** | Native dark vault theme with emerald accents and glassmorphism | P0 | UI/Theme | **Implemented** |
| **PV-AC-008** | Three primary media tabs: Photos, Videos, Audio with dynamic grid | P1 | UI/Vault | **Implemented** |
| **PV-AC-009** | Storage Access Framework (SAF) folder selection with persisted permissions | P0 | Storage | **Implemented** |
| **PV-AC-010** | Read-only folder scan without creating or modifying files | P0 | Storage | **Implemented** |
| **PV-AC-011** | User-confirmed "Hide from Gallery" creates `.nomedia` without overwriting | P0 | Storage | **Implemented** |
| **PV-AC-012** | In-app Gallery Visibility Checklist with step-by-step guidance | P1 | UI/Checklist | **Implemented** |
| **PV-AC-013** | PBKDF2-HMAC-SHA256 master PIN verification with random salt | P0 | Security | **Tested** |
| **PV-AC-014** | Biometric fingerprint / face authentication integration | P0 | Security | **Implemented** |
| **PV-AC-015** | Rate limiting lockout penalties (5 wrong = 30s, 6 = 60s, 7 = 300s, 8+ = 900s) | P0 | Security | **Implemented** |
| **PV-AC-016** | Tamper-proof auto-lock timing using `SystemClock.elapsedRealtime()` | P0 | Security | **Tested** |
| **PV-AC-017** | Configurable auto-lock timeouts (0s, 60s default, 120s, 300s, 1800s, -1) | P0 | Security | **Tested** |
| **PV-AC-018** | Screen-off broadcast receiver automatically locks vault on screen sleep | P0 | Security | **Implemented** |
| **PV-AC-019** | Window `FLAG_SECURE` blocks screenshots and recents previews | P0 | Security | **Implemented** |
| **PV-AC-020** | Photo viewer with pinch zoom, double-tap zoom, pan, and EXIF inspection | P1 | UI/Viewer | **Implemented** |
| **PV-AC-021** | Video player with Media3 ExoPlayer, HUD brightness & volume gestures | P1 | UI/Player | **Implemented** |
| **PV-AC-022** | Aspect ratio cycling (Fit, Zoom, Stretch) and playback speed controls | P1 | UI/Player | **Implemented** |
| **PV-AC-023** | "Open in External Player" fallback intent with temporary content grants | P1 | UI/Player | **Implemented** |
| **PV-AC-024** | Audio player with rotating vinyl animation, frequency bars, and scrubbing | P1 | UI/Player | **Implemented** |
| **PV-AC-025** | Repeat modes: Loop Off, Loop One, Loop All | P1 | UI/Player | **Implemented** |
| **PV-AC-026** | Headphone disconnect automatically pauses playback and mutes stream | P1 | UI/Player | **Implemented** |
| **PV-AC-027** | Headphone disconnect on-screen resume banner | P1 | UI/Player | **Implemented** |
| **PV-AC-028** | Phone lock pauses video playback while audio continues | P1 | UI/Player | **Implemented** |
| **PV-AC-029** | Copy-verify-delete protocol for file move operations | P0 | Storage | **Tested** |
| **PV-AC-030** | Room Operation Journal for crash resilience and recovery | P0 | Persistence | **Implemented** |
| **PV-AC-031** | Recycle bin (trash) with 30-day auto-purge and restore | P1 | Storage | **Implemented** |
| **PV-AC-032** | Permanent deletion with optional multi-pass secure shredding | P1 | Storage | **Implemented** |
| **PV-AC-033** | Safe reset clears master PIN and database index without touching user media | P0 | Security | **Tested** |
| **PV-AC-034** | Intruder photo front-camera capture with rate-limiting and private storage | P2 | Security | **Implemented** |
| **PV-AC-035** | Security Alert Sheet shown to owner on unlock after failed intruder attempts | P2 | UI/Alert | **Implemented** |
| **PV-AC-036** | Single Settings Registry consolidating all app configuration | P0 | Security | **Tested** |

---

## 9. Toolchain & Build Instructions

### Verified Toolchain Matrix
- **Android Gradle Plugin (AGP):** 9.1.1
- **Gradle:** 9.3.1 (configured in `gradle/wrapper/gradle-wrapper.properties`)
- **Kotlin:** 2.2.10
- **Jetpack Compose BOM:** 2024.09.00
- **Android Room:** 2.7.0 (with KSP)
- **Media3 ExoPlayer:** 1.5.1
- **CameraX:** 1.5.0
- **AndroidX Biometric:** 1.1.0
- **AndroidX DocumentFile:** 1.0.1
- **Min SDK:** API 24 (Android 7.0 Nougat)
- **Target & Compile SDK:** API 36

### Build Commands (in Android Studio or CLI)

```bash
# Clean project
./gradlew clean

# Run local unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Output APK path
app/build/outputs/apk/debug/app-debug.apk
```

### Installation & Sideloading
1. Transfer `app-debug.apk` to your target Android device via USB cable, Google Drive, or SD card.
2. Open your device file manager and tap `app-debug.apk`. Enable "Install from unknown sources" if prompted.
3. Open **SIM Service** from your app drawer.
4. Complete the 4-digit master PIN setup, grant storage permissions to your preferred SD card folder (e.g. `/storage/.../new`), and follow the in-app gallery visibility checklist.

---

## 10. Automated Test Suite & Verification

The repository includes comprehensive Robolectric and unit tests under `app/src/test/java/com/example/`:

- **[SettingsRegistryTest.kt](file:///c:/Users/farma/Desktop/sample%20code/Vault-Secure-App/app/src/test/java/com/example/SettingsRegistryTest.kt)**: Verifies that all 18 registered settings have non-blank keys, descriptions, valid bounds, default 60s auto-lock, and privacy defaults.
- **[FileOperationsJournalTest.kt](file:///c:/Users/farma/Desktop/sample%20code/Vault-Secure-App/app/src/test/java/com/example/FileOperationsJournalTest.kt)**: Verifies the Copy-Verify-Delete protocol, size verification failure handling, and source preservation.
- **[AutoLockTimingTest.kt](file:///c:/Users/farma/Desktop/sample%20code/Vault-Secure-App/app/src/test/java/com/example/AutoLockTimingTest.kt)**: Asserts tamper-proof monotonic elapsed timing thresholds and constants.
- **[CoverScreenLayoutTest.kt](file:///c:/Users/farma/Desktop/sample%20code/Vault-Secure-App/app/src/test/java/com/example/CoverScreenLayoutTest.kt)**: Verifies disguise text conformance ("SIM Service", "Jazz", "Zong") and official helpline strings.
- **[ExampleRobolectricTest.kt](file:///c:/Users/farma/Desktop/sample%20code/Vault-Secure-App/app/src/test/java/com/example/ExampleRobolectricTest.kt)**: Tests PBKDF2 hashing, PIN verification, decoy session activation, and safe reset preservation.

---

## License & Privacy Notice
This software is intended strictly for personal privacy, local data organization, and security. No personal data, media, telemetry, or analytics ever leave the device.
