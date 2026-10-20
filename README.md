# SIM Services (Private Vault)

> Built according to the **Private Vault — Master Build Specification (v3)**.

A native Android app designed for privacy, speed, and discretion. It manages personal photos, videos, and audio on an SD card or internal storage, hides them from standard Android gallery apps using `.nomedia`, and operates behind an authentic, clean SIM Services disguise screen.

---

## Important Privacy & Honesty Disclosure (Read First)

1. **Hidden Mode vs. Encryption**:
   - This app operates in **Hidden Mode** using standard `.nomedia` storage markers. It removes files from Android's MediaStore index (Google Photos device folders, WhatsApp/Instagram media pickers, system gallery).
   - It **does not encrypt** files on disk. Any file manager app that navigates directly to the folder, or a PC reading the SD card over USB, can view the files. This fulfills the user requirement that files remain accessible via file managers.
2. **Zero Network Permissions**:
   - The app does **not** request or use `android.permission.INTERNET`. No analytics, no ads, no trackers, and no network connections exist.
3. **Cloud Sync Caution**:
   - Photos already backed up to Google Photos or other cloud services remain in the cloud until deleted there.
4. **Honest Format Support**:
   - Video playback utilizes **Media3 ExoPlayer**, subject to hardware codecs. An instant **"Open in External Player"** (VLC / MX Player) fallback is provided with temporary content URI read grants.
5. **Carrier Independence**:
   - The app is not affiliated with, endorsed by, or connected to Jazz, Zong, or any telecommunications carrier.

---

## Key Architecture & Features

- **Disguised Cover Screen (Spec Section 8.1 & 10.1)**:
  - Calm, light telecom utility appearance (`#F6F8FB` surface).
  - Own-designed metallic SIM chip vector with subtle animated shine; optional long-press unlock.
  - Two equal carrier cards: **Jazz** (warm gradient) and **Zong** (cool teal gradient) with helpline launch affordance.
  - Tapping either card requires biometric fingerprint / PIN authentication *before* granting access.
- **Post-Authentication Panel (Spec Section 8.2)**:
  - **Call Helpline**: Pre-fills official carrier customer care (`111` or `310`) in the system dialer via `Intent.ACTION_DIAL`. Never auto-calls.
  - **Open SIM Toolkit**: Queries for genuine device STK packages (`com.android.stk`, `com.android.stk2`). Truthfully informs the user if STK is absent without drawing fake menus.
  - **Open Vault**: Primary gradient action button opening the dark cinematic vault.
  - Option to "Go straight to Vault" after authentication.
- **Security & Privacy (Spec Section 9)**:
  - PBKDF2-HMAC-SHA256 PIN hashing with 12,000 iterations and 16-byte random salt.
  - Rate limiting & lockout: 30s after 5 wrong attempts, 1 min after 6, 5 min after 7, 15 min cap after 8+.
  - Auto-lock on app backgrounding or screen-off.
  - `FLAG_SECURE` enabled by default (prevents screenshots and recents previews).
  - **Safe Reset**: Clears master credentials and app database index only. Media files on the SD card are **never touched**.
- **Media Viewing & Playback (Spec Section 12)**:
  - **Photo Viewer**: Gesture zoom/pan/double-tap, EXIF metadata inspection sheet, export/restore to storage.
  - **Video Player**: Media3 ExoPlayer with gesture HUD (swipe left for brightness, swipe right for volume, double-tap 10s seek, speed selector 0.5x–2.0x, aspect ratio toggling, controls lock, and external player fallback).
  - **Audio Player**: Disk visualization, animated frequency bars, track scrubber, repeat mode, and docked mini-player.
- **Storage Model (Spec Section 11 & 14)**:
  - Supports **Index-in-place**, **Copy to Safe**, and **Move to Safe** via Storage Access Framework (SAF).
  - Automatically verifies and maintains `.nomedia` presence.
  - In-app **Gallery Visibility Checklist** with step-by-step guidance.

---

## Building and Sideloading

### Method A: Build in Android Studio
1. Open Android Studio (Ladybug / Iguana or later).
2. Select **Open** and choose this project root folder.
3. Allow Gradle to sync.
4. Run: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. The debug APK is generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Method B: Sideload onto Android Phone
1. Transfer `app-debug.apk` to your phone via USB cable or SD card.
2. Tap the APK file in your phone's File Manager.
3. When prompted, toggle **"Allow from this source"** for your file manager.
4. If Google Play Protect shows a notice for sideloading, tap **"More details"** and select **"Install anyway"**.
5. Launch **SIM Services**. Set your 4–8 digit master PIN and configure your preferred storage folder.
