# Private Vault: Comprehensive UX, Media & Security Upgrade

A complete enhancement suite for the Private Vault application, introducing an advanced gesture-driven video player with Picture-in-Picture, structured album/folder organization, private documents and notes, multi-disguise options, and a hardware sensor flip-to-lock panic trigger.

## User Review & Critical Decisions

> [!IMPORTANT]
> In Phase 1 clarification, you selected **"all"** across all three upgrade vectors:
> - **Video Player Upgrades**: Gesture-based swipe brightness & volume, double-tap seek, variable playback speeds (0.5x–2.0x), Picture-in-Picture (PiP), and aspect ratio toggling.
> - **Vault Organization & Content**: Custom albums and folders, multi-selection batch actions (batch export, batch delete, batch move), search & timeline filtering, and support for secure text notes and documents alongside photos, videos, and audio.
> - **Stealth & Security Upgrades**: Accelerometer-driven flip-to-lock panic gesture, intruder attempt logging with failed PIN logs, and multi-disguise switcher (SIM Service, Calculator, and Notes disguise).

- **Confirmed Decision 1**: All major video player controls (gestures, speed, PiP, aspect ratio) will be natively integrated into `VideoPlayerScreen.kt`.
- **Confirmed Decision 2**: Storage and Room schema will support Folders/Albums (`folderName`) and Document/Note types (`DOCUMENT`, `NOTE`) without breaking backwards compatibility.
- **Confirmed Decision 3**: The disguise layer will support a modular switcher in Settings so users can choose between the classic Pakistani SIM Service disguise, a functional Calculator disguise, or a simple System Notes disguise.
- **Recommended Default**: Accelerometer flip-to-lock is enabled by default with a sensitivity threshold of Z < -8.0 m/s² (phone facedown), with a toggle in Security Settings.

---

## 1. Overview & Core Concept

- **What It Does**: Evolves Private Vault into a full-featured, zero-leak private media and document locker with a military-grade disguise interface. It combines seamless media playback (advanced gesture ExoPlayer) with rich content management (photos, videos, audio, documents, and encrypted text notes), organized in custom albums, guarded by biometric/PIN authentication, decoy modes, and panic gestures.
- **Target Audience / Persona**: Users seeking complete personal media and document privacy on their Android device, wanting both discreet plausible deniability (unassuming disguise) and a delightful media consumption experience.
- **Key Value**: Eliminates the compromise between stealth and power: users get a top-tier media player and document organizer without sacrificing stealth or risking accidental exposure.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Cover / Disguise Entry**:
   - The user opens the app to their selected disguise:
     - **SIM Service (Default)**: Authentic Jazz/Zong carrier utility screen.
     - **Calculator Disguise**: A fully functional arithmetic calculator where entering `=PIN=` or secret sequence triggers the vault unlock.
     - **Notes Disguise**: A clean daily shopping/todo note list where long-pressing the header triggers authentication.
   - Decoy PIN entry unlocks an empty or dummy content vault; Master PIN or Biometric unlocks the genuine vault.

2. **Vault Navigation & Album Organization**:
   - Primary bottom navigation with 5 distinct destinations: **Media (Photos/Videos)**, **Audio**, **Documents & Notes**, **Albums**, and **Settings**.
   - Top action bar featuring instant real-time search, multi-select trigger, sort options (Date, Name, Size), and a quick manual Lock button.
   - Floating Action Button with an animated expandable speed-dial: **Import Media**, **Import File/Doc**, **New Secret Note**, and **Create Album**.

3. **Multi-Select Batch Actions**:
   - Long-pressing any media item activates selection mode with a floating bottom action dock: **Export/Restore to Public Storage**, **Move to Album**, **Favorite**, and **Delete**.
   - Select-all and counter badge header ("X items selected").

4. **Advanced Video Player UX**:
   - Immersive edge-to-edge landscape and portrait playback.
   - **Left half vertical swipe**: Adjusts screen brightness (0% to 100%) with animated HUD indicator.
   - **Right half vertical swipe**: Adjusts media volume with animated HUD indicator.
   - **Double tap left/right**: Smooth 10-second skip backward / forward with ripple feedback.
   - **Top bar controls**: Playback speed dropdown (0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x), Aspect Ratio cycler (FIT, FILL, ZOOM, 16:9), and native Android Picture-in-Picture (PiP) button.
   - **Controls Auto-Hide**: HUD fades out smoothly after 3 seconds of inactivity.

5. **Flip-to-Lock Panic Gesture**:
   - If someone approaches while the vault is open, the user simply flips the phone facedown onto a table.
   - The hardware accelerometer detects the orientation change and instantly clears the session, locks the vault, and navigates immediately back to the disguise screen.

### Visual Identity & Theme

- **Aesthetic Direction**: Deep cinematic dark mode (Dark Carbon `#0B0D12`, Surface Elevated `#14181F`, Surface Card `#1C2230`).
- **Accent Gradients**: Refined Violet-to-Cyan gradient (`#7C5CFF` to `#22D3EE`) for active tabs, player scrubbers, and FAB buttons, maintaining high contrast against dark surfaces.
- **Typography & Rhythm**: Bold, clean sans-serif typography with generous touch targets (min 48dp) and consistent 8dp spacing grid.
- **Empty States**: Elegant custom vector illustrations and friendly guidance when albums or document categories are empty.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Modular Disguise Architecture**
  - *Chosen Approach*: Implement a `DisguiseMode` enum (`SIM_SERVICE`, `CALCULATOR`, `NOTES`) managed in `PinSecurityManager`.
  - *Why*: Allows users to select the disguise most natural for their region and habits without modifying app manifests or compromising the core security model.
  - *Alternatives Considered*: Multiple launcher icons/aliases (causes OS issues and Play Store policy complications). A unified in-app disguise selector is clean, reliable, and compliant.

