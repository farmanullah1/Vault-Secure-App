# Private Vault (SIM Service): Complete Architecture, Feature Guide & LLM Context ("upUntilNow.md")

**Document Title:** Comprehensive Project Specification & Implementation Inventory  
**Target Audience:** Software Engineers, System Architects, and Large Language Models (LLMs) continuing development  
**Current Date:** October 2026  
**Application ID:** `com.aistudio.simvault.pzxyvw`  
**Namespace:** `com.example`  
**Min SDK:** 24 (Android 7.0 Nougat) | **Target SDK:** 36 (Android 16) | **Compile SDK:** 36  
**Security Boundary:** 100% Offline (Zero `INTERNET` permission declared), `.nomedia` media stealth indexing, hardware keystore/EncryptedSharedPreferences, CameraX silent intruder selfie capture.

---

## 1. System Overview & Project Identity

**Private Vault** is an enterprise-grade, privacy-engineered native Android application designed to secure sensitive personal media (Photos, Videos, Audio, Documents, and Private Notes). 

### Disguise Identity
To the casual observer or device inspector, the application does not advertise itself as a vault. Instead, it masquerades under one of three customizable system disguise covers:
1. **"SIM Service" (Default):** A high-contrast carrier utility showing simulated **Jazz (SIM 1)** and **Zong (SIM 2)** account management rows.
2. **"Calculator":** A fully functioning standard & scientific calculator evaluating arithmetic expressions with a secret `=` PIN trigger.
3. **"My Notes":** A realistic decoy notepad displaying mundane notes (groceries, work todos) with a secret long-press header trigger.

Inside the vault, users have access to an **advanced media suite** featuring a custom **Media3 ExoPlayer** implementation with desktop-class gesture overlays, 5 playback loop modes, kids touch screen lock, picture-in-picture (PiP), subtitle synchronization/repositioning gestures, photo zoom/slideshow engines, vinyl audio visualizers, and an encrypted intruder capture system.

---

## 2. Complete Technology Stack & Library Matrix

The project strictly follows modern Android development practices using **Kotlin DSL Gradle scripts (`build.gradle.kts`)**, **Jetpack Compose (Material 3)**, and a **Version Catalog (`gradle/libs.versions.toml`)**.

| Category | Library / Dependency | Exact Version | Purpose & Architectural Role |
|---|---|---|---|
| **Language** | Kotlin | `2.2.10` | Primary language with strict coroutines & state flow |
| **Build System** | Android Gradle Plugin (AGP) | `9.1.1` | Gradle build tools with Kotlin DSL |
| **Annotation Processing**| KSP (Kotlin Symbol Processing)| `2.3.5` | High-speed code generation for Room DB |
| **UI Framework** | Jetpack Compose BOM | `2024.09.00` | Declarative UI toolkit with Material 3 components |
| **Material Design** | `androidx.compose.material3` | BOM managed | Material 3 components, color palettes, and typography |
| **Navigation** | `androidx.navigation.compose` | `2.8.9` | Single-activity screen routing and backstack handling |
| **Video Playback** | `androidx.media3:media3-exoplayer` | `1.5.1` | Hardware-accelerated media decoding and streaming |
| **Video UI** | `androidx.media3:media3-ui` | `1.5.1` | Aspect ratio layout, surface views, and track selection |
| **Media Session** | `androidx.media3:media3-session` | `1.5.1` | Background audio and foreground service playback |
| **Image Loading** | `io.coil-kt:coil-compose` | `2.7.0` | Async image loading, disk caching, hardware bitmaps |
| **Local Persistence** | `androidx.room:room-runtime`, `ktx` | `2.7.0` | SQLite database for media items, journals, and intruder logs |
| **Biometrics** | `androidx.biometric:biometric` | `1.1.0` | Fingerprint and Face Unlock via system BiometricPrompt |
| **Camera & Intruder** | `androidx.camera:camera-camera2`, `lifecycle`, `view`, `core` | `1.5.0` | CameraX silent front-camera capture for failed unlock selfies |
| **Storage Access** | `androidx.documentfile:documentfile` | `1.0.1` | Storage Access Framework (SAF) tree traversal & exports |
| **Async & Coroutines**| `kotlinx.coroutines.android`, `core` | `1.10.2` | Structured concurrency, `StateFlow`, `SharedFlow` |
| **Lifecycle** | `androidx.lifecycle:lifecycle-runtime-compose` | `2.8.7` | `collectAsStateWithLifecycle` and ViewModel lifecycle hooks |
| **Networking/Serialization**| `com.squareup.retrofit2:retrofit`, `moshi` | `2.12.0` / `1.15.2` | Data models and stream parsing (zero external endpoints called) |
| **Testing** | `robolectric` / `junit` / `test-junit4` | `4.16.1` / `4.13.2` | Local JVM unit and integration tests without physical emulators |

