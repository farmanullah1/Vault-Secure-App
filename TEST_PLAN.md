# Private Vault — Quality Assurance & Test Plan

Manual checklist adhering to Section 23 of `Private_Vault_Master_Spec_v3.md`.

---

## Pre-Flight Security Check
- [ ] Verify `AndroidManifest.xml` does **NOT** declare `android.permission.INTERNET`.
- [ ] Verify `android:allowBackup="false"` in `AndroidManifest.xml`.
- [ ] Confirm no hardcoded API keys, passwords, or tokens in source code.

---

## 1. First Launch & Onboarding
1. Fresh install APK on device or emulator.
2. Confirm the app launches to the **SIM Services** disguise screen with:
   - Neutral title "SIM Services"
   - Subtle animated shine on the SIM card illustration
   - Two equal carrier cards: **Jazz** and **Zong**
   - No mention of "vault", "hidden", or "secret" anywhere on the screen
3. Tap either carrier card or the SIM illustration.
4. Set up master PIN (e.g. 4–8 digits) and confirm.
5. If hardware biometric is available, confirm enrollment prompt.

---

## 2. Storage Setup & .nomedia Protection
1. Inside the Vault > Settings > Vault Folder:
   - Pick the target folder (e.g., SD card folder `new` or `.private_vault`).
2. Verify `.nomedia` file creation in the chosen folder.
3. Check status indicator shows `.nomedia Active (Hidden from Gallery)`.
4. Open the **Gallery Visibility Checklist** in Settings and follow verification steps.

---

## 3. Disguise & Authentication Entry Flow
1. Back out or lock the vault.
2. From the Cover Screen, tap **Jazz** card.
3. Authenticate with fingerprint or PIN.
4. Verify the **Post-Auth Panel** displays:
   - **Open Vault** (primary button)
   - **Call Helpline** (opens system dialer with `tel:111`, does not auto-dial)
   - **Open SIM Toolkit** (launches real STK app or shows honest "Not Available" dialog)
5. Tap **Open Vault**; verify immediate transition to Vault Home.
6. Test "Go straight to Vault" toggle in Settings: when enabled, authenticating on the cover screen bypasses the post-auth panel directly into the vault.

---

## 4. Rate Limiting & Lockout
1. Lock vault and return to cover screen.
2. Enter incorrect PIN 5 times.
3. Verify 30-second lockout countdown triggers and keypad is disabled.
4. Enter incorrect PIN 6th time -> 1-minute lockout.
5. Enter incorrect PIN 7th time -> 5-minute lockout.
6. Verify lockout timestamp persists across force-closing and re-opening the app.

---

## 5. Media Playback & Viewing
### Video Player
- [ ] Play MP4, MKV, 3GP videos.
- [ ] Double-tap left half -> rewinds 10s with visual indicator.
- [ ] Double-tap right half -> forwards 10s with visual indicator.
- [ ] Vertical drag on left half -> adjusts screen brightness with on-screen HUD.
- [ ] Vertical drag on right half -> adjusts system volume with on-screen HUD.
- [ ] Tap aspect ratio button -> cycles between Fit, Crop/Zoom, and Stretch.
- [ ] Tap speed button -> changes playback rate (0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x).
- [ ] Tap Lock button -> locks controls and suppresses gesture interference.
- [ ] Tap "Open in External Player" -> verifies Android system chooser displays installed players (VLC, MX Player).

### Photo Viewer
- [ ] Open photo -> smooth zoom and pan with pinch gesture.
- [ ] Double-tap to zoom in / reset zoom.
- [ ] Tap Details icon -> view EXIF metadata, file size, format, and original path mapping.
- [ ] Tap Export icon -> exports a copy to user-selected folder.

### Audio Player
- [ ] Play audio track -> disc visualizer and animated frequency bars activate.
- [ ] Scrub timeline slider to jump forward and back.
- [ ] Toggle repeat mode.
- [ ] Return to Vault Home -> verify floating mini-player bar displays with pause/play and tap-to-expand.

---

## 6. Screen Privacy & Auto-Lock
1. Enable `FLAG_SECURE` in Settings.
2. Attempt taking a screenshot -> confirm system displays "Can't take screenshot due to security policy".
3. Open recent apps overview -> confirm screen appears blank or obscured.
4. Background the app and return -> confirm app requires re-authentication.

---

## 7. Safe Reset
1. Go to Settings > Danger Zone > "Reset Master PIN & App Data".
2. Confirm the reset dialog warning.
3. Confirm app clears PIN and navigates to fresh state.
4. Check phone's file manager: confirm all original media files on SD card/storage are **100% intact and untouched**.
