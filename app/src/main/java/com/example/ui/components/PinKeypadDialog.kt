package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.security.PinSecurityManager
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import kotlinx.coroutines.delay

@Composable
fun PinKeypadDialog(
    securityManager: PinSecurityManager,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onTriggerBiometric: (() -> Unit)? = null
) {
    val view = LocalView.current
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lockoutMs by remember { mutableLongStateOf(securityManager.getLockoutRemainingMs()) }

    fun performHaptic() {
        if (securityManager.isHapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
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

    // Numbers arrangement (shuffled if enabled)
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(DarkCarbon),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security Lock",
                        tint = VaultEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Security Verification",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "Enter 4-6 digit master PIN",
                    fontSize = 13.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // PIN indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxDots = securityManager.pinLength
                    for (i in 0 until maxDots) {
                        val isFilled = i < pin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) VaultEmerald else SurfaceBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error / Lockout banner
                AnimatedVisibility(visible = lockoutMs > 0 || errorMessage != null) {
                    val text = if (lockoutMs > 0) {
                        "Too many failed attempts. Try again in ${lockoutMs / 1000}s"
                    } else {
                        errorMessage ?: ""
                    }
                    Text(
                        text = text,
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Keypad
                val isLockedOut = lockoutMs > 0

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (key in row) {
                                when (key) {
                                    "bio" -> {
                                        if (onTriggerBiometric != null && securityManager.isBiometricEnabled) {
                                            IconButton(
                                                onClick = {
                                                    performHaptic()
                                                    onTriggerBiometric()
                                                },
                                                enabled = !isLockedOut,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .testTag("biometric_auth_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Fingerprint,
                                                    contentDescription = "Biometric Unlock",
                                                    tint = if (!isLockedOut) VaultEmerald else TextMuted,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(64.dp))
                                        }
                                    }
                                    "del" -> {
                                        IconButton(
                                            onClick = {
                                                performHaptic()
                                                if (pin.isNotEmpty()) {
                                                    pin = pin.dropLast(1)
                                                    errorMessage = null
                                                }
                                            },
                                            enabled = !isLockedOut && pin.isNotEmpty(),
                                            modifier = Modifier
                                                .size(64.dp)
                                                .testTag("pin_delete_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Delete digit",
                                                tint = if (pin.isNotEmpty()) TextWhite else TextMuted,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(DarkCarbon)
                                                .clickable(enabled = !isLockedOut && pin.length < securityManager.pinLength) {
                                                    performHaptic()
                                                    val newPin = pin + key
                                                    pin = newPin
                                                    errorMessage = null
                                                    if (newPin.length == securityManager.pinLength) {
                                                        when (val result = securityManager.verifyPin(newPin)) {
                                                            is PinSecurityManager.VerificationResult.Success,
                                                            is PinSecurityManager.VerificationResult.DecoySuccess -> {
                                                                onSuccess()
                                                            }
                                                            is PinSecurityManager.VerificationResult.Failed -> {
                                                                errorMessage = "Incorrect PIN (${result.attemptsRemainingBeforeLockout} attempts remaining)"
                                                                pin = ""
                                                            }
                                                            is PinSecurityManager.VerificationResult.LockedOut -> {
                                                                lockoutMs = result.remainingMs
                                                                pin = ""
                                                            }
                                                            is PinSecurityManager.VerificationResult.NoPinConfigured -> {
                                                                onSuccess()
                                                            }
                                                            is PinSecurityManager.VerificationResult.Error -> {
                                                                errorMessage = result.message
                                                                pin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                                .testTag("pin_key_$key"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = 24.sp,
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pin_cancel_button")
                    ) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            }
        }
    }
}