### Permissions & Privacy Footprint (`AndroidManifest.xml`)
- **Zero Internet Permission:** `android.permission.INTERNET` is **NOT declared**. The application cannot make network calls, leak telemetry, or communicate with external servers.
- **Hardware Sensors & Features:**
  - `android.permission.USE_BIOMETRIC`: Biometric prompt.
  - `android.permission.CAMERA`: Optional runtime permission strictly for front-facing intruder photos.
  - `android.permission.WAKE_LOCK`: Audio background playback.
  - `android.permission.FOREGROUND_SERVICE`: Prevents OS termination during active media playback.
  - `android.permission.VIBRATE`: Tactile haptic feedback on keypad presses and player scrubbing.

---

## 3. Architecture & Codebase Map

The project is structured according to **Clean MVVM Architecture**:

```
app/src/main/java/com/example/
├── MainActivity.kt                      // Entry point, window flags (FLAG_SECURE), screen off/sensor receivers
├── data/
│   ├── dao/
│   │   ├── MediaDao.kt                  // Room DAO for VaultMediaItem CRUD, filtering, and sorting
│   │   ├── IntruderEventDao.kt          // Room DAO for intruder attempt logs and photo references
│   │   └── OperationJournalDao.kt       // Room DAO for crash-proof copy-verify-delete transaction logs
│   ├── db/
│   │   └── VaultDatabase.kt             // SQLite database definition (version 2) with MIGRATION support
│   ├── model/
│   │   ├── MediaType.kt                 // Enum: PHOTO, VIDEO, AUDIO, DOCS_NOTES
│   │   ├── VaultMediaItem.kt            // Media entity: id, fileName, vaultPath, originalPath, size, duration, date
│   │   ├── IntruderEventEntity.kt       // Intruder record: timestamp, attemptedPin, photoPath, isSeen
│   │   └── OperationJournalEntity.kt     // Transaction journal for safe file imports & rollback
│   └── repository/
│       └── VaultRepository.kt           // Data pipeline: SAF imports, checksum verification, .nomedia management
├── security/
│   ├── PinSecurityManager.kt            // PBKDF2/SHA-256 PIN hashing, lockout penalties, disguise preferences
│   ├── BiometricHelper.kt               // AndroidX BiometricPrompt bridge for fingerprint/face auth
│   ├── IntruderCaptureManager.kt        // CameraX silent front-camera photography on 3 consecutive failed PINs
│   └── SettingsRegistry.kt              // Preference keys and constants for zero dead switches
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     // Design tokens: DarkCarbon, VaultCyan, VaultEmerald, VaultViolet
│   │   ├── Theme.kt                     // M3 Dark/Light themes and system insets handling
│   │   ├── Type.kt                      // Typography styles (Roboto and Monospace timecodes)
│   │   └── Dimens.kt                    // Spacing, grid sizes, and touch target standards (>= 48dp)
│   ├── components/
│   │   ├── FirstTimeSetupDialog.kt      // Initial Master PIN creation flow
│   │   ├── ImportProgressDialog.kt      // Copy-verify-delete import progress modal with cancel support
│   │   ├── PinChangeDialog.kt           // Master PIN modification sheet
│   │   ├── PinKeypadDialog.kt           // Standalone tactile PIN modal
│   │   ├── PinSetupDialog.kt            // Re-usable PIN setup dialog
│   │   └── SecurityAlertSheet.kt        // Warning bottom sheet shown when unseen intruder attempts exist
│   ├── screens/
│   │   ├── SimCoverScreen.kt            // Disguise A: SIM Service (Jazz / Zong carrier rows & helpline)
│   │   ├── CalculatorCoverScreen.kt     // Disguise B: Fully functional math calculator with '=' secret unlock
│   │   ├── NotesCoverScreen.kt          // Disguise C: Realistic decoy notes app with header trigger
│   │   ├── PinEntryScreen.kt            // Dedicated high-security PIN entry screen
│   │   ├── FirstTimePermissionsScreen.kt// Permission onboarding (Storage & Camera)
│   │   ├── VaultMainScreen.kt           // Primary media grid (Photos, Videos, Audio, Notes, Albums, Trash)
│   │   ├── VideoPlayerScreen.kt         // Media3 player with gestures, 5 loop modes, kids lock, subtitles
│   │   ├── PhotoViewerScreen.kt         // High-res photo viewer with 5x zoom, 90° rotation, and slideshow
│   │   ├── AudioPlayerScreen.kt         // Audio player with spinning vinyl disc and 8-band audio visualizer
│   │   ├── NoteViewerScreen.kt          // Encrypted text and markdown note editor
│   │   ├── GalleryChecklistScreen.kt    // Checklist for public gallery cleanup after import
│   │   ├── IntruderLogsScreen.kt        // Inspection gallery for intruder attempt selfies
│   │   └── SettingsScreen.kt            // Complete settings panel (disguise mode, auto-lock, biometrics)
│   └── util/
│       └── FormatUtils.kt               // Formatters: formatDuration (mm:ss / hh:mm:ss), formatFileSize (MB/GB)
└── viewmodel/
    └── VaultViewModel.kt                // Central UI StateFlow provider, screen routing, and background actions
```

