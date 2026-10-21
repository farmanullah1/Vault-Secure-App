# Technical Decisions & Architecture Record

> Built according to **Private Vault — Master Build Specification (v4, merged)** Section 40.1.

---

## 1. Verified Toolchain Matrix

| Component | Verified Version | Notes |
|---|---|---|
| Android Gradle Plugin (AGP) | 9.1.1 | Modern declarative DSL in Kotlin script |
| Gradle | 9.3.1 | Configured via `gradle-wrapper.properties` |
| Kotlin | 2.2.10 | Target JVM 11 |
| Jetpack Compose BOM | 2024.09.00 | Material 3, Single-Activity architecture |
| Android Room | 2.7.0 (with KSP) | Version 6 schema (`VaultMediaItem`, `IntruderEventEntity`, `OperationJournalEntity`) |
| Media3 ExoPlayer | 1.5.1 | Hardware accelerated playback with external player fallback |
| CameraX | 1.5.0 | Headless front-camera capture for intruder logging |
| AndroidX Biometric | 1.1.0 | BiometricPrompt authentication |
| AndroidX DocumentFile | 1.0.1 | Storage Access Framework (SAF) tree manipulation |
| Min SDK | API 24 (Android 7.0) | Broad device compatibility |
| Target & Compile SDK | API 36 | Latest modern platform standard |

---

## 2. Master Conflict Resolutions (Spec 2.1)

Where the original v3 spec and the v4 implementation addendum differed, these binding decisions govern the implementation:

1. **Permission Timing**: Core permissions (SAF folder tree, notifications on Android 13+, biometric setup) are requested at first launch via `FirstTimePermissionsScreen` with clear rationale. Camera and battery exemption are requested **only when the user explicitly enables** the corresponding feature in Settings.
2. **`.nomedia` Behavior**: Initial folder scan is **strictly read-only**. It never creates, overwrites, or modifies `.nomedia` or media files. Adding `.nomedia` is a separate, user-confirmed "Hide from gallery" action that checks for existing files and never overwrites them.
3. **Product Design & Identity**: The black SIM Service disguise screen, authentic SIM vector drawables, dark cinematic vault theme, and complete token system (`Dimens.kt`, `Type.kt`) define the product identity. No unapproved redesigns are permitted.
4. **Decoy Safe & Panic Gestures**: Categorized as **P3**. Decoy PIN opens an isolated empty safe. Flip-to-lock and shake-to-lock are disabled by default until user toggles them.
5. **Form Factor Layouts**: Phones (portrait, landscape, font scaling up to 200%) are **P1**. Tablets, foldables, and multi-window split screens are **P2** and built using fluid grids.
6. **Performance Targets**: Numbers (60 fps, < 800 ms cold start) are documented as **engineering targets**, not unsubstantiated benchmark claims.
7. **Settings Consolidation**: Consolidated into **one authoritative registry** (`SettingsRegistry.kt`). Zero dead or decorative toggles exist.
8. **Release Prioritization**: Ship in priority order: P0 (Blockers) -> P1 (Core) -> P2 (Privacy/Comfort) -> P3 (Advanced).
9. **Dependency Stability**: Maintained a stable, compatible version set; no blind upgrades to untested milestones.
10. **Phase Plan**: Governed strictly by the unified phase plan in Spec Section 41.

---

## 3. Storage Architecture & Copy-Verify-Delete

Direct filesystem paths on modern Android (API 30+) are heavily restricted by Scoped Storage. All external and SD card access uses the **Storage Access Framework (SAF)**:
- Persistent root URI permissions are acquired via `takePersistableUriPermission`.
- Operations stream through `ContentResolver` with bounded 8KB buffers.
- Destructive moves employ the **Copy-Verify-Delete Protocol (Spec 14.5)**:
  1. Initialize `OperationJournalEntity` record in Room database.
  2. Stream bytes to destination DocumentFile.
  3. Flush output stream and verify destination byte count against source size.
  4. Only after size verification succeeds, delete source file.
  5. If verification fails or transfer is interrupted, destination is purged and **source is preserved**.
- **Startup Reconciliation**: `VaultRepository.reconcilePendingOperations()` checks for unfinished operations after process death or crash, marking interrupted transfers as failed and ensuring source preservation.

---

## 4. Security & Cryptographic Model

1. **Master PIN Verification**:
   - Algorithm: PBKDF2-HMAC-SHA256 (with fallback to SHA1 if provider requires).
   - Parameters: 12,000 iterations, 16-byte cryptographically secure random salt generated via `SecureRandom`.
   - Never logs or stores plaintext PIN.
2. **Lockout & Tamper Resistance**:
   - Timing uses `SystemClock.elapsedRealtime()`, which monotonic ticks from boot.
   - Changing the device clock or timezone cannot bypass lockout penalties.
   - Lockout timestamps are persisted in private SharedPreferences and survive app restarts.
3. **Screen Privacy**:
   - `WindowManager.LayoutParams.FLAG_SECURE` blocks screen capture and obscures recent apps switcher snapshots.
4. **Intruder Photo Guardrails**:
   - Headless CameraX front-camera capture is opt-in, throttled (minimum 3s gap), stored in private `.pv_intruders/` with `.nomedia`, and pruned after retention window (default 30 days).
