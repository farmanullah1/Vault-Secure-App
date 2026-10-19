package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import com.example.ui.util.formatDuration
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.VaultMediaItem
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    item: VaultMediaItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isLooping by remember { mutableStateOf(false) }

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
            delay(500L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Audio Player", fontSize = 18.sp, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("audio_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
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
            Spacer(modifier = Modifier.height(16.dp))

            // Animated Visualizer Disc
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(VaultEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio Disc",
                        tint = VaultEmerald,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Animated Waveform Bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(48.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "audio_bars")
                val heights = listOf(24.dp, 40.dp, 16.dp, 36.dp, 48.dp, 28.dp, 44.dp, 20.dp, 32.dp)

                heights.forEachIndexed { idx, targetHeight ->
                    val animatedHeight by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = targetHeight.value,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400 + (idx * 60), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "bar_$idx"
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(if (isPlaying) animatedHeight.dp else 8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(VaultEmerald)
                    )
                }
            }

            // Track Information
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.fileName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                val sizeKb = item.fileSizeBytes / 1024
                Text(
                    text = if (sizeKb > 1024) "${String.format("%.1f", sizeKb / 1024f)} MB • Private Safe" else "$sizeKb KB • Private Safe",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Scrubber
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

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isLooping = !isLooping
                        exoPlayer?.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isLooping) VaultEmerald else TextMuted
                    )
                }

                IconButton(
                    onClick = {
                        exoPlayer?.let {
                            val target = (it.currentPosition - 10000L).coerceAtLeast(0L)
                            it.seekTo(target)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
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

                IconButton(
                    onClick = {
                        exoPlayer?.let {
                            val target = (it.currentPosition + 10000L).coerceAtMost(it.duration)
                            it.seekTo(target)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                ) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = "Forward 10s", tint = TextWhite)
                }

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