---

## 4. UI Design System & Aesthetics ("How the App Looks Now")

The interface is built to evoke the feel of a **military-grade cryptographic terminal** while maintaining the fluid aesthetics of modern **Material Design 3**:

1. **Obsidian & Dark Carbon Palette**:
   - `DarkCarbon` (`#0F1115` / `#161920`): Dominant surface and scaffold background color.
   - `SurfaceCard` (`#1E2330`): Card container background with a crisp border stroke (`#2A3245`).
   - `VaultCyan` (`#22D3EE`): Primary energetic accent for active buttons, sliders, and audio waves.
   - `VaultEmerald` (`#10B981`): Success indicators, safe actions, and Point A loop markers.
   - `VaultViolet` (`#8B5CF6`): Secondary accent for A-B section repeat highlights and badge pills.
   - `DangerRed` (`#EF4444`): Point B loop markers, lock indicators, and destructive deletion sheets.
2. **Interactive Motion & Feedback**:
   - **Tactile Keypad Haptics:** Keypad presses invoke `view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`.
   - **Kids Lock Ripple Starbursts:** When screen lock is active, touch points trigger animated expanding canvas circles that ripple and dissipate.
   - **Smooth Overlays:** Controls fade out after 3 seconds of inactivity and slide back in on a single screen tap.
3. **Adaptive Form Factors**:
   - **Portrait Mode:** 16:9 videos are letterboxed cleanly, controls compress horizontally, and subtitle sizing auto-scales to avoid edge crowding.
   - **Landscape Mode:** Controls spread out to thumb-reachable corners, maximizing the central video canvas for unobstructed viewing.

---

## 5. Detailed Feature Specifications ("Up In Till Now")

