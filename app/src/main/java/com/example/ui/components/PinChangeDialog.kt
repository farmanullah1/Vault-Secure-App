package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun PinChangeDialog(
    securityManager: PinSecurityManager,
    isDecoyMode: Boolean = false,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    // Steps:
    // If isDecoyMode: Step 1: Enter new decoy PIN, Step 2: Confirm new decoy PIN
    // If master change: Step 0: Enter current PIN, Step 1: Enter new PIN, Step 2: Confirm new PIN
    var step by remember { mutableStateOf(if (isDecoyMode) 1 else 0) }
    var currentPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val activeDisplayPin = when (step) {
        0 -> currentPinInput
        1 -> newPinInput
        else -> confirmPinInput
    }

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
                        imageVector = Icons.Default.LockReset,
                        contentDescription = "Change PIN",
                        tint = VaultEmerald,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        step == 0 -> "Current PIN"
                        isDecoyMode && step == 1 -> "Create Decoy PIN"
                        isDecoyMode && step == 2 -> "Confirm Decoy PIN"
                        step == 1 -> "New Master PIN"
                        else -> "Confirm New PIN"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = when {
                        step == 0 -> "Enter your current master code to proceed"
                        isDecoyMode -> "A secondary PIN that opens an empty decoy safe"
                        step == 1 -> "Enter 4-6 digit new security code"
                        else -> "Re-enter the new code to confirm"
                    },
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
                    val maxDots = 6
                    for (i in 0 until maxDots) {
                        val isFilled = i < activeDisplayPin.length
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) VaultEmerald else SurfaceBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric Keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "del")
                )

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
                                    "" -> Spacer(modifier = Modifier.size(64.dp))
                                    "del" -> {
                                        IconButton(
                                            onClick = {
                                                errorMessage = null
                                                when (step) {
                                                    0 -> if (currentPinInput.isNotEmpty()) currentPinInput = currentPinInput.dropLast(1)
                                                    1 -> if (newPinInput.isNotEmpty()) newPinInput = newPinInput.dropLast(1)
                                                    2 -> if (confirmPinInput.isNotEmpty()) confirmPinInput = confirmPinInput.dropLast(1)
                                                }
                                            },
                                            modifier = Modifier.size(64.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backspace,
                                                contentDescription = "Delete",
                                                tint = TextWhite,
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
                                                .clickable {
                                                    errorMessage = null
                                                    when (step) {
                                                        0 -> {
                                                            if (currentPinInput.length < 6) {
                                                                currentPinInput += key
                                                                if (currentPinInput.length >= 4) {
                                                                    val res = securityManager.verifyPin(currentPinInput)
                                                                    if (res is PinSecurityManager.VerificationResult.Success) {
                                                                        step = 1
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        1 -> {
                                                            if (newPinInput.length < 6) {
                                                                newPinInput += key
                                                            }
                                                        }
                                                        2 -> {
                                                            if (confirmPinInput.length < 6) {
                                                                confirmPinInput += key
                                                                if (confirmPinInput.length == newPinInput.length) {
                                                                    if (confirmPinInput == newPinInput) {
                                                                        if (isDecoyMode) {
                                                                            securityManager.setDecoyPin(newPinInput)
                                                                        } else {
                                                                            securityManager.setPin(newPinInput)
                                                                        }
                                                                        onSuccess()
                                                                    } else {
                                                                        errorMessage = "PINs do not match. Try again."
                                                                        confirmPinInput = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextWhite
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }

                    if (step == 0 && currentPinInput.length >= 4) {
                        TextButton(
                            onClick = {
                                val res = securityManager.verifyPin(currentPinInput)
                                if (res is PinSecurityManager.VerificationResult.Success) {
                                    step = 1
                                    errorMessage = null
                                } else {
                                    errorMessage = "Current PIN is incorrect"
                                    currentPinInput = ""
                                }
                            }
                        ) {
                            Text("Next", color = VaultEmerald, fontWeight = FontWeight.Bold)
                        }
                    } else if (step == 1 && newPinInput.length >= 4) {
                        TextButton(
                            onClick = {
                                step = 2
                                errorMessage = null
                            }
                        ) {
                            Text("Next", color = VaultEmerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
