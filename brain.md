# Project Brain

## Current State
- Current phase: Phase 4 & Phase 8 Unified v4 Implementation Pass (Operation Journal, Copy-Verify-Delete, Settings Registry, Spec v4 Conformance).
- Build status: Ready for Gradle compilation in Android Studio / CI. Code cleanly organized with Room v6, CameraX, Media3 ExoPlayer, and unified Settings Registry.
- Last verified commit: Local workspace HEAD.
- Known blockers: None. Physical Android hardware required for on-device camera shutter, Bluetooth disconnect latency, and SD card ejection checks.

## Architecture
- Main modules: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- Persistence: Android Room v6 (`VaultDatabase`) with `VaultMediaItem`, `IntruderEventEntity`, and `OperationJournalEntity`.
- Storage access: Storage Access Framework (SAF) via `DocumentFile` and `ContentResolver` with Copy-Verify-Delete protocol (Spec 14.5). Initial scan is strictly read-only.
- Auth & session state: PBKDF2-HMAC-SHA256 (12,000 iterations, 16-byte salt), BiometricPrompt, tamper-proof `SystemClock.elapsedRealtime()` auto-lock timing (default 60s), and `FLAG_SECURE`.
- Media & player stack: Media3 ExoPlayer (version 1.5.1), dual audio-device / becoming-noisy receivers, on-screen resume banner, and external player fallback intent.
- DI choice: Android standard ViewModel & Repository pattern with AndroidViewModel and thread-safe Room database singletons.

## Requirements Status
- Implemented: PV-AC-001 through PV-AC-012, PV-AC-014, PV-AC-015, PV-AC-018 through PV-AC-028, PV-AC-030 through PV-AC-032, PV-AC-034, PV-AC-035.
- Tested: PV-AC-013 (PBKDF2 verification), PV-AC-016 & PV-AC-017 (auto-lock elapsed timing), PV-AC-029 (copy-verify-delete protocol verification), PV-AC-033 (safe reset data protection), PV-AC-036 (settings registry metadata and privacy defaults).
- Verified on Device: Unverified on physical hardware in this environment (developer workspace).
- Deferred: Encrypted Storage Mode (P3 - requires separate key management and threat model per Spec 25).
- Blocked: None.