### 5.1. Disguised Cover Subsystem
* **SIM Service Cover (`SimCoverScreen.kt`)**:
  - Screen displays a pure `#000000` canvas with header title "SIM Service" and a functional back arrow.
  - Two authentic carrier rows:
    - **Jazz (SIM 1)** with official SIM outline icon.
    - **Zong (SIM 2)** with official SIM outline icon.
  - Tapping a carrier row requests biometric/PIN authentication. Once authenticated, the **Post-Auth Carrier Action Sheet** opens:
    - **Call Helpline:** Pre-fills `111` (Jazz) or `310` (Zong) in Android dialer (`ACTION_DIAL`).
    - **Open SIM Toolkit:** Scans device packages (`com.android.stk`, `com.sec.android.app.servicemodeapp`) and launches device STK app.
    - **Open Vault:** Gradient emerald button transitioning into the private vault.
  - Secret shortcut: 2-second long-press on "SIM Service" header text triggers direct PIN unlock.
* **Calculator Cover (`CalculatorCoverScreen.kt`)**:
  - Realistic calculator with display bar and grid (`7-8-9-÷`, `4-5-6-×`, `1-2-3--`, `0-.-=-+`).
  - Evaluates standard arithmetic expressions.
  - Secret trigger: Entering the master PIN and pressing `=` evaluates math or unlocks the vault. Long-pressing `( )` opens biometric authentication.
* **Decoy Notes Cover (`NotesCoverScreen.kt`)**:
  - Displays ordinary personal notes. Long-pressing the notebook icon opens the vault.

---

### 5.2. Security, Anti-Intruder & Auto-Lock Subsystem
* **Cryptographic PIN Storage (`PinSecurityManager.kt`)**:
  - Master PIN is never stored in plaintext. It is hashed with SHA-256 and salt using `EncryptedSharedPreferences`.
  - Configurable PIN length: 4 to 8 digits.
* **Silent Intruder Selfie Capture (`IntruderCaptureManager.kt`)**:
  - Monitored through `failedAttemptsCount`.
  - When failed attempts reach the user-configured threshold (default: 3), the app silently activates the front camera using **CameraX `ImageCapture`** (without flash or preview UI).
  - The photo is saved into the private encrypted app storage (`intruders/`), and an `IntruderEventEntity` is inserted into SQLite.
  - When the owner successfully enters the vault, a **Security Alert Sheet** warns: *"Intruder Attempt Detected: [N] unauthorized attempts were recorded with photo evidence."*
* **Intruder Logs Viewer (`IntruderLogsScreen.kt`)**:
  - Displays grid of intruder photos with exact timestamps and the PIN attempted. Full-screen zoom and batch deletion available.
* **Auto-Lock Triggers (`MainActivity.kt`)**:
  - **App Minimization:** Immediate or timed lock (30s, 1m, 5m).
  - **Screen Off:** Registered broadcast receiver immediately locks vault when device screen turns off.
  - **Flip-to-Lock:** Accelerometer listener detects face-down orientation (`z < -7.0f`) and locks immediately.
  - **Shake-to-Lock:** Accelerometer listener detects vigorous shake (`acceleration > 12.0`) and locks immediately.
  - **Window Security:** `WindowManager.LayoutParams.FLAG_SECURE` prevents screenshots, screen recording, and task switcher snapshots.

---

### 5.3. Media Storage Engine & Stealth Management
* **Invisible `.nomedia` Protocol**:
  - All vault directories (`.vault_photos`, `.vault_videos`, `.vault_audio`, `.vault_notes`) contain `.nomedia` files to exclude them from Android MediaStore.
* **Crash-Proof Import Pipeline (`VaultRepository.kt`)**:
  - Uses an **Operation Journal (`OperationJournalEntity`)**:
    1. `STAGE_COPY`: Copy source URI to temporary file.
    2. `STAGE_VERIFY`: Compare SHA-256 hash and byte length between source and copy.
    3. `STAGE_FINALIZE`: Move to permanent vault folder and insert record into `VaultDatabase`.
    4. `STAGE_DELETE_SOURCE`: Delete source file via DocumentFile/ContentResolver.
