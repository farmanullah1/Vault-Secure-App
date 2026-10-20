# Private Vault — Master Build Specification (v3)

> **How to use this file:** Give this entire file to Google AI Studio as the single source of truth. It merges the original spec and the v2 addendum into one document and expands it with UI design, player details, file management and more. Where this file and any earlier file conflict, **this file wins**.

---

## 0. Read This First (Instructions to the AI building the app)

1. **Be honest about what you can and cannot do.** If your environment cannot compile or run Android, do **not** claim a build, test, or run passed. Output the full project as individual files with exact relative paths so the user can paste them into Android Studio, build, and send errors back.
2. Work **in phases** (Section 30). After each phase: list files created, list what was **not verified**, then stop and wait for the user's build feedback.
3. **Never invent** carrier phone numbers, USSD codes, package names, or API behaviour. If unsure, say so and make it configurable.
4. **Never show fake UI that pretends to be something it isn't** (e.g. a fake SIM Toolkit menu, fake carrier branding, fake "encrypted" claims).
5. Use only maintained, mainstream libraries (listed in Section 5). Pin versions in a version catalog (`libs.versions.toml`).
6. Every user-facing claim in the app must be literally true (e.g. "Hidden", not "Encrypted", unless Encrypted Mode is actually built).
7. Create and maintain a `brain.md` file at the project root (Section 29) recording decisions, state, and known gaps after each phase.

---

## 1. Product Summary

**Working name:** *Private Vault* (user may rename; keep the name in one string resource).

A native **Android** app that:

- Keeps the user's **photos, videos and audio** on the **SD card** (the phone's internal storage is full).
- **Hides them from the phone's gallery ecosystem** (Google Photos, Gallery, Instagram/WhatsApp/Facebook pickers, Chrome/browser pickers) while keeping them reachable from **this app** and from a **file manager** if the user navigates to the folder.
- Opens behind a **disguised cover screen** that looks like a normal SIM/carrier utility, with two cards: **Jazz** and **Zong**.
- Tapping a card requires **fingerprint or PIN/password**. After success the user reaches a **post-auth panel** (carrier actions + Open Vault), then the vault.
- Inside the vault: three sections — **Photos**, **Videos**, **Audio** — each with a premium, polished viewer/player.
- Is **beautiful, fast and smooth**: this is a hard requirement, not a nice-to-have.

**Important truth to state in-app (Settings → About privacy):** This is **Hidden Mode** (removed from gallery indexing), **not encryption**. Anyone who opens the folder in a file manager or reads the card on a PC can see the files. An optional **Encrypted Mode** is listed as a future phase (Section 28).

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

---

## 5. Dependencies (verify latest stable versions at build time)

- `androidx.compose` BOM, Material 3, Navigation Compose, Activity Compose, Lifecycle (runtime/viewmodel compose)
- `androidx.media3`: exoplayer, ui, session, common, datasource (optional: `decoder-ffmpeg` only if built manually — see Section 12)
- `androidx.room` (runtime, ktx, compiler via KSP), `androidx.work:work-runtime-ktx`
- `androidx.datastore:datastore-preferences`
- `androidx.biometric:biometric`
- `androidx.documentfile:documentfile`
- `io.coil-kt.coil3` (compose, video, gif)
- `com.google.dagger:hilt` (optional)
- `androidx.exifinterface:exifinterface`
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
- **Do NOT** request: `INTERNET`, `READ_PHONE_STATE`, `CALL_PHONE`, `MANAGE_EXTERNAL_STORAGE`, contacts, location, camera, mic.

`<queries>` block for SIM Toolkit package names (Section 10.3) and for external-player intents.

