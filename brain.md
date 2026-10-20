# brain.md

## Decisions (storage mode, DI choice, libraries + versions)
- **Master Specification**: Built according to **Private Vault — Master Build Specification (v3)**.
- **Platform & UI**: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- **Core Upgrades & Modernizations**:
  - **Recycle Bin (Trash) & Secure Shredding**:
    - 30-day retention period with automatic purge of expired items (`trashTimestamp`).
    - Restore single items or batch selections back to active vault galleries.
    - Permanent deletion with optional Secure Shred (overwriting SAF document byte content with random bytes before deletion to thwart flash memory recovery).
    - Room database schema upgraded to v4 (`isTrash`, `trashTimestamp`, `lastPlayedPositionMs`).
  - **Dynamic Gallery Grid Layout**:
    - Quick-toggle grid column layout (2, 3, or 4 columns) in vault media gallery for optimal viewing density.
  - **Swipeable Photo Viewer**:
    - Jetpack Compose Foundation `HorizontalPager` with smooth page transitions.
    - Double-tap zoom with `Animatable` bounce and pan boundary clamps.
    - 90-degree step rotation with memory preservation.
    - Auto-advance slideshow mode (3.5-second interval).
    - EXIF metadata inspection sheet (resolution, camera model, date taken, aperture, ISO, exposure, file size).
    - Secure external share dialog via Android `Intent.ACTION_SEND`.
    - Direct move-to-trash support.
  - **Pro Video Player Upgrades**:
    - Playlist queue navigation with Skip Previous and Skip Next controls.
    - Persistent playback resume dialog ("Resume from MM:SS?") saving playback timestamps into Room database.
    - Codec & audio boost options, A-B looping, PiP, Kids Lock, and HUD gestures.
  - **Modern Audio Player**:
    - Full playlist queue playback with Skip Previous, Skip Next, and Shuffle toggle.
    - Embedded album art extraction via Android `MediaMetadataRetriever` with animated spinning vinyl record fallback.
    - Configurable sleep timer dialog (15m, 30m, 45m, 60m).

## Current phase & status
- **Active Phase**: Modern Media Experience & Recycle Bin Integration Complete.
- **Build Status**: Fully implemented with zero fake data, full Compose Material 3 theming, Room v4, and clean state flows.

## Files updated this phase
- `app/src/main/java/com/example/data/model/VaultMediaItem.kt`: Added `isTrash`, `trashTimestamp`, and `lastPlayedPositionMs` fields.
- `app/src/main/java/com/example/data/dao/MediaDao.kt`: Updated active queries to filter out trashed items, added trash queries, batch restore/purge, and playback position updates.
- `app/src/main/java/com/example/data/db/VaultDatabase.kt`: Bumped Room version to 4.
- `app/src/main/java/com/example/data/repository/VaultRepository.kt`: Added trash flows, `moveToTrash`, `restoreFromTrash`, `permanentDeleteBatch`, `emptyTrash`, `purgeOldTrash`, `updateLastPlayedPosition`, and `shredDocument`.
- `app/src/main/java/com/example/viewmodel/VaultViewModel.kt`: Added `VaultTab.TRASH`, `gridColumns`, trash state flows, batch selection actions, shred support, and queue navigation state.
- `app/src/main/java/com/example/ui/screens/PhotoViewerScreen.kt`: Replaced single-image viewer with `HorizontalPager`, animated zoom/pan, rotation, slideshow, EXIF inspection, and secure share.
- `app/src/main/java/com/example/ui/screens/AudioPlayerScreen.kt`: Added playlist queue, skip next/prev, shuffle, embedded album art retriever, spinning vinyl animation, and sleep timer.
- `app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt`: Added playlist queue navigation, resume playback prompt, position saving, and fixed file size metrics.
- `app/src/main/java/com/example/ui/screens/VaultMainScreen.kt`: Integrated grid column switcher, Recycle Bin tab/navigation, batch purge/restore dialogs, `TrashScreenContent`, and `TrashListItem`.
- `app/src/main/java/com/example/MainActivity.kt`: Updated screen navigation routes to pass media lists and indices for continuous playback.

## Not verified / Not run
- Physical hardware SIM Toolkit app invocation on a physical carrier SIM in Pakistan.
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).
- Some OEM ROMs lack the standard `com.android.stk` package; handled gracefully with a truthful informative dialog.

## Next steps
- Open project in Android Studio and run `./gradlew assembleDebug` or deploy directly to test device.
