# brain.md

## Decisions (storage mode, DI choice, libraries + versions)
- **Platform & UI**: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- **Entry Flow**: Disguised SIM Services Carrier Dashboard (Jazz and Zong cards with balance, bundles, and telemetry) -> PIN / Biometric intercept -> Post-Auth Panel (Helpline, SIM Toolkit, Open Safe) -> Private Safe.
- **Storage Mode**: Index-in-place default with `.nomedia` insertion via Storage Access Framework (SAF). Also supports Copy and Move to dedicated vault folders.
- **Auto-Lock Timing Settings (Default: 1 minute)**:
  - `Immediate` (0 seconds): Locks immediately on backgrounding or screen-off.
  - `1 minute` (60 seconds, **DEFAULT**)
  - `2 minutes` (120 seconds)
  - `5 minutes` (300 seconds)
  - `30 minutes` (1800 seconds)
  - `Until app is closed` (-1): User can continue using the app while running; re-entering password is required whenever the app is closed and reopened.
- **First-Time Safe Permissions Flow**:
  - Modal onboarding when Private Safe is opened for the first time.
  - Prompts for `POST_NOTIFICATIONS` (Android 13+ lock-screen player controls & import progress).
  - Prompts for SAF Vault Folder selection (`ACTION_OPEN_DOCUMENT_TREE`).
  - Sets `.nomedia` marker automatically.
- **Advanced Security Features**:
  - Change Master PIN (verify current -> enter new -> confirm).
  - Decoy PIN Safe (secondary PIN opening an isolated blank safe).
  - Keypad Number Shuffle (anti-smudge / anti-shoulder surfing).
  - Haptic feedback toggle on numeric keypad taps.
  - `FLAG_SECURE` window privacy toggle.
  - Auto-lock background timing enforcement.
  - Zero network permissions: App requests NO `android.permission.INTERNET`.

## Current phase & status
- **Active Phase**: Fully implemented Auto-Lock timing selector, first-time permissions setup sheet, realistic SIM Services carrier dashboard, and PIN interception flow.
- **Build Status**: Verified with `compile_applet` and Robolectric test suite.

## Files created this phase
- `/app/src/main/java/com/example/ui/components/FirstTimeSetupDialog.kt` (First-time permissions & storage setup sheet)
- `/app/src/main/java/com/example/ui/components/PinChangeDialog.kt` (Change Master PIN & Decoy PIN setup dialog)
- Updated `/app/src/main/java/com/example/security/PinSecurityManager.kt` (Auto-lock timing constants, decoy PIN, haptic, shuffle)
- Updated `/app/src/main/java/com/example/MainActivity.kt` (Auto-lock timeout check onStart/onResume & onStop)
- Updated `/app/src/main/java/com/example/ui/screens/SimCoverScreen.kt` (Realistic SIM Services carrier dashboard with PIN interception)
- Updated `/app/src/main/java/com/example/ui/screens/SettingsScreen.kt` (Auto-lock radio picker dialog, Change PIN, Decoy PIN)
- Updated `/app/src/main/java/com/example/ui/screens/VaultMainScreen.kt` (Wired FirstTimeSetupDialog)
- Updated `/app/src/main/java/com/example/ui/components/PinKeypadDialog.kt` (Haptic feedback, keypad shuffle, decoy PIN support)

## Not verified / Not run
- Physical SD card hardware ejection during active playback.
- Physical fingerprint scanner hardware (verified via software fallback).
- Hardware SIM Toolkit app invocation on a physical carrier SIM in Pakistan.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 11+ Scoped Storage prevents picking the root of the SD card. User picks a subfolder (e.g. `new` or `.private_vault`).
- Some devices without STK (SIM Toolkit) will display an informative dialog rather than crashing.

## Open questions for the user
- All current requirements are addressed. Ready for user testing and build feedback.