Other manifest items:
- `android:allowBackup="false"` and data-extraction rules excluding everything (so PIN data isn't backed up).
- Disable activity transition leaks in recents (`FLAG_SECURE`, Section 9).
- A `FileProvider` is **not** needed if content URIs from SAF are shared directly with temporary grants.
- Launcher: one activity alias for the cover screen. Optional **alternate launcher aliases** (Section 10.6) to let the user choose a plain "SIM Services" or "Utilities" icon/name.

---

## 7. Design System ("make it look amazing")

### 7.1 Visual identity
Two **distinct personalities** inside one app:

1. **Cover / SIM section — "Normal but very good":** looks like a clean, trustworthy telecom utility. Calm, light-leaning, familiar. Nothing about it should look like a vault.
2. **Vault — "Premium & private":** deep, cinematic, dark. Feels like a high-end media app (think Apple Photos meets a pro video player).

### 7.2 Color
- **Cover (light default):** background `#F6F8FB`, surface `#FFFFFF`, primary text `#0F172A`, secondary `#64748B`, accent neutral blue `#2563EB`. Cards use two **generic, own-designed** accent gradients (e.g. warm red-orange and cool teal) — **not** carrier brand colors or logos.
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
First launch only: Onboarding (welcome → set PIN → biometric opt-in → choose storage mode → pick folder → scan)
```

### 8.1 Cover screen (the disguise) — detailed
```
┌──────────────────────────────┐
│  SIM Services          ⋮     │  ← neutral title; no "vault" wording
│                              │
│        ┌────────────┐        │
│        │  ▭ SIM chip │       │  ← own-designed generic SIM card illustration
│        └────────────┘        │     with soft animated shine (subtle)
│   Network status (static)    │
│                              │
│  ┌──────────┐ ┌──────────┐   │
│  │  Jazz    │ │  Zong    │   │  ← two equal cards, own gradients, text labels
│  │  📞 Call │ │  📞 Call │   │
│  └──────────┘ └──────────┘   │
│                              │
│  Recent: —   Help   Settings │
└──────────────────────────────┘
```
- Both cards trigger authentication **before any action**.
- A discreet **long-press on the SIM illustration** may be offered as an *optional* alternate unlock (off by default) — but standard flow must always work.
- "⋮" menu contains only generic items (About, Help). Settings are reachable **only after auth** (so a bystander can't discover the vault).

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

### 9.4 Session & auto-lock
- Unlocking via either carrier card unlocks the whole session.
- Auto-lock on background: Immediate / 30 s / 1 min / 5 min (default: 30 s). Re-lock on screen-off.
- **Media playback exception:** audio may continue in the background with lock-screen controls, but the **UI** re-locks; notification shows neutral text (hide title option, default on).

### 9.5 Screen privacy
- `FLAG_SECURE` **on by default** (blocks screenshots, hides content in recents) with a Settings toggle.
- Notification content hidden on lock screen by default.
- Recents thumbnail shows the cover screen or blank.

### 9.6 Forgot PIN
Offer **"Reset app"**: clears the app's database, settings and credentials **only**. **Media files on the SD card are never touched.** The user re-selects the folder and re-indexes. State plainly: because Hidden Mode isn't encryption, a reset doesn't expose anything an attacker couldn't already read from the card. Require a confirmation step, and offer a 10-second undo-free explanation screen.

### 9.7 Optional extras (build if time allows, flagged in brain.md)
- **Decoy PIN:** a second PIN opens a harmless empty/benign state.
- **Intruder log:** local-only log of failed attempts (time, count). No camera capture unless the user explicitly enables it and grants permission.
- **Panic gesture:** quick double-shake or a fast back-press sequence returns to the cover screen and locks.
- **Disguise icon/name** choices (Section 10.6).

---

## 10. Cover Screen & Carrier / SIM Flow

### 10.1 Rules
- Generic SIM illustration only; **no carrier logos**, no "official" wording.
- Cards: **Jazz** and **Zong** (text labels and own gradients).
- Tapping a card → authenticate → post-auth panel. No carrier action and no vault access before success.

### 10.2 Helpline calling
- Use `Intent.ACTION_DIAL` with a pre-filled `tel:` URI. **Never auto-call.** No call permission.
- Numbers are **user-editable in Settings** (after auth). Pre-fill only with numbers the builder has verified from the carriers' **official websites at build time**; if not verified, leave empty and prompt the user to enter one. Do not hardcode unverified numbers or USSD codes. If USSD is supported later, encode `#` as `%23` and let the system dialer handle it.

### 10.3 SIM Toolkit
- On many devices the SIM Toolkit is an installed launchable app (commonly package `com.android.stk`, `com.android.stk2` for a second SIM, plus vendor variants).
- Declare candidates in `<queries>`, resolve with `PackageManager.getLaunchIntentForPackage`, and launch **only if found**.
- If not found: show "SIM Toolkit isn't available on this device" and offer the dialer. **Never draw a fake SIM Toolkit menu.**
- Handle: no SIM, dual SIM (let the system choose), missing dialer, and OEM-specific names. Make the candidate package list editable in a hidden "Advanced" screen.

### 10.4 "Looks normal but very good" checklist
- Clean spacing, readable type, subtle shadows, smooth card press animation (scale 0.97), ripple, and tasteful micro-interactions.
- Localised strings; works in light/dark.
- Never any mention of "vault", "hidden", or "private" on this screen.

### 10.5 Auth gate details
- Biometric prompt title/subtitle should be neutral: e.g. "Confirm it's you" / "Required to continue".
- If biometrics unavailable → PIN keypad (custom, large touch targets, shuffle-keys option, haptics, dots animation).

### 10.6 Disguise options (optional)
- Alternate launcher aliases (e.g. "SIM Services", "Utilities", "Calculator-style" is **not** to be faked as functioning calculator unless fully built). Changing alias may need app restart; explain this.

---

## 11. Storage Model

### 11.1 Three modes (selected during onboarding; changeable)
1. **Index in place (recommended default):**
   - The user picks `new` (or any folder) via `ACTION_OPEN_DOCUMENT_TREE`; persist permission with `takePersistableUriPermission`.
   - App creates `.nomedia` in the folder (and in each subfolder, if the user enables "hide subfolders too") and **verifies** it exists.
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
- **Playlist/queue:** play next in folder, shuffle, repeat (off/one/all), next/previous.
- **Keep screen on** during playback; **sleep timer**.
- **A-B loop**, frame-step (when paused), screenshot of current frame (saves **into the vault**, not the gallery).
- **Picture-in-Picture** (optional, off by default for privacy; if enabled, auto-lock rules still apply).
- Seek-bar **thumbnail previews** (generate sprite or use `MediaMetadataRetriever` at intervals; cache).
- Video info sheet: container, codecs, resolution, fps, bitrate, size, duration, HDR flag.
- Error screen with **real** error: container, codec, reason, and action buttons (Open with another player, Remove from index, Details).
- Audio focus handling; pause on headphone unplug (`ACTION_AUDIO_BECOMING_NOISY`); Bluetooth/media-button support.
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

- **Security:** change PIN/password, biometrics toggle, auto-lock time, FLAG_SECURE, hide notification content, decoy PIN, intruder log, "Go straight to Vault".
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

- Plain-language errors with a next step; never raw exception text to users (keep in diagnostics).
- Cases to design for: SD card removed, permission revoked, corrupt file, unsupported codec, full storage, name collision, cancelled job, biometric hardware unavailable, no SIM, dialer missing, FAT32 limit.
- No crash on any of the above. Add a global handler that logs locally only.

---

## 22. Delivery Reality for Google AI Studio

- If the AI Studio environment cannot compile Android, **do not claim a build or test passed.** Output the full project as individual files with exact relative paths (Gradle files, manifest, Kotlin sources, resources), phase by phase.
- Run checks only if the environment truly supports them; otherwise list them under **"Not run"**.
- Provide `README.md` with steps to build a **debug APK** in Android Studio and **sideload** it (enable "install unknown apps" for the file manager/browser used; Play Protect may warn for sideloaded apps).
- Alternative if the user can't use Android Studio: describe building via GitHub Actions workflow (provide a ready `.github/workflows/android.yml` that assembles a debug APK as an artifact). Mark as untested unless verified.

---

## 23. Testing Plan

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

1. Cover screen shows a generic SIM illustration with **Jazz** and **Zong** cards and **no vault wording**.
2. Both cards require biometric/PIN **before** any carrier or vault access.
3. SIM Toolkit launches only when actually installed; otherwise a truthful message appears. No fake menus.
4. **Index in place** works: the only change on the SD card is the added `.nomedia`.
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

## 28. Phased Delivery

| Phase | Deliverable |
|---|---|
| 1 | Project skeleton: Gradle (version catalog), manifest, theme/design system, navigation, `brain.md`, `README.md` skeleton |
| 2 | Onboarding, PIN setup/entry, biometric, lockout, auto-lock, FLAG_SECURE |
| 3 | Cover screen (Jazz/Zong), post-auth panel, SIM Toolkit detection, dialer flow |
| 4 | Folder picker, persisted permission, `.nomedia`, scan, Room index, Photos/Videos/Audio grids, search/sort/filter |
| 5 | Photo viewer (zoom/pan/rotate/slideshow/info) |
| 6 | Video player (ExoPlayer, gestures, brightness/volume, orientation, subtitles, resume, external-player fallback) |
| 7 | Audio player, mini player, `MediaSessionService`, queue, sleep timer |
| 8 | File management: rename/move/copy/trash/restore/export/import, jobs with foreground service, duplicate finder, integrity check |
| 9 | Storage dashboard, Gallery Visibility Checklist, Settings completion |
| 10 | Polish: animations, skeletons, accessibility, tablet/landscape, error states, Urdu scaffolding, tests, `TEST_PLAN.md`, final README |

**After each phase:** list files created, list what was **not** verified, update `brain.md`, then stop for the user's build feedback.

---

## 29. `brain.md` Template (create at project root, update every phase)

```
# brain.md
## Decisions (storage mode, DI choice, libraries + versions)
## Current phase & status
## Files created this phase
## Not verified / Not run
## Known issues & workarounds (SAF quirks, device-specific)
## Open questions for the user
## Next steps
```

---

## 30. User Info the Builder Should Ask For (once, in Phase 1)

1. Android version and phone model.
2. SD card size, free space and format (FAT32 / exFAT / adoptable).
3. Whether the user has Android Studio, or prefers the GitHub-Actions APK build.
4. Confirm: Entry flow Option A, Storage mode Index-in-place (defaults in Section 2).
5. Preferred app name and launcher disguise name.
6. Verified helpline numbers for Jazz and Zong (or leave blank).