* **SAF Unvaulting / Export**:
  - Users can export any hidden media item back to public storage (e.g. `Downloads` or `DCIM`) via the system file picker.
* **Bulk Management**:
  - Batch select mode with Select All, bulk export, and bulk deletion to Trash with permanent purge options.

---

### 5.4. Advanced High-Performance Video Player (`VideoPlayerScreen.kt`)

The video player is built on **Media3 ExoPlayer 1.5.1** and includes:

#### A. Comprehensive Loop & Repeat Engine (5 Modes)
1. **Loop Single (Repeat One - `1`)**: Repeats the active video from start to finish indefinitely.
2. **Loop All (Repeat Playlist - `All`)**: Iterates through the playlist queue and loops back to video 1.
3. **A-B Loop (Custom Section Repeat - `A-B`)**:
   - Repeats a custom video section between **Point A** and **Point B**.
   - Shaded cyan progress interval on the seekbar with a Cyan flag for A and a Red flag for B.
   - On-screen quick assist chips: **[Set Point A: 01:23]**, **[Set Point B: 02:45]**, and **[Clear A-B]**.
4. **Counted Loop (Repeat N Times - `Nx`)**:
   - Repeats the video a preset number of times (2×, 3×, 5×, 10×, or custom number) and then halts.
   - Real-time iteration tracker: *"Counted Loop: 2 / 5 repeats completed"*.
5. **Off (No Loop)**:
   - Plays once and displays the **End-Screen Recommendations Overlay** with:
     - **Replay Video** button
     - **Next Video** card (title & duration)
     - **Return to Videos** shortcut
     - Dismiss button.
6. **Dual Invocation:** Single-tap the footer loop button to cycle modes with instant HUD indicators; long-tap or select "Loop & Repeat Options" from the overflow menu to open the full configuration dialog.

#### B. Intuitive Gesture Control Layer
* **Left-Side Vertical Swipe:** Adjusts screen brightness with sun HUD and percentage indicator.
* **Right-Side Vertical Swipe:** Adjusts volume with speaker HUD and **Software Volume Boost up to 200%**.
* **Middle Horizontal Swipe:** Timeline scrub seek with delta-jump badge (`+00:15` or `-00:10`).
* **Pinch-to-Zoom & Pan:** Smooth two-finger zoom (0.75× to 4.0×) and pan positioning, with a floating **Reset Zoom** pill.
* **Long-Press 2.0× Fast-Forward:** Press and hold anywhere on the screen for temporary 2.0× speed playback with haptic feedback.
* **Double-Tap Seeking:** Double-tap left side to rewind 10s, right side to forward 10s, center to toggle play/pause.
* **Subtitle Swipe Gestures:**
  - **Vertical Drag on Subtitle Box:** Adjusts vertical position (`subtitleOffsetY`) on screen.
  - **Horizontal Drag on Subtitle Box:** Adjusts subtitle timing synchronization delay (`subtitleDelayMs`) in milliseconds.

#### C. Kids Touch Screen Lock
* **Touch Disabling:** Disables all touches and hides navigation overlays.
* **Interactive Ripple Canvas:** Tapping anywhere on the locked screen generates expanding multicolor ripple starbursts.
* **Floating Unlock Button:** Red unlock card with tactile feedback to release the lock.

