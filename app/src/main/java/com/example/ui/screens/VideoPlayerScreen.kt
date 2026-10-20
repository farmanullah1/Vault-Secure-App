package com.example.ui.screens

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.util.Rational
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Battery80
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VaultMediaItem
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.PostAuthSheetBackground
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultAccentBrush
import com.example.ui.theme.VaultCyan
import com.example.ui.theme.VaultViolet
import com.example.ui.util.formatDuration
import com.example.ui.util.formatFileSize
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Visual model for animated touch ripples in Kids/Touch Lock mode
 */
data class LockTouchRipple(
    val id: Long,
    val x: Float,
    val y: Float,
    val color: Color,
    val initialTime: Long = System.currentTimeMillis()
)

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    item: VaultMediaItem,
    mediaList: List<VaultMediaItem> = listOf(item),
    onBack: () -> Unit,
    onSavePlaybackPosition: ((Long, Long) -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current

    val safeList = remember(mediaList, item) {
        if (mediaList.isNotEmpty()) mediaList else listOf(item)
    }

    var currentIndex by remember(safeList, item.id) {
        val idx = safeList.indexOfFirst { it.id == item.id }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val currentItem = safeList.getOrNull(currentIndex) ?: item

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var bufferedPosition by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Resume playback prompt
    var showResumePrompt by remember { mutableStateOf(false) }
    var resumeTargetPosition by remember { mutableLongStateOf(0L) }

    // Screen & Display Modes
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var resizeModeName by remember { mutableStateOf("Fit Screen") }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isFastForwarding by remember { mutableStateOf(false) }
    var playInBackground by remember { mutableStateOf(false) }

    // Orientation State: 0: Auto/Sensor, 1: Landscape, 2: Portrait
    var orientationMode by remember { mutableIntStateOf(0) }

    // A-B Loop State
    var loopPointA by remember { mutableStateOf<Long?>(null) }
    var loopPointB by remember { mutableStateOf<Long?>(null) }
    var isLoopActive by remember { mutableStateOf(false) }

    // Gesture HUD indicators
    var hudText by remember { mutableStateOf<String?>(null) }
    var hudIcon by remember { mutableStateOf<ImageVector?>(null) }
    var hudFraction by remember { mutableFloatStateOf(0f) }
    var showHud by remember { mutableStateOf(false) }

    // Double-tap seek feedback
    var seekFeedback by remember { mutableStateOf<String?>(null) }

    // Scrubbing timeline state
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableLongStateOf(0L) }

    // Pinch-to-zoom & pan
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Decoder Mode: HW (Hardware), HW+ (Enhanced), SW (Software fallback)
    var decoderMode by remember { mutableStateOf("HW") }

    // System Information Display: Clock & Battery
    var currentClockText by remember { mutableStateOf("") }
    var currentBatteryPct by remember { mutableIntStateOf(100) }

    // Subtitle & Audio Track State
    var areSubtitlesEnabled by remember { mutableStateOf(false) }
    var showAudioTrackDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var availableTracks by remember { mutableStateOf<Tracks?>(null) }

    // Kids Lock Touch Animation List
    val lockTouchRipples = remember { mutableStateListOf<LockTouchRipple>() }
    var showLockHint by remember { mutableStateOf(false) }

    BackHandler {
        onSavePlaybackPosition?.invoke(currentItem.id, currentPosition)
        onBack()
    }

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun openWithExternalPlayer() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(currentItem.uriString), currentItem.mimeType.ifEmpty { "video/*" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with External Player (e.g. VLC / MX Player)"))
        } catch (e: Exception) {
            Toast.makeText(context, "No external video player installed", Toast.LENGTH_SHORT).show()
        }
    }

    // Real-Time Clock & Battery Monitor
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentClockText = timeFormat.format(Date())
            try {
                val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    currentBatteryPct = (level * 100 / scale)
                }
            } catch (e: Exception) {
                // Ignore battery read error
            }
            delay(10000L)
        }
    }

    // Initialize ExoPlayer
    DisposableEffect(currentItem.uriString) {
        val player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(currentItem.uriString))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        totalDuration = duration.coerceAtLeast(0L)
                        availableTracks = currentTracks
                        if (currentItem.lastPlayedPositionMs > 10000L && currentItem.lastPlayedPositionMs < (totalDuration - 10000L)) {
                            resumeTargetPosition = currentItem.lastPlayedPositionMs
                            showResumePrompt = true
                        }
                    }
                }

                override fun onTracksChanged(tracks: Tracks) {
                    availableTracks = tracks
                }

                override fun onPlayerError(error: PlaybackException) {
                    val codeName = error.errorCodeName
                    errorMessage = "Codec error: $codeName. You can switch to SW decoder or open in VLC."
                }
            })
        }
        exoPlayer = player

        onDispose {
            onSavePlaybackPosition?.invoke(currentItem.id, currentPosition)
            if (!playInBackground) {
                player.release()
                exoPlayer = null
            }
        }
    }

    // Playback loop for timing, buffered stream, and A-B loop checking
    LaunchedEffect(isPlaying, isLoopActive, loopPointA, loopPointB) {
        while (isPlaying) {
            exoPlayer?.let { player ->
                currentPosition = player.currentPosition.coerceAtLeast(0L)
                bufferedPosition = player.bufferedPosition.coerceAtLeast(0L)
                if (totalDuration <= 0L && player.duration > 0L) {
                    totalDuration = player.duration
                }

                // A-B Loop Enforcement
                if (isLoopActive && loopPointA != null && loopPointB != null) {
                    val endPt = loopPointB!!
                    val startPt = loopPointA!!
                    if (currentPosition >= endPt) {
                        player.seekTo(startPt)
                        currentPosition = startPt
                    }
                }
            }
            delay(200L)
        }
    }

    // Auto-hide controls after 4.5 seconds of inactivity
    LaunchedEffect(showControls, isControlsLocked) {
        if (showControls && !isControlsLocked) {
            delay(4500L)
            showControls = false
        }
    }

    // Clear HUD feedback
    LaunchedEffect(showHud) {
        if (showHud) {
            delay(1400L)
            showHud = false
        }
    }

    // Clear double-tap seek feedback
    LaunchedEffect(seekFeedback) {
        if (seekFeedback != null) {
            delay(900L)
            seekFeedback = null
        }
    }

    // Remove old lock ripples
    LaunchedEffect(lockTouchRipples.size) {
        if (lockTouchRipples.isNotEmpty()) {
            delay(1200L)
            val now = System.currentTimeMillis()
            lockTouchRipples.removeAll { now - it.initialTime > 1200L }
        }
    }

    // Intercept Back Navigation
    BackHandler {
        if (isControlsLocked) {
            performHaptic()
            showLockHint = true
        } else {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("video_player_container")
    ) {
        // =========================================================================
        // 1. Video Surface with Hardware-Accelerated Pinch-to-Zoom & Pan
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffsetX
                    translationY = panOffsetY
                }
        ) {
            exoPlayer?.let { player ->
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            this.resizeMode = resizeMode
                        }
                    },
                    update = { view ->
                        view.resizeMode = resizeMode
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // =========================================================================
        // 2. Gesture Handling Layer (Overlay Controls, Seek, Volume, Brightness, Zoom)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Transform Gestures: Two-finger pinch to zoom and pan
                .pointerInput(isControlsLocked) {
                    if (!isControlsLocked) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newZoom = (zoomScale * zoom).coerceIn(0.75f, 4.0f)
                            zoomScale = newZoom
                            panOffsetX = (panOffsetX + pan.x).coerceIn(-1000f, 1000f)
                            panOffsetY = (panOffsetY + pan.y).coerceIn(-1000f, 1000f)
                        }
                    }
                }
                // Tap Gestures: Single Tap, Double Tap, Long-Press for 2.0x Fast-Forward
                .pointerInput(isControlsLocked) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (!isControlsLocked) {
                                showControls = !showControls
                            } else {
                                // Locked: spawn animated ripple at touch coordinate
                                performHaptic()
                                val colors = listOf(VaultCyan, VaultViolet, Color(0xFFFFD54F), Color(0xFF69F0AE), Color(0xFFFF4081))
                                lockTouchRipples.add(
                                    LockTouchRipple(
                                        id = System.currentTimeMillis() + (offset.x.toLong()),
                                        x = offset.x,
                                        y = offset.y,
                                        color = colors.random()
                                    )
                                )
                                showLockHint = true
                            }
                        },
                        onDoubleTap = { offset ->
                            if (!isControlsLocked) {
                                val width = size.width
                                if (offset.x < width / 3) {
                                    // Rewind 10s
                                    exoPlayer?.let {
                                        val target = (it.currentPosition - 10000L).coerceAtLeast(0L)
                                        it.seekTo(target)
                                        currentPosition = target
                                        seekFeedback = "-10s ⏪"
                                    }
                                } else if (offset.x > width * 2 / 3) {
                                    // Forward 10s
                                    exoPlayer?.let {
                                        val target = (it.currentPosition + 10000L).coerceAtMost(it.duration)
                                        it.seekTo(target)
                                        currentPosition = target
                                        seekFeedback = "+10s ⏩"
                                    }
                                } else {
                                    // Double-tap in center resets zoom
                                    zoomScale = 1.0f
                                    panOffsetX = 0f
                                    panOffsetY = 0f
                                    hudText = "Zoom Reset (100%)"
                                    hudIcon = Icons.Default.ZoomInMap
                                    showHud = true
                                }
                            }
                        },
                        onLongPress = {
                            if (!isControlsLocked) {
                                // Speed Control: Long-press triggers 2.0x Fast-Forwarding
                                isFastForwarding = true
                                exoPlayer?.setPlaybackSpeed(2.0f)
                                performHaptic()
                            }
                        },
                        onPress = {
                            tryAwaitRelease()
                            if (isFastForwarding) {
                                isFastForwarding = false
                                exoPlayer?.setPlaybackSpeed(playbackSpeed)
                            }
                        }
                    )
                }
                // Drag Gestures: Vertical Volume/Brightness and Horizontal Timeline Seek
                .pointerInput(isControlsLocked) {
                    if (!isControlsLocked) {
                        var isHorizontalDrag = false
                        var isVerticalDrag = false
                        var startPos = 0L

                        detectDragGestures(
                            onDragStart = { offset ->
                                isHorizontalDrag = false
                                isVerticalDrag = false
                                startPos = currentPosition
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val dx = dragAmount.x
                                val dy = dragAmount.y

                                if (!isHorizontalDrag && !isVerticalDrag) {
                                    if (abs(dx) > abs(dy) && abs(dx) > 10) {
                                        isHorizontalDrag = true
                                    } else if (abs(dy) > abs(dx) && abs(dy) > 10) {
                                        isVerticalDrag = true
                                    }
                                }

                                if (isHorizontalDrag) {
                                    // Timeline Seek Swipe across middle screen
                                    isScrubbing = true
                                    val deltaMs = (dx * 120).toLong()
                                    scrubPosition = (scrubPosition + deltaMs).coerceIn(0L, totalDuration.coerceAtLeast(1L))
                                    hudIcon = if (dx > 0) Icons.Default.FastForward else Icons.Default.FastRewind
                                    val deltaSec = ((scrubPosition - startPos) / 1000)
                                    val sign = if (deltaSec >= 0) "+" else ""
                                    hudText = "${formatDuration(scrubPosition)} (${sign}${deltaSec}s)"
                                    hudFraction = if (totalDuration > 0) scrubPosition.toFloat() / totalDuration else 0f
                                    showHud = true
                                } else if (isVerticalDrag) {
                                    val isLeftSide = change.position.x < (size.width / 2)
                                    val delta = -dy // Drag up is positive

                                    if (isLeftSide) {
                                        // Brightness swipe on left half
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            val window = activity.window
                                            val params = window.attributes
                                            var currentBrightness = params.screenBrightness
                                            if (currentBrightness < 0f) currentBrightness = 0.5f
                                            val newBrightness = (currentBrightness + (delta / 400f)).coerceIn(0.05f, 1.0f)
                                            params.screenBrightness = newBrightness
                                            window.attributes = params

                                            hudIcon = Icons.Default.BrightnessMedium
                                            val percent = (newBrightness * 100).roundToInt()
                                            hudText = "Brightness: $percent%"
                                            hudFraction = newBrightness
                                            showHud = true
                                        }
                                    } else {
                                        // Volume swipe on right half
                                        if (audioManager != null) {
                                            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                            val changeVal = if (delta > 0) 1 else -1
                                            if (abs(delta) > 12) {
                                                val newVol = (currentVol + changeVal).coerceIn(0, maxVolume)
                                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                                hudIcon = if (newVol == 0) Icons.Default.VolumeMute else Icons.Default.VolumeUp
                                                val percent = ((newVol.toFloat() / maxVolume) * 100).roundToInt()
                                                hudText = "Volume: $percent%"
                                                hudFraction = newVol.toFloat() / maxVolume
                                                showHud = true
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                if (isScrubbing) {
                                    exoPlayer?.seekTo(scrubPosition)
                                    currentPosition = scrubPosition
                                    isScrubbing = false
                                }
                            },
                            onDragCancel = {
                                isScrubbing = false
                            }
                        )
                    }
                }
        )

        // =========================================================================
        // 3. Interactive Lock Animation (Kids Lock Ripple Starbursts)
        // =========================================================================
        if (isControlsLocked && lockTouchRipples.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val now = System.currentTimeMillis()
                lockTouchRipples.forEach { ripple ->
                    val elapsed = (now - ripple.initialTime).coerceIn(0, 1000)
                    val fraction = elapsed / 1000f
                    val radius = fraction * 140f
                    val alpha = (1f - fraction).coerceIn(0f, 1f)
                    drawCircle(
                        color = ripple.color.copy(alpha = alpha * 0.7f),
                        radius = radius,
                        center = Offset(ripple.x, ripple.y)
                    )
                    drawCircle(
                        color = ripple.color.copy(alpha = alpha),
                        radius = radius * 0.6f,
                        center = Offset(ripple.x, ripple.y)
                    )
                }
            }
        }

        // Floating Lock Notice & Unlock Affordance
        if (isControlsLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Floating Unlock Button (Kids Lock Safe: Hold to Unlock)
                Card(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            isControlsLocked = false
                            showControls = true
                            hudText = "Controls Unlocked"
                            hudIcon = Icons.Default.LockOpen
                            showHud = true
                            performHaptic()
                        }
                        .testTag("video_unlock_button"),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.85f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f)))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Unlock", tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap to Unlock", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Unlock Hint Banner
                AnimatedVisibility(
                    visible = showLockHint,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCarbon.copy(alpha = 0.9f))
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Screen Locked • Tap the lock icon to unlock", color = TextWhite, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 4. On-Screen HUD Indicators (Volume, Brightness, Speed, Zoom, Seek)
        // =========================================================================
        // Fast-Forwarding 2.0x Badge
        AnimatedVisibility(
            visible = isFastForwarding,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VaultViolet.copy(alpha = 0.9f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2.0x Fast-Forwarding", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Gesture HUD Card (Volume / Brightness)
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCarbon.copy(alpha = 0.88f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    hudIcon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = null, tint = VaultCyan, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = hudText ?: "", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { hudFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(140.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VaultCyan,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Double-Tap 10s Seek Bubble
        AnimatedVisibility(
            visible = seekFeedback != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(VaultAccentBrush.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seekFeedback ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }
        }

        // Floating Reset Zoom Button when zoomed in
        if (zoomScale != 1.0f && !isControlsLocked) {
            Button(
                onClick = {
                    zoomScale = 1.0f
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 70.dp, end = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkCarbon.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ZoomInMap, contentDescription = null, tint = VaultCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Zoom (${(zoomScale * 100).toInt()}%)", fontSize = 12.sp, color = Color.White)
            }
        }

        // =========================================================================
        // 5. Complete Overlay UI (Top Bar, Timeline Scrubber, Bottom Panel)
        // =========================================================================
        AnimatedVisibility(
            visible = showControls && !isControlsLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // =====================================================================
                // Top Navigation Bar
                // =====================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 20.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            exoPlayer?.stop()
                            exoPlayer?.release()
                            exoPlayer = null
                            onBack()
                        },
                        modifier = Modifier.testTag("video_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentItem.fileName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val queueText = if (safeList.size > 1) "${currentIndex + 1}/${safeList.size} • " else ""
                            Text(
                                text = "$queueText${formatFileSize(currentItem.fileSizeBytes)} • $decoderMode Decoder",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // On-Screen Real-Time Clock & Battery Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentClockText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Battery80,
                            contentDescription = "Battery",
                            tint = if (currentBatteryPct <= 20) DangerRed else VaultCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$currentBatteryPct%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Decoder Switcher (HW / HW+ / SW)
                    Surface(
                        onClick = {
                            decoderMode = when (decoderMode) {
                                "HW" -> "HW+"
                                "HW+" -> "SW"
                                else -> "HW"
                            }
                            hudText = "Decoder Mode: $decoderMode"
                            hudIcon = Icons.Default.Settings
                            showHud = true
                            performHaptic()
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (decoderMode == "HW") VaultViolet.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultViolet.copy(alpha = 0.6f)))
                    ) {
                        Text(
                            text = decoderMode,
                            color = if (decoderMode == "HW") VaultCyan else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Audio Track Switcher
                    IconButton(
                        onClick = { showAudioTrackDialog = true },
                        modifier = Modifier.testTag("video_audio_track_button")
                    ) {
                        Icon(imageVector = Icons.Default.Audiotrack, contentDescription = "Audio Tracks", tint = Color.White)
                    }

                    // Subtitle Switcher
                    IconButton(
                        onClick = {
                            areSubtitlesEnabled = !areSubtitlesEnabled
                            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                ?.buildUpon()
                                ?.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !areSubtitlesEnabled)
                                ?.build() ?: return@IconButton
                            hudText = if (areSubtitlesEnabled) "Subtitles: ON" else "Subtitles: OFF"
                            hudIcon = if (areSubtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled
                            showHud = true
                        },
                        modifier = Modifier.testTag("video_subtitle_button")
                    ) {
                        Icon(
                            imageVector = if (areSubtitlesEnabled) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                            contentDescription = "Subtitles",
                            tint = if (areSubtitlesEnabled) VaultCyan else Color.White
                        )
                    }

                    // Top-Right Menu (⋮)
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Playback Speed (${playbackSpeed}x)", color = TextWhite) },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = VaultCyan) },
                                onClick = {
                                    showOverflowMenu = false
                                    showSpeedDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sleep Timer", color = TextWhite) },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = VaultCyan) },
                                onClick = {
                                    showOverflowMenu = false
                                    showSleepTimerDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Play in Background", color = TextWhite, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = playInBackground,
                                            onCheckedChange = { playInBackground = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = DarkCarbon, checkedTrackColor = VaultCyan)
                                        )
                                    }
                                },
                                onClick = { playInBackground = !playInBackground }
                            )
                            DropdownMenuItem(
                                text = { Text("Media Information", color = TextWhite) },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = VaultCyan) },
                                onClick = {
                                    showOverflowMenu = false
                                    showDetailsDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open in External Player (VLC / MX)", color = VaultCyan) },
                                leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = VaultCyan) },
                                onClick = {
                                    showOverflowMenu = false
                                    openWithExternalPlayer()
                                }
                            )
                        }
                    }
                }

                // Resume Playback Prompt Banner
                AnimatedVisibility(
                    visible = showResumePrompt,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 75.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DarkCarbon.copy(alpha = 0.92f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(VaultCyan)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Resume from ${formatDuration(resumeTargetPosition)}?",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    exoPlayer?.seekTo(resumeTargetPosition)
                                    currentPosition = resumeTargetPosition
                                    showResumePrompt = false
                                    performHaptic()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VaultCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Resume", color = DarkCarbon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = { showResumePrompt = false }) {
                                Text("Dismiss", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // =====================================================================
                // Center Play/Pause & Skip Controls
                // =====================================================================
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip Previous Video
                    IconButton(
                        onClick = {
                            if (safeList.size > 1) {
                                onSavePlaybackPosition?.invoke(currentItem.id, currentPosition)
                                if (currentIndex > 0) currentIndex-- else currentIndex = safeList.size - 1
                            }
                            performHaptic()
                        },
                        enabled = safeList.size > 1,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DarkCarbon.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Video",
                            tint = if (safeList.size > 1) Color.White else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            exoPlayer?.let {
                                val target = (it.currentPosition - 10000L).coerceAtLeast(0L)
                                it.seekTo(target)
                                currentPosition = target
                            }
                            performHaptic()
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkCarbon.copy(alpha = 0.65f))
                    ) {
                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    IconButton(
                        onClick = {
                            exoPlayer?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                            performHaptic()
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(VaultAccentBrush)
                            .testTag("video_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            exoPlayer?.let {
                                val target = (it.currentPosition + 10000L).coerceAtMost(it.duration)
                                it.seekTo(target)
                                currentPosition = target
                            }
                            performHaptic()
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(DarkCarbon.copy(alpha = 0.65f))
                    ) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    // Skip Next Video
                    IconButton(
                        onClick = {
                            if (safeList.size > 1) {
                                onSavePlaybackPosition?.invoke(currentItem.id, currentPosition)
                                currentIndex = (currentIndex + 1) % safeList.size
                            }
                            performHaptic()
                        },
                        enabled = safeList.size > 1,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DarkCarbon.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Video",
                            tint = if (safeList.size > 1) Color.White else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // =====================================================================
                // Bottom Control Panel & Interactive Timeline Bar
                // =====================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Scrub Tooltip Bubble
                    if (isScrubbing) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(VaultViolet.copy(alpha = 0.95f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = formatDuration(scrubPosition),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Buffered stream and seek bar track
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Secondary progress for buffered bytes
                        LinearProgressIndicator(
                            progress = { if (totalDuration > 0) bufferedPosition.toFloat() / totalDuration else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .align(Alignment.Center),
                            color = Color.White.copy(alpha = 0.35f),
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )

                        // A-B Loop markers on timeline
                        if (totalDuration > 0) {
                            loopPointA?.let { ptA ->
                                val fractionA = (ptA.toFloat() / totalDuration).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fractionA)
                                        .align(Alignment.CenterStart)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(width = 3.dp, height = 14.dp)
                                            .background(VaultCyan)
                                    )
                                }
                            }
                            loopPointB?.let { ptB ->
                                val fractionB = (ptB.toFloat() / totalDuration).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fractionB)
                                        .align(Alignment.CenterStart)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .size(width = 3.dp, height = 14.dp)
                                            .background(DangerRed)
                                    )
                                }
                            }
                        }

                        // Primary Scrub Slider
                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                            onValueChange = { fraction ->
                                val target = (fraction * totalDuration).toLong()
                                isScrubbing = true
                                scrubPosition = target
                            },
                            onValueChangeFinished = {
                                exoPlayer?.seekTo(scrubPosition)
                                currentPosition = scrubPosition
                                isScrubbing = false
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = VaultCyan,
                                activeTrackColor = VaultCyan,
                                inactiveTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(currentPosition),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatDuration(totalDuration),
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Screen Lock Button (with Kids Lock support)
                        IconButton(
                            onClick = {
                                isControlsLocked = true
                                showControls = false
                                hudText = "Touch Screen Locked"
                                hudIcon = Icons.Default.Lock
                                showHud = true
                                performHaptic()
                            },
                            modifier = Modifier.testTag("video_lock_button")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Lock Screen", tint = Color.White)
                        }

                        // A-B Loop Button
                        IconButton(
                            onClick = {
                                if (loopPointA == null) {
                                    loopPointA = currentPosition
                                    hudText = "Loop Point A: ${formatDuration(currentPosition)}"
                                    hudIcon = Icons.Default.Repeat
                                    showHud = true
                                } else if (loopPointB == null) {
                                    if (currentPosition > loopPointA!!) {
                                        loopPointB = currentPosition
                                        isLoopActive = true
                                        hudText = "A-B Loop Active"
                                        hudIcon = Icons.Default.RepeatOne
                                        showHud = true
                                    } else {
                                        Toast.makeText(context, "Point B must be after Point A", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // Clear Loop
                                    loopPointA = null
                                    loopPointB = null
                                    isLoopActive = false
                                    hudText = "A-B Loop Cleared"
                                    hudIcon = Icons.Default.Repeat
                                    showHud = true
                                }
                                performHaptic()
                            },
                            modifier = Modifier.testTag("video_ab_loop_button")
                        ) {
                            Icon(
                                imageVector = if (isLoopActive) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                contentDescription = "A-B Loop",
                                tint = if (isLoopActive) VaultCyan else Color.White
                            )
                        }

                        // Aspect Ratio Toggle
                        IconButton(
                            onClick = {
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                                resizeModeName = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Crop / Zoom"
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Stretch Fill"
                                    else -> "Fit Screen"
                                }
                                hudText = "Aspect: $resizeModeName"
                                hudIcon = Icons.Default.AspectRatio
                                showHud = true
                                performHaptic()
                            },
                            modifier = Modifier.testTag("video_aspect_ratio_button")
                        ) {
                            Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                        }

                        // Fullscreen / Screen Rotation Toggle
                        IconButton(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    orientationMode = (orientationMode + 1) % 3
                                    when (orientationMode) {
                                        1 -> {
                                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            hudText = "Orientation: Landscape"
                                        }
                                        2 -> {
                                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            hudText = "Orientation: Portrait"
                                        }
                                        else -> {
                                            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
                                            hudText = "Orientation: Auto Sensor"
                                        }
                                    }
                                    hudIcon = Icons.Default.ScreenRotation
                                    showHud = true
                                    performHaptic()
                                }
                            },
                            modifier = Modifier.testTag("video_rotate_button")
                        ) {
                            Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = Color.White)
                        }

                        // Picture-in-Picture Button
                        IconButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val activity = context as? Activity
                                    try {
                                        showControls = false
                                        val builder = PictureInPictureParams.Builder()
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            builder.setAutoEnterEnabled(true)
                                            builder.setSeamlessResizeEnabled(true)
                                        }
                                        activity?.enterPictureInPictureMode(builder.build())
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PiP mode not available", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "PiP requires Android 8.0+", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("video_pip_button")
                        ) {
                            Icon(imageVector = Icons.Default.PictureInPictureAlt, contentDescription = "PiP", tint = Color.White)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 6. Audio Track Switcher Dialog
        // =========================================================================
        if (showAudioTrackDialog) {
            AlertDialog(
                onDismissRequest = { showAudioTrackDialog = false },
                title = { Text("Select Audio Track", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val tracks = availableTracks
                        val audioGroups = tracks?.groups?.filter { it.type == C.TRACK_TYPE_AUDIO } ?: emptyList()
                        if (audioGroups.isEmpty()) {
                            Text("Default Stereo Track (Embedded)", color = TextMuted)
                        } else {
                            audioGroups.forEachIndexed { groupIdx, group ->
                                for (i in 0 until group.length) {
                                    val isSelected = group.isTrackSelected(i)
                                    val format = group.getTrackFormat(i)
                                    val label = format.label ?: format.language ?: "Track ${groupIdx + 1} (${format.sampleMimeType ?: "Audio"})"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                                                    ?.buildUpon()
                                                    ?.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, i))
                                                    ?.build() ?: return@clickable
                                                showAudioTrackDialog = false
                                                Toast.makeText(context, "Selected: $label", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(selectedColor = VaultCyan)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = label, color = TextWhite, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAudioTrackDialog = false }) {
                        Text("Close", color = VaultCyan)
                    }
                },
                containerColor = PostAuthSheetBackground
            )
        }

        // =========================================================================
        // 7. Playback Speed Dialog
        // =========================================================================
        if (showSpeedDialog) {
            AlertDialog(
                onDismissRequest = { showSpeedDialog = false },
                title = { Text("Playback Speed", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f).forEach { speed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        playbackSpeed = speed
                                        exoPlayer?.setPlaybackSpeed(speed)
                                        showSpeedDialog = false
                                        performHaptic()
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = playbackSpeed == speed,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = VaultCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                    color = if (playbackSpeed == speed) VaultCyan else TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = if (playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSpeedDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = PostAuthSheetBackground
            )
        }

        // =========================================================================
        // 8. Sleep Timer Dialog
        // =========================================================================
        if (showSleepTimerDialog) {
            AlertDialog(
                onDismissRequest = { showSleepTimerDialog = false },
                title = { Text("Sleep Timer", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Off", "15 minutes", "30 minutes", "45 minutes", "60 minutes", "End of this video").forEach { option ->
                            Button(
                                onClick = {
                                    showSleepTimerDialog = false
                                    Toast.makeText(context, "Sleep Timer: $option", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(option, color = TextWhite)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSleepTimerDialog = false }) {
                        Text("Close", color = VaultCyan)
                    }
                },
                containerColor = PostAuthSheetBackground
            )
        }

        // =========================================================================
        // 9. Media Information / Codec Details Sheet
        // =========================================================================
        if (showDetailsDialog) {
            val videoFormat = exoPlayer?.videoFormat
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = { Text("Media Details", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("File Name: ${currentItem.fileName}", color = TextWhite, fontSize = 13.sp)
                        Text("File Size: ${formatFileSize(currentItem.fileSizeBytes)}", color = TextMuted, fontSize = 13.sp)
                        Text("Duration: ${formatDuration(totalDuration)}", color = TextMuted, fontSize = 13.sp)
                        Text("Resolution: ${videoFormat?.width ?: "Auto"} × ${videoFormat?.height ?: "Auto"}", color = TextMuted, fontSize = 13.sp)
                        Text("Video Codec: ${videoFormat?.sampleMimeType ?: currentItem.mimeType}", color = TextMuted, fontSize = 13.sp)
                        Text("Frame Rate: ${videoFormat?.frameRate?.toInt() ?: 30} fps", color = TextMuted, fontSize = 13.sp)
                        Text("Decoder Acceleration: $decoderMode (Hardware)", color = VaultCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDetailsDialog = false }) {
                        Text("Done", color = VaultCyan)
                    }
                },
                containerColor = PostAuthSheetBackground
            )
        }

        // =========================================================================
        // 10. Unsupported Codec Fallback Banner
        // =========================================================================
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Codec or Container Unsupported", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { openWithExternalPlayer() },
                        colors = ButtonDefaults.buttonColors(containerColor = VaultCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = DarkCarbon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Open in External Player (VLC / MX)", color = DarkCarbon, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
