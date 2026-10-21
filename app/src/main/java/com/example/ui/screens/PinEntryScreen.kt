package com.example.ui.screens

import android.app.Activity
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.data.model.IntruderEntryPoint
import com.example.ui.theme.Dimens
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.security.BiometricHelper
import com.example.security.PinSecurityManager
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultAccentBrush
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.Screen
import com.example.viewmodel.VaultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen PIN Entry screen that intercepts app launch or cover navigation
 * and strictly gates access to core vault functionality upon successful validation.
 */
@Composable
fun PinEntryScreen(
    viewModel: VaultViewModel,
    onBackToCover: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val securityManager = viewModel.securityManager
    val coroutineScope = rememberCoroutineScope()

    val isPinAlreadySet = securityManager.isPinSetup
    var isSetupMode by remember { mutableStateOf(!isPinAlreadySet) }
    var setupStep by remember { mutableStateOf(1) } // 1 = Enter new PIN, 2 = Confirm new PIN
    var firstEnteredPin by remember { mutableStateOf("") }

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutMs by remember { mutableLongStateOf(securityManager.getLockoutRemainingMs()) }

    val targetLength = if (isSetupMode) 4 else securityManager.pinLength

    fun performHaptic() {
        if (securityManager.isHapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    // Intercept back navigation: Return to cover screen or finish activity
    BackHandler {
        if (onBackToCover != null) {
            onBackToCover()
        } else {
            (context as? Activity)?.finish()
        }
    }

    // Lockout countdown timer
    LaunchedEffect(lockoutMs) {
        if (lockoutMs > 0) {
            while (lockoutMs > 0) {
                delay(1000L)
                lockoutMs = securityManager.getLockoutRemainingMs()
            }
            errorMessage = null
        }
    }

    // Trigger biometric automatically if enabled and available on entry (and not locked out)
    LaunchedEffect(Unit) {
        if (!isSetupMode && securityManager.isBiometricEnabled && lockoutMs <= 0) {
            val activity = context as? FragmentActivity
            if (activity != null && BiometricHelper.isBiometricAvailable(context)) {
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    title = context.getString(R.string.auth_confirm_title),
                    subtitle = context.getString(R.string.auth_confirm_subtitle),
                    onSuccess = {
                        if (!securityManager.isFirstTimeSetupDone) {
                            viewModel.navigateTo(Screen.FirstTimePermissions)
                        } else {
                            viewModel.unlockVault()
                        }
                    },
                    onError = {
                        if (securityManager.isCaptureOnBiometricFailureEnabled) {
                            viewModel.intruderCaptureManager.onAuthFailure(
                                lifecycleOwner = lifecycleOwner,
                                attemptNumber = 1,
                                entryPoint = IntruderEntryPoint.BIOMETRIC,
                                isDeviceLocked = false
                            )
                        }
                    },
                    onFallbackToPin = { /* stays on PIN keypad */ }
                )
            }
        }
    }

    // Keypad numbers (optionally shuffled for anti-shoulder-surfing)
    val numberKeys = remember(securityManager.isShuffleKeypadEnabled) {
        val base = (1..9).map { it.toString() }
        if (securityManager.isShuffleKeypadEnabled) base.shuffled() else base
    }

    val keys = listOf(
        listOf(numberKeys[0], numberKeys[1], numberKeys[2]),
        listOf(numberKeys[3], numberKeys[4], numberKeys[5]),
        listOf(numberKeys[6], numberKeys[7], numberKeys[8]),
        listOf("bio", "0", "del")
    )

    fun handleDigitPress(digit: String) {
        if (lockoutMs > 0) return
        performHaptic()
        errorMessage = null

        if (enteredPin.length < targetLength) {
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == targetLength) {
                if (isSetupMode) {
                    if (setupStep == 1) {
                        firstEnteredPin = newPin
                        enteredPin = ""
                        setupStep = 2
                    } else {
                        // Confirm step
                        if (newPin == firstEnteredPin) {
                            val success = securityManager.setPin(newPin)
                            if (success) {
                                Toast.makeText(context, "PIN Created Successfully", Toast.LENGTH_SHORT).show()
                                isSetupMode = false
                                // Ask for all permissions on first time
                                if (!securityManager.isFirstTimeSetupDone) {
                                    viewModel.navigateTo(Screen.FirstTimePermissions)
                                } else {
                                    viewModel.unlockVault()
                                }
                            } else {
                                errorMessage = "Failed to store PIN securely"
                                enteredPin = ""
                                setupStep = 1
                            }
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            enteredPin = ""
                            setupStep = 1
                        }
                    }
                } else {
                    // Verification
                    when (val result = securityManager.verifyPin(newPin)) {
                        is PinSecurityManager.VerificationResult.Success -> {
                            if (!securityManager.isFirstTimeSetupDone) {
                                viewModel.navigateTo(Screen.FirstTimePermissions)
                            } else {
                                viewModel.unlockVault()
                            }
                        }
                        is PinSecurityManager.VerificationResult.DecoySuccess -> {
                            // Opens empty decoy vault
                            if (!securityManager.isFirstTimeSetupDone) {
                                viewModel.navigateTo(Screen.FirstTimePermissions)
                            } else {
                                viewModel.unlockVault()
                            }
                        }
                        is PinSecurityManager.VerificationResult.Failed -> {
                            val attempts = 5 - result.attemptsRemainingBeforeLockout
                            viewModel.intruderCaptureManager.onAuthFailure(
                                lifecycleOwner = lifecycleOwner,
                                attemptNumber = attempts,
                                entryPoint = IntruderEntryPoint.COVER,
                                isDeviceLocked = false
                            )
                            errorMessage = "Incorrect PIN (${result.attemptsRemainingBeforeLockout} attempts remaining)"
                            enteredPin = ""
                        }
                        is PinSecurityManager.VerificationResult.LockedOut -> {
                            viewModel.intruderCaptureManager.onAuthFailure(
                                lifecycleOwner = lifecycleOwner,
                                attemptNumber = 5,
                                entryPoint = IntruderEntryPoint.COVER,
                                isDeviceLocked = false
                            )
                            lockoutMs = result.remainingMs
                            errorMessage = "Too many failed attempts"
                            enteredPin = ""
                        }
                        is PinSecurityManager.VerificationResult.NoPinConfigured -> {
                            isSetupMode = true
                            setupStep = 1
                            enteredPin = ""
                        }
                        is PinSecurityManager.VerificationResult.Error -> {
                            errorMessage = result.message
                            enteredPin = ""
                        }
                    }
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("pin_entry_screen"),
        color = Color(0xFF0C0D10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (onBackToCover != null) {
                            onBackToCover()
                        } else {
                            (context as? Activity)?.finish()
                        }
                    },
                    modifier = Modifier.testTag("pin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSetupMode) "Security Setup" else "Private Safe",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(VaultEmerald.copy(alpha = 0.15f))
                    .border(1.dp, VaultEmerald.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSetupMode) Icons.Default.Shield else Icons.Default.Lock,
                    contentDescription = "Security Status",
                    tint = VaultEmerald,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Instruction
            val headerTitle = when {
                isSetupMode && setupStep == 1 -> "Create Master PIN"
                isSetupMode && setupStep == 2 -> "Confirm Master PIN"
                else -> "Enter Master PIN"
            }
            val subtitle = when {
                isSetupMode && setupStep == 1 -> "Choose a 4-digit PIN for your private safe"
                isSetupMode && setupStep == 2 -> "Re-enter the 4-digit PIN to confirm"
                else -> "Authenticate to access your hidden files"
            }

            Text(
                text = headerTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until targetLength) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) VaultEmerald else SurfaceBorder)
                            .border(
                                width = 1.dp,
                                color = if (isFilled) VaultEmerald else TextMuted.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Error / Lockout Banner
            Box(
                modifier = Modifier.height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (lockoutMs > 0 || errorMessage != null) {
                    val message = if (lockoutMs > 0) {
                        "Too many failed attempts. Try again in ${lockoutMs / 1000}s"
                    } else {
                        errorMessage ?: ""
                    }
                    Text(
                        text = message,
                        color = DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad
            val isLockedOut = lockoutMs > 0

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key) {
                                "bio" -> {
                                    if (!isSetupMode && securityManager.isBiometricEnabled) {
                                        IconButton(
                                            onClick = {
                                                performHaptic()
                                                val activity = context as? FragmentActivity
                                                if (activity != null) {
                                                    BiometricHelper.showBiometricPrompt(
                                                        activity = activity,
                                                        title = context.getString(R.string.auth_confirm_title),
                                                        subtitle = context.getString(R.string.auth_confirm_subtitle),
                                                        onSuccess = {
                                                            if (!securityManager.isFirstTimeSetupDone) {
                                                                viewModel.navigateTo(Screen.FirstTimePermissions)
                                                            } else {
                                                                viewModel.unlockVault()
                                                            }
                                                        },
                                                        onError = { /* stay on keypad */ },
                                                        onFallbackToPin = { /* stay on keypad */ }
                                                    )
                                                }
                                            },
                                            enabled = !isLockedOut,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .testTag("pin_biometric_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometric Unlock",
                                                tint = if (!isLockedOut) VaultEmerald else TextMuted,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(72.dp))
                                    }
                                }
                                "del" -> {
                                    IconButton(
                                        onClick = {
                                            performHaptic()
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                errorMessage = null
                                            }
                                        },
                                        enabled = !isLockedOut && enteredPin.isNotEmpty(),
                                        modifier = Modifier
                                            .size(72.dp)
                                            .testTag("pin_backspace_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = if (enteredPin.isNotEmpty()) TextWhite else TextMuted,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(DarkCarbon)
                                            .clickable(enabled = !isLockedOut) {
                                                handleDigitPress(key)
                                            }
                                            .testTag("pin_screen_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isLockedOut) TextMuted else TextWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
