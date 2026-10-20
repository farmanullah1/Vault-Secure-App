package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import com.example.ui.util.formatDuration
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VaultMediaItem
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    item: VaultMediaItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isControlsLocked by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Gesture HUD indicators
    var hudText by remember { mutableStateOf<String?>(null) }
    var hudIcon by remember { mutableStateOf<androidx.compose.ui.graphics.vector.ImageVector?>(null) }
    var hudFraction by remember { mutableFloatStateOf(0f) }
    var showHud by remember { mutableStateOf(false) }

    // Seek feedback
    var seekFeedback by remember { mutableStateOf<String?>(null) }

    fun openWithExternalPlayer() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(item.uriString), item.mimeType.ifEmpty { "video/*" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with External Player (e.g. VLC / MX Player)"))
        } catch (e: Exception) {
            Toast.makeText(context, "No external video player installed", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(item.uriString) {
        val player = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(item.uriString))
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
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    val codeName = error.errorCodeName
                    errorMessage = "Unsupported container or hardware codec (${codeName}). Tap below to open in VLC or another media player."
                }
            })
        }
        exoPlayer = player

        onDispose {
            player.release()
            exoPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            exoPlayer?.let {
                currentPosition = it.currentPosition.coerceAtLeast(0L)
                if (totalDuration <= 0L && it.duration > 0L) {
                    totalDuration = it.duration
                }
            }
            delay(400L)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls) {
        if (showControls && !isControlsLocked) {
            delay(4000L)
            showControls = false
        }
    }

    // Clear HUD after gesture
    LaunchedEffect(showHud) {
        if (showHud) {
            delay(1200L)
            showHud = false
        }
    }

    LaunchedEffect(seekFeedback) {
        if (seekFeedback != null) {
            delay(800L)
            seekFeedback = null
        }
    }

    BackHandler {
        if (isControlsLocked) {
            isControlsLocked = false
        } else {
            exoPlayer?.stop()
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Gesture detection for seek, brightness, volume, and control toggle
            .pointerInput(isControlsLocked) {
                detectTapGestures(
                    onTap = {
                        if (!isControlsLocked) {
                            showControls = !showControls
                        } else {
                            showControls = true
                        }
                    },
                    onDoubleTap = { offset ->
                        if (!isControlsLocked) {
                            val width = size.width
                            if (offset.x < width / 2) {
                                // Double tap left: Rewind 10s
                                exoPlayer?.let {
                                    val target = (it.currentPosition - 10000L).coerceAtLeast(0L)
                                    it.seekTo(target)
                                    currentPosition = target
                                    seekFeedback = "-10s"
                                }
                            } else {
                                // Double tap right: Forward 10s
                                exoPlayer?.let {
                                    val target = (it.currentPosition + 10000L).coerceAtMost(it.duration)
                                    it.seekTo(target)
                                    currentPosition = target
                                    seekFeedback = "+10s"
                                }
                            }
                        }
                    }
                )
            }
            .pointerInput(isControlsLocked) {
                if (!isControlsLocked) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val isLeftSide = change.position.x < (size.width / 2)
                        val delta = -dragAmount.y // Drag up is positive

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
                                val percent = (newBrightness * 100).toInt()
                                hudText = "Brightness: $percent%"
                                hudFraction = newBrightness
                                showHud = true
                            }
                        } else {
                            // Volume swipe on right half
                            if (audioManager != null) {
                                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val changeVal = if (delta > 0) 1 else -1
                                if (Math.abs(delta) > 15) {
                                    val newVol = (currentVol + changeVal).coerceIn(0, maxVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    hudIcon = Icons.Default.VolumeUp
                                    val percent = ((newVol.toFloat() / maxVolume) * 100).toInt()
                                    hudText = "Volume: $percent%"
                                    hudFraction = newVol.toFloat() / maxVolume
                                    showHud = true
                                }
                            }
                        }
                    }
                }
            }
            .testTag("video_player_container")
    ) {
        // Player Surface
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

        // On-screen HUD for Brightness & Volume Gestures
        AnimatedVisibility(
            visible = showHud,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCarbon.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    hudIcon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = null, tint = VaultEmerald, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = hudText ?: "", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { hudFraction },
                        modifier = Modifier
                            .width(140.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = VaultEmerald,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }

        // Double Tap Seek Ripple indicator
        AnimatedVisibility(
            visible = seekFeedback != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(VaultEmerald.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seekFeedback ?: "",
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Top Action Bar
                if (!isControlsLocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer?.stop()
                                onBack()
                            },
                            modifier = Modifier.testTag("video_back_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = item.fileName,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Aspect Ratio Cycle Button
                        IconButton(onClick = {
                            resizeMode = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                            }
                            val modeName = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Crop / Zoom"
                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> "Stretch Fill"
                                else -> "Fit Screen"
                            }
                            hudText = "Aspect Ratio: $modeName"
                            hudIcon = Icons.Default.AspectRatio
                            showHud = true
                        }) {
                            Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = TextWhite)
                        }

                        // Speed Dropdown
                        Box {
                            IconButton(onClick = { showSpeedMenu = true }) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed", tint = TextWhite)
                            }
                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(SurfaceCard)
                            ) {
                                listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text("${speed}x", color = if (playbackSpeed == speed) VaultEmerald else TextWhite) },
                                        onClick = {
                                            playbackSpeed = speed
                                            exoPlayer?.setPlaybackSpeed(speed)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Picture-in-Picture Button
                        IconButton(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    val activity = context as? Activity
                                    try {
                                        showControls = false
                                        activity?.enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "PiP not supported on this device", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "PiP requires Android 8.0+", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("video_pip_button")
                        ) {
                            Icon(imageVector = Icons.Default.PictureInPictureAlt, contentDescription = "Picture-in-Picture", tint = TextWhite)
                        }

                        // Screen Rotation Toggle Button
                        IconButton(
                            onClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    val currentOrientation = activity.requestedOrientation
                                    activity.requestedOrientation = if (currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    } else {
                                        android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    }
                                }
                            },
                            modifier = Modifier.testTag("video_rotate_button")
                        ) {
                            Icon(imageVector = Icons.Default.ScreenRotation, contentDescription = "Rotate Screen", tint = TextWhite)
                        }

                        // Open with External Player (VLC / MX)
                        IconButton(
                            onClick = { openWithExternalPlayer() },
                            modifier = Modifier.testTag("video_external_player_button")
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open in External Player", tint = VaultEmerald)
                        }
                    }
                }

                // Lock Controls Toggle Button (Always accessible on side)
                IconButton(
                    onClick = {
                        isControlsLocked = !isControlsLocked
                        hudText = if (isControlsLocked) "Controls Locked" else "Controls Unlocked"
                        hudIcon = if (isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen
                        showHud = true
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isControlsLocked) DangerRed.copy(alpha = 0.75f) else DarkCarbon.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isControlsLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock Controls",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center Play/Pause & Seek Buttons
                if (!isControlsLocked) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                exoPlayer?.let {
                                    val target = (it.currentPosition - 10000L).coerceAtLeast(0L)
                                    it.seekTo(target)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkCarbon.copy(alpha = 0.65f))
                        ) {
                            Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = TextWhite)
                        }

                        IconButton(
                            onClick = {
                                exoPlayer?.let {
                                    if (it.isPlaying) it.pause() else it.play()
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(VaultEmerald)
                                .testTag("video_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = DarkCarbon,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                exoPlayer?.let {
                                    val target = (it.currentPosition + 10000L).coerceAtMost(it.duration)
                                    it.seekTo(target)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(DarkCarbon.copy(alpha = 0.65f))
                        ) {
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward 10s", tint = TextWhite)
                        }
                    }
                }

                // Bottom Progress Scrubber
                if (!isControlsLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat() else 0f,
                            onValueChange = { fraction ->
                                val target = (fraction * totalDuration).toLong()
                                exoPlayer?.seekTo(target)
                                currentPosition = target
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = VaultEmerald,
                                activeTrackColor = VaultEmerald,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatDuration(currentPosition), color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(text = formatDuration(totalDuration), color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Codec Issue / Unsupported Format Banner
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
                        colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
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
