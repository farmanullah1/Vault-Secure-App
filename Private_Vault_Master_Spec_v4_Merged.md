# Private Vault — Master Build Specification (v4, merged)

> **How to use this file:** Give this entire file to Google AI Studio (or any coding agent) as the single source of truth. **This is v4: the v3 master specification (product, UI, players, privacy, layout) merged with the "V4 Update & Implementation Addendum" (process discipline, data-integrity rules, settings registry, traceability, test evidence).** Where the two source documents disagreed, **Section 2.1 (Conflict Resolutions)** records the decision; in any other doubt, the **safer, more data-preserving, more honest** rule wins. Where this file and any earlier file conflict, **this file wins**.

---

## 0. Builder Operating Rules (Read This First)

### 0.1 Two starting situations
- **A. New project (no code yet):** build in the phases of **Section 41**. If your environment cannot compile or run Android, **do not claim a build, test or run passed**. Output the full project as individual files with exact relative paths so the user can paste them into Android Studio, build, and send errors back.
- **B. Existing project (code already exists, for example an earlier AI Studio output):** **do not rebuild blindly or replace working features.** Before changing code:
  1. Inspect the whole repository: structure, Gradle files, manifest, dependencies, navigation, persistence, UI, tests, and read `brain.md`, `README.md`, `TEST_PLAN.md` if present.
  2. Build a **feature inventory**: each requirement in this spec and its current status (vocabulary in 0.4).
  3. List contradictions, duplicate implementations, unsafe file operations, missing tests, dependency incompatibilities, and **controls that do nothing**.
  4. Write a short implementation plan and update `brain.md`, then work phase by phase.

### 0.2 Workflow for every phase
1. State the plan and update `brain.md` **before** substantial changes.
2. Implement in small steps that can be built and verified.
3. Run the build and the tests that actually exist; record the exact command and result. Do not continue past a blocking failure without recording it.
4. Update `README.md`, `TEST_PLAN.md`, `brain.md` and `REQUIREMENTS_TRACEABILITY.md` with **real** results.
5. Report truthfully (files changed, what works, tests actually run, failures, manual checks still needed, deferred items), then **stop and wait** for the user's build feedback.

### 0.3 Non-negotiable rules
- **Be honest about the environment.** Never claim a build, test, permission, device compatibility, safe move or encryption works without evidence.
- **No dummy data.** No sample photos, fake albums, fabricated counts, fake intruder events, pretend scan results or demo media on production screens.
- **No fake functionality.** Every visible button, toggle, menu item, setting, gesture and permission action must work, or be clearly marked *Unavailable / Not implemented*. Never show a success message unless the operation actually succeeded. **No dead switches.**
- **No silent omissions.** A feature that cannot be done safely or in the current phase is recorded as `Deferred` with a reason, never reported as implemented.
- **No destructive shortcuts.** Never overwrite, move or delete original media without explicit user intent and a recovery safeguard.
- **No security exaggeration.** Hidden Mode, a dot-prefixed folder, `.nomedia`, a disguised launcher or a hidden UI is **not** encryption and **not** guaranteed invisibility.
- **Never invent** carrier numbers, USSD codes, package names or API behavior. If unsure, say so and make it user-configurable.
- **No fake UI** that imitates something real (a fake SIM Toolkit menu, fake carrier branding, fake "encrypted" badges).
- **No unnecessary permissions.** Ask only for what a feature the user enables needs (the first-launch checklist in Section 9.8 is the one deliberate exception, and it still excludes everything not needed).
- **Preserve product identity.** This spec's own design (Sections 7, 8.1, 10.7, 31, 34) *is* the identity; do not redesign it without a documented defect. No needless dependencies; reuse what exists.
- **Offline only.** No INTERNET permission, accounts, cloud sync, analytics, ads, remote crash reporting or remote media processing in this release.
- **No automatic execution of sensitive workflows.** The app must not silently enable the camera, permanently delete files, export private media, reset credentials, or perform a move that can lose data.
- Use only maintained, mainstream libraries; keep versions compatible (Section 2.3).
- Keep `brain.md` (Section 40) current after every phase.

### 0.4 Status vocabulary (use only these words in docs and reports)

| Status | Meaning |
|---|---|
| `Planned` | Not implemented yet. |
| `In Progress` | Implementation has started. |
| `Implemented` | Code exists; validation may be incomplete. |
| `Tested` | The relevant automated or manual test was **actually run and passed**. |
| `Verified on Device` | Checked on a real device (or a clearly named emulator where appropriate). |
| `Deferred` | Deliberately postponed, with a reason and follow-up. |
| `Blocked` | Cannot proceed because of a concrete dependency, platform limit or open decision. |

`Implemented`, `Tested` and `Verified on Device` are **never interchangeable**.

---

## 1. Product Summary

**Working name:** *Private Vault* (user may rename; keep the name in one string resource).

A native **Android** app that:

- Keeps the user's **photos, videos and audio** on the **SD card** (the phone's internal storage is full).
- **Hides them from the phone's gallery ecosystem** (Google Photos, Gallery, Instagram/WhatsApp/Facebook pickers, Chrome/browser pickers) while keeping them reachable from **this app** and from a **file manager** if the user navigates to the folder.
- Opens behind a **disguised cover screen** that looks like a normal SIM/carrier utility, titled **"SIM Service"**, with two list rows: **Jazz** and **Zong**.
- Tapping a row (Jazz or Zong) requires **fingerprint or PIN/password**. After success the user reaches a **post-auth panel** (carrier actions + Open Vault), then the vault.
- Inside the vault: three sections — **Photos**, **Videos**, **Audio** — each with a premium, polished viewer/player.
- Is **beautiful, fast and smooth**: this is a hard requirement, not a nice-to-have.

**Important truth to state in-app (Settings → About privacy):** This is **Hidden Mode** (removed from gallery indexing), **not encryption**. Anyone who opens the folder in a file manager or reads the card on a PC can see the files. Encrypted Mode is **`Deferred`** (Section 25).

---

## 2. Decisions Made (defaults — user can change later in Settings)

| Topic | Default | Why |
|---|---|---|
| Platform | Native Android, Kotlin, Jetpack Compose, Material 3 | Required for SD card, biometrics, SIM Toolkit intent, ExoPlayer |
| Entry flow | **Option A**: Cover screen → tap Jazz/Zong → authenticate → post-auth panel → vault | Matches the user's description |
| Storage mode | **Index in place** (recommended) with **Copy** and **Move** also available | User's files are already in the SD card folder `new`; storage is tight, so no duplication by default |
| Minimum Android | 8.0 (API 26); target latest stable | Wide device coverage; modern permissions handled properly |
| Language | English UI, with string resources ready for Urdu translation; full RTL-safe layouts | User is in Pakistan; keep Urdu-ready |
| Theme | Dark-first, with Light and AMOLED-black options | Media apps look best dark |

Open questions to ask the user **once** during Phase 1 (do not block on them; use defaults): Android version, phone model, SD card format (FAT32 / exFAT / adoptable), SD card free space.

### 2.1 Conflict Resolutions (v3 spec vs. the ChatGPT addendum)
The two source documents agree on most things (honesty, no dummy data, offline-only, SAF safety, no all-files access, honest `.nomedia` claims). Where they differed, this is the binding decision:

| # | Topic | v3 spec said | Addendum said | **v4 decision** |
|---|---|---|---|---|
| 1 | When to ask for permissions | Ask for everything the app needs on first launch (your explicit request) | Ask only when the user enables the related feature | **Core permissions at first launch** (folder access, notifications, fingerprint setup); **camera and battery exemption only when the user turns the feature on.** Every request has a rationale first. |
| 2 | `.nomedia` during setup | "Index in place" creates `.nomedia` automatically | The initial scan must be **read-only**; never create files or overwrite an existing `.nomedia` automatically | **Scan is read-only.** Adding `.nomedia` is a **separate, user-confirmed "Hide from gallery" step** that first checks for an existing file and never overwrites it (Section 14.8). |
| 3 | Redesign | Full redesign requested (black SIM Service screen, new icon, layout system) | "No forced redesign; preserve identity" | **The user's explicit design requests are the identity.** After this spec, no further redesign without a documented defect. |
| 4 | Decoy PIN, panic gesture, shake-to-lock, Quick Settings tile | Optional extras | Defer unless state separation is reliable | **P3, off by default, hidden until built and tested.** Decoy mode ships only if real and decoy state are provably separate (Section 9.10). |
| 5 | Tablet / foldable layouts | Required responsive design on all screens | Future scope unless targeted | **Phones (portrait, landscape, font scale up to 200 %) are P1.** Tablet, foldable and split-screen layouts are **P2**: built, but they do not block a release. |
| 6 | Performance numbers (60 fps, < 800 ms cold start) | Stated as targets | Never invent benchmarks | **They remain *targets*.** Report only measurements taken on a named device and dataset (Section 39.6). |
| 7 | Settings sprawl | Many options across Sections 9.9, 16, 31, 33 | One settings registry; no decorative toggles | **One registry** (Section 36). Sections 9.9, 16, 31 and 33.4 are the *candidate catalogue*; any option not working is hidden or shown as Unavailable. |
| 8 | Feature volume | "Add everything useful" | "Fewer, fully working features beat a long menu" | **Keep all requested features, but ship in priority order (Section 2.2).** P2/P3 never block a working P0/P1 app. |
| 9 | Dependency versions | "Verify latest stable" | "Do not blindly upgrade to latest" | **Pick a compatible, build-verified set**; upgrade only in a separate, verified step (Section 2.3). |
| 10 | Phase plans | 10 phases + an "enhancements" phase | 10 different phases | **One unified plan** in Section 41; the older phase tables are superseded. |
| 11 | Reset app | Confirmation + explanation | Fresh auth, explain leftover hidden folders and intruder photos, report real outcome | **Merged flow** in Section 37.4. |
| 12 | Fingerprint priority | Core | P2 | **PIN/password is P0; fingerprint is P1** (you asked for it) and always falls back to the PIN. |

If a new conflict appears while building, **record it in `TECHNICAL_DECISIONS.md` and `brain.md`**, follow the safer data-preserving behavior, and flag any decision that needs the owner's approval. Never silently pick the easier option and never drop a feature without marking it `Deferred`.

### 2.2 Priority tags (what blocks a release)

| Priority | Meaning | Features |
|---|---|---|
| **P0 Release blocker** | Core access, security, data integrity, crash prevention | Cover screen and PIN/password entry; hashing, lockout, auto-lock modes, cold-start/reboot lock; folder access via SAF; **read-only scan**; copy-verify-delete + operation journal; reset never deleting media; no INTERNET; honest security wording; no crashes on SD removal |
| **P1 Core release** | A complete, reliable app | Photos/Videos/Audio libraries and thumbnails; photo viewer; video and audio players (play/pause/seek, speed, brightness and volume gestures, orientation, loop off/one/all); **headphone-disconnect stop + mute**; **phone-lock video pause**; neutral notifications; fingerprint unlock; first-launch permission checklist; trash/restore; export/share; `FLAG_SECURE`; settings registry; app icon; spacing/border/size tokens; phone responsiveness |
| **P2 Optional privacy / comfort** | Valuable, must not block P0/P1 | **Intruder photo + owner alert**; A-B loop and loop count; subtitles and audio-track switching; PiP; albums, tags, search syntax, smart collections; duplicate finder; locked albums; playlists; sleep timer; tablet/foldable/split-screen layouts; settings backup; Urdu translation; storage dashboard |
| **P3 Advanced / future** | Needs separate feasibility or testing | Photo editing; batch rename; compare mode; ratings/notes; shake-to-lock; panic gesture; decoy PIN; Quick Settings tile; "Add to vault" share target; disguise alias choices; **Encrypted Mode (`Deferred`, Section 25)** |

Tag every item with its priority in `REQUIREMENTS_TRACEABILITY.md`.

### 2.3 Platform & dependency compatibility
- Before coding, **inspect and record the actual versions** of Android Gradle Plugin, Gradle, Kotlin, Compose compiler/plugin, KSP, Room, Media3, CameraX and every other library. Record the verified matrix in `TECHNICAL_DECISIONS.md`.
- Do not mix incompatible versions or add a second dependency-injection system. Preserve the existing package/application ID unless a migration is approved.
- State `minSdk` and `targetSdk` from the real build configuration. Any version change is its own build-verified step.

---

## 3. Goals, Non-Goals, Constraints

### Goals
- Hide media from MediaStore-based apps using `.nomedia`.
- Fast browsing of thousands of items with smooth thumbnails.
- Excellent photo viewer, video player and audio player.
- Proper file management (Section 14).
- A security model that is honest and robust (Section 9).

### Non-Goals (do not build, do not claim)
- Root, "all files access" bypasses, hacking MediaStore rows, or any trick to deceive the OS.
- Cloud sync, accounts, analytics, ads, network access. **The app must request no INTERNET permission.**
- Claiming to support "all video formats" (Section 12 explains the honest approach).
- Copying carrier logos or implying the app is official/affiliated with Jazz or Zong.

### Constraints
- Android 11+ **scoped storage**: access SD card content through the **Storage Access Framework (SAF)** folder picker. The SD card **root cannot be selected** on Android 11+; the user must pick `new` or a subfolder.
- FAT32 has a **4 GB per-file limit** and coarse timestamps; exFAT is more lenient. Detect and report, never fail silently.
- Adoptable-storage SD cards are tied to the phone; warn the user.

---

## 4. Architecture

- **Language/UI:** Kotlin, Jetpack Compose, Material 3, single-activity, Navigation Compose.
- **Pattern:** MVVM + Unidirectional Data Flow. `ViewModel` exposes `StateFlow<UiState>`; UI sends `Events`.
- **Layers:** `ui/` → `domain/` (use cases) → `data/` (repositories, Room, SAF, DataStore).
- **DI:** Hilt (or manual DI if Hilt complicates AI Studio output — state the choice in `brain.md`).
- **Async:** Coroutines + Flow. No blocking IO on main thread.
- **Persistence:** Room (media index), DataStore (settings), EncryptedSharedPreferences is **not** required — store PIN hash + salt in DataStore/Room (Section 9).
- **Background work:** WorkManager + foreground service for scans/imports (Section 17).
- **Playback:** Media3 (ExoPlayer + MediaSession + UI components where useful).
- **Images:** Coil 3 (with video-frame and GIF/WebP support); custom zoomable image composable.

### Suggested package layout
```
app/src/main/java/com/example/privatevault/
  App.kt
  MainActivity.kt
  core/        (result types, dispatchers, formatters, permissions helpers)
  data/
    db/        (Room: MediaEntity, AlbumEntity, TagEntity, dao, database)
    storage/   (SafRepository, NoMediaManager, StorageDiagnostics, MoveCopyEngine)
    settings/  (SettingsRepository via DataStore)
    security/  (PinHasher, LockoutStore, AuthSession)
  domain/      (use cases: ScanFolder, ImportMedia, RenameMedia, DeleteToTrash, ...)
  ui/
    theme/     (Color, Type, Shape, Motion)
    cover/     (CoverScreen, SimCard, PostAuthPanel)
    auth/      (PinSetup, PinEntry, BiometricGate)
    onboarding/
    home/      (VaultHome, Sections)
    library/   (Photos/Videos/Audio grids, search, filters, selection)
    viewer/    (PhotoViewer)
    player/    (VideoPlayer, AudioPlayer, MiniPlayer, controls)
    files/     (FileManager, Details, Trash, Albums)
    storage/   (StorageDashboard, GalleryChecklist)
    settings/
  service/     (ImportService/Worker, PlaybackService)
```

### 4.1 Responsibilities (keep them separate)
- **UI** renders state and forwards intents. **ViewModels** hold screen state. **Repositories** own data access. The **storage / file-operation layer** owns SAF URIs, copy/move/delete, recovery and permission failures. The **media layer** owns metadata, thumbnails, playback, audio focus and formats. The **security layer** owns authentication state, lockout, re-authentication and sensitive-operation gates. The **persistence layer** owns settings, index/scan state, the operation journal and event logs.
- Follow the project's existing architecture where it is sound; avoid a rewrite just to match a preferred pattern. Use one DI approach only.

