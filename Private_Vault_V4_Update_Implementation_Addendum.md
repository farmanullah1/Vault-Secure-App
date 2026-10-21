# Private Vault --- V4 Update & Implementation Addendum

**Document type:** Implementation update for the existing *Private Vault
--- Master Build Specification (v3)*\
**Purpose:** Improve the current Android app safely and consistently
without discarding the existing product vision.\
**Status:** Authoritative update requirements for the next
implementation pass\
**Primary instruction:** Read this entire file and the existing
project/specification before changing code.

------------------------------------------------------------------------

## 1. Instructions for the AI Builder

You are updating an existing Android application called **Private
Vault**. This document is an implementation addendum to the existing
**Private Vault --- Master Build Specification (v3)**, not permission to
rebuild the project blindly or replace working features with a new
design.

### 1.1 Required workflow

1.  Inspect the entire repository, current application structure, Gradle
    files, manifest, dependencies, navigation, persistence layer, UI,
    tests, and the existing v3 specification.
2.  Read `brain.md`, `README.md`, `TEST_PLAN.md`, and any implementation
    notes if they exist.
3.  Build a feature inventory showing each v3 requirement and its
    current state.
4.  Identify contradictions, duplicate implementations, unsafe file
    operations, missing tests, dependency incompatibilities, and UI
    controls that do not perform real actions.
5.  Produce a concise implementation plan and update the project's
    `brain.md` before making substantial changes.
6.  Implement in the phases defined in this document. Keep changes small
    enough to build and verify.
7.  Run the relevant tests and build after each phase. Do not proceed
    past a blocking failure without recording it and determining whether
    it can safely be fixed.
8.  Update `README.md`, `TEST_PLAN.md`, `brain.md`, and the traceability
    matrix with actual results.
9.  Finish with a truthful implementation report: files changed,
    features implemented, tests actually run, failures, manual checks
    still needed, and anything deferred.

### 1.2 Non-negotiable implementation rules

-   **No dummy data:** Do not add sample photos, fake albums, fabricated
    counts, fake intruder events, pretend scan results, or demonstration
    media to production screens.
-   **No fake functionality:** Every visible button, toggle, menu item,
    setting, gesture, and permission action must work or be clearly
    marked as unavailable/not implemented. Do not show a success message
    unless the operation actually succeeded.
-   **No silent omissions:** If a feature cannot be implemented safely
    or within the current phase, record it as `Deferred` with a reason.
    Do not report it as implemented.
-   **No destructive shortcuts:** Never overwrite, move, or delete
    original media without explicit user intent and appropriate recovery
    safeguards.
-   **No security exaggeration:** Do not describe Hidden Mode, a
    dot-prefixed folder, `.nomedia`, a disguised launcher icon, or a
    hidden UI as encryption or guaranteed invisibility.
-   **No unverified claims:** Do not claim a build passed, a permission
    works, a device is compatible, a file was safely moved, or
    encryption is secure unless there is evidence.
-   **No unnecessary permissions:** Request only permissions required by
    a feature the user enables. Do not request internet, contacts,
    location, SMS, phone-call, microphone, or all-files access merely
    for convenience.
-   **No forced redesign:** Preserve the existing product identity and
    established visual language unless a documented defect or usability
    issue requires a change.
-   **No needless dependencies:** Reuse appropriate existing libraries.
    Add a dependency only when it solves a concrete requirement and is
    compatible with the project's verified toolchain.
-   **No network behavior in the offline release:** The app must remain
    local-only unless a future, explicitly approved scope change says
    otherwise.
-   **No automatic execution of destructive or sensitive workflows:**
    The app must not silently enable the camera, permanently delete
    files, export private media, reset credentials, or perform a move
    operation that can lose data.

### 1.3 Status vocabulary

Use only these implementation statuses in documentation and reports:

-   `Planned` --- not implemented yet.
-   `In Progress` --- implementation has started.
-   `Implemented` --- code exists, but validation may be incomplete.
-   `Tested` --- the relevant automated or manual test was actually run
    and passed.
-   `Verified on Device` --- the behavior was checked on a real device
    or a clearly identified emulator where appropriate.
-   `Deferred` --- deliberately postponed, with a reason and follow-up
    requirement.
-   `Blocked` --- cannot proceed because of a concrete dependency,
    platform limitation, or unresolved decision.

Never treat `Implemented`, `Tested`, and `Verified on Device` as
interchangeable.

------------------------------------------------------------------------

## 2. Product Boundaries and Release Scope

### 2.1 Preserve the existing product

The app remains a privacy-focused, local-only Android media vault with
the existing cover/SIM-style entry flow, authentication, media library,
SD-card folder selection, playback, settings, and optional privacy tools
defined in v3.

This update is intended to make the implementation more reliable,
secure, testable, and coherent---not to remove the original features or
replace the app with a generic gallery.

### 2.2 Release scope labels

Tag each feature in `brain.md` and the traceability matrix with one of
these scopes:

-   **P0 --- Release blocker:** core navigation, authentication, safe
    media access, crash prevention, and data-integrity protections.
-   **P1 --- Core release:** reliable indexing, media browsing, basic
    playback, safe copy/move/delete/trash behavior, and essential
    settings.
-   **P2 --- Optional privacy/comfort:** biometric unlock, privacy blur,
    decoy entry, intruder log/photo, panic gesture, and other optional
    features.
-   **P3 --- Advanced/future:** complex editing, advanced player
    enhancements, or features that need separate feasibility and
    threat-model work.

Do not make P2/P3 features block the release of a working P0/P1
application unless the current release plan explicitly requires them.

### 2.3 Offline and privacy boundary

For this release:

-   Store app data locally.
-   Do not add accounts, cloud sync, analytics, advertising SDKs, remote
    crash reporting, or remote media processing.
-   Do not add internet permission unless a documented and approved
    requirement proves it is necessary.
-   Keep media indexing separate from media movement.
-   Treat SD-card content as user-owned data that must not be altered
    without clear consent.
-   Document that local-only storage reduces network exposure but does
    not by itself encrypt media files or protect them from someone with
    direct access to the storage.

### 2.4 Platform and dependency compatibility

