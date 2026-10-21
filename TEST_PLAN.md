# Private Vault — Quality Assurance & Test Plan

> Built according to **Private Vault — Master Build Specification (v4, merged)** Section 39 & Acceptance Criteria.

---

## 1. Automated Checks (Robolectric & Unit Tests)

Run all unit tests via Gradle:
```bash
./gradlew testDebugUnitTest
```

### Verified Test Suites:
1. **`ExampleRobolectricTest`**:
   - Master PIN PBKDF2 hashing and verification.
   - Decoy PIN session activation and isolation.
   - Wrong PIN detection and failure result.
   - Safe reset verification (clears PIN without deleting media).
   - Default auto-lock verification (60 seconds).
   - String resource verification conforming to SIM disguise spec.
2. **`SettingsRegistryTest`**:
   - Validation that all settings in `SettingsRegistry.ALL_SETTINGS` have non-blank keys, titles, descriptions, effects, and test verification references.
   - Validation of default auto-lock timeout (60 seconds = 1 minute).
   - Privacy defaults verification (`FLAG_SECURE` true, `BIOMETRICS_ENABLED` true, `SHUFFLE_KEYPAD` true, `INTRUDER_PHOTO_ENABLED` false/opt-in).
3. **`FileOperationsJournalTest`**:
   - State machine transition constants (`INITIALIZED`, `COPYING`, `VERIFYING`, `SOURCE_DELETED`, `COMPLETED`, `FAILED`, `ROLLED_BACK`).
   - Copy-Verify-Delete verification logic: destination byte count must match source size before source deletion is permitted.
   - Failure safety verification: when size verification fails, source file is preserved intact.
4. **`AutoLockTimingTest`**:
   - Tamper-proof elapsed timing calculation using `SystemClock.elapsedRealtime()`.
   - Verification that background duration < timeout remains unlocked.
   - Verification that background duration >= timeout triggers lock.
   - Verification that `AUTO_LOCK_ON_APP_CLOSE` (-1) is not locked by timer.

---

## 2. Failure-Path Testing Scenarios (Spec 39.2)

- [ ] **SAF Permission Revoked**: Revoke storage permissions in Android Settings while vault is open; confirm app displays graceful re-grant banner without crashing.
- [ ] **Interrupted File Transfer**: Simulate process termination during large file copy; verify on restart that `reconcilePendingOperations()` logs failure and source file remains intact.
- [ ] **Destination Size Mismatch**: Simulate insufficient storage or partial write; confirm source file is never deleted and partial destination is purged.
- [ ] **Device Clock Tampering**: Advance device system time by 2 hours while app is in background; confirm auto-lock uses `SystemClock.elapsedRealtime()` and does not falsely lock out or miscalculate.
- [ ] **Double-Tap on Destructive Action**: Double-tap "Delete Permanently" or "Empty Trash"; confirm double-submission prevention blocks duplicate execution.
- [ ] **Headphone Disconnection during Playback**: Unplug 3.5mm jack or disconnect Bluetooth headphones; confirm ExoPlayer pauses immediately, media volume mutes to 0, and on-screen resume banner appears.
- [ ] **Phone Lock during Playback**: Lock screen while playing video -> confirm video pauses; lock screen while playing audio -> confirm audio continues playing in background.
- [ ] **Camera Permission Denied**: Disable camera permission in Android Settings and enter wrong PIN; confirm app records failed attempt without crashing.

---

## 3. Manual Device Checklist

### Cover Screen & Authentication
1. Launch app -> confirm black screen titled "SIM Service", back arrow, and Jazz / Zong rows.
2. Tap back arrow -> confirms app exits immediately without authentication.
3. Tap Jazz row -> enter master PIN -> confirm post-auth sheet appears with Helpline, STK, and Vault buttons.
4. Tap "Call Helpline" -> confirm system dialer opens with `tel:111` pre-filled (never auto-calls).
5. Tap "Open Vault" -> transitions smoothly to Vault Home.

### Storage & .nomedia Protection
1. Select SD card vault folder (e.g. `new` or `Vault`).
2. Confirm initial sync is strictly read-only and does not auto-create `.nomedia`.
3. Tap "Hide from Gallery" in Checklist -> confirm `.nomedia` created only after explicit confirmation.
4. Verify files in vault folder do not appear in system gallery apps.

### Media Viewers & Trash
1. Photo Viewer: pinch-to-zoom, pan, double-tap, and view EXIF metadata sheet.
2. Video Player: HUD swipe gestures (left for brightness, right for volume, double-tap 10s seek).
3. Audio Player: rotating vinyl animation, frequency bars, and docked mini-player.
4. Delete item to Trash -> verify item moves to `.pv_trash/` and is restorable.
5. Safe Reset -> confirm PIN and index cleared, while all media files on SD card remain intact.
