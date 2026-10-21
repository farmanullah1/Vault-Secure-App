# Security, Privacy & Threat Model

> Built according to **Private Vault — Master Build Specification (v4, merged)** Section 40.4.

---

## 1. Truth in Advertising & Boundaries

1. **Hidden Mode vs. Encryption**:
   - Private Vault operates in **Hidden Mode** using standard `.nomedia` markers and dot-prefixed directories.
   - This hides media from MediaStore indexing (Google Photos, WhatsApp/Instagram media pickers, system gallery).
   - **It does not encrypt media files on disk**. Any file manager with hidden files revealed, or a computer reading the SD card over USB, can view the files. This fulfills the user requirement that files remain accessible in external file managers.
2. **Zero Network Communication**:
   - `android.permission.INTERNET` is **NOT declared** in `AndroidManifest.xml`.
   - Zero network requests, zero third-party telemetry, zero remote crash logging, and zero ad networks exist.
   - All processing, indexing, and authentication occur strictly locally on device.

---

## 2. Threat Model Matrix (Spec 40.4)

| Threat Scenario | Protection Level | Technical Position & Residual Risk |
|---|---|---|
| **Casual observer opens app on unlocked phone** | **Mitigated** | Protected by the authentic "SIM Service" cover disguise, PIN/biometric verification gate, randomized keypad, and auto-lock. |
| **User inspects installed apps in Android Settings** | **Partially Mitigated** | App appears as "SIM Service" with a genuine carrier utility icon, but technical inspection in Settings -> Apps reveals its package name and storage consumption. |
| **Someone removes the SD card and reads it elsewhere** | **Not Protected** | Storage files are unencrypted in Hidden Mode. Anyone reading the card in a PC or external reader can browse folder contents. |
| **Someone copies exported or shared files** | **Not Protected** | Once a file is exported or shared outside the vault via system intent, it is outside the vault's security sandbox. |
| **Attacker gains physical access after phone is unlocked** | **Partially Mitigated** | Vault auto-locks after configured background timeout (default 60s), screen-off locks immediately, and sensitive operations require fresh PIN re-entry. |
| **Android OS kills process or revokes SAF permissions** | **Mitigated Gracefully** | Handled with graceful recovery UX; persisted URI permissions re-requested via clear prompt; no crashes or data loss. |
| **Damaged, corrupted, or removed SD card** | **Partially Mitigated** | Room database index survives, but media files may be lost if no external backup was maintained by user. |
| **Accidental deletion, interrupted copy, or failed move** | **Mitigated** | Soft deletion to `.pv_trash/` allows 30-day restore; copy-verify-delete protocol never deletes source until destination verified; operation journal reconciles crashes on startup. |

---

## 3. Data Recovery & Failure Expectations

- **Lost Folder Permissions**: If Android revokes tree permissions, the app displays a prominent "Folder Access Needed" card in Settings and First-Time Permissions to re-grant access with one tap.
- **SD Card Replacement / Reformat**: Index entries for unreadable files will gracefully report "File Unavailable" without deleting user metadata. Re-running sync indexes new content cleanly.
- **App Reinstall**: Reinstalling the app resets the private database. However, media files on the SD card (including `.nomedia`) remain intact. Selecting the vault folder re-indexes all media.
- **Database Corruption**: If the SQLite database is corrupted, Room re-creates the database cleanly. Running "Sync Vault Folder" restores the library index.
- **Interrupted File Operations**: On startup, `VaultRepository.reconcilePendingOperations()` checks the `operation_journal` table. Interrupted transfers are flagged as failed while keeping original source files untouched.
- **Forgotten Master PIN & App Reset**: "Safe Reset" in Settings clears master credentials, PIN hashes, and the internal Room index. **It never deletes or touches user media on the SD card**.