Before implementation, inspect and document the actual project versions.
Choose compatible versions of Android Gradle Plugin, Gradle, Kotlin,
Compose compiler/plugin or equivalent UI stack, KSP/KAPT, Room, Media3,
CameraX, and other libraries already in use.

-   Do not blindly upgrade every dependency to "latest."
-   Do not mix incompatible dependency versions or duplicate
    dependency-injection systems.
-   Record the verified version matrix in `README.md` or a dedicated
    `TECHNICAL_DECISIONS.md`.
-   Preserve the current package/application ID unless a migration is
    explicitly approved.
-   State the minimum and target Android SDK based on the actual build
    configuration and tested platform requirements.
-   If a version change is necessary, make it a separate, build-verified
    step.

------------------------------------------------------------------------

## 3. Required Architecture and Source-of-Truth Rules

### 3.1 Keep responsibilities separated

Follow the project's existing architecture where it is sound. Avoid a
broad rewrite just to match a preferred architecture pattern. At
minimum, responsibilities must be clear:

-   **UI layer:** renders state and forwards user actions.
-   **ViewModels or equivalent presentation state holders:** manage
    screen state and user intents.
-   **Repositories:** own data access and coordinate storage/database
    operations.
-   **Storage/file-operation layer:** handles SAF URIs, file access,
    copy, move, delete, recovery, and permission failures.
-   **Media layer:** handles metadata, thumbnails, playback, audio
    focus, and supported formats.
-   **Security layer:** handles authentication state, lockout,
    re-authentication, and sensitive-operation gates.
-   **Persistence layer:** stores app metadata, user settings,
    scan/index state, operation journals, and event logs as appropriate.

Use one consistent dependency-injection approach if the project needs
DI. Do not introduce a second framework without a concrete reason.

### 3.2 State must have a single source of truth

Do not independently maintain multiple conflicting copies of the same
state in a screen, service, database, and settings object.

Define explicit state models for at least:

-   Authentication and lock state.
-   Folder permission/access state.
-   Library scan/index state.
-   File-operation state.
-   Playback state.
-   Intruder-event state, if the feature is enabled.
-   Settings and feature availability.

Use persistent storage for state that must survive process death. Use
in-memory state only for transient UI state. Never assume an Android
process or service will remain alive indefinitely.

### 3.3 Database and migrations

-   Inspect the existing schema before changing it.
-   Use explicit schema migrations; do not solve schema changes by
    deleting the database.
-   Preserve user settings and index data through supported upgrades.
-   Store durable SAF document URIs and the permission/access metadata
    required to reopen them.
-   Keep large media binaries out of the database; store references and
    metadata unless the existing design has a justified exception.
-   Define behavior when the database is unavailable, corrupt, or out of
    date.
-   Back up or recover app metadata only through a documented procedure.
    Never claim that an index backup contains the actual media files.

### 3.4 Avoid unbounded work on the main thread

Scanning, metadata extraction, thumbnail generation, hashing, and file
transfers must not block the UI thread. Support cancellation where safe,
progress reporting, lifecycle changes, and process death. Use paging or
incremental loading for large libraries instead of loading every item
into memory.

------------------------------------------------------------------------

## 4. Security Model: State Clearly What Is and Is Not Protected

### 4.1 Distinguish hiding, app access control, and encryption

The UI and documentation must distinguish three separate concepts:

1.  **Hidden Mode / cover screen:** changes what the app displays before
    successful authentication.
2.  **App authentication and session locking:** restricts access through
    this app's UI.
3.  **Encryption at rest:** cryptographically protects content if the
    storage medium is read outside the app.

Do not imply that the first two provide the third. A `.nomedia` file,
hidden folder name, disguised launcher, or app database lock does not
encrypt media stored in ordinary files on an SD card.

If encrypted storage is not already implemented and independently
reviewed, label it `Deferred` rather than simulating it. A future
encryption feature requires a separate design for key management,
migration of existing files, recovery, exports, and failure handling.

### 4.2 Authentication and lockout

Review and enforce one coherent authentication state machine.

-   A cold start and device reboot must require authentication,
    consistent with the existing v3 requirement.
-   Failed-attempt limits and lockout must be enforced by application
    logic, not just by disabling a button in the UI.
-   Lockout state must survive activity recreation and process death.
-   Use a monotonic time source for in-process timeout calculations
    where appropriate; handle reboot and persisted-time edge cases
    deliberately.
-   Sensitive actions---changing credentials, resetting the app,
    changing security settings, permanently deleting files, emptying
    trash, exporting private media, and deleting intruder
    evidence---must require fresh authentication according to the v3
    policy.
-   Keep the re-authentication validity window centralized and test it.
-   Avoid logging credentials, PINs, passwords, biometric results,
    private file names, or sensitive URIs.
-   Never store a PIN or password in plaintext. Use an appropriate
    salted, computationally expensive password-verification scheme and
    safe parameters for the supported platform. Do not invent a custom
    cryptographic algorithm.
-   If a credential-derived encryption key is introduced in a future
    feature, design it separately; a password hash is not itself an
    encryption key.
-   Provide a safe recovery/reset explanation before the user confirms a
    reset. The reset must not delete user media.

Biometrics may be used as a convenience mechanism only if the device
supports the required authenticator. Define fallback behavior,
enrollment changes, lockout, and when a PIN/password is required again.

### 4.3 App switcher, notifications, and sensitive content

-   Apply `FLAG_SECURE` according to the existing default-on privacy
    setting, and test the setting's effect.
-   Ensure the Recents/task snapshot uses a neutral cover or blank state
    when required. Do not rely on a claim that `FLAG_SECURE` alone
    solves every task-snapshot case on every Android version.
-   Use neutral notification text by default where the specification
    requires privacy.
-   Do not put media titles, private paths, photos, credentials, or
    sensitive event details in logs or notifications unless the user has
    explicitly chosen a clearly explained less-private option.
-   Clear or protect sensitive temporary files, generated thumbnails,
    clipboard content, and caches according to the selected privacy
    settings.
-   Treat temporary exports and share files as sensitive; define cleanup
    behavior and explain that once the user shares or exports a file,
    the receiving app or destination is outside the vault's control.

### 4.4 Disguise and decoy behavior

-   A disguised app name/icon is a visual disguise, not an undetectable
    or system-level hiding mechanism.
