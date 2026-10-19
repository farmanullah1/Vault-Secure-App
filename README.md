# SIM Services (Private Media Safe)

A discreet, high-performance Android private media vault disguised behind an authentic SIM Services utility interface (featuring Jazz and Zong carrier shortcuts).

---

## Key Features

1. **Disguised Cover Screen (Plausible Deniability)**:
   - Launches as **SIM Services** with carrier cards for **Jazz** and **Zong**.
   - Tapping either card initiates biometric verification or master PIN entry.
   - Post-authentication hub provides legitimate carrier actions (system dialer helpline launch, SIM Toolkit app launcher check) along with the private entry button: **Open Private Safe**.

2. **Storage Access Framework (SAF) & .nomedia Protection**:
   - Allows choosing any local or SD card folder as your secure vault directory.
   - Automatically writes and verifies a `.nomedia` file to exclude files from Android MediaStore indexes (Google Photos, WhatsApp picker, Instagram, and default Gallery apps).
   - Supports **Copy to Safe** and **Move to Safe** (with verified byte check before source removal).

3. **Media Viewers & Playback**:
   - **Photo Viewer**: Gesture-based pinch-to-zoom, pan, rotation, file details, and deletion.
   - **Video Player (Media3 ExoPlayer)**: Double-tap 10s seek, playback speed (0.5x to 2x), aspect ratio modes, and an **"Open with External Player"** fallback button (VLC / MX Player) via content URI grants.
   - **Audio Player**: Smooth waveform animation, progress scrubber, and repeat mode.

4. **Security & Data Integrity**:
   - PBKDF2-HMAC-SHA256 password hashing with 12,000 iterations and cryptographic salt.
   - Progressive rate limiting to prevent brute-force attacks.
   - **Safe Reset**: Clears the PIN and app database index only. Media files on physical storage are **never deleted or modified**.

5. **Gallery Visibility Checklist**:
   - In-app diagnostic tool explaining `.nomedia` indexing mechanics, MediaStore cache clearing, device reboot tips, and cloud backup considerations.

---

## Building and Sideloading in Android Studio

To build the APK locally and sideload it onto your physical Android phone:

### 1. Open in Android Studio
1. Launch Android Studio (Ladybug / Iguana or later).
2. Select **Open** and choose this project root folder.
3. Wait for Gradle sync to complete.

### 2. Build Debug APK
1. In Android Studio, go to the top menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Once the build finishes, click the **locate** popup notification, or find the APK at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### 3. Sideload onto your Device
1. Connect your phone via USB cable (or transfer `app-debug.apk` via Google Drive / WhatsApp / SD card reader).
2. On your phone, open your File Manager and tap `app-debug.apk`.
3. If prompted:
   - Enable **"Allow from this source"** for your File Manager or browser under *Settings > Apps > Special app access > Install unknown apps*.
   - If Google Play Protect displays a warning for the sideloaded APK, tap **"More details"** and select **"Install anyway"**.
4. Tap **Open** to launch **SIM Services**.
