# Private Vault — Requirements Traceability Matrix

> Built according to **Private Vault — Master Build Specification (v4, merged)** Section 40.3.
> Status vocabulary: `Planned`, `In Progress`, `Implemented`, `Tested`, `Verified on Device`, `Deferred`, `Blocked`.

---

## 1. Acceptance Criteria (PV-AC-001 through PV-AC-036)

| ID | Requirement | Priority | Phase | Module | Test / Evidence | Status | Notes |
|---|---|---|---|---|---|---|---|
| PV-AC-001 | Disguised Cover Screen titled "SIM Service", Jazz & Zong rows, black background | P0 | 3 | UI/Cover | Cover-screen inspection test | Implemented | Exact Section 8.1 specification (`#000000`, 20×24dp icons) |
| PV-AC-002 | Back arrow in top bar finishes activity without authentication | P0 | 3 | UI/Cover | Navigation exit test | Implemented | System finish / back press closes app cleanly |
| PV-AC-003 | Post-auth sheet appears after carrier row unlock unless direct launch enabled | P1 | 3 | UI/Cover | Post-auth panel test | Implemented | Offers Helpline, SIM Toolkit, and Open Vault |
| PV-AC-004 | "Call Helpline" opens system dialer with `ACTION_DIAL`, pre-filling 111 / 310 | P1 | 3 | UI/Cover | Intent verification | Implemented | Never auto-calls; relies on user action in system dialer |
| PV-AC-005 | "Open SIM Toolkit" detects installed STK packages or shows honest dialog | P1 | 3 | UI/Cover | Package query check | Implemented | Queries official package names without drawing fake menus |
| PV-AC-006 | Adaptive launcher icon with metallic squircle, SIM card, and gold contacts | P0 | 1 | Res/Icon | XML vector drawables | Implemented | Generated from `sim_service_icon.svg` |
| PV-AC-007 | Native dark vault theme with emerald accents and glassmorphic headers | P0 | 1 | UI/Theme | Compose theme inspection | Implemented | Uses `Dimens.kt`, `Type.kt`, and `Color.kt` |
| PV-AC-008 | Three primary media tabs: Photos, Videos, Audio with dynamic grid | P1 | 5 | UI/Vault | Tab & grid toggle test | Implemented | Dynamic 2, 3, or 4 column grid |
| PV-AC-009 | Storage Access Framework (SAF) folder selection with persisted permissions | P0 | 4 | Storage | Tree URI permission test | Implemented | `takePersistableUriPermission` with read/write flags |
| PV-AC-010 | Read-only folder scan without creating or modifying files | P0 | 4 | Storage | Read-only scan test | Implemented | Does not auto-create `.nomedia` during scan |
| PV-AC-011 | User-confirmed "Hide from Gallery" creates `.nomedia` without overwriting | P0 | 4 | Storage | `.nomedia` existence check | Implemented | Separate explicit action checking for existing file |
| PV-AC-012 | In-app Gallery Visibility Checklist with step-by-step guidance | P1 | 4 | UI/Checklist | Checklist navigation test | Implemented | Guides user through cache clearance and media hiding |
| PV-AC-013 | PBKDF2-HMAC-SHA256 master PIN verification with random salt | P0 | 2 | Security | `ExampleRobolectricTest` | Tested | Unit tested with 12,000 iterations & salt |
| PV-AC-014 | Biometric fingerprint / face authentication integration | P0 | 2 | Security | `BiometricHelper` check | Implemented | Uses AndroidX `BiometricPrompt` with crypto fallback |
| PV-AC-015 | Rate limiting lockout penalties (5 wrong = 30s, 6 = 60s, 7 = 300s, 8+ = 900s) | P0 | 2 | Security | Lockout calculation test | Implemented | Persists elapsedRealtime timestamp across restarts |
| PV-AC-016 | Tamper-proof auto-lock timing using `SystemClock.elapsedRealtime()` | P0 | 2 | Security | `AutoLockTimingTest` | Tested | Unit tested; immune to device clock modifications |
| PV-AC-017 | Configurable auto-lock timeouts (0s, 60s default, 120s, 300s, 1800s, -1) | P0 | 2 | Security | `SettingsRegistryTest` | Tested | Verified default is 60s (1 min) |
| PV-AC-018 | Screen-off broadcast receiver automatically locks vault on screen sleep | P0 | 2 | Security | Receiver registration test | Implemented | Listens for `ACTION_SCREEN_OFF` |
| PV-AC-019 | Window `FLAG_SECURE` blocks screenshots and recents previews | P0 | 2 | Security | Window flag inspection | Implemented | Applied on resume; configurable in settings |
| PV-AC-020 | Photo viewer with pinch zoom, double-tap zoom, pan, and EXIF inspection | P1 | 6 | UI/Viewer | Gesture HUD test | Implemented | Smooth gesture zoom/pan with metadata sheet |
| PV-AC-021 | Video player with Media3 ExoPlayer, HUD brightness & volume gestures | P1 | 6 | UI/Player | Player lifecycle test | Implemented | Left swipe brightness, right swipe volume, 10s seek |
| PV-AC-022 | Aspect ratio cycling (Fit, Zoom, Stretch) and playback speed controls | P1 | 6 | UI/Player | Player controls test | Implemented | 0.5x to 2.0x playback rates |
| PV-AC-023 | "Open in External Player" fallback intent with temporary content URI grants | P1 | 6 | UI/Player | Intent chooser test | Implemented | Allows playing in VLC / MX Player |
| PV-AC-024 | Audio player with rotating vinyl animation, frequency bars, and scrubbing | P1 | 6 | UI/Player | Audio player UI test | Implemented | Animated vinyl disc and docked mini-player |
| PV-AC-025 | Repeat modes: Loop Off, Loop One, Loop All | P1 | 6 | UI/Player | Repeat mode test | Implemented | Configured via `REPEAT_MODE` setting |
| PV-AC-026 | Headphone disconnect automatically pauses playback and mutes stream | P1 | 6 | UI/Player | Dual receiver test | Implemented | `ACTION_AUDIO_BECOMING_NOISY` & `AudioDeviceCallback` |
| PV-AC-027 | Headphone disconnect on-screen resume banner | P1 | 6 | UI/Player | Banner UI test | Implemented | Requires explicit on-screen tap to resume |
| PV-AC-028 | Phone lock pauses video playback while audio continues | P1 | 6 | UI/Player | Lock rule test | Implemented | Distinguishes video vs audio on screen lock |
| PV-AC-029 | Copy-verify-delete protocol for file move operations | P0 | 4 | Storage | `FileOperationsJournalTest` | Tested | Verifies byte count before deleting source file |
| PV-AC-030 | Room Operation Journal for crash resilience and recovery | P0 | 4 | Persistence | Room journal DAO test | Implemented | Tracks stage and reconciles on startup |
| PV-AC-031 | Recycle bin (trash) with 30-day auto-purge and restore | P1 | 7 | Storage | Trash DAO & repo test | Implemented | Reversible move to `.pv_trash/` with `.nomedia` |
| PV-AC-032 | Permanent deletion with optional multi-pass secure shredding | P1 | 7 | Storage | Shredder test | Implemented | Random byte overwrite before deletion |
| PV-AC-033 | Safe reset clears master PIN and database index without touching user media | P0 | 2 | Security | Safe reset unit test | Tested | Media files on SD card remain 100% intact |
| PV-AC-034 | Intruder photo front-camera capture with rate-limiting and private storage | P2 | 8 | Security | CameraX headless test | Implemented | Opt-in, stored in `.pv_intruders/` with `.nomedia` |
| PV-AC-035 | Security Alert Sheet shown to owner on unlock after failed intruder attempts | P2 | 8 | UI/Alert | Security sheet test | Implemented | Shows thumbnail, fail count, timestamp, and log |
| PV-AC-036 | Single Settings Registry consolidating all app configuration | P0 | All | Security | `SettingsRegistryTest` | Tested | 18 unified setting definitions with zero dead toggles |

---

## 2. Platform & Security Guardrails

| ID | Requirement | Priority | Status | Verification |
|---|---|---|---|---|
| PV-SEC-NET | Zero Internet permission in manifest | P0 | Implemented | `AndroidManifest.xml` lacks `android.permission.INTERNET` |
| PV-SEC-BAK | Disallow Android backup (`android:allowBackup="false"`) | P0 | Implemented | Verified in manifest `<application>` |
| PV-SEC-TOK | Complete Section 34 design tokens and typography | P0 | Implemented | `Dimens.kt` and `Type.kt` scale (32/40 down to 12/16) |
| PV-SEC-REC | Interrupted file operations reconcile on app startup | P0 | Implemented | `VaultRepository.reconcilePendingOperations()` in `VaultViewModel.init` |