-   Do not claim that the app cannot be discovered in Android settings,
    installed-app lists, permission screens, storage inspection, or
    launcher management.
-   A decoy PIN must not accidentally reveal the real vault through
    navigation, notifications, shared state, recent tasks, or error
    messages.
-   If a decoy mode cannot be implemented without a reliable separation
    of real and decoy state, defer it rather than adding a superficial
    screen.
-   Never silently erase real content when a decoy PIN is entered.

### 4.5 Intruder photo and event log: best-effort only

Keep this feature optional and off until the user enables it and grants
camera permission.

-   Capture only under the foreground/lifecycle constraints defined in
    v3 and permitted by the platform.
-   Never try to bypass Android camera indicators, privacy controls, or
    background camera restrictions.
-   Show a clear explanation that capture may fail because of
    permissions, hardware, another app using the camera, device
    restrictions, or lifecycle changes.
-   Log the qualifying failed attempt even if the photo cannot be
    captured.
-   Do not claim that every failed attempt will produce a photo or that
    the photo will be invisible or silent.
-   Bound retention and storage usage, handle missing SD-card access,
    and protect event details behind authentication.
-   Make deletion and retention behavior explicit. If reset clears the
    database but leaves photos on the SD card, show the user the
    location and consequences before reset.
-   Ensure a photo is not accidentally added to normal media indexing,
    thumbnails, search, albums, or system gallery views.
-   Test the feature on real hardware; a mock or emulator test alone
    does not prove camera behavior on a user's device.

------------------------------------------------------------------------

## 5. SD Card, SAF, and File-Operation Safety

This section is a release-critical requirement. File operations must
favor data integrity over speed or convenience.

### 5.1 Separate indexing from moving

Selecting a folder and indexing its media must not move, rename, hide,
delete, or rewrite the original files.

-   The initial scan is read-only.
-   Display the selected folder and explain what the app will access.
-   Store the persisted SAF URI permission only after the user grants
    it.
-   If access is lost or revoked, show a clear `Access needed` state
    with a button to reselect the folder.
-   Do not assume that selecting a folder grants access to the entire SD
    card.
-   Respect Android's Storage Access Framework restrictions, including
    directories that cannot be selected on particular Android versions
    or devices.
-   Do not request broad all-files access to work around SAF
    restrictions.

### 5.2 Define every file operation precisely

The UI and code must distinguish:

-   **Index:** record metadata/reference; do not alter the source file.
-   **Copy to vault:** create a separate copy and preserve the original.
-   **Move to vault:** transfer content only after a safe transfer and
    verification strategy succeeds.
-   **Remove from library:** remove the app's index entry only; leave
    the file untouched.
-   **Move to Trash:** use the defined trash behavior; do not
    permanently delete.
-   **Delete permanently:** remove the selected file only after clear
    confirmation and appropriate re-authentication.
-   **Export:** create a user-selected copy outside the vault.
-   **Share:** grant a temporary URI to the selected receiving app using
    an appropriate content URI; do not expose raw private file paths.
-   **Reset app:** clear app-owned settings/database/credentials as
    specified; do not delete original media.

Do not label an operation "Move" if it only removes the item from the
app's index.

### 5.3 Copy-verify-delete strategy

Where the provider cannot perform a reliable atomic move, implement a
staged operation:

1.  Validate source access, destination access, available space where
    measurable, and destination naming.
2.  Create a unique operation record and temporary destination.
3.  Copy the bytes using bounded buffers and cancellation-aware I/O.
4.  Flush/close the destination and verify transfer success. Use size
    and, where appropriate and affordable, a content hash or
    byte-for-byte verification strategy.
5.  Rename/finalize the destination where the provider supports it.
6.  Only after the destination is verified and the user authorized the
    move, attempt source deletion.
7.  Verify the source deletion result where possible.
8.  Mark the operation complete only when the resulting state is known.
9.  If any step fails, preserve the source, retain enough operation
    metadata for recovery, and explain what happened.

Never delete the source simply because a copy function returned without
throwing. Provider behavior can vary. If safe verification is not
possible, keep the original and report that the move could not be safely
completed.

### 5.4 Operation journal and recovery

Persist a file-operation journal for long or destructive operations. It
should include a unique ID, operation type, source/destination
references, start time, current stage, result, and a sanitized error
category. Do not store credentials or unnecessary private content.

Handle at least:

-   App process death during copy/move/delete.
-   User cancels a transfer.
-   SD card removal or disconnection.
-   Permission revocation during an operation.
-   Read-only destination.
-   Insufficient storage.
-   Destination name collision.
-   Provider refusing rename or delete.
-   Corrupt/unreadable source file.
-   A source that changes during the transfer.
-   A duplicate operation triggered by double-tapping.
-   A destination that exists after a previous partial failure.
-   A database write failure after a file-system step.

On restart, reconcile the journal with the actual source and destination
state. Do not blindly retry a destructive operation. Offer a clear
recovery action and preserve recoverable originals.

### 5.5 Trash semantics

Document the actual trash implementation and its limits.

-   Specify whether trash is inside the selected vault folder,
    app-private storage, or another location.
-   Specify whether trash retains the original file, moves it, or
    creates a copy.
-   Do not claim that a SAF provider supports reversible deletion unless
    that behavior has been verified.
-   Provide restore, delete permanently, and empty trash actions where
    supported.
-   Require confirmation and re-authentication for permanent deletion
    and empty-trash operations.
-   Apply retention settings only when the app can safely determine
    which items it owns and can delete.
-   Do not silently purge files if permission is lost or the card is
    absent.
-   Show the number of items affected and the consequences before bulk
    deletion.

### 5.6 Hidden folders and `.nomedia`

-   A dot-prefixed folder and `.nomedia` may reduce media-scanner
    visibility, but neither is encryption or a guarantee against
    discovery.
-   Do not promise that every Gallery app, file manager, backup service,
    or system indexer will honor `.nomedia`.
-   Do not overwrite a user's existing `.nomedia` file without checking
    and explaining the impact.
-   Do not create or move files into a hidden folder automatically
    during a read-only scan.
-   Explain the consequences before the user chooses a storage
    organization that changes where files are kept.

