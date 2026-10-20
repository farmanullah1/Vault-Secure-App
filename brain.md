# brain.md

## Decisions (storage mode, DI choice, libraries + versions)
- **Master Specification**: Built according to **Private Vault — Master Build Specification (v3)**.
- **Platform & UI**: Native Android (Kotlin, Jetpack Compose, Material 3, Single-Activity architecture).
- **Video Player Architecture & Capabilities (Spec Section 12 & Enhancements §31.4)**:
  - **Security (Kids Lock)**: Touch lock mode that intercepts touch inputs and system back navigation. Touching anywhere on screen triggers colorful expanding canvas particle/ripple animations with a floating unlock badge.
  - **Screen & Display Modes**:
    - Picture-in-Picture (PiP) multitasking via Android `PictureInPictureParams`.
    - Background playback (audio-only playback continues when app is backgrounded / minimized).
    - Aspect Ratio adjustments (Fit Screen, Stretch Fill, Crop / Zoom, 16:9, 4:3, Original).
    - Screen Autorotation (Auto Sensor, Landscape, Portrait).
    - A-B Loop repeating (Point A, Point B, seamless looping and timeline visual markers).
  - **Intuitive Gesture Controls**:
    - Swipe up/down on left half: Window brightness with HUD percentage and progress bar.
    - Swipe up/down on right half: Media volume with HUD percentage and progress bar.
    - Horizontal swipe across middle: Interactive timeline scrub seek with timestamp preview.
    - Two-finger pinch to zoom (0.75x to 4.0x) and pan across video frame; double tap resets zoom.
    - Long-press fast-forwarding (2.0x playback speed while held) and speed selector (0.25x to 3.0x).
  - **Top Navigation Bar & On-Screen Status**:
    - Video title, back button, real-time clock overlay, battery percentage overlay.
    - Decoder Switcher (HW / HW+ / SW).
    - Audio Track Switcher dialog (language and channel selection).
    - Subtitle toggle & search.
    - Top-right menu: Sleep Timer, Media Details / Codec sheet, External Player fallback (VLC / MX).
  - **Interactive Timeline Bar**:
    - Scrubbing tooltip bubble, elapsed/total time in tabular font, secondary buffered stream indicator, A-B loop marker lines.
  - **Bottom Control Panel**:
    - Play/Pause button with glowing gradient circle, Rewind/Forward 10s buttons, Screen Lock button, Aspect Ratio toggle, Rotation toggle, PiP button, A-B loop quick toggle.

## Current phase & status
- **Active Phase**: Video Player Pro-Grade Features Integration (Kids Lock, Display Modes, Gesture HUDs, On-Screen Info, Timeline Scrubber, Track Switcher).
- **Build Status**: Fully implemented and aligned with Master Spec v3.

## Files created this phase
- Updated `/app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt` (Pro video player with Kids Lock, PiP, gestures, A-B loop, audio/subtitle tracks, HW/SW decoder)
- Updated `/app/src/main/java/com/example/ui/util/FormatUtils.kt` (Added `formatFileSize` helper)
- Updated `/brain.md`

## Not verified / Not run
- Physical hardware SIM Toolkit app invocation on a physical carrier SIM in Pakistan.
- Physical biometric fingerprint scanner hardware (verified via software logic & fallback).
- Physical SD card hardware ejection during active Media3 ExoPlayer playback.

## Known issues & workarounds (SAF quirks, device-specific)
- Android 11+ Scoped Storage prevents picking the root directory of the SD card; the user picks a subfolder (e.g. `new` or a vault subfolder).
- Some OEM ROMs lack the standard `com.android.stk` package; handled gracefully with a truthful informative dialog.

## Open questions for the user
- None. Video player features fully integrated. Ready for build and testing in Android Studio.

## Next steps
- Sync and build debug APK in Android Studio (`./gradlew assembleDebug`).
- Sideload onto Android device and verify video gestures, PiP, A-B loop, and Kids Lock animations.
