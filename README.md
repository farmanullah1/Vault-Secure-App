# Private Vault (SIM Service)

> Built according to the **Private Vault — Master Build Specification (v4, merged)**.

A privacy-focused, native Android media vault engineered for local-only security, discretion, and high performance. It stores photos, videos, and audio on external SD cards or internal storage, hides them from Android gallery indexers via consent-based `.nomedia` markers, and operates behind a minimalist **SIM Service** cover disguise.

---

## 1. Important Disclosures & Boundaries (Read First)

1. **Hidden Mode vs. Encryption**:
   - The app operates in **Hidden Mode** using standard Android `.nomedia` markers and folder structuring.
   - Files are hidden from MediaStore queries (Google Photos device folders, WhatsApp/Instagram media pickers, and system gallery apps).
   - **It does not encrypt media files on disk**. Any file manager with hidden files shown, or a PC reading the SD card over USB, can view the files. This preserves direct access via external file managers as requested.
2. **Zero Network Permissions**:
   - `android.permission.INTERNET` is **NOT declared**. The app cannot make network connections, transmit telemetry, or download remote assets.
3. **Data Integrity & Copy-Verify-Delete**:
   - Initial scans are strictly **read-only**.
   - Moves and transfers follow the **Copy-Verify-Delete Protocol (Spec 14.5)**: destination byte size is verified before any source deletion is performed.
   - An asynchronous **Operation Journal (Room)** tracks transfers and automatically reconciles interrupted operations on app restart.
4. **Media Format Support Matrix**:
   - **Photos**: JPEG, PNG, WebP, GIF, BMP, HEIF.
   - **Videos**: MP4, MKV, WebM, 3GP via Media3 ExoPlayer with hardware acceleration. Instant **"Open in External Player"** fallback intent provided for specialized formats.
   - **Audio**: MP3, AAC, FLAC, OGG, WAV, M4A with ID3 embedded artwork extraction.

---

## 2. Key Architecture & Features

- **Disguised Cover Screen (Spec Section 8.1 & PV-AC-001)**:
  - Edge-to-edge pure black screen (`#000000`, ignores system theme).
  - Navigation bar with back arrow (cleanly finishes activity without authentication) and bold **"SIM Service"** title.
  - Upper-middle list: two rows — **Jazz** (SIM 1) and **Zong** (SIM 2) with 20 × 24 dp SIM card outline vector icons, slot numerals, and light-gray chevrons.
  - Tapping either row initiates biometric or PBKDF2-HMAC-SHA256 PIN authentication.
- **Adaptive Launcher Icon (Spec Section 10.7 & Appendix A)**:
  - Vector adaptive icons generated from master `sim_service_icon.svg`: metallic squircle background, dark SIM card body with angled notch, and gold-orange contact pad.
- **Post-Authentication Carrier Sheet (Spec Section 8.2 & PV-AC-003)**:
  - **Call Helpline**: Pre-fills official carrier customer care (`111` for Jazz, `310` for Zong) in the system dialer via `ACTION_DIAL`. Never auto-calls.
  - **Open SIM Toolkit**: Queries for genuine device STK packages (`com.android.stk`, `com.sec.android.app.servicemodeapp`, etc.). Truthfully informs user if absent.
  - **Open Vault**: Primary gradient action button opening the dark vault.
- **Settings Registry (Spec Section 36 & PV-AC-036)**:
  - Unified single source of truth (`SettingsRegistry.kt`) governing 18 application settings with zero dead switches.
  - Default auto-lock timeout: **1 minute (60 seconds)** with options: Immediate, 1m, 2m, 5m, 30m, and Until app is closed.
  - Tamper-proof timing using `SystemClock.elapsedRealtime()`.
- **Playback Privacy Protections (Spec Section 33 & PV-AC-026..028)**:
  - Dual receiver (`ACTION_AUDIO_BECOMING_NOISY` and `AudioDeviceCallback`) pauses playback and mutes stream volume to 0 immediately upon headphone disconnection.
  - On-screen resume confirmation banner.
  - Phone screen lock automatically pauses video playback while background audio playback continues.
- **Intruder Photo & Owner Alert (Spec Section 32 & PV-AC-034..035 - P2 Priority)**:
  - Opt-in front-camera capture using CameraX when wrong PIN is entered.
  - Stored in private `.pv_intruders/` with `.nomedia`.
  - Security bottom sheet displayed to owner on unlock after failed attempts.
- **Safe Reset (Spec Section 37.4 & PV-AC-033)**:
  - Clears master PIN and Room database index only. Media files on the SD card are **never touched**.

---

## 3. Building and Verification

### Requirements
- Android Studio (Ladybug, Iguana, or later)
- JDK 17 or JDK 21 (recommended for Android Gradle Plugin 9+)
- Minimum Android SDK: 24 (Android 7.0)
- Target Android SDK: 36

### Build Commands
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Sideloading
1. Transfer `app-debug.apk` to phone via USB or SD card.
2. Tap the APK in file manager and allow installation from unknown sources.
3. Launch **SIM Service**, complete master PIN setup, and configure your preferred SD card folder.