### 5.7 Scan/index quality

-   Make scans incremental and cancellable.
-   Do not load the entire library into memory.
-   Deduplicate entries by stable document reference and/or a carefully
    chosen identity strategy; do not assume display names are unique.
-   Handle nested folders, overlapping selected folders, duplicate
    names, unsupported formats, unreadable items, and stale database
    entries.
-   Distinguish scan progress, indexed items, skipped items, errors, and
    cancellation.
-   A failed scan must not erase a previously valid index.
-   Do not show fabricated progress or a fake "scan complete" result.
-   Avoid repeated thumbnail generation when a valid cached thumbnail
    exists, while respecting the privacy cache settings.

------------------------------------------------------------------------

## 6. Media Library and Playback Refinements

### 6.1 Library behavior

-   Keep the existing categories and screen organization defined in v3.
-   Show accurate counts derived from successfully indexed items, not
    placeholder values.
-   Provide honest empty, loading, permission-required, scan-failed,
    unsupported-format, and unavailable-file states.
-   If an indexed file disappears, mark it unavailable and provide a
    rescan/relink action rather than crashing or silently treating it as
    deleted.
-   Use stable identifiers and paging for large collections.
-   Keep sorting, filtering, search, albums, favorites, and recent items
    consistent with actual persisted data.
-   Respect privacy settings for titles, thumbnails, recent history, and
    counts.

### 6.2 Media support and error behavior

Create a media support matrix in `README.md` or `TEST_PLAN.md` listing
the formats actually supported by the chosen platform libraries and the
behavior when a format is unsupported.

-   Do not claim support solely because a file extension is recognized.
-   Handle corrupt, partially downloaded, zero-byte, unusually large,
    and unsupported files gracefully.
-   Avoid loading full-resolution images into memory when a sampled
    decode or thumbnail is sufficient.
-   Keep editing separate from the original: if image editing is
    implemented, default to **Save as copy** unless the user explicitly
    confirms replacement and the operation is safe.
-   Define whether animated images are supported and how
    playback/decoding limits work.
-   If subtitle support is offered, document and test the supported
    subtitle formats and how subtitles are selected.

### 6.3 Playback state and lifecycle

Define one playback state model and test transitions between:

-   Idle.
-   Preparing.
-   Playing.
-   Paused.
-   Buffering, where applicable.
-   Completed.
-   Failed.
-   Interrupted by audio focus or another system event.
-   Stopped because the source became unavailable.
-   Released when the player is no longer needed.

Rules:

-   Seeking, play/pause, next/previous, repeat, shuffle, playback speed,
    and orientation controls must perform the advertised action only
    where supported.
-   Restore playback state only when enabled and when the referenced
    media is still accessible.
-   Avoid accidentally restarting playback multiple times after activity
    recreation.
-   Respect audio focus, headphone/Bluetooth disconnection behavior,
    calls and other interruptions as specified in v3.
-   If background audio is enabled, keep the UI lock state separate from
    playback state. The UI may lock while permitted audio continues.
-   When the whole device is locked, apply the v3 rules for pausing
    video and slideshows.
-   Notifications must use neutral text by default and expose only the
    controls the app can genuinely support.
-   Stop/release playback cleanly when the user stops it, when the
    source becomes invalid, or when the applicable lifecycle policy
    requires it.
-   Do not claim that Android will keep a background process alive
    indefinitely.

### 6.4 Optional advanced playback

Picture-in-Picture, gestures, floating controls, advanced queue
behavior, playback speed, subtitles, and other advanced player features
must be marked `P2` or `P3` unless already stable and required by the
release.

Do not implement gestures that conflict with system navigation or
accessibility. Every gesture must have an equivalent accessible control
where practical.

------------------------------------------------------------------------

## 7. Import, Export, Share, Backup, and Reset

### 7.1 One consistent user flow

Use clear, distinct workflows for importing, copying, moving, exporting,
and sharing. Before a workflow that changes storage, show:

-   What will happen.
-   Which files are affected.
-   Whether the original will remain.
-   Where the result will be stored.
-   Whether the destination is outside the vault.
-   What recovery is possible if the operation fails.

Never hide these details behind ambiguous labels such as "Secure,"
"Clean," or "Move safely."

### 7.2 Export and share

-   Use Android's supported document creation and sharing mechanisms.
-   Use content URIs and appropriate temporary URI permissions for
    sharing; never expose raw file-system paths.
-   Export only the files selected by the user.
-   Use the system destination picker when required or when the user has
    enabled "Ask where to save."
-   Make it clear that an exported file is outside the vault and is no
    longer protected by the app's UI lock.
-   Clean up app-created temporary share files according to a documented
    policy.
-   Handle cancellation, destination failures, duplicate names, and
    partial exports.
-   Require fresh authentication according to the existing
    sensitive-action policy.

### 7.3 Backup and restore

Keep these concepts separate:

-   **Settings backup:** user preferences without secrets.
-   **Library/index backup:** metadata and references, which may become
    invalid if the folder changes.
-   **Media backup:** actual file contents; this is a separate, explicit
    operation and must not be implied by an index backup.

A restore flow must preview conflicts and explain whether it will merge,
replace, or skip items. Do not overwrite files by default. Do not
include credentials, encryption keys, or intruder photos in a settings
backup. If encryption is later implemented, define a separate, reviewed
recovery plan.

### 7.4 Reset app

The reset workflow must:

1.  Explain exactly which app-owned records, settings, and credentials
    will be cleared.
2.  State that ordinary media on the SD card is not deleted by reset.
3.  Explain that hidden-folder files or intruder photos left on the card
    may remain.
4.  Require fresh authentication and an explicit final confirmation.
5.  Avoid starting the reset if the user cancels.
6.  Clear only the app-owned data that the reset specification names.
7.  Return the user to first-run setup and folder re-selection.
8.  Report the real outcome and any items that could not be cleaned up.

Do not imply that reset encrypts, securely erases, or hides media that
was already accessible on the storage device.

------------------------------------------------------------------------

## 8. Settings: One Registry, Real Effects

The v3 specification contains settings described across multiple
sections. Consolidate them into one settings registry and remove
duplicate or conflicting implementations.