- **Decision 2: Room Schema Evolution with Backward Compatibility**
  - *Chosen Approach*: Update `VaultDatabase` to version 2 with a non-destructive migration adding `folderName`, `isNote`, and `noteContent` columns with default values, and supporting `DOCUMENT` / `NOTE` in `MediaType`.
  - *Why*: Prevents any data loss for existing imported media while unlocking album grouping and secret notes.

- **Decision 3: Video Player Native Gestures vs Third-Party Libraries**
  - *Chosen Approach*: Implement gesture detection using Jetpack Compose's native `pointerInput` and `detectDragGestures` alongside Media3 ExoPlayer.
  - *Why*: Keeps the binary lightweight, avoids adding bloated unmaintained external video libraries, and gives 100% fine-grained control over UI animations and HUD styling.

- **Decision 4: Hardware Sensor SensorManager Lifecycle**
  - *Chosen Approach*: Register `Sensor.TYPE_ACCELEROMETER` only while the Vault screen is active (`DisposableEffect`), unregistering immediately on lock, background, or disguise mode.
  - *Why*: Zero battery drain when the app is in the background or locked.

---

## 4. Technical Architecture & Data Strategy

### System Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                              DISGUISE LAYER                               │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌─────────────────┐  │
│  │   SIM Service Screen │  │  Calculator Disguise │  │  Notes Disguise │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └────────┬────────┘  │
└─────────────┼─────────────────────────┼───────────────────────┼───────────┘
              └────────────────────┬────┴───────────────────────┘
                                   │ Authentication (Master / Decoy PIN / Bio)
                                   ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                               VAULT ENGINE                                │
│                                                                           │
│  ┌─────────────────────────┐  ┌───────────────────┐  ┌─────────────────┐  │
│  │     VaultViewModel      │  │ PinSecurityManager│  │  SensorManager  │  │
│  │ (StateFlow / UIEvents)  │  │ (PBKDF2 / Decoy)  │  │ (Flip-to-Lock)  │  │
│  └──────────┬──────────────┘  └───────────────────┘  └─────────────────┘  │
│             │                                                             │
│             ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │                         VaultRepository                             │  │
│  │            SAF File Storage + .nomedia + Document Storage           │  │
│  └──────────┬──────────────────────────────────────────────────────────┘  │
└─────────────┼─────────────────────────────────────────────────────────────┘
              │
              ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                      DATA LAYER (Room Database v2)                        │
│  ┌─────────────────────────────────────────────────────────────────────┐  │
│  │ MediaDao:                                                           │  │
│  │ - getAllMedia(), getMediaByType(), getByAlbum(), getFavorites()     │  │
│  │ - batchDelete(), batchMoveToAlbum(), updateFavorite()               │  │
│  │ - searchMedia(query, type, album)                                   │  │
│  └─────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
              │
              ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                            VAULT SCREENS & UI                             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────────────┐  │
│  │  VaultMainScreen │  │ VideoPlayerScreen│  │ PhotoViewer / NoteEditor│  │
│  │  - Media Grid    │  │  - ExoPlayer v3  │  │  - Zoom / Pan Viewer   │  │
│  │  - Albums View   │  │  - Gestures HUD  │  │  - Secure Note Editor   │  │
│  │  - Documents Tab │  │  - PiP & Speeds  │  │  - Intruder Log Viewer  │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────────────┘  │
└───────────────────────────────────────────────────────────────────────────┘
```

### Data Model & Schema Migration

```kotlin
// VaultMediaItem.kt additions
@Entity(tableName = "vault_media")
data class VaultMediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val originalPathOrUri: String = "",
    val originalDirectory: String = "",
    val fileName: String,
    val mimeType: String,
    val mediaType: String,          // PHOTO, VIDEO, AUDIO, DOCUMENT, NOTE
    val fileSizeBytes: Long,
    val durationMs: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val relativePath: String = "",
    val folderName: String = "Default", // Album / Folder name
    val noteContent: String = ""        // In-vault secure note text
)
```

### Implementation Phases

1. **Phase 1: Database & Core Models**:
   - Update `VaultMediaItem`, `MediaType`, `MediaDao`, and `VaultDatabase` to support folders, documents, and notes with Room schema migration.
   - Add intruder attempt tracking and disguise settings to `PinSecurityManager`.
2. **Phase 2: Video Player Overhaul**:
   - Implement vertical drag gestures for brightness & volume with custom HUD indicators.
   - Implement double-tap 10s seek overlays (left & right).
   - Integrate playback speed selection (0.5x to 2.0x), aspect ratio modes, and Android Picture-in-Picture (`Activity.enterPictureInPictureMode`).
3. **Phase 3: Vault Content & Organization Expansion**:
   - Add Albums / Folders tab and filtering in `VaultMainScreen`.
   - Add Documents & Notes tab with a built-in Markdown / text reader & editor.
   - Add multi-selection batch action dock (batch export, batch delete, batch move).
4. **Phase 4: Stealth & Security Enhancements**:
   - Add Accelerometer flip-to-lock sensor listener in `MainActivity` / `VaultMainScreen`.
   - Add Calculator disguise mode and Notes disguise mode.
   - Add Intruder Log screen in Settings.
5. **Phase 5: Verification & Polish**:
   - Compile and verify through `compile_applet`.
   - Run Robolectric unit tests to ensure all existing security, auto-lock, and media operations pass without regressions.
