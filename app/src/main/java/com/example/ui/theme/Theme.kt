package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val VaultColorScheme = darkColorScheme(
    primary = VaultEmerald,
    onPrimary = DarkCarbon,
    primaryContainer = VaultEmeraldContainer,
    onPrimaryContainer = VaultEmeraldLight,
    secondary = VaultEmeraldLight,
    onSecondary = DarkCarbon,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextWhite,
    tertiary = ZongAmber,
    onTertiary = DarkCarbon,
    tertiaryContainer = ZongAmberContainer,
    onTertiaryContainer = TextWhite,
    background = DarkCarbon,
    onBackground = TextWhite,
    surface = SurfaceElevated,
    onSurface = TextWhite,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextMuted,
    outline = SurfaceBorder,
    error = DangerRed,
    errorContainer = DangerRedContainer,
    onError = TextWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DarkCarbon.toArgb()
                window.navigationBarColor = DarkCarbon.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = VaultColorScheme,
        typography = Typography,
        content = content
    )
}