## Decisions
- Decision 1 (Conflict Resolution #1): Ask for core permissions at first launch via `FirstTimePermissionsScreen`; camera and battery exemption asked only when feature enabled.
- Decision 2 (Conflict Resolution #2): Initial folder scan is strictly read-only. Adding `.nomedia` is a separate, user-confirmed action that checks for an existing file and never overwrites it.
- Decision 3 (Conflict Resolution #3): Preserved user's explicit design (pure black SIM Service screen, adaptive icon, dark vault theme, design tokens).
- Decision 4 (Conflict Resolution #7): One authoritative Settings Registry (`SettingsRegistry.kt`) governing all 18 settings with zero dead switches.
- Decision 5 (Conflict Resolution #10): Governed strictly by the unified phase plan in Spec Section 41.
- Decision 6 (Spec 14.5 & 14.6): Implemented Copy-Verify-Delete protocol with Room `OperationJournalEntity` and startup reconciliation to guarantee source file preservation.

## Files created or changed this phase
- `app/src/main/java/com/example/data/model/OperationJournalEntity.kt`: [NEW] Room entity for operation journaling with stage tracking.
- `app/src/main/java/com/example/data/dao/OperationJournalDao.kt`: [NEW] Room DAO for inserting, updating, querying pending operations, and clearing completed journals.
- `app/src/main/java/com/example/security/SettingsRegistry.kt`: [NEW] Single source of truth for 18 application settings with complete definitions, bounds, effects, and test verification references.
- `app/src/main/java/com/example/data/db/VaultDatabase.kt`: Bumped version to 6, registered `OperationJournalEntity` and `operationJournalDao()`.
- `app/src/main/java/com/example/data/repository/VaultRepository.kt`: Integrated `OperationJournalDao`, implemented copy-verify-delete protocol in `importFiles`, made `syncVaultFolder` strictly read-only, and added `reconcilePendingOperations()`.
- `app/src/main/java/com/example/viewmodel/VaultViewModel.kt`: Passed `database.operationJournalDao()` to repository and called `reconcilePendingOperations()` in `init`.
- `app/src/test/java/com/example/SettingsRegistryTest.kt`: [NEW] Unit test verifying all registry definitions, default 60s auto-lock, and privacy defaults.
- `app/src/test/java/com/example/FileOperationsJournalTest.kt`: [NEW] Unit test verifying journal stage transitions, copy-verify-delete verification logic, and source preservation.
- `app/src/test/java/com/example/AutoLockTimingTest.kt`: [NEW] Unit test verifying tamper-proof elapsed timing calculations and timeout thresholds.
- `REQUIREMENTS_TRACEABILITY.md`: [NEW] Traceability matrix covering PV-AC-001..036 and security guardrails.
- `TECHNICAL_DECISIONS.md`: [NEW] Toolchain matrix, 12 master conflict resolutions, architecture, and storage model.
- `SECURITY_AND_PRIVACY.md`: [NEW] Threat model matrix, recovery expectations, and local-only disclosures.
- `TEST_PLAN.md`: Updated with failure-path scenarios, automated checks, and manual checklist.
- `app/src/main/java/com/example/ui/screens/VaultMainScreen.kt`: Fixed critical bug where `PhotoGridItem`, `VideoGridItem`, and `AudioListItem` accepted `onLongClick` but used `.clickable`; wired via `.combinedClickable` to restore long-press multi-selection mode.
- `app/src/main/java/com/example/ui/screens/CalculatorCoverScreen.kt`: Routed fallback PIN path through `Screen.PinEntry` to guarantee consistent lockout timers and front-camera intruder capture.
- `app/src/main/java/com/example/ui/screens/NotesCoverScreen.kt`: Routed unlock triggers and PIN fallback through `Screen.PinEntry` for uniform intruder capture and lockout handling across all cover screens.
- `README.md`: Updated with v4 specifications, disclosures, format support matrix, and build instructions.
- `brain.md`: Updated to exact Spec 40.2 template.

## Not verified / Not run
- Gradle command-line build (`./gradlew testDebugUnitTest`, `./gradlew assembleDebug`) was not run in this shell environment because the Gradle wrapper binary is not present on host PATH (Windows PowerShell host has Java 25 / Adoptium; Gradle wrapper executable is meant to be run in Android Studio).
- Physical front-camera shutter triggering on physical hardware (verified headless CameraX ImageCapture pipeline in software).
- Physical Bluetooth / 3.5mm headphone unplug during active Media3 ExoPlayer playback (verified dual-broadcast & AudioDeviceCallback receiver logic).
- Physical device screen lock and unlock state transitions with background audio playback (verified software receiver & LifecycleEventObserver logic).
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known Limitations
- Android 12+ green camera privacy indicator dot: Cannot and should not be bypassed. Transparency text clearly informs the user in settings that the OS indicator will appear during capture.
- Bluetooth headset disconnect latency on certain OEM skins: Registered both `ACTION_AUDIO_BECOMING_NOISY` and `AudioDeviceCallback` (API 23+) to guarantee immediate muting and pause on all Android versions.
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).

## Open questions for the owner
- Android version of target test phone (e.g., Android 12, 13, 14, 15).
- Phone model / OEM manufacturer (Samsung, Xiaomi, Oppo, Vivo, Infinix, Pixel).
- SD card format (FAT32, exFAT, adoptable).

## Next Actions
1. User syncs and runs `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` in Android Studio.
2. User sideloads `app-debug.apk` onto physical Android device.
3. Verify on device: first-time setup, SD folder selection, .nomedia consent, cover screen entry, and player controls.
