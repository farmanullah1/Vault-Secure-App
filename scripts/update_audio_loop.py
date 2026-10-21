file_path = 'app/src/main/java/com/example/ui/screens/AudioPlayerScreen.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace isLooping state with loopMode: 0 = OFF, 1 = ALL, 2 = ONE
old_state = "    var isLooping by remember { mutableStateOf(false) }"
new_state = "    var loopMode by remember { mutableIntStateOf(0) } // 0 = OFF, 1 = ALL, 2 = ONE"
assert old_state in content, "old_state not found"
content = content.replace(old_state, new_state, 1)

# Update onPlaybackStateChanged
old_playback_state = """                override fun onPlaybackStateChanged(playbackState: Int) {
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
                }"""

new_playback_state = """                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        totalDuration = duration.coerceAtLeast(0L)
                        // Sync repeat mode to player
                        repeatMode = when (loopMode) {
                            1 -> Player.REPEAT_MODE_ALL
                            2 -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                    } else if (playbackState == Player.STATE_ENDED) {
                        if (loopMode == 1 && safeList.size > 1) {
                            // Loop all tracks in playlist
                            if (isShuffle) {
                                currentIndex = (0 until safeList.size).random()
                            } else {
                                currentIndex = (currentIndex + 1) % safeList.size
                            }
                        } else if (loopMode == 0 && safeList.size > 1) {
                            // Normal sequential playback until end of queue
                            if (isShuffle) {
                                currentIndex = (0 until safeList.size).random()
                            } else if (currentIndex < safeList.size - 1) {
                                currentIndex++
                            }
                        }
                    }
                }"""

assert old_playback_state in content, "old_playback_state not found"
content = content.replace(old_playback_state, new_playback_state, 1)

# Update Repeat Mode Toggle button in UI
old_button = """                // Repeat Mode Toggle
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
                }"""

new_button = """                // Loop / Repeat Mode Toggle: OFF -> ALL -> ONE -> OFF
                IconButton(
                    onClick = {
                        loopMode = (loopMode + 1) % 3
                        exoPlayer?.repeatMode = when (loopMode) {
                            1 -> Player.REPEAT_MODE_ALL
                            2 -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        exoPlayer?.setPlaybackSpeed(playbackSpeed)
                        val toastMsg = when (loopMode) {
                            1 -> "Loop All Tracks"
                            2 -> "Loop Current Track"
                            else -> "Loop Off"
                        }
                        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = if (loopMode == 2) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = when (loopMode) {
                            1 -> "Loop All"
                            2 -> "Loop One"
                            else -> "Loop Off"
                        },
                        tint = if (loopMode > 0) VaultEmerald else TextMuted
                    )
                }"""

assert old_button in content, "old_button not found"
content = content.replace(old_button, new_button, 1)

with open(file_path, 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)
print("Updated AudioPlayerScreen with 3-mode loop successfully!")
