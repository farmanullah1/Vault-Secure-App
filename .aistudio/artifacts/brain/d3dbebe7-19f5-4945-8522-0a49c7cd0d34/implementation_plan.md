# Private Vault — Disguised Media Safe

A discreet, high-performance Android private media vault designed to safely organize, view, and play hidden photos, videos, and audio files behind an innocent SIM Services cover interface (featuring Jazz and Zong carrier shortcuts).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on your responses:

- **Confirmed Decision 1 (Entry Flow)**: Disguised SIM screen displaying clean carrier cards for **Jazz** and **Zong**. Tapping either card prompts for PIN/Biometric authentication. Successful authentication unlocks the carrier actions (Dial Helpline, check SIM Toolkit) alongside a secure **Open Vault** button.
- **Confirmed Decision 2 (Storage Handling)**: **Copy or Move** files into a dedicated vault folder created on the device or SD card via the Android Storage Access Framework (SAF). The vault automatically creates and maintains a `.nomedia` file to prevent system gallery indexers (Google Photos, WhatsApp pickers, default Gallery) from listing private media.
- **Confirmed Decision 3 (Video Playback & Fallback)**: **Both** integrated Media3/ExoPlayer with detailed codec/container error diagnostics, coupled with an immediate **"Open with external player"** fallback action (via content URI with temporary read grant) for unsupported formats.

---

## 1. Overview & Core Concept

- **What It Does**: Conceals sensitive photos, videos, and music behind an authentic-looking SIM utility screen. Once unlocked, users enter a full-featured media vault offering multi-tab browsing (Photos, Videos, Audio, All), smooth zooming for images, playback controls for videos with speed/seek gestures, background audio playback, batch file import/export, and a gallery visibility checklist.
- **Target Audience**: Users seeking discreet local privacy on Android without risk of accidental exposure in social app pickers or photo feeds.
- **Key Value**: Dual security (plausible deniability via the SIM cover screen + cryptographic PIN verification), local-first data integrity (files remain on your device), and zero data loss during import/move operations.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Cover / Launch Screen (SIM Services)**:
   - App launches into a crisp, utility-style screen titled **"SIM Services"** or **"SIM Toolkit"**.
   - Two prominent cards: **Jazz** and **Zong**, each displaying carrier brand colors (subtle dark red/amber accents), signal/SIM icons, and helpline labels.
   - Tapping either card activates `BiometricPrompt` with numeric PIN fallback.
   - Upon unlocking: displays a bottom sheet or action panel with carrier actions (Helpline dialer, SIM Toolkit launcher if installed) and the primary **"Open Private Vault"** button.

2. **First-Time Setup**:
   - Master 4–6 digit PIN creation with confirmation.
   - Setup of recovery phrase / salt and biometric enrollment toggle.
   - SAF Directory Selection: User selects a dedicated storage folder (e.g. on SD card or internal storage) to serve as the Vault directory. The app creates `.nomedia` and verifies read/write permissions.

3. **Vault Media Dashboard**:
   - Material 3 navigation bar: **Photos**, **Videos**, **Audio**, and **Settings/Checklist**.
   - Grid and list views with thumbnail caching, file size badges, duration badges, and date sorting.
   - Floating Action Button (FAB) for **"Import Media"** (supporting Copy or Move operations with progress dialog).

4. **Media Viewers**:
   - **Photo Viewer**: Full-screen view with pinch-to-zoom, pan, rotation, EXIF metadata viewer, and share/export.
   - **Video Player**: Media3 ExoPlayer with double-tap 10s seek, swipe brightness/volume gestures, aspect ratio toggling (Fit/Crop/100%), playback speed controls (0.5x to 2x), and an external player fallback button if codecs fail.
   - **Audio Player**: Mini-player bar and expanded view with scrubber, repeat/shuffle, playback speed, and background playback support.

5. **Gallery Visibility Checklist & Diagnostics**:
   - Interactive verification guide explaining `.nomedia` behavior.
   - Checks presence of `.nomedia`, provides step-by-step instructions to clear Media Storage cache if legacy thumbnails persist, and clarifies backup status.

### Visual Identity & Theme
- **Theme Direction**: High-contrast, sleek Obsidian Slate with emerald security accents for the vault, paired with an understated telecom aesthetic on the cover screen.
- **Palette**:
  - Background: Deep Carbon (`#0F1115`) & Surface Elevated (`#1A1D24`)
  - Accent / Primary: Secure Emerald (`#10B981`)
  - Jazz Card: Warm Ruby Tint (`#E11D48` accent on dark slate card)
  - Zong Card: Vibrant Amber Tint (`#F59E0B` accent on dark slate card)
  - Text: High-contrast off-white (`#F8FAFC`) and muted slate (`#94A3B8`)
- **Typography**: Clean, geometric sans-serif styling with strict 48dp minimum touch targets.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Disguise & Plausible Deniability**
  - *Chosen Approach*: SIM Services launcher cover with Jazz and Zong options.
  - *Why*: Provides immediate plausible deniability if the phone is picked up or unlocked by others.
  - *Alternative Considered*: Standard lock screen. Passed over to satisfy the user's preference for cover disguise.

