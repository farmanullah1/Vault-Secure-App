package com.example.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VaultMediaItem
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.ui.util.formatDuration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    mediaList: List<VaultMediaItem>,
    initialMediaId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    val safeList = remember(mediaList) {
        if (mediaList.isEmpty()) emptyList() else mediaList
    }

    var currentIndex by remember(safeList, initialMediaId) {
        val idx = safeList.indexOfFirst { it.id == initialMediaId }
        mutableIntStateOf(if (idx >= 0) idx else 0)
    }

    val currentItem = safeList.getOrNull(currentIndex)

    if (currentItem == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isLooping by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var embeddedArtBytes by remember { mutableStateOf<ByteArray?>(null) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Load Embedded Artwork & Metadata via MediaMetadataRetriever
    LaunchedEffect(currentItem.uriString) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(currentItem.uriString))
            embeddedArtBytes = retriever.embeddedPicture
            retriever.release()
        } catch (e: Exception) {
            embeddedArtBytes = null
        }
    }

    // Sleep Timer countdown
    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            delay(sleepTimerMinutes * 60 * 1000L)
            exoPlayer?.pause()
            sleepTimerMinutes = 0
            Toast.makeText(context, "Sleep timer: Audio playback paused", Toast.LENGTH_SHORT).show()
        }
    }

    // Initialize ExoPlayer for currentItem
    DisposableEffect(currentItem.uriString) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val noisyReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    exoPlayer?.pause()
                    isPlaying = false
                    try {
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    } catch (e: Exception) {
                        // Volume fallback
                    }
                }
            }
        }
        val noisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(noisyReceiver, noisyFilter)

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
                    } else if (playbackState == Player.STATE_ENDED) {
                        if (!isLooping && safeList.size > 1) {
                            if (isShuffle) {
                                currentIndex = (0 until safeList.size).random()
                            } else if (currentIndex < safeList.size - 1) {
                                currentIndex++
                            }
                        }
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            try {
                context.unregisterReceiver(noisyReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            player.release()
            exoPlayer = null
        }
    }

    // Playback loop for timing
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

    // Rotating vinyl disk animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val diskRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disk_rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Secure Audio Player", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(
                            text = "${currentIndex + 1} of ${safeList.size} in queue",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("audio_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerMinutes > 0) VaultEmerald else TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceElevated)
            )
        },
        containerColor = DarkCarbon
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Animated Visualizer Disc or Embedded Album Artwork
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                if (embeddedArtBytes != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(embeddedArtBytes)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .rotate(if (isPlaying) diskRotation else 0f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .background(VaultEmerald.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Audio Disc",
                            tint = VaultEmerald,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                }
            }

            // Animated Frequency Waveform Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(48.dp)
            ) {
                val heights = listOf(24.dp, 40.dp, 16.dp, 36.dp, 48.dp, 28.dp, 44.dp, 20.dp, 32.dp, 18.dp, 38.dp)
                heights.forEachIndexed { idx, targetHeight ->
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = targetHeight.value,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400 + (idx * 55), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "audio_bar_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(if (isPlaying) animatedHeight.dp else 8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(VaultEmerald)
                    )
                }
            }

            // Track Information
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentItem.fileName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                val sizeKb = currentItem.fileSizeBytes / 1024
                Text(
                    text = if (sizeKb > 1024) "${String.format("%.1f", sizeKb / 1024f)} MB • Private Safe" else "$sizeKb KB • Private Safe",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (sleepTimerMinutes > 0) {
                    Text(
                        text = "Sleep Timer Active ($sleepTimerMinutes min)",
                        fontSize = 11.sp,
                        color = VaultEmerald,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Scrubber Slider
            Column(modifier = Modifier.fillMaxWidth()) {
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
                        inactiveTrackColor = SurfaceCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatDuration(currentPosition), fontSize = 12.sp, color = TextMuted)
                    Text(text = formatDuration(totalDuration), fontSize = 12.sp, color = TextMuted)
                }
            }

            // Control Buttons Row (Shuffle, Previous, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Toggle
                IconButton(
                    onClick = {
                        isShuffle = !isShuffle
                        Toast.makeText(context, if (isShuffle) "Shuffle On" else "Shuffle Off", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) VaultEmerald else TextMuted
                    )
                }

                // Previous Track
                IconButton(
                    onClick = {
                        if (safeList.size > 1) {
                            if (currentIndex > 0) {
                                currentIndex--
                            } else {
                                currentIndex = safeList.size - 1
                            }
                        } else {
                            exoPlayer?.seekTo(0L)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                ) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous Track", tint = TextWhite)
                }

                // Play / Pause
                IconButton(
                    onClick = {
                        exoPlayer?.let {
                            if (it.isPlaying) it.pause() else it.play()
                        }
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(VaultEmerald)
                        .testTag("audio_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DarkCarbon,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = {
                        if (safeList.size > 1) {
                            if (isShuffle) {
                                currentIndex = (0 until safeList.size).random()
                            } else {
                                currentIndex = (currentIndex + 1) % safeList.size
                            }
                        } else {
                            exoPlayer?.seekTo(0L)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                ) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next Track", tint = TextWhite)
                }

                // Repeat Mode Toggle
                IconButton(
                    onClick = {
                        isLooping = !isLooping
                        exoPlayer?.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            exoPlayer?.setPlaybackSpeed(playbackSpeed)
                    }
                ) {
                    Icon(
                        imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isLooping) VaultEmerald else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Speed Selector Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val speedOptions = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                speedOptions.forEach { speed ->
                    val isSelected = playbackSpeed == speed
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                playbackSpeed = speed
                                exoPlayer?.setPlaybackSpeed(speed)
                            },
                        color = if (isSelected) VaultEmerald else SurfaceCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) VaultEmerald else SurfaceElevated
                        )
                    ) {
                        Text(
                            text = if (speed == 1.0f) "1.0x" else "${speed}x",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DarkCarbon else TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        val timerOptions = listOf(0, 15, 30, 45, 60)
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text("Sleep Timer", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    timerOptions.forEach { minutes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sleepTimerMinutes == minutes,
                                onClick = {
                                    sleepTimerMinutes = minutes
                                    showSleepTimerDialog = false
                                    Toast.makeText(
                                        context,
                                        if (minutes == 0) "Sleep timer cancelled" else "Audio will pause in $minutes minutes",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = VaultEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (minutes == 0) "Turn Off Sleep Timer" else "$minutes Minutes",
                                color = TextWhite,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}