For every setting, document:

  Field             Required meaning
  ----------------- ---------------------------------------------------------------
  Key               Stable unique identifier
  Type              Boolean, enum, integer, duration, URI, or other explicit type
  Default           Actual default value
  Allowed values    Valid choices and bounds
  Description       User-facing explanation
  Effect            Exact behavior the setting changes
  Persistence       Where and how the value is stored
  Security impact   Whether the setting changes privacy or security
  Dependencies      Required permission or feature availability
  Test              How its behavior is verified

Rules:

-   A toggle must not be decorative. If a feature is unavailable, show
    why and do not let the UI imply it is active.
-   Persist settings across process death and app updates as expected.
-   Reconcile related settings so they cannot produce contradictory
    states.
-   Keep app auto-lock, idle lock, manual lock, device-screen lock, and
    playback interruption as separate concepts with documented
    precedence.
-   Explain that Android may terminate background work and that the app
    cannot guarantee uninterrupted background execution.
-   Request permissions when the user enables the related feature, not
    all at first launch.
-   If a permission is denied or revoked, show the feature's actual
    status and a clear recovery path.
-   Hide advanced/experimental settings from the default settings screen
    until the associated feature works and has been tested.
-   Resetting settings must not silently delete media.

### 8.1 Resolve the lock-versus-playback rule

Implement the following precedence unless a documented v3 rule requires
a more restrictive behavior:

-   **Locking the app UI** blocks access to private screens.
-   **Background audio** may continue only when the user has enabled it
    and the platform permits it.
-   The player notification must remain privacy-conscious and must not
    reveal media titles by default.
-   **Stop playback when app locks** must do exactly what its label
    says.
-   **Device locked** behavior for video and slideshows follows the
    existing v3 requirement.
-   **Lock now** must lock the UI promptly; if playback is configured to
    continue, it may continue without exposing private UI.
-   Cold starts and device reboots always require authentication.

Add automated tests for the combinations that are supported. Do not
allow a setting to claim behavior that the player or Android lifecycle
cannot reliably deliver.

------------------------------------------------------------------------

## 9. UI, Accessibility, and Interaction Quality

### 9.1 Preserve visual identity while refining usability

Keep the existing premium-dark visual direction and cover/SIM-style
entry concept. Improve spacing, hierarchy, contrast, alignment,
transitions, and consistency without adding ornamental effects that
obscure function.

-   Use a documented set of design tokens for colors, spacing,
    typography, corner radii, elevations, motion, and component sizes.
-   Avoid scattering unexplained hard-coded values throughout screens.
-   Reuse components for repeated patterns such as permission status,
    progress, confirmations, empty states, and errors.
-   Keep motion purposeful, brief, and interruptible.
-   Avoid excessive blur, glow, gradients, and animation that reduce
    readability or drain battery.
-   Respect system reduced-motion/accessibility preferences where
    feasible.
-   Do not copy carrier logos or imply official carrier affiliation.
    Preserve the existing requirement for simple original SIM icons and
    plain labels.

### 9.2 Every screen needs explicit states

For each screen, define and implement:

-   Initial/loading state.
-   Empty state.
-   Normal populated state.
-   Recoverable error state.
-   Permission/access-required state.
-   Unavailable or unsupported content state.
-   Disabled-feature state, when applicable.

Do not display fake progress, fake file counts, generic success banners
after failures, or empty buttons with no action.

### 9.3 Accessibility

-   Use meaningful accessibility labels and semantics for buttons,
    icons, and media items.
-   Ensure adequate contrast and readable text at supported font scales.
-   Do not communicate security state using color alone.
-   Make primary actions reachable by keyboard/switch/accessibility
    services where applicable.
-   Ensure keypad, confirmation dialogs, and sensitive actions are
    usable with accessibility features.
-   Avoid gesture-only actions; provide visible alternatives.
-   Respect touch-target guidance and handle narrow screens, landscape,
    and large text without clipping essential controls.

### 9.4 Responsive layout

Test compact and large phones, portrait and landscape, and supported
font scaling. If foldable/tablet support is not part of the release
target, mark it as future scope rather than allowing it to block core
reliability. Do not force a fixed layout that clips content on smaller
devices.

------------------------------------------------------------------------

## 10. Carrier / SIM-Style Cover Flow

Preserve the v3 cover flow while keeping carrier functionality honest
and safe.

-   The cover screen is a visual entry flow, not a system-level security
    boundary.
-   Selecting Jazz or Zong must follow the specified authentication
    sequence before revealing the protected panel or vault.
-   Use `ACTION_DIAL` for helpline calls; never auto-call and do not
    request call permission for dialing.
-   Do not hardcode unverified helpline numbers or USSD codes. Use only
    values verified from official carrier sources at implementation
    time; otherwise leave them empty and let the user enter a value.
-   Carrier settings must be editable only after authentication as
    specified.
-   SIM Toolkit or carrier-specific actions must be tested on supported
    devices and SIMs. If the device or carrier does not support the
    requested action, show a truthful unavailable state.
-   Do not imply official affiliation with Jazz or Zong.
-   Do not request phone, SMS, contacts, or location permissions unless
    a specific, approved, working feature truly requires them. Prefer
    not to request them.
-   Do not allow a carrier action, deep link, or external intent to
    bypass authentication or expose vault state.

------------------------------------------------------------------------

## 11. Error Handling and Recovery UX

Use actionable, user-friendly errors. Avoid raw stack traces,
unexplained error codes, and generic "Something went wrong" messages
when a specific cause is known.

For each recoverable error, identify:

1.  What failed.
2.  Whether the original data is safe.
3.  What completed before the failure.
4.  What the user can do next.
5.  Whether retrying is safe.

Cover at least:

-   Folder access missing or revoked.
-   SD card unavailable or changed.
-   Scan failed or cancelled.
-   Media moved, renamed, or deleted outside the app.
-   Unsupported/corrupt media.
-   Copy/move/delete interrupted.
-   Storage full.
-   Permission denied by provider.
-   Camera unavailable or denied.
-   Biometric authentication unavailable.
-   Playback failure or audio-focus interruption.
-   Database migration or persistence failure.
-   Export/share cancellation.
-   Restore conflict.
-   Invalid or expired operation state.