### 4.2 Single source of truth
Never keep conflicting copies of the same state in a screen, a service, the database and settings. Define explicit state models for: **authentication/lock**, **folder permission/access**, **library scan/index**, **file operations**, **playback**, **intruder events** (if enabled), and **settings/feature availability**. State that must survive process death is persisted; only transient UI state lives in memory. Never assume a process or service stays alive.

### 4.3 Database & migrations
- Inspect the existing schema first. Use **explicit migrations**; never "fix" a schema change by deleting the database. Preserve settings and index data through supported upgrades.
- Store durable SAF document URIs plus the metadata needed to reopen them. Keep media binaries out of the database (references and metadata only).
- Define behavior when the database is unavailable, corrupt or out of date. An index backup never contains the actual media (Section 37.3).

### 4.4 Work off the main thread
Scanning, metadata extraction, thumbnails, hashing and file transfers never run on the UI thread. They support cancellation, progress, lifecycle changes and process death, and use paging or incremental loading rather than loading a whole library into memory.

---

## 5. Dependencies (use a compatible, build-verified set; no blind upgrades)

- `androidx.compose` BOM, Material 3, Navigation Compose, Activity Compose, Lifecycle (runtime/viewmodel compose)
- `androidx.media3`: exoplayer, ui, session, common, datasource (optional: `decoder-ffmpeg` only if built manually — see Section 12)
- `androidx.room` (runtime, ktx, compiler via KSP), `androidx.work:work-runtime-ktx`
- `androidx.datastore:datastore-preferences`
- `androidx.biometric:biometric`
- `androidx.documentfile:documentfile`
- `io.coil-kt.coil3` (compose, video, gif)
- `com.google.dagger:hilt` (optional)
- `androidx.exifinterface:exifinterface`
- `androidx.camera` (camera-core, camera-camera2, camera-lifecycle) for the optional intruder photo (Section 32)
- `androidx.window` (window size classes, fold posture) and `androidx.compose.material3:material3-window-size-class` (Section 34)
- `androidx.core:core-splashscreen`
- Test: JUnit, Turbine, Room testing, Compose UI test

No network libraries. No analytics. No ads.

---

## 6. Permissions & Manifest

Declare **only** what is needed:

- `USE_BIOMETRIC` (and `USE_FINGERPRINT` only if supporting very old APIs)
- `POST_NOTIFICATIONS` (Android 13+, for import/playback notifications)
- `FOREGROUND_SERVICE` + the correct foreground service types for the target SDK (`dataSync` for imports, `mediaPlayback` for audio)
- `WAKE_LOCK` (player)
- `CAMERA` (runtime, **optional**, requested only when the user turns on "Intruder photo"; declare `<uses-feature android:name="android.hardware.camera.front" android:required="false"/>` so the app still installs on devices without a front camera)
- `MODIFY_AUDIO_SETTINGS` is **not** needed for `setStreamVolume`; do not declare it unless a specific API requires it.
- **Do NOT** request: `INTERNET`, `READ_PHONE_STATE`, `CALL_PHONE`, `MANAGE_EXTERNAL_STORAGE`, contacts, location, mic, or `BLUETOOTH_CONNECT` (headset removal is detected through `AudioManager`, which needs no Bluetooth permission).

`<queries>` block for SIM Toolkit package names (Section 10.3) and for external-player intents.

