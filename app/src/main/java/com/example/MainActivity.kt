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

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}

@Composable
fun AppNavigation(viewModel: VaultViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()

    when (val screen = currentScreen) {
        is Screen.SimCover -> {
            SimCoverScreen(viewModel = viewModel)
        }

        is Screen.VaultMain -> {
            VaultMainScreen(viewModel = viewModel)
        }

        is Screen.PhotoViewer -> {
            val item = allMedia.find { it.id == screen.mediaId }
            if (item != null) {
                PhotoViewerScreen(
                    item = item,
                    onBack = { viewModel.navigateTo(Screen.VaultMain) },
                    onDelete = { viewModel.deleteItem(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onRestore = { mediaItem, treeUri ->
                        viewModel.restoreMedia(mediaItem, treeUri, deleteFromVault = false)
                    }
                )
            } else {
                viewModel.navigateTo(Screen.VaultMain)
            }
        }

        is Screen.VideoPlayer -> {
            val item = allMedia.find { it.id == screen.mediaId }
            if (item != null) {
                VideoPlayerScreen(
                    item = item,
                    onBack = { viewModel.navigateTo(Screen.VaultMain) }
                )
            } else {
                viewModel.navigateTo(Screen.VaultMain)
            }
        }

        is Screen.AudioPlayer -> {
            val item = allMedia.find { it.id == screen.mediaId }
            if (item != null) {
                AudioPlayerScreen(
                    item = item,
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
    }
}