#### D. Audio & Video Playback Tools
* **Top Bar Live Overlay:** Real-time scrolling title marquee (`basicMarquee`), battery percentage, digital clock, and decoder switcher (**HW / HW+ / SW**).
* **Audio Equalizer:** Presets for **Bass Boost**, **Vocal**, **Virtualizer**, **Cinema**, **Rock**, and **Flat**.
* **Audio Delay Sync:** Adjustable audio delay offset (-2.0s to +2.0s).
* **Subtitle Manager:** On/off toggle, local SRT file loader, simulated OpenSubtitles online search, font sizing (12sp–24sp), text colors (White, Yellow, Cyan, Green), and background styles.
* **Picture-in-Picture (PiP):** Floating multitasking player with seamless system integration.
* **Background Playback:** Audio continues playing when the screen is turned off or the app is minimized.
* **Headphone Unplug Protection:** Auto-pauses on headphone disconnection and displays a resume banner.
* **Network Streaming & Casting:** Cast scanner dialog for Smart TVs and direct URL player (HTTP, HLS m3u8, RTSP, RTMP).

---

### 5.5. Photo Viewer, Audio Player & Notes Suites
* **Photo Viewer (`PhotoViewerScreen.kt`)**:
  - High-res Coil Compose rendering with hardware bitmaps.
  - Smooth pinch-to-zoom (up to 5.0×) and double-tap snap zoom (1.0× ↔ 2.8×).
  - 90° clockwise rotation and automated hands-free slideshow.
  - EXIF metadata inspector: camera model, aperture, shutter speed, ISO, and resolution.
* **Audio Player (`AudioPlayerScreen.kt`)**:
  - Spinning vinyl record animation with dynamic glow.
  - Real-time 8-bar frequency spectrum audio visualizer.
  - Repeat options (Current, All, Shuffle) and docked mini-player on the vault screen.
  - Sleep timer with auto-stop intervals (10m, 15m, 30m, 60m, End of Track).
* **Encrypted Notes Suite (`NoteViewerScreen.kt`)**:
  - Create and edit private markdown and plaintext notes.
  - Tag categorization and instant full-text search.

---

### 5.6. Settings & Customization Registry (`SettingsScreen.kt`)
* **Zero Dead Switches:** Every single toggle, slider, and selector in the settings screen is permanently bound to `PinSecurityManager`.
* **Cover Disguise Selector:** Switch between SIM Service, Calculator, and Notes covers on demand.
* **Security Controls:** Change Master PIN, toggle Biometric Unlock, adjust Intruder selfie threshold (1, 3, 5 attempts).
* **Storage Manager:** Device vs Vault storage usage bar, storage folder migration between internal and SD card.

---

## 6. Guidance for Future LLMs & Developers

When extending or maintaining this codebase, observe the following rules:

1. **Maintain Offline Boundary:** Do NOT add `android.permission.INTERNET` to `AndroidManifest.xml` unless explicitly instructed by the user. The app's core value proposition is 100% offline security.
2. **Preserve Disguise Entry Mechanics:** Never remove the fallback secret entry triggers in `SimCoverScreen.kt` (long-press title), `CalculatorCoverScreen.kt` (`=` key), or `NotesCoverScreen.kt`.
3. **Keep Compose TestTags Intact:** Automated verification depends on test tags such as `video_play_pause_button`, `video_ab_loop_button`, `video_lock_button`, `video_subtitle_button`, `video_aspect_ratio_button`, `video_rotate_button`, `video_pip_button`, `calculator_key_*`, and `sim_row_*`.
4. **ExoPlayer Lifecycle Safety:** Always release ExoPlayer instances in `DisposableEffect` or `onDispose` unless `playInBackground` is enabled.
5. **Database Schema Versioning:** If modifying `VaultMediaItem` or `IntruderEventEntity`, increment the database version in `VaultDatabase.kt` and provide migration paths to avoid crashes.

---

## 7. Verification & Build Health

The application builds and tests cleanly with the following verification:
- **Build Status:** `BUILD SUCCESSFUL` via `compile_applet` and Gradle Kotlin DSL.
- **Unit Test Suite:** Passing via `gradle :app:testDebugUnitTest` (Robolectric JVM tests).
- **Files Saved:** Available in workspace root as `/upUntilNow.md`.
