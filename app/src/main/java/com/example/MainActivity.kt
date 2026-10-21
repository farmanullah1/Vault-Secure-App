package com.example

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.example.security.PinSecurityManager
import com.example.ui.screens.AudioPlayerScreen
import com.example.ui.screens.GalleryChecklistScreen
import com.example.ui.screens.PhotoViewerScreen
import com.example.ui.screens.SimCoverScreen
import com.example.ui.screens.VaultMainScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.Screen
import com.example.viewmodel.VaultViewModel

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.ui.screens.CalculatorCoverScreen
import com.example.ui.screens.IntruderLogsScreen
import com.example.ui.screens.NoteViewerScreen
import com.example.ui.screens.NotesCoverScreen

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val screenOffReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (intent?.action == android.content.Intent.ACTION_SCREEN_OFF) {
                if (viewModel.isUnlocked.value &&
                    viewModel.securityManager.isLockOnScreenOffEnabled &&
                    viewModel.securityManager.autoLockSeconds != PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE
                ) {
                    viewModel.lockVault()
                }
            }
        }
    }

    private val sensorListener = object : SensorEventListener {
        private var lastShakeTime = 0L
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                if (z < -7.0f && viewModel.isUnlocked.value && viewModel.securityManager.isFlipToLockEnabled) {
                    viewModel.lockVault()
                }
                if (viewModel.isUnlocked.value && viewModel.securityManager.isShakeToLockEnabled) {
                    val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                    val now = SystemClock.elapsedRealtime()
                    if (acceleration > 12.0 && now - lastShakeTime > 1000L) {
                        lastShakeTime = now
                        viewModel.lockVault()
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            registerReceiver(screenOffReceiver, android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF))
        } catch (e: Exception) {}

        sensorManager = getSystemService(SENSOR_SERVICE) as? SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Edge-to-edge system bar contrast
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        // Apply FLAG_SECURE if enabled (Spec 9.5: prevents screenshots & recents leaks)
        applySecurityFlags()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkAutoLockTimeout()
    }

    override fun onResume() {
        super.onResume()
        applySecurityFlags()
        checkAutoLockTimeout()
        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(sensorListener)
    }

    private fun applySecurityFlags() {
        if (viewModel.securityManager.isFlagSecureEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun checkAutoLockTimeout() {
        if (viewModel.isUnlocked.value) {
            val autoLockSetting = viewModel.securityManager.autoLockSeconds
            when {
                autoLockSetting == PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE -> {
                    // Stays unlocked while app remains running in memory;
                    // re-requires authentication whenever app is closed and reopened.
                }
                autoLockSetting > 0 -> {
                    val lastBg = viewModel.securityManager.lastBackgroundElapsedRealtime
                    if (lastBg > 0L) {
                        val elapsed = SystemClock.elapsedRealtime() - lastBg
                        if (elapsed >= autoLockSetting * 1000L) {
                            viewModel.lockVault()
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isUnlocked.value) {
            val autoLockSetting = viewModel.securityManager.autoLockSeconds
            if (autoLockSetting == PinSecurityManager.AUTO_LOCK_IMMEDIATE) {
                viewModel.lockVault()
            } else if (autoLockSetting > 0) {
                viewModel.securityManager.lastBackgroundElapsedRealtime = SystemClock.elapsedRealtime()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {}
    }
}

@Composable
fun AppNavigation(viewModel: VaultViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val showSecurityAlertSheet by viewModel.showSecurityAlertSheet.collectAsState()
    val unseenEventsForAlert by viewModel.unseenEventsForAlert.collectAsState()

    if (showSecurityAlertSheet && unseenEventsForAlert.isNotEmpty()) {
        com.example.ui.components.SecurityAlertSheet(
            unseenEvents = unseenEventsForAlert,
            onDismiss = { viewModel.dismissSecurityAlert() },
            onViewLog = { viewModel.navigateTo(Screen.IntruderLogs) },
            onDeletePhotos = { viewModel.deleteIntruderPhotos() }
        )
    }

    when (val screen = currentScreen) {
        is Screen.PinEntry -> {
            com.example.ui.screens.PinEntryScreen(
                viewModel = viewModel,
                onBackToCover = { viewModel.lockVault() }
            )
        }

        is Screen.FirstTimePermissions -> {
            com.example.ui.screens.FirstTimePermissionsScreen(viewModel = viewModel)
        }

        is Screen.SimCover -> {
            SimCoverScreen(viewModel = viewModel)
        }

        is Screen.CalculatorCover -> {
            CalculatorCoverScreen(viewModel = viewModel)
        }

        is Screen.NotesCover -> {
            NotesCoverScreen(viewModel = viewModel)
        }

        is Screen.VaultMain -> {
            VaultMainScreen(viewModel = viewModel)
        }

        is Screen.PhotoViewer -> {
            val photos = allMedia.filter { it.mediaType == com.example.data.model.MediaType.PHOTO.name }
            val listToUse = if (photos.any { it.id == screen.mediaId }) photos else allMedia
            PhotoViewerScreen(
                mediaList = listToUse,
                initialMediaId = screen.mediaId,
                onBack = { viewModel.navigateTo(Screen.VaultMain) },
                onDelete = { viewModel.deleteItem(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onRestore = { mediaItem, treeUri ->
                    viewModel.restoreMedia(mediaItem, treeUri, deleteFromVault = false)
                }
            )
        }

        is Screen.VideoPlayer -> {
            val videos = allMedia.filter { it.mediaType == com.example.data.model.MediaType.VIDEO.name }
            val listToUse = if (videos.any { it.id == screen.mediaId }) videos else allMedia
            val item = allMedia.find { it.id == screen.mediaId }
            if (item != null) {
                VideoPlayerScreen(
                    item = item,
                    mediaList = listToUse,
                    onBack = { viewModel.navigateTo(Screen.VaultMain) },
                    onSavePlaybackPosition = { id, pos ->
                        viewModel.updatePlaybackPosition(id, pos)
                    }
                )
            } else {
                viewModel.navigateTo(Screen.VaultMain)
            }
        }

        is Screen.AudioPlayer -> {
            val audios = allMedia.filter { it.mediaType == com.example.data.model.MediaType.AUDIO.name }
            val listToUse = if (audios.any { it.id == screen.mediaId }) audios else allMedia
            AudioPlayerScreen(
                mediaList = listToUse,
                initialMediaId = screen.mediaId,
                onBack = { viewModel.navigateTo(Screen.VaultMain) }
            )
        }

        is Screen.NoteViewer -> {
            val item = allMedia.find { it.id == screen.mediaId }
            if (item != null) {
                NoteViewerScreen(
                    item = item,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateTo(Screen.VaultMain) }
                )
            } else {
                viewModel.navigateTo(Screen.VaultMain)
            }
        }

        is Screen.GalleryChecklist -> {
            GalleryChecklistScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.VaultMain) }
            )
        }

        is Screen.IntruderLogs -> {
            IntruderLogsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo(Screen.VaultMain) }
            )
        }
    }
}