- **Decision 2: SAF Copy/Move with `.nomedia`**
  - *Chosen Approach*: Storage Access Framework (SAF) folder picker with persistent URI permissions (`takePersistableUriPermission`). Vault files are copied or moved into this dedicated vault folder, and a `.nomedia` file is written.
  - *Why*: Complies with Android 11+ scoped storage rules without needing intrusive all-files access permissions, works reliably across both internal memory and SD cards, and prevents MediaStore indexing.

- **Decision 3: Authentication & Hash Security**
  - *Chosen Approach*: PBKDF2-HMAC-SHA256 with 10,000+ iterations and a device-generated cryptographic salt stored in EncryptedSharedPreferences / secure storage. Constant-time verification with progressive rate-limiting delays after repeated failed attempts.
  - *Why*: Solid protection against brute-force attacks while running cleanly on Android runtime.

- **Decision 4: Safe Reset Flow**
  - *Chosen Approach*: "Reset App" removes app database, cached thumbnails, and stored PIN, but **never deletes user media files** on the storage volume. Clear disclaimer is presented.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Cover Screen (Disguise)                  │
│       [ Jazz Service Card ]       [ Zong Service Card ]     │
└──────────────────────────────┬──────────────────────────────┘
                               │ Tap + Authenticate
                               ▼
┌─────────────────────────────────────────────────────────────┐
│              Biometric / PBKDF2 Security Gate               │
└──────────────────────────────┬──────────────────────────────┘
                               │ Valid Auth
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Post-Auth Action Hub                        │
│    [ Helpline Dialer ]  [ SIM Toolkit ]  [ OPEN VAULT ]     │
└──────────────────────────────┬──────────────────────────────┘
                               │ Open Vault
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      Private Vault UI                       │
│  ┌──────────────┬──────────────┬──────────────┬───────────┐ │
│  │ Photos Grid  │ Videos Grid  │  Audio List  │  Settings │ │
│  └──────┬───────┴──────┬───────┴──────┬───────┴─────┬─────┘ │
│         │              │              │             │       │
│         ▼              ▼              ▼             ▼       │
│     PhotoViewer    VideoPlayer    AudioPlayer   Checklist   │
│    (Zoom / Pan)     (ExoPlayer)   (Controller)  & SAF Config│
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Repository & Service Layer                 │
│  - VaultRepository (SAF DocumentTree & File I/O)            │
│  - MediaImportService (Copy/Move with Progress)             │
│  - AudioPlaybackService (MediaSession & Background Player)  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Storage                          │
│  - Room Database (Indexed Media, Tags, Metadata)            │
│  - File System via SAF URI (Files + .nomedia)               │
└─────────────────────────────────────────────────────────────┘
```

### Data Models & Entities (Room)
1. **`VaultMediaItem`**:
   - `id`: Long (PrimaryKey)
   - `uriString`: String (SAF document URI)
   - `fileName`: String
   - `mimeType`: String
   - `mediaType`: Enum (`PHOTO`, `VIDEO`, `AUDIO`, `OTHER`)
   - `fileSizeBytes`: Long
   - `durationMs`: Long
   - `dateAdded`: Long
   - `width`: Int, `height`: Int
   - `thumbnailUri`: String?
2. **`VaultFolder`**:
   - `folderUri`: String (Persistent tree URI)
   - `folderName`: String
   - `hasNoMedia`: Boolean
   - `lastScannedTimestamp`: Long

### Key Implementation Steps
1. **Gradle Dependencies**: Configure Media3 ExoPlayer (`androidx.media3:media3-exoplayer`, `media3-ui`, `media3-session`), Coil Compose, Room KSP, Material Icons Extended, and Biometric (`androidx.biometric:biometric-ktx`).
2. **Security & Auth Layer**: Implement `PinSecurityManager` with PBKDF2 hashing, salt generation, rate-limit failure backoff, and `BiometricPrompt` helper.
3. **Disguised Cover Screen**: Build `SimCoverScreen` featuring authentic Jazz & Zong cards, carrier helpline intents (`ACTION_DIAL`), SIM Toolkit package detection (`com.android.stk`, `com.android.stk2`), and secure unlock transition.
4. **SAF & Media Engine**: Build `VaultFileManager` to handle SAF directory access, `.nomedia` generation, batch file copy/move with progress tracking, and file deletion.
5. **Room Database**: Create `VaultDatabase`, `MediaItemDao`, and `VaultRepository`.
6. **Vault Dashboard & Grids**: Compose UI with tabs (Photos, Videos, Audio), search, sorting, multi-select batch export/delete, and import picker (`ActivityResultContracts.OpenMultipleDocuments`).
7. **Media Viewers**:
   - Photo Viewer with gesture-based pan and zoom.
   - Video Player with ExoPlayer, custom playback controls, orientation lock, double-tap seek, and "Open with External App" content Intent fallback.
   - Audio Player with scrubber and background playback capability.
8. **Gallery Visibility Checklist**: Interactive self-service guide verifying `.nomedia` and instructing user on MediaStore cache refresh.