Never expose sensitive internal paths or credentials in user-facing
errors. Logs should use sanitized error categories and be local-only
unless the user explicitly exports diagnostics.

------------------------------------------------------------------------

## 12. Test Plan and Release Gates

### 12.1 Required automated checks

Use the project's existing test stack where possible. Add or update:

-   Unit tests for authentication, lockout, settings validation,
    sorting/filtering, operation-state transitions, and recovery
    decisions.
-   Repository/storage tests using test doubles for provider failures.
-   Database migration tests for supported schema upgrades.
-   UI tests for primary flows, permission-denied states, empty/error
    states, and confirmation dialogs.
-   Playback tests for the supported player state transitions.
-   Static analysis, Android lint, and dependency checks where supported
    by the project.

Do not create tests that merely assert that a method exists or that a
fake operation returned success. Test observable behavior and failure
cases.

### 12.2 Required failure-path testing

At minimum, verify the following scenarios:

-   User denies folder access and later grants it.
-   Permission is revoked after a successful scan.
-   SD card is removed during a scan or transfer.
-   A copy/move is interrupted and the app is restarted.
-   The destination becomes read-only or runs out of space.
-   Destination name already exists.
-   A delete or move provider call fails.
-   Database write fails after a file operation.
-   User double-taps a destructive action.
-   App is backgrounded or process is killed during work.
-   Authentication lockout survives app recreation.
-   Reset is cancelled and then separately confirmed.
-   Media is unavailable after indexing.
-   Playback is interrupted by audio focus, device lock, or headset
    disconnection as applicable.
-   Notification permission is denied.
-   Camera permission is denied or camera capture fails, if intruder
    photo is enabled.
-   App upgrades from the previous database schema without losing
    settings/index records.

### 12.3 Real-device verification

Before marking a feature `Verified on Device`, record device model,
Android version, feature tested, result, and any limitations. Use a
physical SD card for SD-card workflows. Emulator-only success is not
evidence that every OEM's SAF, camera, notification, or background
behavior works.

### 12.4 Build evidence

Run the actual Gradle tasks appropriate for the repository, such as the
project's debug build, unit tests, lint, and instrumented tests where
available. Use the exact task names discovered in the project.

-   Record the command and result.
-   If a task was not run, say so.
-   If the build fails, include the relevant failure summary and whether
    it was introduced by this update.
-   Do not fabricate test reports, screenshots, APKs, or CI results.
-   If a release APK is produced, verify that the output file exists and
    record its exact path.

### 12.5 Definition of Done

A phase is complete only when:

-   Its requirements have been implemented or explicitly deferred.
-   The project builds for the intended variant, or the blocking failure
    is documented.
-   Relevant tests have actually run.
-   No known data-loss regression remains unresolved.
-   Visible controls for the phase perform real actions.
-   Permission and error states are handled.
-   Documentation and traceability statuses match reality.
-   A concise phase report is available.

The entire release is not "done" while a P0 data-integrity or
authentication blocker remains unresolved.

------------------------------------------------------------------------

## 13. Requirements Traceability Matrix

Create or update `REQUIREMENTS_TRACEABILITY.md` using one row per
requirement. Use unique IDs that remain stable across edits.

Recommended columns:

  ----------------------------------------------------------------------------------------------------------------
  ID           Requirement      Priority   Phase    Module           Test/evidence       Status    Notes/blocker
  ------------ ---------------- ---------- -------- ---------------- ------------------- --------- ---------------
  PV-SEC-001   Cold start       P0         2        Security         Auth state test     Planned   
               requires                                                                            
               authentication                                                                      

  PV-STO-001   Initial scan     P0         3        Storage          Read-only scan test Planned   
               does not modify                                                                     
               source files                                                                        

  PV-STO-002   Move preserves   P0         3        File operations  Interrupted         Planned   
               source until                                          transfer test                 
               destination                                                                         
               verification                                                                        

  PV-STO-003   Interrupted      P0         3        Recovery         Process-death test  Planned   
               operations can                                                                      
               be reconciled                                                                       

  PV-DB-001    Existing user    P0         1        Persistence      Migration tests     Planned   
               data survives                                                                       
               supported                                                                           
               migrations                                                                          

  PV-UI-001    Every visible    P0         All      UI               UI test/manual      Planned   
               primary control                                       checklist                     
               has real                                                                            
               behavior                                                                            

  PV-PRV-001   Notifications do P1         2/5      Privacy/player   Notification        Planned   
               not expose media                                      inspection                    
               titles by                                                                           
               default                                                                             

  PV-MED-001   Unsupported      P1         4/5      Media            Unsupported-file    Planned   
               media fails                                           test                          
               gracefully                                                                          

  PV-EXP-001   Export clearly   P1         6        Export           Export flow test    Planned   
               warns that                                                                          
               output is                                                                           
               outside the                                                                         
               vault                                                                               

  PV-CAM-001   Intruder photo   P2         8        Security         Permission/camera   Planned   
               is optional and                                       test                          
               reports capture                                                                     
               failure honestly                                                                    

  PV-REL-001   Release build    P0         10       Release          Build log/test      Planned   
               and required                                          report                        
               tests have                                                                          
               recorded                                                                            
               evidence                                                                            
  ----------------------------------------------------------------------------------------------------------------

The examples above are templates, not evidence that any feature is
already implemented or tested. Expand the matrix to cover every
requirement retained from v3 and every new requirement in this addendum.
Do not delete v3 requirements just because they are inconvenient; mark
them `Deferred` with a reason if they are outside the release scope.

------------------------------------------------------------------------

## 14. Implementation Phases and Required Deliverables

Complete the following in order. Reorder only if repository evidence
shows a necessary dependency, and document the reason.

### Phase 1 --- Repository audit and foundation (P0)

-   Inventory existing screens, dependencies, database schema,
    permissions, and features.
-   Resolve incompatible dependency versions without broad, unnecessary
    upgrades.
-   Establish a working build baseline and record current failures.
-   Define shared UI tokens and common loading/error/empty/permission
    components.
-   Establish the traceability matrix and update `brain.md`.
-   Ensure no dummy content is used in production screens.

**Exit gate:** baseline build status and current feature inventory
documented; no unexplained project-wide rewrite.

### Phase 2 --- Authentication and privacy state (P0)