Other manifest items:
- `android:allowBackup="false"` and data-extraction rules excluding everything (so PIN data isn't backed up).
- Disable activity transition leaks in recents (`FLAG_SECURE`, Section 9).
- A `FileProvider` is **not** needed if content URIs from SAF are shared directly with temporary grants.
- Launcher: one activity alias for the cover screen. Optional **alternate launcher aliases** (Section 10.6) to let the user choose the default "SIM Service" icon/name or a plain "Utilities" one. The launcher icon is specified in Section 10.7.

---

## 7. Design System ("make it look amazing")

### 7.1 Visual identity
Two **distinct personalities** inside one app:

1. **Cover / "SIM Service" screen — "Normal but very good":** a minimalist, high-contrast, **pure-black** system-utility look: a slim top bar with a back arrow and the bold title "SIM Service", two simple list rows, and an empty black screen below. It looks like an ordinary SIM menu, not a vault. Exact layout in Section 8.1.
2. **Vault — "Premium & private":** deep, cinematic, dark. Feels like a high-end media app (think Apple Photos meets a pro video player).

### 7.2 Color
- **Cover / SIM Service (always dark, ignores system theme):** screen, status bar and navigation bar all `#000000`; primary text and icons `#FFFFFF`; chevrons `#C7C7CC` (light gray); pressed-row highlight `#141416`; optional hairline divider `#1C1C1E`. **No accent color, no gradients, no cards, no carrier brand colors or logos.** Contrast of white on black exceeds 20:1; chevron on black exceeds 11:1.
- **Vault (dark default):** background `#0B0D12`, elevated surface `#14181F`, high surface `#1C2230`, outline `#262E3D`. Primary accent: a refined violet-to-cyan gradient (`#7C5CFF → #22D3EE`) used sparingly for focus, progress and key actions.
- **AMOLED mode:** true `#000000` background.
- **Light vault mode:** warm off-white with the same accent.
- **Dynamic color (Material You):** optional toggle in Settings, off by default to keep identity consistent.
- Verify contrast ≥ 4.5:1 for text; ≥ 3:1 for large text and icons.

### 7.3 Typography
- Display/headings: a geometric sans (e.g. *Plus Jakarta Sans* or *Manrope*) bundled as a font resource (check licence: OFL).
- Body/UI: *Inter* or system default fallback.
- Numerals in the player (time, speed): tabular figures.
- Scale: Display 32/Headline 24/Title 18/Body 15/Label 12; respect user font-scale up to 200%.

### 7.4 Shape, elevation, spacing
- 8-pt grid. Corner radii: cards 20 dp, chips 12 dp, sheets 28 dp top corners, buttons full-pill or 14 dp.
- Prefer **tonal surfaces and thin borders** over heavy shadows in the dark vault.
- Subtle glass/blur effect only for the player overlay and top bars (blur on API 31+; solid translucent fallback below).

### 7.5 Motion
- Shared-element transition from grid thumbnail → photo/video viewer.
- Spring-based, 200–350 ms transitions; no jank; respect "Remove animations" accessibility setting.
- Skeleton shimmer placeholders while thumbnails load.
- Haptic feedback on selection, lock/unlock, errors (respect system haptic setting).
- Lottie is **not** required; build animations with Compose APIs.

### 7.6 Iconography
- Material Symbols Rounded (outlined) for UI; all custom icons (SIM, lock, etc.) as vector drawables **designed in-house**.

### 7.7 Loading, empty and error states
Every screen defines: loading (skeleton), empty (illustration + helpful action), error (plain-language message + retry), and offline-irrelevant (no network used).

---

## 8. Screens & Navigation (wireframes)

```
Splash (system SplashScreen API, neutral, no vault branding)
  └─ Cover Screen ──tap Jazz/Zong──▶ Auth (Biometric/PIN)
                                         └─▶ Post-Auth Panel
                                               ├─ Call helpline (dialer)
                                               ├─ Open SIM Toolkit
                                               └─ Open Vault ──▶ Vault Home
Vault Home
  ├─ Photos ─▶ Grid ─▶ Photo Viewer
  ├─ Videos ─▶ Grid/List ─▶ Video Player
  ├─ Audio  ─▶ List ─▶ Audio Player (+ Mini Player, Queue)
  ├─ Files (File Manager, Albums, Favorites, Trash)
  ├─ Storage (Dashboard, Gallery-Visibility Checklist)
  └─ Settings
First launch only: Onboarding (welcome → set PIN/password → choose auto-lock → "Set up access" permission checklist (Section 9.8: folder access, notifications, biometrics, optional battery) → choose storage mode → pick folder → scan)
```

### 8.1 Cover screen "SIM Service" (the disguise) — exact layout
This replaces any earlier cover design. Build it **exactly** like this:

```
┌──────────────────────────────┐
│ 12:43              ▂▄▆ 🔋    │  ← system status bar (black, light icons)
│ ←  SIM Service               │  ← top bar directly below status bar
│                              │
│                              │
│  ▯¹   Jazz               ›   │  ← row 1
│                              │
│  ▯²   Zong               ›   │  ← row 2 (vertically spaced below row 1)
│                              │
│                              │
│  (rest of the screen:        │
│   completely black, empty)   │
│                              │
└──────────────────────────────┘
```

**Top navigation bar** (sits immediately below the status bar, edge-to-edge, `#000000`, no elevation, no divider):
- Height 56 dp (plus the status-bar inset). Horizontal padding 4 dp for the back button, 8 dp before the title.
- **Left-pointing back arrow** (24 dp, white, Material "arrow_back" outlined/rounded, 48 dp touch target, ripple bounded/circular).
- **Title "SIM Service"** directly after the arrow: **bold** (700), clean sans-serif (Inter or system sans), 20 sp, white, single line, left-aligned, **not** centered. The title is a string resource (`sim_service_title`); do not call it "Vault", "Private" or "Safe".
- **Back arrow behavior:** closes the screen (finishes the activity), returning to the home screen or the previous app. It never requires authentication and never navigates elsewhere in the app.

**Menu list (upper-middle of the screen):**
- Two rows, stacked vertically with clear spacing (row height 64 dp, 12 dp gap between rows, 20 dp horizontal padding). The first row's top edge sits at roughly **20–25 % of the screen height** (below the top bar), so the list reads as "upper-middle".
- **Row 1:** tiny SIM-card icon (about 20 × 24 dp, white outline, own vector drawable) with the numeral **"1"** in its **bottom-right corner** (bold, about 9–10 sp, white), then 16 dp gap, then the text **"Jazz"** in white (17 sp, medium weight), and a **light-gray chevron "›"** (`#C7C7CC`, 20 dp, right-pointing) on the **far right** edge.
- **Row 2:** identical structure with the numeral **"2"** and the text **"Zong"**.
- Rows are full-width tap targets with a subtle pressed state (`#141416`) and a light haptic tick. No cards, no shadows, no gradients, no borders around rows. Optional hairline divider between rows (`#1C1C1E`) only if it still looks minimal.
- The carrier names are plain text labels. **No carrier logos or brand colors.**

**Everything below the list:** completely black (`#000000`) and empty. No buttons, no hints, no menu, no "help", no settings, no illustrations, no status text. The bottom (navigation) bar is black as well. This empty area must not contain hidden labels that screen readers announce as "vault".

**Behavior:**
- Tapping **Jazz** or **Zong** starts authentication (`BiometricPrompt` with PIN fallback, Section 10.5) **before any action**. On success, show the post-auth panel (Section 8.2) for that carrier.
- Unlocking from either row unlocks the whole app session.
- The screen uses edge-to-edge drawing: status-bar and navigation-bar icons are light on black; insets are respected so the top bar never overlaps the clock or camera cutout. Works in portrait and landscape (in landscape the list keeps the same left alignment and the empty area stays black).
- Always dark: the screen ignores the system light/dark theme and the vault's theme setting.
- Entry animation: a quick (150 ms) fade-in of the two rows with a 4 dp upward settle; reduced to none when "Remove animations" is on.
- **Optional alternate unlock (off by default):** a 2-second long-press on the title text. The normal tap flow must always work. Nothing visual indicates this gesture.
- There is **no overflow (⋮) menu, no Help, no Settings and no Recents list** on this screen. Settings are reachable only after authentication so a bystander can't discover anything else in the app.

### 8.2 Post-auth panel
A bottom sheet or small page with three large, clear actions:
1. **Call helpline** (dialer opens with pre-filled number; never auto-dials)
2. **Open SIM Toolkit** (if installed)
3. **Open Vault** (primary, gradient button)

Remembers last choice if the user enables "Go straight to Vault after unlock" in Settings (off by default).

### 8.3 Vault Home
- Large header with greeting-free, minimal style; storage ring showing used/free on the SD card.
- Three big **section cards** — Photos, Videos, Audio — each showing count, total size, and a **live mosaic preview** (4 latest thumbnails) with a gentle parallax.
- Quick actions row: Import, Search, Favorites, Trash.
- "Recently added" and "Continue watching/listening" horizontal carousels.
- Bottom navigation (Home, Library, Files, Settings) **or** a floating glass pill nav — pick one and keep it consistent.

### 8.4 Library grids
- **Photos:** adaptive grid (2–6 columns via pinch-to-zoom on the grid), date group headers (sticky), fast-scroll bar with date bubble, select mode with drag-to-select, thumbnails 1:1 or original aspect (toggle).
- **Videos:** thumbnail with duration badge and a thin **resume progress bar**; grid or list toggle; resolution badge (HD/4K) where known.
- **Audio:** list with album art (embedded art via `MediaMetadataRetriever` or a generated gradient placeholder), title, artist, duration; tabs for Tracks / Albums / Artists / Folders.
- Top bar: search, sort, filter, view toggle, select.
- Sort: name, date added, date taken/modified, size, duration, type. Asc/desc.
- Filter chips: Favorites, Recently added, Large files, Unplayed, By format, By album, By tag.

---

## 9. Security & Authentication

### 9.1 Credentials
- PIN (4–8 digits) **or** password (alphanumeric, min 6). User chooses at setup; can change later.
- Hash with **PBKDF2-HMAC-SHA256**, unique random salt (16+ bytes), iterations **calibrated on-device** (target ~250–500 ms, store iteration count). Constant-time compare. (Argon2id via a maintained library is acceptable if size/licence are fine.)
- Never store the PIN in plain text, logs, or backups.

### 9.2 Biometrics
- `BiometricPrompt` with `BIOMETRIC_STRONG` and PIN always available as fallback.
- Optional: bind a Keystore key to biometrics; if enrollment changes, require PIN re-entry once.

### 9.3 Lockout / rate limiting
- After 5 wrong attempts: 30 s, then 1 min, 5 min, 15 min, capped at 15 min. Persist counters across process death. Never permanent lockout.
- Show a calm countdown, not alarming language.

### 9.4 Session & auto-lock (user-selectable)
- Unlocking via either carrier row unlocks the whole session.
- **Auto-lock setting** (Settings → Security → Auto-lock), single choice, shown as radio rows with a one-line explanation under each:
  1. **Immediately:** locks the moment the app leaves the foreground (home, recents, another app, screen off).
  2. **After 1 minute**
  3. **After 2 minutes**
  4. **After 5 minutes**
  5. **After 30 minutes**
  6. **Until the app is closed:** after the user enters the password once, they can keep using the app (including leaving and coming back, switching apps, screen off) with **no further prompts until the app is closed**. When the user closes the app and opens it again, the password/fingerprint is required.
- **Default: After 1 minute.** It is pre-selected in onboarding (with a short explainer) and applied after "Reset app"; the user can change it any time in Settings.
- **How the timer works:** record `backgroundedAt` using `SystemClock.elapsedRealtime()` (not wall-clock, so changing the phone's clock can't bypass it) when the last activity stops; on return, compare elapsed time with the chosen limit and lock if exceeded. Do not rely on a coroutine timer alone (it dies with the process).
- **"Until the app is closed" semantics (be exact and honest):**
  - The unlocked state lives **only in memory** (an `AuthSession` singleton). It is never persisted, so it cannot survive the app being closed.
  - "Closed" means: the user swipes the app away from Recents, uses the in-app **Exit/Lock & close** button, the system kills the process (low memory), the phone reboots, or the app is force-stopped. Handle `onTaskRemoved` in the service and clear the session.
  - Because Android may kill background apps at any time, the user can occasionally be asked for the password even though they didn't close the app. Say this in the setting's helper text.
  - While this mode is active, show a small, persistent **"Lock now"** button in the vault top bar and in the media notification so the user can still lock instantly.
- **Always-applied rules, whatever the setting:**
  - Cold start (fresh process) **always** requires authentication.
  - After a **device reboot**, authentication is always required.
  - After **N failed attempts**, lockout applies (Section 9.3).
  - Changing the PIN/password, resetting, or changing security settings requires re-authentication even if the session is unlocked ("sensitive action re-auth", valid for 2 minutes).
- **Media playback exception (configurable):** music audio may continue in the background with lock-screen controls while the **app UI** is locked; the notification shows neutral text (hide-title option, default on). A toggle **"Stop playback when the app locks"** (default off) lets the user choose. **Separate from this, when the whole phone is locked, video and slideshows always pause (Section 33.3).** A second toggle **"Don't auto-lock while a video is playing in the foreground"** (default on) prevents locking mid-movie.

### 9.5 Screen privacy
- `FLAG_SECURE` **on by default** (blocks screenshots, hides content in recents) with a Settings toggle.
- Notification content hidden on lock screen by default.
- Recents thumbnail shows the cover screen or blank.

### 9.6 Forgot PIN
Offer **"Reset app"**: clears the app's database, settings and credentials **only**. **Media files on the SD card are never touched.** The user re-selects the folder and re-indexes. State plainly: because Hidden Mode isn't encryption, a reset doesn't expose anything an attacker couldn't already read from the card. Require fresh authentication (or the forgotten-PIN path) and a final confirmation; the full flow is in **Section 37.4**.

### 9.7 Optional extras (build if time allows, flagged in brain.md)
- **Decoy PIN (P3, `Deferred` unless real/decoy state separation is proven, Section 9.10):** a second PIN opens a harmless empty/benign state.
- **Intruder log + intruder photo:** full specification in **Section 32** (wrong-password photo, in-vault storage, alert on the owner's next successful login). Opt-in; needs the optional camera permission.
- **Panic gesture:** quick double-shake or a fast back-press sequence returns to the cover screen and locks.
- **Disguise icon/name** choices (Section 10.6).

### 9.8 First-launch permission setup ("ask for everything the app needs, once, properly")
On the **first launch**, right after the PIN is created, show a dedicated **"Set up access"** step (a friendly checklist, not a wall of system dialogs). Each item has an icon, a one-line reason, a status chip (Needed / Granted / Skipped) and a button. The user can continue with items skipped, and every item can be fixed later from **Settings → Permissions**.

| Item | Why the app needs it | How it's requested | Required? |
|---|---|---|---|
| **Folder access (SD card)** | Read, play and manage your media in the chosen folder | SAF folder picker (`ACTION_OPEN_DOCUMENT_TREE`), then `takePersistableUriPermission`. Guide the user to pick the `new` folder or the vault folder (Android 11+ doesn't allow the SD card root). Show a short illustrated how-to. | **Yes**, the app cannot work without it |
| **Notifications** (Android 13+) | Import/scan progress and audio playback controls | `POST_NOTIFICATIONS` runtime request after a short rationale screen | Strongly recommended; app works without it, but playback/import progress won't show |
| **Fingerprint / face unlock** | Faster unlocking | Check `BiometricManager.canAuthenticate`; if not enrolled, offer a shortcut to system security settings (`ACTION_BIOMETRIC_ENROLL` on API 30+) | Optional |
| **Battery optimization exemption** | Keeps long imports and background audio from being killed | Explain, then open the system battery-optimization screen (use `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` **only** if the platform policy allows for this app type; otherwise just open the settings screen and explain) | Optional, off by default |
| **Display over other apps / full-screen** | **Not requested.** | n/a | No |

Rules:
- **Request only what the app truly uses.** Do not request storage, phone, SMS, contacts, location, microphone or all-files access. State this on the screen: "This app never asks for internet, contacts or location."
- **Camera is the one extra, and it is optional:** add a row **"Camera (intruder photo)"** to the checklist table above. It is **not** requested during first-launch setup; it is requested the moment the user switches on "Intruder photo" (Section 32), with a rationale screen first. Status chip: Off / Granted / Denied.
- Show the rationale **before** the system dialog. If the user denies, show what stops working and a button to retry; if "Don't ask again" was triggered, deep-link to the app's system settings page.
- Handle permission revocation at any time (e.g. folder permission lost after SD card change): show a gentle "Access needed" screen with the fix, never a crash.
- A **Permissions status card** in Settings shows live state for each item (Granted / Not granted) with one-tap fix.
- Re-run the checklist automatically after an app update that adds a new permission.

### 9.9 More options (Settings → Security & Behavior)
*This list is the **candidate catalogue**. Every option must pass the registry rules in Section 36 and carry a priority from Section 2.2; anything not working is hidden or shown as Unavailable, never as a dead switch.*
Group into clear sections; each option has a title, a one-line description and a safe default.

**Locking & unlocking**
- Auto-lock timing (Section 9.4).
- **Lock when screen turns off** (default on; forced on unless "Until app is closed" is chosen, where it's a toggle, default off).
- **Lock now** shortcut: button in the vault top bar, in the notification, and as an optional **Quick Settings tile**.
- **Lock on shake** (optional, adjustable sensitivity, default off).
- **Lock on volume-button combo or power-button double press** is **not** included (not reliably possible; do not promise).
- **Fingerprint-only for quick unlock, PIN every N unlocks:** require the PIN again after every 5/10/20 biometric unlocks (default 10) or after 72 hours.
- **Require PIN instead of biometrics** (turn biometrics off).
- **Require password after reboot** (always on, shown as info).
- **Re-authenticate to open Settings** (default on).
- **Re-authenticate to delete permanently / empty trash / export** (default on).

**Keypad & input**
- Shuffle keypad digits (default off), hide PIN dots (show only a pulse), haptic feedback, vibration on wrong PIN, large-keypad mode for accessibility.
- Password strength meter and rule hints when creating/changing the password; block trivial PINs (0000, 1234, repeated digits) with a warning (user may override).
- **PIN length** 4–8 or alphanumeric password; switch type from Settings.

**Privacy**
- Screenshot/recording protection (`FLAG_SECURE`, default on).
- Hide app content in Recents (default on).
- Hide notification content on lock screen (default on).
- Hide media names in notifications and in "Continue watching" on the home screen (default off).
- **Blur thumbnails until tapped** ("Privacy blur" for grids, default off).
- **Hide section counts** on the home screen (default off).
- **Clear thumbnail cache on lock** (default off; trades speed for privacy).
- **Clear recent searches and "recently played" on lock** (default off).
- Disguise launcher name/icon (Section 10.6).
- Return to the cover screen (not the vault) when the app is relaunched from Recents.

**Safety nets**
- **Auto-exit idle timer on the vault** (separate from background auto-lock): lock after X minutes of no touches while the app is in the foreground and nothing is playing (Off / 5 / 10 / 15 minutes, default off).
- **Intruder log and Intruder photo** (Section 32): time, attempt count, which entry point, and an optional front-camera photo; local only, viewable after unlocking, with an alert on the next successful login.
- **Failed-attempt alert banner** on next successful unlock: "3 wrong attempts at 14:02" (default on).
- **Decoy PIN** (Section 9.7).
- **Reset app** (Section 9.6).
- **Backup of settings** (without secrets) as a local file, optional.

**Playback & behavior**
- Resume where you left off (on/off), auto-play next, background audio on/off, keep screen on during video, remember brightness per session, default orientation, default seek step, haptics, gesture toggles.
- **Headphone-disconnect protection** (stop + mute, default on) and **phone-lock pause** (Section 33), **pause when another app takes audio focus** (default on).
- **Confirm before delete** (default on), **trash retention** (7/30/90 days/never), **"Ask where to save" for exports** (default on).
- **Scan on app open** (default on), **scan only on Wi-Fi** is **not applicable** (no network), **scan hidden sub-folders** (default on).
- **Data saver for thumbnails** (lower quality thumbnails, default off), **thumbnail cache limit** (256 MB / 512 MB / 1 GB / 2 GB).

### 9.10 Security model clarity & hardening rules
**Three different things — never blur them in the UI or docs:**
1. **Hidden Mode / cover screen:** changes what is displayed before authentication.
2. **App authentication and session locking:** restricts access through *this app's UI*.
3. **Encryption at rest:** protects file contents if the card is read outside the app. **Not implemented in this release (`Deferred`, Section 25).** A `.nomedia` file, hidden folder name, disguised launcher or app lock does not encrypt anything.

**Authentication rules**
- One coherent authentication state machine. Lockout and attempt limits are enforced in **application logic**, not just by disabling a button, and **survive activity recreation and process death**.
- Use a monotonic clock (`elapsedRealtime`) for in-process timeouts. After a **reboot** the monotonic clock resets, so persisted lockouts fall back to wall-clock time but **never shorten a lockout because the user moved the clock back**.
- The sensitive-action re-authentication window (2 minutes, Section 9.4) is **one centralized constant with a test**.
- Never log credentials, PINs, biometric results, private file names or sensitive URIs.
- Never store a PIN in plaintext; use the salted, calibrated PBKDF2 scheme in 9.1. Do not invent cryptography. A password hash is **not** an encryption key; any future credential-derived key needs a separate design.
- Biometrics are a convenience: define behavior for no hardware, not enrolled, enrollment changes, lockout and when the PIN is required again.

**App switcher, notifications and temporary data**
- `FLAG_SECURE` is on by default, but it does not solve every task-snapshot case on every Android version. Also draw a neutral cover/blank surface when the app goes to Recents, and call `setRecentsScreenshotEnabled(false)` on Android 13+.
- Notifications use neutral text by default; never put media titles, private paths, photos, credentials or intruder details in notifications or logs unless the user explicitly picks a clearly explained less-private option.
- Clear or protect temporary files, thumbnails, clipboard content and caches per the privacy settings. Treat temporary exports and share files as sensitive (Section 37.2).

**Disguise and decoy honesty**
- A disguised name/icon is a **visual disguise only**. It stays visible in Settings → Apps, storage screens, permission screens and launcher management. Never claim it is undetectable.
- A decoy PIN must not reveal the real vault through navigation, notifications, shared state, recent tasks or error messages, and must **never erase real content**. If real and decoy state cannot be separated reliably, **defer it** instead of shipping a superficial screen.

---

## 10. Cover Screen & Carrier / SIM Flow

### 10.1 Rules
- Tiny own-designed SIM icons only; **no carrier logos**, no "official" wording.
- Rows: **Jazz** and **Zong**, plain white text labels on black (Section 8.1).
- Tapping a row → authenticate → post-auth panel. No carrier action and no vault access before success.

### 10.2 Helpline calling
- Use `Intent.ACTION_DIAL` with a pre-filled `tel:` URI. **Never auto-call.** No call permission.
- Numbers are **user-editable in Settings** (after auth). Pre-fill only with numbers the builder has verified from the carriers' **official websites at build time**; if not verified, leave empty and prompt the user to enter one. Do not hardcode unverified numbers or USSD codes. If USSD is supported later, encode `#` as `%23` and let the system dialer handle it.

### 10.3 SIM Toolkit
- On many devices the SIM Toolkit is an installed launchable app (commonly package `com.android.stk`, `com.android.stk2` for a second SIM, plus vendor variants).
- Declare candidates in `<queries>`, resolve with `PackageManager.getLaunchIntentForPackage`, and launch **only if found**.
- If not found: show "SIM Toolkit isn't available on this device" and offer the dialer. **Never draw a fake SIM Toolkit menu.**
- Handle: no SIM, dual SIM (let the system choose), missing dialer, and OEM-specific names. Make the candidate package list editable in a hidden "Advanced" screen.

### 10.4 "Looks normal but very good" checklist
- Minimal and modern: high-contrast white-on-black text, tiny simple icons, generous spacing, a 4/8 dp rhythm, and nothing decorative. Restraint is the design.
- Rows have a soft pressed highlight and haptic tick; no scale/bounce animations on this screen.
- Localised strings (English now, Urdu-ready); the screen stays dark in every theme and supports font scaling up to 200 % without clipping (rows grow in height, never truncate "Jazz"/"Zong").
- TalkBack: rows read "Jazz, SIM 1, button" and "Zong, SIM 2, button"; the back arrow reads "Back". No vault wording anywhere.
- Never any mention of "vault", "hidden", or "private" on this screen, in its accessibility labels, or in the Recents title.

### 10.5 Auth gate details
- Biometric prompt title/subtitle should be neutral: e.g. "Confirm it's you" / "Required to continue".
- If biometrics unavailable → PIN keypad (custom, large touch targets, shuffle-keys option, haptics, dots animation).

### 10.6 Disguise options (optional)
- (See also Section 10.7 for the launcher icon.)
- Alternate launcher aliases (default launcher label **"SIM Service"** with the SIM icon from Section 10.7; optional alternates such as "Utilities", "Calculator-style" is **not** to be faked as functioning calculator unless fully built). Changing alias may need app restart; explain this.

### 10.7 App icon ("SIM Service")
**Concept:** a modern, minimalist icon that looks like a plain SIM utility. A master vector is provided (`sim_service_icon.svg`; source also in Appendix A). Use it as the visual reference and rebuild it as native Android vectors.

**Composition (1024 × 1024 master):**
- **Backdrop (presentation/store art only):** dark charcoal `#2E3035 → #1C1D21` diagonal gradient. It is **not** part of the launcher icon the OS draws.
- **Container:** a smooth, light-gray **squircle** (about 70 % of the canvas, corner radius about 29 % of its width) with a soft top-left-to-bottom-right gradient `#F1F2F4 → #D9DCE1 → #BFC3CA`, a faint 3 px white inner stroke at about 55 % opacity, and a soft, low drop shadow.
- **SIM card:** centered, **matte black** (`#232427 → #0B0B0D`), portrait, about 45 % of the container's width, rounded corners (about 20 px at 1024), with the characteristic **angled top-right corner notch** (a 45° cut of about 80 px). A very faint inner outline (`#3A3C41` at 60 %) gives depth without gloss.
- **Chip:** at the exact center of the SIM card, a **metallic gold-and-orange contact pad** (190 × 170 px, 28 px corners) with a diagonal gradient `#FFE08A → #F7B84A → #EE8E2A → #C96A16`, a thin darker amber edge, a top highlight line, a soft sheen band, and **soft sub-divided segment lines** (`#8A4710` at about 60 %): two vertical and two horizontal dividers forming six outer pads around a rounded inner pad.

**Android implementation:**
- **Adaptive icon** (`mipmap-anydpi-v26/ic_launcher.xml`, plus `ic_launcher_round.xml`):
  - **Background layer:** the light-gray squircle gradient painted full-bleed (the OS applies its own mask shape).
  - **Foreground layer:** the black SIM with gold chip as a vector drawable, scaled so all content sits inside the central **66 %** safe zone of the 108 dp canvas.
  - **Monochrome layer** (Android 13+ themed icons): a single-color silhouette of the SIM outline with the chip segment lines as cut-outs.
- **Legacy fallback** (API < 26): PNGs at mdpi–xxxhdpi containing the full squircle composition on a transparent background.
- **Play/sideload listing art:** 512 × 512 PNG exported from the master SVG, with the charcoal backdrop.
- Verify the icon at 48 dp, 36 dp and 24 dp (notification/small sizes): the notch, chip and squircle must stay recognisable. Do not add text to the icon.
- The icon must **not** use any carrier logo, color or name. The app label is "SIM Service" (string resource, easy to change).
- The splash screen reuses the same icon on a black background (`windowSplashScreenBackground = #000000`), with no vault branding.

---

## 11. Storage Model

### 11.1 Three modes (selected during onboarding; changeable)
1. **Index in place (recommended default):**
   - The user picks `new` (or any folder) via `ACTION_OPEN_DOCUMENT_TREE`; persist permission with `takePersistableUriPermission`.
   - The scan itself is **read-only**. The app then offers a separate **"Hide from gallery"** step: after the user confirms, it creates `.nomedia` in the folder (and subfolders if "hide subfolders too" is on), **never overwrites an existing one**, and **verifies** the result (Section 14.8).
   - Scan recursively; build the Room index. **No files are copied, moved or deleted.**
   - Tell the user: files remain readable by file managers and USB.
2. **Copy to vault folder:** user creates/picks a destination (e.g. `Android-style` hidden folder such as `SD/.pv_data/`) — requires free space; warn if insufficient.
3. **Move to vault folder:** use `DocumentsContract.moveDocument` when the provider supports it on the same volume (instant, no duplication); else verified copy-then-delete with explicit confirmation and per-file verification (size + checksum).

### 11.2 The "paste files into a new folder" workflow the user wants
- Offer a **"Create vault folder"** button: the user picks a parent folder on the SD card; the app creates a subfolder (default name `.private_vault`, dot-prefixed so it's hidden in many file managers by default — **tell the user how to show hidden files**) containing a `.nomedia` file.
- User then pastes files into this folder using any file manager; the app's **Watch/Rescan** detects them (manual "Rescan" and optional periodic scan when the app opens).
- Clarify: a folder under `Android/data/<app>` on the SD card is **not** recommended because uninstalling the app deletes it and file managers can't reliably open it on Android 11+.

### 11.3 Scanning
- Recursive SAF walk using `DocumentsContract.buildChildDocumentsUriUsingTree` + cursor queries (**much faster than DocumentFile**; use DocumentFile only for convenience paths).
- Classify by MIME type and extension fallback.
- Extract metadata in the background: dimensions/orientation (EXIF), duration, codec/resolution (`MediaMetadataRetriever`), title/artist/album/art for audio.
- Incremental scans: compare `lastModified` + size; add, update, mark missing.
- **Reconciliation:** files removed outside the app appear as "Missing" (greyed) with options to remove from index; never silently delete user data.
- Skip: files starting with `._`, tiny system files, `.thumbnails`, trash dir.

### 11.4 SD card specifics
- Show card volume info (total/free) via `StorageManager`/`StorageVolume` and the SAF tree.
- Detect and warn: FAT32 4 GB limit, read-only mount, card removed (graceful "SD card not available" screen with retry; never crash; keep the index).
- Handle card re-insertion / URI permission loss: guide user to re-grant.

### 11.5 Thumbnails & caching
- Generate thumbnails lazily and cache them in the app's private cache (`cacheDir`) with LRU limits (user-configurable cap, default 512 MB). Never write thumbnails to the SD card.
- Cache can be cleared in Settings → Storage.

---

## 12. Media Viewing & Playback

### 12.1 Photo viewer (premium)
- Full-screen pager with swipe navigation, shared-element open animation, preloading neighbors.
- **Zoom/pan** (pinch, double-tap to zoom, fling), rotate (90°, **non-destructive** preview; "Save rotation" optional), **flip**.
- Fit / Fill / Original size toggles; background tint option (black / blurred image).
- **Info panel:** name, path, size, resolution, date, camera/EXIF (hide GPS by default with a "Show location" tap; **offer "Remove location" is out of scope** unless copying).
- Slideshow (interval, shuffle, transition effect), swipe-down to close, swipe-up for info.
- Respects EXIF orientation. Supports JPEG, PNG, WebP, GIF (animated), HEIC/HEIF (device-dependent), BMP, AVIF (API 31+), RAW as "open externally".
- Actions: favorite, share (temporary read grant), open with, rename, move to album, delete to trash, set as "Cover" for album.
- Edge handling: very large images use tiled/subsampled decoding to avoid OOM.

### 12.2 Video player (the showpiece)
**Engine:** Media3 ExoPlayer. **Be honest about formats** (see 12.4).

**Layout & controls (overlay auto-hides after 3 s; tap to toggle):**
```
┌──────────────────────────────────────────┐
│ ←  Title.mp4            CC  ⚙  ⋮         │ top bar (glass)
│                                          │
│    ⏪10        ⏯ (big)        10⏩        │ center controls
│                                          │
│ 00:42 ━━━━●━━━━━━━━━━━━━━━━━━ 12:08      │ seek bar with thumbnails preview
│ 🔒  ⏮  ▶  ⏭   1.0x   ⛶ fit   🔄   PiP   │ bottom bar
└──────────────────────────────────────────┘
```

**Gestures (all with on-screen indicators and haptics):**
- **Double-tap left/right:** seek −/+10 s (configurable 5/10/15/30), with ripple + cumulative counter.
- **Vertical swipe on left half:** brightness (window brightness only; restore on exit).
- **Vertical swipe on right half:** volume (`AudioManager`); show volume HUD; allow boost up to 200% **only if** `LoudnessEnhancer` is used and clearly labeled.
- **Horizontal swipe:** scrub with time preview.
- **Long-press:** temporary 2x speed (release returns); slide up/down while holding to fine-tune.
- **Pinch:** zoom/fill; double-finger tap = reset.
- **Single tap:** show/hide controls. **Lock button:** disables all gestures and hides controls (long-press the lock to unlock).

**Features:**
- Playback speed 0.25x–4x (presets + slider), pitch correction on.
- **Orientation:** Auto / Portrait / Landscape / Reverse landscape / Follow sensor; smart auto-landscape for wide videos; remember per-user setting. Layout adapts for **both vertical and horizontal viewing** and tablets/foldables.
- **Aspect modes:** Fit, Fill/Crop, Stretch, 16:9, 4:3, 21:9, Original — cycle button.
- **Resume position** per file (Room), "Continue watching" carousel, auto-mark watched at 95%.
- **Subtitles:** load sidecar `.srt/.vtt/.ass`(if supported) from the same folder, or pick a file; style options (size, color, background, offset ±); embedded track selection.
- **Audio tracks** selector (multi-language), audio delay adjust (±).
- **Playlist/queue:** play next in folder, shuffle, next/previous.
- **Loop (repeat) modes:** full specification in **Section 33.1** (Off / Loop one / Loop all, A-B loop, loop count, remembered per item).
- **Keep screen on** during playback; **sleep timer**.
- **A-B loop**, frame-step (when paused), screenshot of current frame (saves **into the vault**, not the gallery).
- **Picture-in-Picture** (optional, off by default for privacy; if enabled, auto-lock rules still apply).
- Seek-bar **thumbnail previews** (generate sprite or use `MediaMetadataRetriever` at intervals; cache).
- Video info sheet: container, codecs, resolution, fps, bitrate, size, duration, HDR flag.
- Error screen with **real** error: container, codec, reason, and action buttons (Open with another player, Remove from index, Details).
- Audio focus handling; Bluetooth/media-button support. **Headphone disconnect and phone-lock behavior are defined in Section 33** (stop + mute volume to 0; pause video when the phone locks).
- Caption: Cast/network features are **not** included.

### 12.3 Audio player
- **Full player screen:** large artwork with blurred-artwork background, seek bar, shuffle/repeat, speed, sleep timer, queue sheet, lyrics (local `.lrc` sidecar only, optional), equalizer (system `Equalizer` API if available, optional).
- **Mini player** docked above bottom nav, swipe up to expand, swipe to dismiss.
- **Background playback** via Media3 `MediaSessionService` with notification and lock-screen controls (title hidden option).
- Gapless playback; fade-in/out; audio focus; resume position for long files (podcasts/audiobooks flag at > 20 min).
- Groupings: Tracks, Albums, Artists, Folders, Favorites, Recently played.
- Waveform seek bar is optional (only if time permits and verified).

### 12.4 Video format strategy (honest)
- **Primary:** Media3 ExoPlayer — plays MP4, MKV, WebM, MPEG-TS, FLV, 3GP, MOV, and more, **subject to device hardware codecs**.
- **Optional:** Media3 FFmpeg decoder extension **only if** the user builds it and accepts LGPL obligations and larger APK; document ABIs.
- Some containers/codecs (e.g. AVI, WMV, exotic codecs) may fail. Provide a genuine **"Open with another player"** using a content `Intent` with a temporary read grant, and tell the user it leaves the vault.
- **Never** state "supports all formats." Show the real failure and the fallback.

### 12.5 Accessibility for players
- All controls have content descriptions, ≥ 48 dp touch targets, TalkBack-friendly order, captions support, and a setting to disable gestures.
- **Every gesture has an equivalent visible control**, and no gesture may conflict with system back/navigation gestures or accessibility services.

---

## 13. Library Features

- **Search:** name, folder, tag, album, extension; instant results with debounce; recent searches (local, clearable).
- **Favorites**, **Albums** (virtual collections stored in Room, **no file movement**), **Tags** (user-defined, colored).
- **Smart collections:** Recently added, Large files (> 100 MB, configurable), Long videos, Never opened, Possible duplicates.
- **Multi-select:** long-press to enter, drag-select across grid, select all/none/invert; contextual action bar with count.
- **Batch actions:** favorite, add to album, tag, move, copy, share, export, delete to trash.
- **Grouping:** by day/month/year/folder/type.
- **Statistics screen:** counts and sizes per type, biggest files, storage trend (simple).

---

## 14. File Management (proper, safe, explicit)

All destructive actions go through a single, tested `FileOperations` layer using SAF. Every operation reports **success / partial / failure per item**.

### 14.1 Operations

| Operation | Behavior |
|---|---|
| **Rename** | Validates name, extension kept by default, conflict handling (skip/replace/keep both), updates index. |
| **Move** | Within same volume: `moveDocument`; else copy-verify-delete. Updates index, albums, favorites, resume data. |
| **Copy** | Streams with buffer, progress, cancel, free-space check, size + checksum verify (optional "fast verify" = size only). |
| **Delete** | **Soft delete to in-vault Trash** (a `.pv_trash/` folder next to the vault root containing `.nomedia`). |
| **Restore** | Returns to original location (or lets user choose if gone). |
| **Empty trash / permanent delete** | Requires confirmation with item count and total size; optional "auto-empty after 30 days" (default off). |
| **Create folder** | Within the vault tree; folders appear in the Files view. |
| **Share/Open with** | Temporary read grant via content URI; warns it leaves the vault; shows what apps. |
| **Export (un-hide)** | Copy or move selected files **out** to a user-chosen folder (e.g. back to DCIM) so the gallery can see them; explicit confirmation. |
| **Import** | From the system picker (multi-select) into the vault using the active storage mode. |
| **Properties** | Path, size, type, dates, checksum on demand, index status, `.nomedia` status. |

### 14.2 File Manager screen
- Breadcrumb path bar, list/grid toggle, sort/filter, folder sizes (calculated lazily, cached).
- Shows **hidden status** per folder (`.nomedia` ✓/✗) with a one-tap "Fix" that creates it.
- Drag-and-drop between folders inside the app (long-press → drag) **or** "Move to…" picker for reliability.
- Handles name collisions, illegal characters, very long names, Unicode (including Urdu) names.

### 14.3 Integrity & safety
- **Verify** screen: compares index vs. disk; lists Missing / New / Changed; one-tap resolve.
- **Duplicate finder:** group by size then partial hash then full hash (background, cancelable); user picks which to keep; never auto-delete.
- **Dry-run summary** before batch Move/Delete: "You're about to move 148 files (3.2 GB) …".
- **Undo** via Trash for deletes; **operation log** (local) viewable in Settings → Advanced.
- Never modify original file contents (no rewriting EXIF, no re-encoding) unless the user explicitly invokes an edit feature.

### 14.4 Operation semantics (labels must match behavior)

| Operation | What it does | Touches the source file? |
|---|---|---|
| **Index** | Records metadata/reference in the app database | **No** |
| **Copy to vault** | Creates a separate copy; original stays | No |
| **Move to vault** | Transfers content only after a verified transfer (14.5) | Yes, deletes only after verification and consent |
| **Remove from library** | Removes the app's index entry only | **No** |
| **Move to Trash** | Moves into the app's trash folder (14.7) | Moves (reversible) |
| **Delete permanently** | Removes the selected file after confirmation + fresh re-authentication | Yes |
| **Export** | Creates a user-chosen copy outside the vault | No |
| **Share** | Grants a temporary content URI to the receiving app | No |
| **Reset app** | Clears app-owned settings/database/credentials only | **Never deletes media** |

Never label something "Move" if it only removes an index entry. Never use vague labels such as "Secure", "Clean" or "Move safely".

### 14.5 Copy-verify-delete protocol (when no reliable atomic move exists)
1. Validate source access, destination access, free space (where measurable) and destination naming.
2. Create a unique **operation record** and a temporary destination file.
3. Copy with bounded buffers and cancellation-aware I/O.
4. Flush/close the destination and **verify** (size always; content hash or byte-for-byte compare when affordable).
5. Rename/finalize the destination where the provider supports it.
6. **Only after** the destination is verified **and** the user authorized the move, delete the source.
7. Verify the deletion result where possible.
8. Mark the operation complete only when the resulting state is known.
9. On **any** failure: keep the source, keep enough metadata to recover, and tell the user what happened.
**Never delete the source just because a copy call did not throw.** If safe verification is impossible, keep the original and report that the move could not be completed safely.

### 14.6 Operation journal & recovery
Persist a journal (Room) for long or destructive operations: unique ID, type, source/destination references, start time, **stage**, result and a sanitized error category (no credentials, no unnecessary private content).
Handle at least: process death mid-operation; user cancel; SD card removed; permission revoked mid-transfer; read-only destination; insufficient space; name collision; provider refusing rename/delete; corrupt/unreadable source; source changing during transfer; **double-tap duplicate operation**; leftover partial destination; **database write failing after a file-system step**.
On restart, **reconcile the journal with the real source and destination state**. Never blindly retry a destructive step; offer a clear recovery action (Resume / Roll back / Discard) and preserve recoverable originals.

### 14.7 Trash semantics (document what is actually true)
- **Location:** `.pv_trash/` inside the vault root on the same volume (with its own `.nomedia`). Items are **moved** there (the original file is not copied and left behind).
- Use `DocumentsContract.moveDocument`/rename when the provider supports it. **If a provider cannot move reversibly, trash is unavailable for that item** (or uses the staged protocol in 14.5 with confirmation); never claim reversible deletion that has not been verified on that provider.
- Actions: Restore, Delete permanently, Empty trash. **Permanent delete and Empty trash require confirmation and fresh re-authentication**, and show the item count and total size first.
- Retention auto-purge acts **only on items the app itself put in trash**, and **never runs** when permission is lost or the card is absent.

### 14.8 Hidden folders & `.nomedia` rules
- A dot-prefixed folder and `.nomedia` may reduce media-scanner visibility; **neither is encryption or a guarantee**. Do not promise every gallery, file manager, backup service or system indexer will honor `.nomedia`.
- **The initial scan never creates, moves or modifies files.** "Hide from gallery" is a separate, user-confirmed step that first **checks whether a `.nomedia` already exists**, never overwrites it, and tells the user what changes.
- Explain the consequences *before* the user chooses a storage organization that changes where files are kept.

### 14.9 Scan/index quality
- Scans are incremental and cancellable; never load the entire library into memory.
- De-duplicate by stable document reference (and a carefully chosen identity strategy), **never by display name alone**.
- Handle nested and overlapping selected folders, duplicate names, unsupported formats, unreadable items and stale entries.
- Distinguish **scanned / indexed / skipped / errored / cancelled**. **A failed scan never erases a previously valid index.** No fake progress or fake "scan complete".
- Reuse a valid cached thumbnail instead of regenerating, while respecting the privacy cache settings.

---

## 15. Gallery Visibility ("make it disappear" — the realistic procedure)

- `.nomedia` is honored by Android's media scanner and most apps reading MediaStore (Google Photos' device folders, Instagram/WhatsApp/Facebook pickers, most galleries). It is **not** honored by apps that browse the file system directly or by the system file picker's folder view. **Say so in-app.**
- Files already indexed may remain visible until the index refreshes. Provide a **"Gallery Visibility Checklist"** screen:
  1. App verifies `.nomedia` exists in the folder (and reports yes/no per folder).
  2. Restart the phone (normally triggers a rescan respecting `.nomedia`).
  3. If items still show: Settings → Apps → Media Storage (and Google Photos) → clear cache.
  4. Make sure Google Photos **backup** is off for this device folder; anything already uploaded stays in the cloud until deleted there.
  5. Re-open the picker in Instagram/WhatsApp to verify.
- The app must **not** delete MediaStore rows or request all-files access to force this. Never claim success; only report "`.nomedia` present: yes/no" and what the user should check.
- Apps with their own all-files permission, plus file managers, can still see the files. This matches the user's requirement that the file manager can still reach them.

---

## 16. Settings (reachable only after auth)

- **Security & Behavior:** change PIN/password, biometrics toggle, **auto-lock (Immediately / 1 min / 2 min / 5 min / 30 min / Until app is closed)**, lock-when-screen-off, Lock now, shake-to-lock, PIN every N biometric unlocks, FLAG_SECURE, hide notification content, privacy blur, decoy PIN, intruder log, "Go straight to Vault" and everything in Section 9.9.
- **Permissions:** live status and one-tap fix for folder access, notifications, biometrics and battery optimization (Section 9.8).
- **Storage:** active mode, folder(s), rescan, cache size/clear, trash settings, FAT32/exFAT info.
- **Playback:** default seek step, gesture toggles, default speed, orientation, subtitle style, background audio, PiP.
- **Appearance:** theme (System/Light/Dark/AMOLED), accent, dynamic color, grid density, thumbnail shape, language.
- **Carrier:** edit helpline numbers, SIM Toolkit package list.
- **Privacy & About:** the honest explanation of Hidden vs Encrypted; open-source licences; version; reset app.
- **Advanced:** operation log, export/import settings (no secrets), diagnostics report (local text, user-shareable).

---

## 17. Long-Running Work

- Imports, copies, moves, scans, duplicate search run in **WorkManager** with a **foreground notification** (correct service type for the target SDK; ask for notification permission on Android 13+).
- Persist job state in Room; after process death, reconcile and offer **Resume** or **Discard** — never auto-delete.
- Show real progress (bytes and files), speed, ETA, with **Pause/Cancel/Retry failed**.
- Batch results screen: succeeded / skipped / failed with reasons.
- Respect battery: pause large jobs on low battery (< 15 %) unless the user overrides.

---

## 18. Data Model (Room)

```
MediaEntity(id, uri, relativePath, displayName, mime, type[PHOTO|VIDEO|AUDIO],
            size, dateModified, dateAdded, dateTaken?, width?, height?, durationMs?,
            title?, artist?, album?, codecInfo?, isFavorite, isMissing, inTrash,
            trashedAt?, originalUri?, checksum?, lastPlayedPosMs, lastPlayedAt?, playCount)
AlbumEntity(id, name, coverMediaId?, createdAt)
AlbumMediaCrossRef(albumId, mediaId, position)
TagEntity(id, name, color) ; MediaTagCrossRef(mediaId, tagId)
JobEntity(id, type, state, payloadJson, progress, createdAt, updatedAt)
SettingsKey-values in DataStore (not Room)
```
Indexes on `type`, `dateModified`, `displayName`, `isFavorite`, `inTrash`. Use Paging 3 for large lists.

---

## 19. Performance Targets
*These are **targets**, not claims: report only measurements taken on a named device and dataset (Section 39.6).*

- Cold start to cover screen < 800 ms on mid-range device.
- Grid scroll at 60 fps with 10 000+ items (Paging + thumbnail cache + `key`s + stable lambdas).
- Memory: thumbnails sized to cell; no full-size bitmaps in grids.
- Scan throughput: report items/sec; must stay responsive during scan.
- Release build with R8/minify, baseline profile if possible.

---

## 20. Accessibility & Localisation

- TalkBack labels everywhere; focus order logical; dynamic font up to 200%; colour-independent state cues; reduced-motion support.
- Strings in resources; **English + Urdu** scaffolding; RTL-safe layout (`start/end`, mirrored icons where appropriate).
- Number/date formatting by locale.

---

## 21. Error Handling Principles
*Detailed recovery UX and the required error list are in **Section 38**.*

- Plain-language errors with a next step; never raw exception text to users (keep in diagnostics).
- Cases to design for: SD card removed, permission revoked, corrupt file, unsupported codec, full storage, name collision, cancelled job, biometric hardware unavailable, no SIM, dialer missing, FAT32 limit.
- No crash on any of the above. Add a global handler that logs locally only.

---

## 22. Delivery Reality for Google AI Studio
*Applies whether the project is new or existing; see Section 0.1 for the existing-project audit workflow.*

- If the AI Studio environment cannot compile Android, **do not claim a build or test passed.** Output the full project as individual files with exact relative paths (Gradle files, manifest, Kotlin sources, resources), phase by phase.
- Run checks only if the environment truly supports them; otherwise list them under **"Not run"**.
- Provide `README.md` with steps to build a **debug APK** in Android Studio and **sideload** it (enable "install unknown apps" for the file manager/browser used; Play Protect may warn for sideloaded apps).
- Alternative if the user can't use Android Studio: describe building via GitHub Actions workflow (provide a ready `.github/workflows/android.yml` that assembles a debug APK as an artifact). Mark as untested unless verified.

---

## 23. Testing Plan
*Automated checks, failure-path scenarios, device evidence and the Definition of Done are in **Section 39**.*

**Unit:** PinHasher (vectors, timing), lockout logic, MIME classification, path/name sanitizer, sort/filter, scan diffing, trash/restore logic, move/copy planner.
**Instrumented:** Room migrations, SAF operations against a test tree (emulator with an SD-like volume if possible), biometric fallback flow (mock), foreground job resume.
**Manual checklist (provide as `TEST_PLAN.md`):**
1. Fresh install → onboarding → PIN → folder pick (`new`) → scan.
2. `.nomedia` present; restart phone; check Google Photos, Instagram/WhatsApp/Facebook pickers, Chrome upload picker.
3. Wrong PIN ×5 lockout and persistence after force-stop.
4. Each media type opens; videos: MP4/H.264, MKV, HEVC, 4K; audio: MP3/AAC/FLAC; photos: JPEG/PNG/WebP/GIF/HEIC.
5. Rotate device mid-playback; fold/tablet; split-screen.
6. Pull SD card while playing → graceful message.
7. Move/copy/delete/restore, name collisions, Urdu filenames.
8. TalkBack pass; font 200%; dark/light/AMOLED.
9. Reset app → media untouched.
10. Verify no INTERNET permission in the built APK.

---

## 24. Acceptance Criteria (must all be true)
*Each numbered criterion (here, 31.10 and 35: items 1–36) becomes a row `PV-AC-001…036` in `REQUIREMENTS_TRACEABILITY.md` (Section 40.3), with a status from Section 0.4.*

1. Cover screen matches Section 8.1 exactly: black screen, back arrow plus bold "SIM Service" title directly below the status bar, **Jazz** (SIM "1") and **Zong** (SIM "2") rows with light-gray right chevrons in the upper-middle, and a completely empty black area below. No vault wording, no extra buttons or menus.
2. Both rows require biometric/PIN **before** any carrier or vault access; the back arrow closes the app without authentication.
3. SIM Toolkit launches only when actually installed; otherwise a truthful message appears. No fake menus.
4. **Index in place** works: the scan is read-only, and the only change on the SD card is the `.nomedia` the user explicitly confirmed (never overwriting an existing one).
5. Copy and Move modes work with progress, cancel, verification and space checks.
6. Photos, Videos and Audio sections list files with thumbnails/metadata and open in dedicated polished viewers/players.
7. Video player has: brightness, volume, seek gestures, speed, aspect modes, orientation control (portrait + landscape), lock, resume, subtitles, audio tracks, and honest error handling with an external-player fallback.
8. Audio plays in the background with notification controls; Photo viewer supports zoom/pan/rotate/slideshow/info.
9. File management supports rename, move, copy, soft-delete/restore, create folder, export, import, properties, duplicate finder, integrity check.
10. Gallery Visibility Checklist exists and reports only verified facts.
11. **Reset app** leaves SD-card media untouched and says so.
12. `FLAG_SECURE` on by default; auto-lock works; no INTERNET permission.
13. README includes Android Studio build and sideload instructions; `brain.md` is current.
14. The UI looks premium: consistent theme, smooth transitions, skeleton loaders, empty/error states on every screen.
15. Auto-lock offers exactly: Immediately, 1 minute, 2 minutes, 5 minutes, 30 minutes, and "Until the app is closed"; the choice persists and is changeable in Settings.
16. In "Until the app is closed" mode the user is not prompted again while the app stays open (including after backgrounding and screen-off), but **is** prompted after swiping the app from Recents, an in-app Exit, process death or reboot.
17. Timers use `elapsedRealtime`; changing the phone's clock cannot bypass the lock.
18. First launch shows the "Set up access" checklist with a rationale before each system dialog, requests only the permissions in Section 9.8, handles denial and "Don't ask again", and the Settings → Permissions screen reflects live state.
19. Sensitive actions (change PIN, reset, permanent delete, export) re-authenticate even when the session is unlocked.
20. The Section 9.9 options exist with the stated defaults, and each toggle actually changes behavior (or is omitted and listed in `brain.md` as not built, never shown as a dead switch).

---

## 25. Future Phases (not for first build, listed so the architecture leaves room)

- **Encrypted Mode:** files encrypted on the SD card (AES-GCM/streaming, keys wrapped by Android Keystore + PIN-derived key), only openable in the app; streaming decrypt for ExoPlayer via a custom `DataSource`. Needs a migration tool and a strong "lose your PIN = lose your files" warning.
- Local-only **backup/restore** of the index.
- Wi-Fi transfer from PC (local network only; must be opt-in and would require the INTERNET/network permission — disclose clearly).
- Widgets, Android Auto-style audio browsing, Chromecast (out of scope for privacy).
- Tablet/two-pane layouts and desktop-mode keyboard shortcuts (Space, ←/→, F, M, J/L).

---

## 26. Honesty Notes (show in-app and keep in README)

- This app **hides** media from gallery indexing; it does **not encrypt** it. Anyone with a file manager or PC card reader can see the files.
- Google Photos copies already uploaded stay in your Google account until you delete them there.
- "Supports all video formats" is not achievable; the fallback plan above is the realistic version.
- The app is not affiliated with, endorsed by, or connected to Jazz, Zong, or any carrier.

---

## 27. Content Rules for the Builder

- No copying of proprietary assets, fonts without licence, carrier logos, or third-party app screenshots.
- Keep code commented where behavior is non-obvious (SAF quirks, `.nomedia` creation).
- **`.nomedia` creation via SAF:** `createFile("application/octet-stream", ".nomedia")` — then **verify the resulting file name** is exactly `.nomedia` (some providers append extensions); if renamed, rename it or report failure honestly.

---

## 28. Phased Delivery (superseded)
The phase plan is now **one unified plan in Section 41.1**, with the exit gates from the implementation addendum. After each phase: list files created/changed, give each requirement a status from Section 0.4, list what was **not verified**, update the documents in Section 40.1, then stop for the user's build feedback.

---

## 29. `brain.md` Template (superseded)
Use the template in **Section 40.2**, updated after every phase.

---

## 30. User Info the Builder Should Ask For (once, in Phase 1)

1. Android version and phone model.
2. SD card size, free space and format (FAT32 / exFAT / adoptable).
3. Whether the user has Android Studio, or prefers the GitHub-Actions APK build.
4. Confirm: Entry flow Option A, Storage mode Index-in-place (defaults in Section 2).
5. Preferred app name and launcher disguise name.
6. Verified helpline numbers for Jazz and Zong (or leave blank).

---

## 31. Enhancements Pack (design polish + extra features)

Everything here is **in addition** to the sections above. If a feature cannot be made to work properly, omit it and list it under "Not built" in `brain.md`; never ship a dead button.

### 31.1 Design refinements (whole app)
- **Design tokens:** colors, type, spacing (4/8 dp), radii, elevation and motion defined once in `ui/theme/` and used everywhere. No hard-coded colors in screens.
- **Two themes, one family:** the cover screen is pure black and minimal (Section 8.1). The vault is deep dark with a violet-to-cyan accent. The transition between them (after authentication) is a short cross-fade through black so it feels intentional.
- **Edge-to-edge** everywhere, with correct insets, cutout handling, **predictive back** gesture support and consistent back behavior.
- **Collapsing large-title top bars** on Photos, Videos, Audio and Files; sticky filter chips under them.
- **Floating glass bottom bar** (Home, Library, Files, Settings) with a subtle blur on Android 12+ and a solid fallback; hides while scrolling down, returns on scroll up; hidden in the players.
- **Hero header on Vault Home:** a storage ring (used/free on the SD card), counts per type, and a "Continue" carousel. A subtle animated gradient behind the ring (respecting reduced motion).
- **Selection polish:** animated check badges, scale-in thumbnails, a bottom action bar that slides up with batch actions; a drag handle for range-select.
- **Thumbnails:** blur-up placeholders (dominant color first, then image), duration pill on videos, resume progress line, a small badge for favorites/format (GIF, RAW, HDR, 4K).
- **Empty states with purpose:** in-house line illustrations and one clear action ("Pick folder", "Import files", "Rescan").
- **Haptics and sounds:** light haptics for selection/lock/unlock; no sound effects.
- **Adaptive layouts:** window size classes (compact/medium/expanded): two-pane on tablets and foldables (grid + details), navigation rail on wide screens, landscape-optimised players.
- **Consistent micro-interactions:** pull-to-refresh to rescan, swipe actions on list rows (favorite, trash), long-press preview peek on thumbnails.
- **Post-auth panel styling:** a black bottom sheet (`#101012`, 28 dp top corners) matching the cover screen: three large rows (Call helpline, Open SIM Toolkit, Open Vault) with simple icons; **Open Vault** is the single accented (gradient) action.
- **Authentication screens** use the same black, high-contrast style as the cover: large keypad, white digits, subtle dot animation, clear but calm error shake.

### 31.2 Library & organisation
- **Timeline view** for photos and videos: grouped by day/month with a draggable date scrubber and a year jump.
- **Advanced search syntax:** `type:video`, `size:>100mb`, `dur:>10m`, `tag:family`, `album:trip`, `fav`, `ext:mkv`, with suggestion chips so users never need to type syntax.
- **Locked albums:** albums that require re-authentication every time they open (separate from the main session), plus an option to hide their counts and thumbnails on the home screen.
- **Ratings and notes:** 1–5 star rating and a private text note per item (stored in Room only; never written into the file).
- **Batch rename** with patterns (`Trip_{n}`, `{date}_{name}`) and a live preview.
- **Smart collections** (Section 13) plus: "Recently played", "Never opened", "Largest 50", "Short clips (< 30 s)", "Screenshots-like images" (by aspect ratio and size, no AI).
- **Sort/view memory per section** (each of Photos/Videos/Audio remembers its own grid size, sort and filters).
- **Compare mode:** open two photos side by side with synced zoom/pan.

### 31.3 Photo tools (non-destructive)
- Crop, rotate, flip and basic adjustments (brightness, contrast, saturation, warmth) with live preview.
- **"Save as copy" is the default**; "Replace original" requires an explicit confirmation and is blocked on read-only media.
- Slideshow with **music from the Audio section**, transition choices, interval and shuffle, and a screen-on setting.
- Copy a photo to the clipboard or share it only after a clear "this leaves the vault" prompt.

### 31.4 Video extras
- **Audio-only mode** (screen off, keeps playing with notification controls) and "Play audio in background".
- **Bookmarks and chapters:** tap to add a bookmark at the current time; list bookmarks in the info sheet; jump from the seek bar.
- **Per-video speed and subtitle-style memory**; global defaults in Settings.
- **Auto-play next** with a 5-second "Next up" card (cancelable); loop one/all; mirror flip.
- **First-run gesture tutorial** (skippable) that shows the swipe zones once.
- **In-app floating mini-player** when the user leaves the player screen while a video is playing (dismissible; not system PiP unless enabled).
- Frame capture saves **into the vault** and shows a snackbar with "View".

### 31.5 Audio extras
- Local **playlists** (create, reorder, duplicate), "Add to playlist" from anywhere, queue editing (drag to reorder, swipe to remove), "Play next" and "Add to queue".
- **Audiobook mode** for long files: remembers position, adds bookmarks, 0.5×–3× speed, skip 15/30 s.
- Sleep timer with "end of track", crossfade (0–10 s), and equalizer presets when the device supports it.
- Embedded artwork and tags read in the background, with a tasteful generated-gradient fallback.

### 31.6 Import & storage tools
- **Import wizard:** pick files from any storage via the system picker, choose Copy or Move, see space needed vs. free before starting, and get a results screen.
- **Optional "Add to vault" share target** (receives photos/videos/audio shared from other apps) is **off by default** because it makes the app visible in the share sheet; when enabled, it uses a neutral label.
- **Move from gallery:** after copying, offer to remove the originals using the system's own delete-confirmation request; never delete without that system confirmation.
- **Storage dashboard:** breakdown by type (simple chart), largest files, duplicates saving estimate, SD-card health hints (read-only mount, nearly full, slow card), and "Safe to remove card?" guidance that checks no job is running.
- **Local backup/restore of the index** (database + settings, no secrets) to a file the user chooses.
- **Scan history and diagnostics** (counts, durations, errors) viewable in Settings → Advanced.

### 31.7 Security extras
- **Lockdown mode:** one tap (from the lock-now button or Quick Settings tile) locks, clears caches and returns to the cover screen.
- **Panic gesture** (optional): triple-tap on the status-bar area of the vault returns to the cover screen and locks.
- **Secure session indicator:** a small, non-alarming lock dot in the vault top bar showing the time until auto-lock (hidden when the mode is "Until the app is closed").
- **Biometric re-enrollment handling** and clear messaging when biometrics are disabled by the system.
- **Tamper-evident hints:** if `.nomedia` disappears from a vault folder (for example after a file-manager clean-up), show a banner with a one-tap fix.

### 31.8 Help & onboarding polish
- Animated, skippable onboarding with a progress stepper and a **"Test your unlock"** step so the user confirms the PIN/biometrics work before continuing.
- In-app **Help & tips** (available only after authentication): how to paste files into the vault folder, how to show hidden folders in a file manager, how to verify gallery hiding, how to switch auto-lock modes.
- A one-time **"What this app does and doesn't do"** screen (Hidden ≠ encrypted), reachable again from Settings.

### 31.9 Quality bars
- 60 fps scrolling on 10 000+ items; cold start to cover screen < 800 ms; no ANR; crash-free handling of SD-card removal.
- **Accessibility audit:** TalkBack pass on every screen, touch targets ≥ 48 dp, contrast ≥ 4.5:1, font scale up to 200 %, reduced-motion support.
- **Tests:** unit tests for auth/lockout, auto-lock timing, scan diffing, file operations planner; Compose UI tests for the cover screen (layout, tap → auth), onboarding and the players' controls.
- **Release hygiene:** R8/minify, baseline profile if possible, no debug logging of file names or PINs, strict mode in debug builds.

### 31.10 Additional acceptance criteria
21. The default auto-lock timing is **1 minute** on a fresh install and after "Reset app".
22. The launcher icon matches Section 10.7 (light-gray squircle, matte black SIM with angled top-right notch, gold-and-orange segmented chip at the card's center) and has adaptive, monochrome and legacy variants.
23. The post-auth panel, PIN screen and biometric prompt visually match the black cover screen.
24. Photo editing is non-destructive and defaults to "Save as copy".
25. Locked albums re-prompt for authentication each time they open.
26. Every feature in this section is either working or listed as "Not built" in `brain.md`; there are no dead controls.
27. The cover screen has automated UI tests asserting: back arrow present, title text "SIM Service", exactly two rows ("Jazz", "Zong") with chevrons, and no other visible elements.

### 31.11 Phase adjustments (superseded)
See the unified plan in **Section 41.1**: foundation items go in Phase 1, the cover screen in Phase 3, enhancements in Phases 8–10 according to their priority (Section 2.2).

---

## 32. Intruder Photo & Owner Alert

**Purpose:** if someone enters a wrong PIN/password, the app quietly takes a front-camera photo, stores it privately, and shows the real owner an alert the next time they unlock successfully. This is **opt-in** (Settings → Security → Intruder photo, default **off**, offered during onboarding), because it needs the optional camera permission.

### 32.1 Trigger rules
- **Which failures count:** a wrong PIN/password on the cover-screen unlock, the re-authentication prompts (Settings, delete, export, change PIN) and the decoy-PIN screen. **Biometric mismatches** count only if the user turns on "Also capture on fingerprint failure" (default off, since a fingerprint failure is usually the owner's wet finger).
- **After how many wrong attempts:** user choice **1 (default) / 2 / 3**. One photo per qualifying attempt, with a minimum 3-second gap between captures so rapid tapping doesn't flood storage.
- **No capture during a lockout countdown** (no attempts can be made then) and none when the camera permission is missing: the event is **still logged** (time, count, entry point), just without a photo.
- **Owner-testing:** a **"Test intruder photo"** button in settings takes one sample so the user can see the result and verify it works on their phone.

### 32.2 How the capture works (honest limits)
- Use **CameraX `ImageCapture`** with the **front camera**, bound to the lifecycle of the unlock screen. Capture is **headless** (no preview shown), JPEG, long side 1280 px, quality ~80, EXIF orientation applied. Skip silently if no front camera exists.
- The capture happens **only while the unlock screen is in the foreground** (Android 9+ blocks camera use from the background, so the app never tries). It is never triggered from a service.
- **Be truthful in the UI text:** Android 12+ shows a small camera-in-use indicator and some devices make a shutter sound or briefly show a privacy dot. The app cannot hide these and **must not claim the capture is invisible or silent**. If another app holds the camera, the capture fails gracefully (event logged, no crash).
- Never keep the camera open: open → capture → close within about 1.5 s. A timeout of 3 s cancels a stuck capture.

### 32.3 Where the photos live ("privacy folder")
- Saved into the **vault's protected folder** `.pv_intruders/` (dot-prefixed, containing a `.nomedia`), next to the vault root on the SD card, so they never show in Gallery, Google Photos or share pickers.
- **Fallback:** if the SD card/folder is not reachable (card removed, permission lost), save to the app's **private internal storage** instead and move them to the vault folder on the next unlock. Never lose an event.
- File name: `intruder_YYYYMMDD_HHmmss.jpg` (no personal data). The entry is stored in Room as `IntruderEventEntity(id, timestamp, attemptNumber, entryPoint[COVER|SETTINGS|DELETE|EXPORT|DECOY|BIOMETRIC], photoUri?, seen, deviceLockedState)`.
- These photos are **not** part of the Photos library, search, albums or smart collections. They appear only in **Security → Intruder log**.
- Retention: keep the latest **50** (user choice 25 / 50 / 100 / unlimited); auto-prune oldest; "Delete all" requires re-authentication.
- **Reset app** never deletes the photos on the SD card; it clears the log entries and tells the user where the files remain.

### 32.4 Alert for the real owner
When the owner next **unlocks successfully**, before the post-auth panel opens:
1. **Security alert sheet** (black bottom sheet, matches the cover style): "**2 wrong attempts while you were away**", the time of the latest one, the entry point, and up to 3 photo thumbnails with a count badge.
2. Buttons: **View** (opens the Intruder log), **Dismiss** (marks as seen), and **Delete photos**.
3. A small **red dot** stays on the Security entry in the vault until the events are seen.
4. **Optional immediate notification** (default **off**): a local notification with neutral wording ("SIM Service: activity detected") and no photo on the lock screen. It is off by default because a visible notification can tip off the intruder. Respect `POST_NOTIFICATIONS`.
- The app has no internet permission, so it **cannot send the photo to email or the cloud**. State this plainly in settings. (A future, opt-in "send to my email" feature would need the INTERNET permission and a clear disclosure; it is out of scope for now.)

### 32.5 Intruder log screen
- List of events with thumbnail, date/time, attempt number and entry point; tap for a full-screen viewer with **Share (leaves the vault prompt)**, **Export**, **Delete**.
- Filters: unseen, last 7 days, by entry point. Empty state: "No failed attempts."
- Viewing requires the session to be unlocked; **sensitive-action re-auth** applies to "Delete all" and "Export".

### 32.6 Privacy & fairness notes (show in the setting's help text)
- Photos stay on this phone only. Only use this on your own device.
- If a family member mistypes the PIN, their photo is stored too, so keep "capture after 2–3 attempts" in mind for shared devices.

### 32.7 Additional guardrails (merged from the implementation addendum)
- **Priority P2, best-effort, opt-in.** Never enabled silently; never bypass Android camera indicators, privacy controls or background-camera restrictions.
- The explanation screen must say capture **can fail** because of permission, hardware, another app using the camera, device restrictions or lifecycle changes. **Never claim every failed attempt produces a photo, or that the photo is invisible or silent.**
- The qualifying attempt is **always logged** even when no photo could be taken.
- Retention and storage are bounded (32.3); event details are protected behind authentication; deletion and retention rules are explicit.
- **Before a reset**, show where intruder photos live and that they stay on the card unless the user deletes them.
- Photos are excluded from normal media indexing, thumbnails, search, albums and system gallery views.
- **Test on real hardware.** A mock or emulator does not prove camera behavior; record it as `Verified on Device` only after a physical test.

---

## 33. Playback Rules: Loop, Headphone Disconnect, Phone Lock

### 33.1 Loop (repeat) for video and audio
**Modes (same for both players), cycled by one button and also selectable from the options sheet:**

| Mode | Icon | Behavior |
|---|---|---|
| **Off** | repeat (dimmed) | Play once; stop (or go to the next item if "Auto-play next" is on). |
| **Loop one** | repeat-one (accent) | The current item restarts seamlessly from 0:00 when it ends (`Player.REPEAT_MODE_ONE`). |
| **Loop all** | repeat (accent) | The whole queue/folder/playlist repeats (`Player.REPEAT_MODE_ALL`). |

- **A-B loop** in **both** the video and audio players: tap **A** to mark the start, tap **B** to mark the end, the section repeats until cleared. Markers appear on the seek bar; **Clear** removes them. Works with speed changes.
- **Loop count** (optional): "Loop N times" (2, 3, 5, 10, ∞) for Loop one, with a small counter badge ("Loop 2/5").
- **Remember per item:** the last loop mode is saved per video/audio file (Room) and a **global default** exists in Settings → Playback (default Off). A user toggle controls whether loop resets when the app closes (default: remember).
- **Where the controls are:** a clearly visible loop button in the video bottom bar, and in the audio full player and **mini player** (long-press for the A-B/loop-count options). Also exposed as a **custom action in the media notification** and via `MediaSession` repeat commands so Bluetooth/lock-screen controls work.
- **Seamless looping:** for short clips, use `REPEAT_MODE_ONE` (no re-prepare) and avoid showing the controls overlay or a loading spinner on each loop. For audio, keep gapless behavior.
- **Interactions:** the sleep timer and "Stop after this track" still win over loop; a headphone disconnect or phone-lock pause (below) pauses the loop and does not auto-resume unless the user sets it so.

### 33.2 Headphone / earphone disconnect protection (privacy)
**Rule:** if headphones or wireless earphones are **disconnected while video or audio is playing**, playback must **stop (pause) immediately and the media volume must be set to 0**, so nothing suddenly plays out loud.

**Detection (use both, because neither alone is reliable on every phone):**
1. `AudioManager.ACTION_AUDIO_BECOMING_NOISY` (Media3 `setHandleAudioBecomingNoisy(true)` as a first line of defense).
2. `AudioManager.registerAudioDeviceCallback` → `onAudioDevicesRemoved` for output devices of type wired headphones/headset, USB headset, Bluetooth A2DP/SCO, BLE headset/broadcast (API 31+), and hearing aid. Only react if **that device was the active output when playback was running** (track the current routed device), so removing an unrelated accessory does nothing.
3. Treat **Bluetooth turning off** or the earbuds going out of range the same way.

**Action, in this order and within ~100 ms:**
1. `player.pause()` and **set `player.volume = 0f`** (player-level mute as the instant guard).
2. Set the media stream volume to 0: `AudioManager.setStreamVolume(STREAM_MUSIC, 0, FLAG_SHOW_UI)`. Skip with a clear on-screen message if the device uses fixed volume (`isVolumeFixed`). Because Android stores volume per output device, this mutes the **new** (speaker) route, which is what is wanted.
3. Show a calm banner/toast: "Playback stopped: headphones disconnected" with a **Resume** button. Resume does **not** restore loudness automatically; if the system volume is 0 it first shows the volume HUD and asks "Volume is muted. Turn it up?" with a slider, so the user must raise it deliberately.
4. Do **not** auto-restore the previous volume when headphones reconnect (avoids blasting the speaker if it was left high); the user raises volume themselves.

**Applies to:** video player, audio player (including background playback and the mini player), photo-slideshow music, and any item playing from the queue. It also applies while the app is in the background (handled in the playback service, not only the UI).

**Settings (Playback → Privacy):** "Stop when headphones disconnect" (default **on**); "Also set volume to 0" (default **on**); "Show Resume banner" (default on). If the user turns the volume option off, behavior falls back to pause only.

**Honest notes (show in the setting's help text):** the app has no internet access, so everything is local playback ("streaming" in your request means any currently playing media). Some Bluetooth/LE Audio devices report removal late, so the mute can lag by a moment on those phones.

### 33.3 Phone lock behavior
**Rule:** when the **phone is locked** (screen turned off or the system lock screen appears), **all media pauses except music audio**.
- **Detection:** `ACTION_SCREEN_OFF` plus `KeyguardManager.isKeyguardLocked()` checked in the playback service (registered at runtime) and in the UI; `ACTION_USER_PRESENT` for unlock.
- **Pauses on phone lock:** **video** (always), photo **slideshows**, and any **video-with-audio-only mode**. The paused position is saved so the user continues exactly there.
- **Keeps playing on phone lock:** **music audio** (audio files in the Audio section), with lock-screen controls and neutral notification text (title hidden by default).
- **Audiobook/podcast files** (Section 31.5 audiobook mode) are treated as music audio for this rule so they keep playing, because they are audio. A single toggle **"Also pause audio when the phone locks"** (default **off**) makes even audio pause for people who want stricter privacy.
- **After unlock:** playback does **not** auto-resume by default; the paused video waits at its position behind the app's own lock if the auto-lock timer has elapsed. A toggle **"Auto-resume after unlock"** (default off) exists for convenience.
- **Interaction with other rules:** headphone-disconnect protection (33.2) applies to the music that keeps playing; the app auto-lock (Section 9.4) controls the UI only.
- **Edge cases to handle:** phone call or alarm interrupts (audio focus), screen turning off while casting/PiP (not built), and a lock caused by the proximity sensor during a call (ignore proximity-only screen-off).

### 33.4 Playback settings summary
Settings → Playback → **Privacy & loop**: Loop default; Loop count; Stop when headphones disconnect; Also set volume to 0; Pause video when phone locks (always on, shown as info); Also pause audio when the phone locks; Auto-resume after unlock.

### 33.5 Playback state model & precedence
**One playback state model** with tested transitions: `Idle → Preparing → Playing ⇄ Paused → Buffering (slow SD/card read) → Completed | Failed | Interrupted (audio focus, call) | Stopped (source unavailable) → Released`.
- Every control (seek, play/pause, next/previous, loop, shuffle, speed, orientation) does what it advertises **only where supported**; unsupported controls are hidden or disabled with a reason.
- Restore playback only when the setting is on **and** the media is still accessible. Never restart playback several times after activity recreation.
- Respect audio focus, calls and other interruptions. Stop and release the player cleanly when the user stops it, the source becomes invalid, or the lifecycle policy requires it.
- Background audio runs in a foreground `MediaSessionService`. **Do not claim Android keeps a process alive indefinitely**; handle the service being killed.

**Precedence when rules overlap (highest first):**
1. **Headphone/earphone disconnect** → stop + mute (33.2).
2. **Phone locked** → pause video and slideshows; music continues unless "Also pause audio" is on (33.3).
3. **App UI locks** → blocks private screens; background audio continues only if enabled; "Stop playback when the app locks" does exactly what it says.
4. **Lock now** → locks the UI promptly; permitted audio may keep playing without exposing private UI.
5. Sleep timer / "stop after this track".
6. Loop mode (33.1) and then auto-play next.
Cold start and reboot **always** require authentication. Add automated tests for every supported combination; never let a setting promise behavior the player or Android lifecycle cannot deliver. Every gesture needs an equivalent visible control (12.5).

---

## 34. Layout, Spacing, Sizing & Responsive Design

**Goal:** every screen has consistent margins, padding, gaps, borders and component sizes, and works on every phone, tablet, foldable and orientation without clipping, overlap or tiny tap targets. Define everything as **design tokens** once (`ui/theme/Dimens.kt`) and never use a hard-coded dp value inside a screen.

### 34.1 Spacing scale (4-dp base)
`space0=0, space1=4, space2=8, space3=12, space4=16, space5=20, space6=24, space7=32, space8=40, space9=48, space10=64` dp.
- **Screen edge margin:** 16 dp (compact), 24 dp (medium), 32 dp (expanded); content is also inset by system bars/cutouts.
- **Gap between sections:** 24 dp. **Gap between a title and its content:** 8–12 dp. **Gap between sibling items (rows/cards/chips):** 8–12 dp. **Grid gutters:** 4 dp for dense photo grids, 8 dp for video/album cards, 12 dp for cards on tablets.
- **Card/sheet inner padding:** 16 dp (20 dp on expanded). **List row padding:** 16 dp horizontal, 12 dp vertical (min height 56 dp, 72 dp with thumbnail). **Dialog padding:** 24 dp. **Bottom sheet:** 24 dp top corners 28 dp, 16 dp content padding, drag handle 4×32 dp with 12 dp margin.

### 34.2 Borders & shape (vault screens)
- **Border width:** `border1 = 1 dp` (default), `border2 = 2 dp` (focus/selected). Borders are **inside** the element bounds so they never change layout size.
- **Border color:** `outline` `#262E3D` (default), `outlineStrong` `#3A4560` (hover/pressed), accent gradient 2 dp (focused/selected), error `#F87171` 2 dp.
- **Applied to:** buttons (outlined and tonal variants), text fields, cards, chips, list-row containers, album tiles, sheet containers, dialogs, mini player, player control clusters, keypad keys, toggles' track, and thumbnails in selection mode.
- **Radii:** chips 12 dp, buttons 14 dp (or full pill for primary actions), text fields 14 dp, cards 20 dp, sheets/dialogs 28 dp, thumbnails 12 dp (8 dp in dense grids), keypad keys full circle.
- **Cover screen exception (kept as designed):** the pure-black "SIM Service" screen stays borderless and card-less per Section 8.1; it still follows the spacing scale (20 dp horizontal padding, 12 dp row gap, 64 dp rows). A subtle 1 dp hairline divider `#1C1C1E` is the only line allowed there. *(If the user wants outlined rows on the cover, it is a one-token change, but it makes the screen look less like a plain SIM menu.)*

### 34.3 Component sizing table

| Component | Size / spec |
|---|---|
| **Primary button** | height **52 dp** (min width 120 dp), horizontal padding 24 dp, text 16 sp semibold, icon 20 dp + 8 dp gap |
| **Secondary / outlined button** | height **48 dp**, padding 20 dp, 1 dp border, text 15 sp medium |
| **Small / tonal button** | height **40 dp** (touch target still 48 dp via padding), text 14 sp |
| **Icon button** | visual 24 dp icon in a **48×48 dp** target; 4 dp inner padding; optional 1 dp circular border in the player |
| **Floating/primary action (FAB)** | 56 dp (extended: 56 dp height, 20 dp padding) |
| **Text field** | height **56 dp**, 16 dp horizontal padding, label 12 sp, input 16 sp, 1 dp border (2 dp accent focus), helper/error text 12 sp with 4 dp top gap |
| **Chip** | height **36 dp** (target 48), padding 12 dp, text 14 sp, 1 dp border, 8 dp gap between chips |
| **Switch row** | row height **64 dp**, title 16 sp + description 13 sp, switch 52×32 dp, 16 dp padding |
| **List item** | height 56 dp (one line) / 72 dp (two lines + thumb 48 dp) / 88 dp (video row with 96×64 dp thumb) |
| **Top app bar** | height **56 dp** (+ status-bar inset); collapsing large title 112 dp; title 20 sp |
| **Bottom bar (glass pill)** | height **64 dp**, floating 16 dp above the nav inset with 16 dp side margins, icon 24 dp, label 12 sp |
| **Mini player** | height **64 dp**, 12 dp padding, 48 dp artwork, 1 dp top border, 12 dp radius |
| **Keypad key** | **72 dp** diameter (compact) / 80 dp (large screens), 16 dp gap, digits 28 sp, 1 dp border |
| **PIN dots** | 14 dp dots, 16 dp gap |
| **Thumbnails** | photo grid cell = (width − gutters) / columns, 1:1; video 16:9 card; album tile 1:1 with 20 dp radius |
| **Seek bar** | track 4 dp (12 dp when dragging), thumb 16 dp (24 dp active), touch height 48 dp |
| **Player controls** | center play 72 dp (border 1 dp, glass), seek ±10 s 56 dp, bottom-bar icons 24 dp in 48 dp targets, 8 dp gaps |
| **Dialog** | max width 560 dp, 24 dp padding, buttons 48 dp tall with 8 dp gap |
| **Snackbar/toast** | 48 dp min height, 16 dp margin from edges, 12 dp radius |
| **Section header** | 20 sp semibold, 24 dp top margin, 8 dp bottom margin |

### 34.4 Typography sizing (all in `sp`, respects system font scale)
Display 32/40, Headline 24/32, Title 20/28 (app bar) and 18/26 (section), Body 16/24 (default) and 15/22 (secondary), Label 14/20 (buttons/chips), Caption 12/16 (hints). **Minimum text size 12 sp; never use `dp` for text.** At 200 % font scale, rows and buttons **grow in height** (`heightIn(min=…)`), text wraps up to 2 lines (names use ellipsis only after 2 lines), and nothing is clipped.

### 34.5 Responsive rules (Compose `WindowSizeClass`)

| Class | Width | Layout |
|---|---|---|
| **Compact** | < 600 dp | Single column; bottom bar; photo grid 3–4 columns (portrait), 5–6 (landscape); video grid 1–2 columns; sheets full width. |
| **Medium** | 600–839 dp | Navigation **rail** on the left; photo grid 4–6 columns; two-pane library + details when a landscape tablet; dialogs max 560 dp centered. |
| **Expanded** | ≥ 840 dp | Rail + **two/three-pane** (list/grid, details, preview); photo grid 6–10 columns; forms and settings limited to a **max content width of 720 dp**, centered; players use side panels (queue, info). |

- **Columns formula:** `columns = clamp(floor(availableWidth / minCellWidth), min, max)` with `minCellWidth` = 96 dp (photos, dense), 120 dp (default), 160 dp (video cards), 180 dp (album tiles); pinch-to-zoom changes the column count within the allowed range.
- **Orientation:** every screen supports portrait and landscape and survives rotation with state preserved (`rememberSaveable`, ViewModels). Players switch to **landscape-optimized layouts** (controls spread along the sides, gesture zones unchanged, bars shrink to 48 dp).
- **Foldables and multi-window:** use `androidx.window` fold posture; in **tabletop** posture the video sits on the top half with controls on the bottom half; support split-screen and freeform windows down to 320 dp width without clipping; resizable activity (`resizeableActivity=true`).
- **Insets:** respect status/navigation bars, display cutouts, IME (keyboard pushes fields into view) and gesture-navigation insets; the cover and player screens are edge-to-edge.
- **No fixed pixel widths.** Use weights, `fillMaxWidth`, `widthIn(max=…)`, `Modifier.aspectRatio`, and `LazyVerticalGrid(GridCells.Adaptive(minSize))`.
- **Small phones** (320–360 dp wide, short screens < 640 dp tall): scroll instead of squeezing; keypad scales to 64 dp keys; bottom sheets get a scrollable body; never hide the primary action off-screen.

### 34.6 Layout test matrix (must pass before release)

| Device class | Size (dp) | Checks |
|---|---|---|
| Small phone | 320×568 and 360×640 | No clipping, keypad fits, buttons ≥ 48 dp, text wraps |
| Normal phone | 392×800, 412×915 | Reference layouts for all screens |
| Large phone / phablet | 430×932 | Grids use extra width, no stretched buttons |
| Phone landscape | 800×360 | Players and cover screen usable, bars not covering content |
| Small tablet | 600×960 | Rail + two-pane layouts, 720 dp max content width |
| Large tablet | 1280×800 | Three-pane library, centered dialogs |
| Foldable | 674×841 folded/inner, tabletop posture | Layout reflows without restart |
| Accessibility | font scale 1.3× and 2.0×, display size large | Nothing overlaps or truncates the primary labels |

Use Compose previews with `@PreviewScreenSizes`, `@PreviewFontScale` and `@PreviewLightDark`, plus screenshot tests for each class.

### 34.7 Layout quality checklist (per screen)
- Every element sits on the 4-dp grid and uses a spacing token.
- Every interactive element has a **≥ 48×48 dp touch target**, a visible pressed/focused state and a content description.
- Text, icon and container **padding is symmetric** unless intentional; icons are optically aligned to text baselines.
- Borders are 1 dp (2 dp for focus/selected) and use the tokens above; no element has a shadow *and* a heavy border.
- Lists and grids use `contentPadding` instead of outer margins so scrolled content isn't cut off at the edges, with `key`s for stable items.
- No text overlaps media or controls; overlays on media use a scrim/gradient so white text meets 4.5:1 contrast.

---

## 35. Acceptance Criteria for Sections 32–34

28. With **Intruder photo on**, a wrong password takes a front-camera photo, saves it in `.pv_intruders/` (with `.nomedia`) or the internal fallback, and logs the event; the photo never appears in the gallery, Photos section or share pickers.
29. On the owner's next successful unlock, the Security alert sheet shows the number of failed attempts, time, entry point and photo thumbnails, with View / Dismiss / Delete; a red dot remains until seen.
30. Intruder photo is **off by default**, requests the camera permission only when enabled (after a rationale), works as "log only" if permission is denied, and never claims to be silent or invisible.
31. Video and audio each have **Loop Off / Loop one / Loop all** plus **A-B loop**; the mode shows in the player, mini player and notification, and is remembered per the setting.
32. Disconnecting wired or Bluetooth headphones during playback **pauses immediately and sets media volume to 0**, shows a Resume banner, and does not auto-restore loudness.
33. When the phone is locked, **video and slideshows pause and music audio keeps playing**; with "Also pause audio" on, audio pauses too. No auto-resume unless enabled.
34. Every screen uses the spacing, border, radius and sizing tokens of Section 34; all touch targets are ≥ 48 dp; buttons and fields use the stated heights.
35. The layout matrix in 34.6 passes: no clipping, overlap or horizontal scrolling at any listed size, in portrait and landscape, at font scale up to 2.0×.
36. Phone rotation, split-screen and fold/unfold keep playback position, selection and scroll state.

*Phase placement for these features is defined in Section 41.1.*

---

## 36. Settings Registry (one source of truth)

All settings described in Sections 9.4, 9.9, 16, 31, 32, 33 and 34 are consolidated into **one registry**. Remove duplicate or conflicting implementations. For **every** setting record:

| Field | Meaning |
|---|---|
| Key | Stable unique identifier |
| Type | Boolean, enum, integer, duration, URI, … |
| Default | The actual default value |
| Allowed values | Valid choices and bounds |
| Description | User-facing explanation |
| Effect | The exact behavior it changes |
| Persistence | Where/how it is stored (DataStore) |
| Security impact | Whether it changes privacy or security |
| Dependencies | Required permission or feature availability |
| Test | How its behavior is verified |

*Example rows:* `auto_lock_mode` (enum; default `AFTER_1_MIN`; Immediate/1m/2m/5m/30m/UntilClosed; security-relevant; test: elapsed-time lock test) · `intruder_photo_enabled` (boolean; default off; needs CAMERA; test: permission-denied and capture-failure tests) · `stop_on_headphone_disconnect` (boolean; default on; test: audio-device-removed test).

**Rules**
- A toggle must never be decorative. If a feature is unavailable, show why and do not imply it is active.
- Persist settings across process death and app updates. Reconcile related settings so they cannot contradict each other.
- Keep **app auto-lock, idle lock, manual lock, phone screen lock and playback interruption** as separate concepts with the documented precedence (33.5).
- Request a permission when the user enables the related feature (except the first-launch checklist, Section 9.8). If it is denied or revoked, show the feature's real status and a recovery path.
- Hide experimental or unfinished settings until the feature works and is tested. Resetting settings never deletes media.
- Explain that Android may terminate background work and the app cannot guarantee uninterrupted background execution.

---

## 37. Data Flows: Import, Export, Share, Backup, Reset

### 37.1 One consistent disclosure
Before any workflow that changes storage, show: what will happen; which files are affected; whether the original stays; where the result is stored; whether the destination is **outside the vault**; and what recovery exists if it fails.

### 37.2 Export and share
- Use Android's document-creation and sharing mechanisms with **content URIs and temporary URI permissions**; never expose raw file-system paths. Export only files the user selected.
- Use the system destination picker when required or when "Ask where to save" is on. Say clearly that an exported file is **outside the vault and no longer protected by the app lock**, and that the receiving app is beyond the vault's control.
- App-created temporary share files are **deleted on a documented schedule** (after the share completes, and on next app start; nothing older than 24 hours remains). Handle cancellation, destination failure, duplicate names and partial exports. Require fresh authentication per the sensitive-action policy.

### 37.3 Backup and restore (three different things)
- **Settings backup:** preferences only, **no secrets**.
- **Library/index backup:** metadata and references, which become invalid if the folder changes. **It does not contain media.**
- **Media backup:** actual files; a separate, explicit operation.
A restore **previews conflicts** and says whether it will merge, replace or skip. It never overwrites files by default. Backups never include credentials, keys or intruder photos.

### 37.4 Reset app (merged flow)
1. Explain exactly which app-owned records, settings and credentials will be cleared.
2. State that ordinary media on the SD card is **not** deleted, and that hidden-folder files and **intruder photos may remain** (show their location).
3. Require **fresh authentication** (or the forgotten-PIN path) and an explicit final confirmation; cancelling changes nothing.
4. Clear only the named app-owned data; return to first-run setup and folder re-selection (default auto-lock restored to 1 minute).
5. Report the **real** outcome, including anything that could not be cleaned up. Never imply that reset encrypts, securely erases or hides media.

---

## 38. Error Handling & Recovery UX
Messages are specific and actionable; never raw stack traces, unexplained codes or a generic "Something went wrong" when the cause is known. For every recoverable error state: **what failed; whether the original data is safe; what completed before the failure; what the user can do next; whether retrying is safe.**
Cover at least: folder access missing/revoked; SD card unavailable or changed; scan failed/cancelled; media moved, renamed or deleted outside the app; unsupported or corrupt media; copy/move/delete interrupted; storage full; provider permission denied; camera unavailable/denied; biometrics unavailable; playback failure or audio-focus interruption; database migration/persistence failure; export/share cancelled; restore conflict; invalid or expired operation state.
Never show private paths or credentials in errors. Logs hold sanitized error categories only, stay local, and are exported only if the user explicitly exports diagnostics.

---

## 39. Testing, Evidence & Release Gates

### 39.1 Automated checks (use the project's existing stack)
Unit tests for authentication, lockout, settings validation, sorting/filtering, operation-state transitions, recovery decisions, **auto-lock timing, loop modes, headphone-removal and phone-lock rules**; repository/storage tests with test doubles for provider failures; **database migration tests**; UI tests for primary flows, permission-denied, empty/error states, confirmation dialogs and the **cover-screen layout**; playback state-transition tests; **layout/screenshot tests for the size matrix (34.6)**; Android lint and dependency checks where supported.
**Tests must verify observable behavior and failure cases**; never write tests that only assert a method exists or that a fake returned success.

### 39.2 Required failure-path scenarios
Folder access denied then granted · permission revoked after a scan · SD card removed during scan or transfer · copy/move interrupted then app restarted · destination read-only or full · destination name exists · provider delete/move fails · **database write fails after a file operation** · **double-tap on a destructive action** · app backgrounded or killed mid-work · **lockout survives recreation** · reset cancelled then separately confirmed · media unavailable after indexing · playback interrupted by audio focus, phone lock or headset removal · notification permission denied · **camera permission denied or capture fails** · upgrade from the previous schema without losing settings or index.

### 39.3 Real-device verification
Before marking anything `Verified on Device`, record **device model, Android version, feature, result and limitations**. Use a **physical SD card** for SD workflows. Emulator success does not prove OEM-specific SAF, camera, notification or background behavior.

### 39.4 Build evidence
Run the real Gradle tasks for the project (debug build, unit tests, lint, instrumented tests where available) and **record the command and result**. If a task was not run, say so. If the build fails, record the failure summary and whether this update caused it. Never fabricate reports, screenshots, APKs or CI results. If a release APK is produced, verify it exists and record its exact path.

### 39.5 Definition of Done (per phase)
Requirements implemented or explicitly deferred · builds for the intended variant (or the blocker is documented) · relevant tests **actually ran** · no unresolved data-loss regression · every visible control does something real · permission and error states handled · documentation and statuses match reality · a concise phase report exists. **The release is not done while any P0 data-integrity or authentication blocker is open.**

### 39.6 Performance budgets
The numbers in Section 19 and 31.9 are **targets**, not claims. Keep scrolling responsive on large libraries; decode thumbnails without full-resolution loads; bound the thumbnail cache; avoid duplicate scans, transfers and player initialisations; use cancellation and back-pressure; release camera, player and temporary resources when not needed; handle low storage and memory pressure. **Profile the real bottleneck before adding caching, concurrency or dependencies**, and report measurements only with the named device and dataset.

---

## 40. Traceability, Documentation Set & Threat Model

### 40.1 Documents the builder must keep accurate
`README.md` (setup, build, supported Android versions, permissions, feature inventory, **media-support matrix**, limitations) · `TEST_PLAN.md` · `REQUIREMENTS_TRACEABILITY.md` · `TECHNICAL_DECISIONS.md` (versions, conflicts, security/storage decisions) · `SECURITY_AND_PRIVACY.md` · `brain.md` · `CHANGELOG.md` (if maintained). **Never write "all tests passed" unless every required test actually ran and passed.**

### 40.2 `brain.md` template (update after every phase)
```markdown
# Project Brain
## Current State
- Current phase / Build status / Last verified commit / Known blockers
## Architecture
- Main modules / Persistence / Storage access / Auth & session state / Media & player stack / DI choice
## Requirements Status   (use the Section 0.4 vocabulary only)
- Implemented / Tested / Verified on Device / Deferred / Blocked
## Decisions
- Decision / Reason / Alternatives considered (include conflict-resolution entries)
## Files created or changed this phase
## Not verified / Not run
## Known Limitations
- Limitation / User impact / Follow-up
## Open questions for the owner
## Next Actions
```

### 40.3 Requirements traceability matrix
One row per requirement, **stable IDs** (`PV-<AREA>-NNN`). Seed it from **every acceptance criterion in Sections 24, 31.10 and 35 (criteria 1–36 → `PV-AC-001…036`)** and from this table's examples; add rows for every retained v3 requirement. Mark anything out of scope `Deferred` with a reason; never delete inconvenient requirements.

| ID | Requirement | Priority | Phase | Module | Test / evidence | Status | Notes |
|---|---|---|---|---|---|---|---|
| PV-SEC-001 | Cold start requires authentication | P0 | 2 | Security | Auth state test | Planned | |
| PV-STO-001 | Initial scan does not modify source files | P0 | 4 | Storage | Read-only scan test | Planned | |
| PV-STO-002 | Move preserves the source until the destination is verified | P0 | 4 | File ops | Interrupted-transfer test | Planned | |
| PV-STO-003 | Interrupted operations reconcile after restart | P0 | 4 | Recovery | Process-death test | Planned | |
| PV-DB-001 | User data survives supported migrations | P0 | 1 | Persistence | Migration tests | Planned | |
| PV-UI-001 | Every visible control has real behavior | P0 | All | UI | UI test + manual checklist | Planned | |
| PV-PRV-001 | Notifications show no media titles by default | P1 | 6 | Privacy/player | Notification inspection | Planned | |
| PV-PLY-001 | Headphone removal stops playback and sets volume to 0 | P1 | 6 | Player | Audio-device-removed test + device check | Planned | |
| PV-PLY-002 | Phone lock pauses video; music continues | P1 | 6 | Player | Lock-state test + device check | Planned | |
| PV-CAM-001 | Intruder photo is opt-in and reports capture failure honestly | P2 | 8 | Security | Permission/camera tests | Planned | |
| PV-LAY-001 | No clipping at any size in the layout matrix | P1 | 9 | UI | Screenshot tests | Planned | |
| PV-REL-001 | Release build and required tests have recorded evidence | P0 | 11 | Release | Build log + test report | Planned | |

The rows above are templates, not evidence that anything is implemented or tested.

### 40.4 Threat model (`SECURITY_AND_PRIVACY.md`)
For each threat state **mitigated / partially mitigated / not protected**, with plain-language residual risk:

| Threat | Expected position |
|---|---|
| Someone casually opens the app on your unlocked phone | Mitigated by the cover screen, PIN/biometric and auto-lock |
| Someone inspects installed apps and Android settings | **Partially**: the disguise does not hide the app from system screens |
| Someone removes the SD card and reads it elsewhere | **Not protected** (files are unencrypted in Hidden Mode) |
| Someone copies exported or shared files | **Not protected** once a file leaves the vault |
| Someone gains access after the device is unlocked | Partially: re-auth for sensitive actions, auto-lock |
| Android kills the process or revokes a permission | Handled gracefully (recovery UX), not preventable |
| A damaged or removed SD card | Index survives; media may not (no media backup unless the user makes one) |
| Accidental deletion, failed move, interrupted migration | Mitigated by trash, journal and copy-verify-delete |

Recovery expectations to document: lost folder permission; SD card replaced or reformatted; app reinstall; database corruption; interrupted operations; forgotten credentials and reset; restoring an index/settings backup; any future encrypted-storage migration. **Do not promise recovery when no recoverable copy exists**; prefer preserving originals and being honest about uncertainty.

---

## 41. Unified Phases, Change Control & Final Reporting

**This section replaces every earlier phase table (Sections 28, 31.11 and the Section 35 phase notes).** Reorder only if repository evidence requires it, and record why.

### 41.1 Phases and exit gates

| # | Phase | Priority | Contents | Exit gate |
|---|---|---|---|---|
| 1 | Audit & foundation | P0 | Inventory (if code exists), version matrix, build baseline, **design/layout tokens (`Dimens.kt`, borders, radii, type)**, shared loading/empty/error/permission components, **app icon assets and black splash**, traceability matrix, `brain.md`, no dummy content | Baseline build status and inventory documented; no unexplained rewrite |
| 2 | Authentication & privacy state | P0 | Onboarding, PIN/password, hashing, lockout, biometric, **auto-lock modes (default 1 min)**, `FLAG_SECURE`, Recents snapshot, reset semantics, **settings registry**, **first-launch permission checklist** | Auth and reset tests pass; security wording corrected |
| 3 | Cover screen & carrier flow | P0/P1 | **Exact Section 8.1 screen**, post-auth panel, SIM Toolkit detection, `ACTION_DIAL` only, verified numbers only | Carrier actions truthful and cannot bypass authentication |
| 4 | SAF & data-integrity foundation | P0 | Folder picker, persisted permission, **read-only scan**, scan state/cancel, permission-loss UX, **operation journal before any destructive operation**, consent-based `.nomedia` step | Failure-path tests show sources are preserved when a transfer cannot be verified |
| 5 | Media index & library UX | P1 | Real metadata and thumbnails, paging, accurate counts and filters, empty/error/unavailable states, hidden-folder wording | Library works with no placeholder items and handles missing/unsupported media |
| 6 | Viewers & basic playback | P1 | Photo viewer; video and audio players with loop off/one/all, brightness/volume/seek, orientation; **headphone-disconnect stop+mute**; **phone-lock rules**; neutral notifications; lifecycle and audio focus | Core playback and interruption tests pass for the formats claimed |
| 7 | File management & export | P1 | Verified copy/move, trash/restore/permanent delete, import wizard with foreground jobs, export/share with warnings, recovery UX, bulk confirmations | No operation can silently discard an unverified original |
| 8 | Optional privacy features | P2 | **Intruder photo + owner alert**, fingerprint extras, A-B loop and loop count, subtitles/audio tracks, locked albums, playlists; decoy/panic/shake **only if** state separation is proven | Each enabled feature has permission, failure, privacy and retention tests |
| 9 | Responsive layout & polish | P1/P2 | Layout matrix (34.6), tablet/foldable/split-screen, accessibility pass, Urdu scaffolding, animations, error-state review | Matrix passes; no clipping or overlap |
| 10 | Advanced enhancements | P3 | Photo editing, batch rename, compare mode, ratings/notes, share target, other Section 31 extras | No regression in core flows |
| 11 | Release verification | P0 | Build, lint, unit/migration/UI tests, **physical-device checks** (SD card, playback, notifications, camera), manifest/exported-components/backup review, claims review, README/TEST_PLAN/brain/traceability update, final report | No unresolved P0; every limitation documented |

After each phase: list files created or changed, status of each requirement (Section 0.4 vocabulary), what was **not verified**, update the documents in 40.1, then **stop for the user's feedback**.

### 41.2 Change control (answer before adding or changing anything)
Which requirement does it satisfy? Is it already implemented elsewhere? What security, privacy, storage or lifecycle risk does it add? What permission or dependency does it need? How is it tested? Which phase and priority? What happens when it fails? **A smaller set of fully working, tested features beats a long menu of unreliable options.**

### 41.3 Final implementation report format
```markdown
# Implementation Report
## Summary            - What changed / Current release readiness
## Completed          - [Requirement ID] Feature — evidence and test result
## Partially Completed- Feature — what works, what does not, why
## Deferred / Blocked - Feature — reason and dependency
## Validation Actually Run - Command / Result / Device or emulator / Limitations
## Data Safety        - Copy/move/delete/trash verified; recovery cases verified; remaining data-loss risks
## Security and Privacy - Auth checks; permissions; offline/network verification; claims and limitations documented
## Changed Files      - Path — purpose
## Known Issues       - Issue — impact — next step
## Next Recommended Phase
```
Never fabricate completion status, logs, tests, device results, carrier details or security guarantees.

### 41.4 Final acceptance checklist (in addition to criteria 1–36)
- [ ] Repository and spec were inspected before code changes (existing-project case); build/dependency baseline documented.
- [ ] No dummy media, fake counts or fabricated success states in production UI.
- [ ] Cold start and reboot require authentication; lockout survives recreation; credentials never plaintext or logged.
- [ ] Hidden Mode, `.nomedia`, the disguise and encryption are described accurately everywhere.
- [ ] Initial indexing is read-only; `.nomedia` is added only after consent and never overwrites an existing file.
- [ ] Copy/move/delete/trash have explicit semantics; source deletion never precedes verification; interrupted operations reconcile after restart.
- [ ] Export/share warns that files leave the vault and uses safe URIs.
- [ ] Supported formats documented honestly; corrupt/missing/unsupported files fail gracefully.
- [ ] Loop, headphone-disconnect and phone-lock rules behave as specified and are tested.
- [ ] Intruder photo is opt-in, best-effort, and does not crash on denial or capture failure.
- [ ] Layout matrix passes; touch targets ≥ 48 dp; every visible control works or is marked unavailable.
- [ ] Required builds and tests were actually run; device checks are recorded or clearly left unverified.
- [ ] Traceability, `brain.md`, `README.md`, `TEST_PLAN.md` and technical decisions match the code; no P0 blocker is unreported.

### 41.5 Final instruction to the builder
Implement against the existing codebase (if any) and this whole specification. Where parts conflict, do not silently choose the easier one: record it, follow the **safer, data-preserving** behavior, and flag decisions that need the owner. Do not remove a requirement without recording it as `Deferred` or explaining the approved change. Work in small, verifiable phases; prioritise authentication correctness, local-only privacy, safe SD-card operations, real functionality and honest behavior over visual extras. Do not stop after writing a plan: implement the phase, run the checks that exist, update the documents and report the actual outcome. **Never claim the app is complete while a required feature is unimplemented or unverified.**

---

## Appendix A — Icon master source (`sim_service_icon.svg`)

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#2E3035"/><stop offset="1" stop-color="#1C1D21"/></linearGradient>
    <linearGradient id="squircle" x1="0.15" y1="0" x2="0.85" y2="1"><stop offset="0" stop-color="#F1F2F4"/><stop offset="0.55" stop-color="#D9DCE1"/><stop offset="1" stop-color="#BFC3CA"/></linearGradient>
    <linearGradient id="sim" x1="0.2" y1="0" x2="0.8" y2="1"><stop offset="0" stop-color="#232427"/><stop offset="1" stop-color="#0B0B0D"/></linearGradient>
    <linearGradient id="gold" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#FFE08A"/><stop offset="0.35" stop-color="#F7B84A"/><stop offset="0.7" stop-color="#EE8E2A"/><stop offset="1" stop-color="#C96A16"/></linearGradient>
    <linearGradient id="sheen" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#FFF" stop-opacity="0"/><stop offset="0.5" stop-color="#FFF" stop-opacity="0.35"/><stop offset="1" stop-color="#FFF" stop-opacity="0"/></linearGradient>
  </defs>
  <rect width="1024" height="1024" fill="url(#bg)"/>
  <rect x="152" y="152" width="720" height="720" rx="210" fill="url(#squircle)"/>
  <rect x="156" y="156" width="712" height="712" rx="206" fill="none" stroke="#FFF" stroke-opacity="0.55" stroke-width="3"/>
  <g transform="translate(0 52)">
    <path d="M375 255 H585 L665 335 V645 Q665 665 645 665 H375 Q355 665 355 645 V275 Q355 255 375 255 Z" fill="url(#sim)" stroke="url(#sim)" stroke-width="14" stroke-linejoin="round"/>
    <path d="M375 262 H582 L658 338 V645 Q658 658 645 658 H375 Q362 658 362 645 V275 Q362 262 375 262 Z" fill="none" stroke="#3A3C41" stroke-opacity="0.6" stroke-width="2.5"/>
    <g transform="translate(0 -27)">
      <rect x="417" y="402" width="190" height="170" rx="28" fill="url(#gold)"/>
      <rect x="417" y="402" width="190" height="170" rx="28" fill="none" stroke="#A3540F" stroke-opacity="0.7" stroke-width="3"/>
      <g stroke="#8A4710" stroke-opacity="0.6" stroke-width="5" stroke-linecap="round" fill="none">
        <path d="M417 459 H472 M552 459 H607 M417 515 H472 M552 515 H607 M472 402 V459 M552 402 V459 M472 515 V572 M552 515 V572"/>
        <rect x="472" y="459" width="80" height="56" rx="12"/>
      </g>
      <rect x="417" y="402" width="190" height="170" rx="28" fill="url(#sheen)" opacity="0.7"/>
      <path d="M440 414 H584" stroke="#FFF" stroke-opacity="0.5" stroke-width="4" stroke-linecap="round"/>
    </g>
  </g>
</svg>
```

---

## Appendix B — What was merged from where

| Area | From my v3 spec | From the ChatGPT addendum | Result in v4 |
|---|---|---|---|
| Product, UI, cover screen, icon, players, loop, headphone/lock rules, intruder photo, layout system, file manager features, settings catalogue | ✔ (sole source) | — | Kept intact (Sections 1–35) |
| Builder rules and status vocabulary | Partial (honesty rules) | ✔ detailed workflow and 7-status vocabulary | Section 0 |
| Release priorities P0–P3 | — | ✔ | Section 2.2 mapped onto every feature |
| Existing-project audit workflow | — | ✔ | Section 0.1-B |
| Dependency compatibility | "Verify latest" | ✔ "no blind upgrades" | Section 2.3 |
| State model, migrations, threading | Partial | ✔ | Sections 4.1–4.4 |
| Security model clarity, auth state machine, task-snapshot caveat, disguise/decoy honesty | Partial | ✔ | Section 9.10 |
| Operation semantics, copy-verify-delete, journal, trash, `.nomedia` consent, scan quality | Partial (basic ops) | ✔ | Sections 14.4–14.9 |
| Intruder guardrails | ✔ feature design | ✔ best-effort rules | Sections 32 and 32.7 |
| Playback state model and precedence | ✔ rules | ✔ lifecycle model | Section 33.5 |
| Settings registry | ✔ long catalogue | ✔ registry discipline | Section 36 |
| Import/export/share/backup/reset flows | Partial | ✔ | Section 37 |
| Error UX, tests, device evidence, build evidence, Definition of Done | Partial | ✔ | Sections 38–39 |
| Traceability, documentation set, threat model | `brain.md` only | ✔ | Section 40 |
| Phases, change control, final report | 10 + 1 phases | 10 different phases | One unified plan, Section 41 |
| Conflicts (12) | — | — | Section 2.1 |
