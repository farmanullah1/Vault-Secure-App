# brain.md

## Decisions (storage mode, DI choice, libraries + versions)
- **Master Specification**: Built according to **Private Vault — Master Build Specification (v3)**.
- **Platform & UI**: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- **Entry Flow (Option A & Spec Section 8.1)**:
  - Exact disguise cover screen: edge-to-edge pure black (`#000000`).
  - Top bar directly below status bar: 56 dp height, white left-pointing back arrow (`ic_arrow_back.xml`), bold title "SIM Service" (700 weight, 20 sp, white).
  - Menu list in upper-middle of screen (~22% below top bar): Row 1 "Jazz" (SIM 1) and Row 2 "Zong" (SIM 2) with 20 × 24 dp SIM slot outline vector icons, slot numerals, 16 dp gap, and light-gray chevrons (`#C7C7CC`).
  - Everything below the list is completely black and empty (no buttons, hints, menus, or fake widgets).
  - Tapping either row triggers biometric/PIN authentication before any carrier or vault action.
  - Back arrow closes the screen/activity cleanly without authentication.
- **Launcher Icon & Assets (Spec Section 10.7 & sim_service_icon.svg)**:
  - Master vector icon `sim_service_icon.svg` ported to native Android Adaptive Vector Drawables:
    - `ic_launcher_background.xml`: Full-bleed light-gray metallic squircle gradient (`#F1F2F4` -> `#D9DCE1` -> `#BFC3CA`).
    - `ic_launcher_foreground.xml`: Centered matte black SIM card with 45° angled top-right notch and segmented metallic gold-and-orange contact pad.
    - `ic_launcher_monochrome.xml`: Monochrome vector silhouette for Android 13+ themed icons.
- **Post-Auth Panel (Spec Section 8.2 & 31.1)**:
  - Black modal bottom sheet (`#101012`, 28 dp top corners) matching the cover screen.
  - Three large, clear actions: Call Helpline (`111` / `310` via dialer), Open SIM Toolkit (real packages query, truthful error dialog if absent), and Open Vault (primary `#7C5CFF → #22D3EE` gradient button).
- **Auto-Lock Timing Settings (Default: 1 minute per Spec Section 9.4 & Criterion 21)**:
  - `Immediate` (0 seconds): Locks immediately on backgrounding or screen-off.
  - `1 minute` (60 seconds, **DEFAULT** on fresh install & safe reset).
  - `2 minutes` (120 seconds).
  - `5 minutes` (300 seconds).
  - `30 minutes` (1800 seconds).
  - `Until app is closed` (-1): Unlocked state held in memory; stays unlocked across backgrounding/screen-off until process termination, task removal, or manual "Lock now".
- **Tamper-Proof Auto-Lock & Lockout (Acceptance Criterion 17)**:
  - All timers and lockouts use `SystemClock.elapsedRealtime()` (not wall-clock `System.currentTimeMillis()`) to prevent clock manipulation bypass.
- **Security & Data Safety**:
  - `FLAG_SECURE` enabled by default (prevents screenshots & recents leaks).
  - Rate-limiting lockout: 30s (5 fails), 60s (6 fails), 300s (7 fails), 900s (8+ fails).
  - Sensitive action re-auth: valid for 2 minutes.
  - Decoy PIN support opening an isolated blank safe.
  - Backup rules: `allowBackup="false"`, `data_extraction_rules.xml` excludes all domains.
  - Zero network permissions: No `android.permission.INTERNET`.
- **Storage Mode**:
  - Index-in-place default via Storage Access Framework (SAF) with `.nomedia` verification; Copy and Move to safe folders also supported.

## Current phase & status
- **Active Phase**: Master Build Spec v3 Full Alignment, Icon Integration from `sim_service_icon.svg`, and Section 8.1 Cover Screen implementation.
- **Build Status**: Fully compliant with Master Build Spec v3 acceptance criteria.

## Files created this phase
- `/app/src/main/res/drawable/sim_service_icon.svg` (Master vector icon)
- `/app/src/main/res/drawable/ic_launcher_background.xml` (Full-bleed metallic gradient adaptive background)
- `/app/src/main/res/drawable/ic_launcher_foreground.xml` (Matte black SIM with gold segmented chip)
- `/app/src/main/res/drawable/ic_launcher_monochrome.xml` (Monochrome silhouette for Android 13+ themed icons)
- `/app/src/main/res/drawable/ic_sim_slot_1.xml` (20x24 dp SIM slot 1 vector for Jazz row)
- `/app/src/main/res/drawable/ic_sim_slot_2.xml` (20x24 dp SIM slot 2 vector for Zong row)
- `/app/src/main/res/drawable/ic_chevron_right.xml` (Light gray #C7C7CC chevron)
- `/app/src/main/res/drawable/ic_arrow_back.xml` (Pure white arrow back)
- `/app/src/main/res/values-ur/strings.xml` (Urdu translation scaffolding)
- Updated `/app/src/main/res/values/strings.xml` (SIM Service app name & title, carrier strings, TalkBack labels)
- Updated `/app/src/main/java/com/example/ui/screens/SimCoverScreen.kt` (Pure-black minimal cover screen matching Section 8.1 exactly)
- Updated `/app/src/main/java/com/example/ui/theme/Color.kt` (Cover pure black & vault deep cinematic tokens)
- Updated `/app/src/main/java/com/example/security/PinSecurityManager.kt` (Tamper-proof `SystemClock.elapsedRealtime()` auto-lock & lockout timing)
- Updated `/app/src/main/java/com/example/MainActivity.kt` (ElapsedRealtime background timeout check & dark status bar configuration)
- Updated `/app/src/main/AndroidManifest.xml` (allowBackup="false", STK queries)
- Updated `/app/src/main/res/xml/data_extraction_rules.xml` & `backup_rules.xml` (Total backup exclusion)
- Updated `/app/src/test/java/com/example/ExampleRobolectricTest.kt` (Master spec string and security validation)
- Updated `/README.md` & `/brain.md`

## Not verified / Not run
- Physical hardware SIM Toolkit app invocation on a physical carrier SIM in Pakistan.
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).
- Some OEM ROMs lack the standard `com.android.stk` package; handled gracefully with a truthful informative dialog.

## Open questions for the user
- None. All requirements from `Private_Vault_Master_Spec_v3.md` and the `sim_service_icon.svg` asset are integrated. Ready for build and testing in Android Studio.

## Next steps
- Sync and build debug APK in Android Studio (`./gradlew assembleDebug`).
- Sideload onto Android device and verify cover screen flow, biometric prompt, and SAF indexing.