-   Consolidate authentication/session state.
-   Verify cold-start and reboot authentication behavior.
-   Make lockout and sensitive-action re-authentication consistent.
-   Protect app-switcher and notification content according to settings.
-   Verify reset semantics and ensure reset does not delete ordinary
    media.
-   Implement settings through the single registry.

**Exit gate:** authentication and reset tests pass; unresolved security
claims are corrected in UI/docs.

### Phase 3 --- SAF and data-integrity foundation (P0)

-   Make folder selection and permission persistence reliable.
-   Keep indexing read-only.
-   Implement scan state, cancellation, stale-item handling, and
    permission-loss UX.
-   Add operation journal/recovery before enabling destructive file
    operations.
-   Implement safe copy/move/delete/trash semantics or disable
    unsupported operations with a clear explanation.

**Exit gate:** failure-path tests demonstrate that source files are
preserved when a transfer cannot be verified.

### Phase 4 --- Media index and library UX (P1)

-   Verify real metadata and thumbnail behavior.
-   Add paging/incremental loading where needed.
-   Implement accurate empty/error/unavailable states.
-   Ensure counts and filters reflect real indexed data.
-   Confirm hidden-folder and `.nomedia` behavior is accurately
    described.

**Exit gate:** library works without placeholder items and handles
missing/unsupported media gracefully.

### Phase 5 --- Basic playback (P1)

-   Stabilize core image/video/audio viewing supported by the app.
-   Implement lifecycle, audio-focus, lock, and notification rules.
-   Verify resume behavior and player cleanup.
-   Defer advanced features that could destabilize basic playback.

**Exit gate:** core playback and interruption tests pass for the formats
the app claims to support.

### Phase 6 --- File management and export (P1)

-   Complete verified copy/move behavior.
-   Implement trash/restore/permanent deletion only where provider
    behavior is reliable.
-   Implement export/share with clear privacy warnings and URI-safe
    handling.
-   Add recovery UX and bulk-operation confirmation.

**Exit gate:** no known operation can silently discard an unverified
original.

### Phase 7 --- Cover and carrier flow (P1/P2 according to current release)

-   Preserve the v3 visual flow and authentication gate.
-   Verify official carrier numbers before pre-filling them.
-   Use `ACTION_DIAL`; never auto-call.
-   Handle unsupported carrier/SIM features without fake success.

**Exit gate:** carrier actions are truthful, user-controlled, and cannot
bypass authentication.

### Phase 8 --- Settings and optional privacy features (P2)

-   Complete the settings registry and permission status UI.
-   Add biometric unlock and other optional security features only if
    supported and tested.
-   Implement intruder log/photo as best-effort, opt-in behavior.
-   Implement decoy/panic/disguise features only if their state
    separation and limitations are understood.
-   Do not allow optional features to weaken the core authentication or
    file-integrity behavior.

**Exit gate:** each enabled optional feature has permission, failure,
privacy, and retention tests.

### Phase 9 --- Advanced enhancements (P3)

Consider advanced editing, extra playback controls, sophisticated
gestures, foldable-specific layouts, and other nonessential features
only after P0/P1 behavior is stable. Add each feature only with a clear
user benefit, platform feasibility, and test plan.

**Exit gate:** optional features do not introduce regressions in core
flows.

### Phase 10 --- Release verification (P0)

-   Run build, lint, unit tests, migration tests, and available
    UI/instrumented tests.
-   Perform physical-device checks for SD card, playback, notification,
    and camera behavior where relevant.
-   Verify permissions, manifest, exported components, backup behavior,
    and release configuration.
-   Review privacy claims and remove misleading language.
-   Update README, TEST_PLAN, brain, and traceability.
-   Produce an honest final report.

**Exit gate:** no unresolved P0 issue, with every remaining limitation
documented.

------------------------------------------------------------------------

## 15. `brain.md` and Project Documentation Rules

Update `brain.md` after every completed phase. Keep it concise and
factual. It should contain:

``` markdown
# Project Brain

## Current State
- Current phase:
- Build status:
- Last verified commit:
- Known blockers:

## Architecture
- Main modules:
- Persistence:
- Storage access:
- Authentication/session state:
- Media/player stack:

## Requirements Status
- Implemented:
- Tested:
- Verified on Device:
- Deferred:
- Blocked:

## Decisions
- Decision:
- Reason:
- Alternatives considered:

## Known Limitations
- Limitation:
- User impact:
- Follow-up:

## Next Actions
1.
2.
3.
```

Also keep these documents accurate:

-   `README.md` --- setup, build, supported Android versions, feature
    inventory, permissions, limitations, and actual media support.
-   `TEST_PLAN.md` --- automated/manual test cases, platform/device
    details, results, and known gaps.
-   `REQUIREMENTS_TRACEABILITY.md` --- requirement-to-code/test mapping
    and status.
-   `TECHNICAL_DECISIONS.md` --- important dependency, architecture,
    security, and storage decisions.
-   `CHANGELOG.md` --- user-visible changes between versions, if the
    project maintains one.

Do not write "all tests passed" unless all required tests were actually
run and passed.

------------------------------------------------------------------------

## 16. Performance and Resource Budgets

Set measurable targets based on the current code and test device rather
than inventing unrealistic guarantees.

At minimum:

-   Keep scrolling responsive in a library with a large number of items.
-   Do not decode full-resolution images solely to render small
    thumbnails.
-   Bound thumbnail cache size and honor the configured cache policy.
-   Avoid duplicate scans, duplicate transfers, and repeated player
    initialization.
-   Use cancellation and backpressure for expensive work.
-   Avoid keeping camera, player, or other hardware resources active
    when not needed.
-   Release temporary resources on cancellation and lifecycle
    transitions.
-   Handle low storage and memory pressure gracefully.
-   Avoid long-running work that depends on an activity staying alive.
-   Record performance observations on a named device and test dataset;
    do not fabricate benchmark numbers.

If performance needs improvement, profile the actual bottleneck before
adding caching, concurrency, or dependencies.

------------------------------------------------------------------------

## 17. Privacy, Threat Model, and Data Recovery Notes

Document a simple threat model in `SECURITY_AND_PRIVACY.md`.

At minimum, consider:

-   Someone casually opening the app on the unlocked phone.
-   Someone who can inspect installed apps and Android settings.
-   Someone who removes the SD card and reads it on another device.
-   Someone who copies exported/shared files.
-   Someone who obtains access after the device is unlocked.
-   Android terminating the process or revoking a permission.
-   A damaged or removed SD card.
-   Accidental deletion, failed move, or interrupted migration.

For each threat, state whether the app mitigates it, partially mitigates
it, or does not protect against it. Explain residual risk in plain
language. Do not imply that a UI lock protects a file against direct
access to unencrypted storage.

Define recovery expectations for:

-   Lost folder permission.
-   SD card replacement or reformatting.
-   App reinstall.
-   Database corruption.
-   Interrupted file operations.
-   Forgotten credentials and app reset.
-   Restoring an index/settings backup.
-   Future encrypted-storage migration, if separately approved.

Do not promise recovery of media if no recoverable copy exists. Prefer
preserving originals and clearly communicating uncertainty over
pretending every operation is reversible.

------------------------------------------------------------------------

## 18. Change Control and Final Reporting

Before adding a feature or changing existing behavior, answer:

1.  Which existing requirement does this satisfy?
2.  Is it already implemented elsewhere?
3.  What security, privacy, storage, or lifecycle risk does it
    introduce?
4.  What permission or dependency does it need?
5.  How will it be tested?
6.  Which phase and priority does it belong to?
7.  What happens when it fails?

Avoid feature creep. A smaller set of fully working and tested features
is better than a long menu of unreliable options.

At the end of the update, provide a report in this format:

``` markdown
# Implementation Report

## Summary
- What changed:
- Current release readiness:

## Completed
- [Requirement ID] Feature — implementation evidence and test result.

## Partially Completed
- Feature — what works, what does not, and why.

## Deferred / Blocked
- Feature — reason and dependency.

## Validation Actually Run
- Command:
- Result:
- Device/emulator:
- Limitations:

## Data Safety
- Copy/move/delete/trash behavior verified:
- Recovery cases verified:
- Remaining data-loss risks:

## Security and Privacy
- Authentication checks:
- Permissions:
- Offline/network verification:
- Claims or limitations documented:

## Changed Files
- Path — purpose.

## Known Issues
- Issue — impact — next step.

## Next Recommended Phase
- Phase and reason.
```

Never fabricate completion status, logs, tests, device results, official
carrier details, or security guarantees.

------------------------------------------------------------------------

## 19. Final Acceptance Checklist

The update is acceptable only when the applicable items below have real
implementation and evidence.

### Core and architecture

-   [ ] The existing repository and v3 specification were inspected
    before code changes.
-   [ ] The build/dependency baseline is documented.
-   [ ] The existing product identity and required features are
    preserved unless a documented change was approved.
-   [ ] Production UI contains no dummy media, fake counts, or
    fabricated success states.
-   [ ] State has a clear source of truth and background work does not
    block the UI.
-   [ ] Database migrations preserve supported existing user data.

### Authentication and privacy

-   [ ] Cold start and reboot require authentication.
-   [ ] Lockout and sensitive-action re-authentication behave
    consistently.
-   [ ] Credentials are never stored in plaintext or written to logs.
-   [ ] Notifications and Recents content follow the configured privacy
    policy.
-   [ ] Hidden Mode, `.nomedia`, disguise, and encryption are described
    accurately.
-   [ ] Reset does not delete ordinary media and explains remaining
    hidden-folder data.

### Storage and data integrity

-   [ ] Initial indexing is read-only.
-   [ ] SAF permission loss and SD-card removal are handled gracefully.
-   [ ] Copy/move/delete operations have explicit semantics.
-   [ ] Source deletion is not attempted until the destination is
    verified for a move.
-   [ ] Interrupted operations can be reconciled after restart.
-   [ ] Trash and permanent deletion semantics are documented and
    tested.
-   [ ] Export/share warns that files leave the vault and uses safe URI
    handling.
-   [ ] No unsupported provider behavior is presented as guaranteed.

### Media and UI

-   [ ] Supported formats are documented honestly.
-   [ ] Missing/corrupt/unsupported files fail gracefully.
-   [ ] Playback handles lifecycle and audio focus appropriately.
-   [ ] Lock-versus-background-audio rules are consistent.
-   [ ] Loading, empty, error, and permission states exist for relevant
    screens.
-   [ ] Accessibility and compact-screen behavior have been reviewed.
-   [ ] Every visible control works or is clearly marked
    unavailable/deferred.

### Optional features

-   [ ] Camera/intruder photo remains opt-in and best-effort.
-   [ ] Permission denial and capture failure do not crash the app.
-   [ ] Decoy, panic, disguise, and advanced features are deferred
    unless genuinely implemented and tested.
-   [ ] Optional features cannot weaken authentication or cause data
    loss.

### Validation and handover

-   [ ] Required build and tests were actually run and results recorded.
-   [ ] Physical-device checks were performed where required, or clearly
    remain unverified.
-   [ ] Traceability, `brain.md`, `README.md`, `TEST_PLAN.md`, and
    technical decisions match the code.
-   [ ] No P0 data-integrity/authentication blocker remains unreported.
-   [ ] The final implementation report distinguishes implemented,
    tested, and device-verified work.

------------------------------------------------------------------------

## 20. Final Instruction to the AI Builder

Implement this update against the existing Private Vault codebase and
the full v3 specification. Use the v3 specification as the source for
the original product requirements, and use this addendum to resolve
implementation quality, consistency, safety, testing, and scope issues.

Where the two documents conflict, do not silently choose whichever is
easier. Record the conflict in `TECHNICAL_DECISIONS.md` and `brain.md`,
follow the safer data-preserving behavior, and call out any product
decision that truly requires owner approval. Do not remove a v3 feature
without recording it as deferred or explaining the approved change.

Work in small, verifiable phases. Prioritize authentication correctness,
local-only privacy, safe SD-card operations, real functionality, and
honest user-facing behavior over adding more visual effects or optional
features. Do not stop after writing a plan: implement the approved
phase, run the available checks, update the project documentation, and
report the actual outcome. Do not claim the entire app is complete if
any required feature remains unimplemented or unverified.
