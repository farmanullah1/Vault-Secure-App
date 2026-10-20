package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.security.BiometricHelper
import com.example.security.PinSecurityManager
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.PinSetupDialog
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.VaultViewModel

@Composable
fun CalculatorCoverScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val securityManager = viewModel.securityManager

    var expression by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("0") }
    var showPinDialog by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }

    fun vibrate() {
        if (securityManager.isHapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun triggerVaultUnlock() {
        if (!securityManager.isPinSetup) {
            showSetupDialog = true
            return
        }

        val activity = context as? FragmentActivity
        if (activity != null && securityManager.isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Unlock Private Vault",
                subtitle = "Verify identity to open vault",
                onSuccess = { viewModel.unlockVault() },
                onError = { showPinDialog = true },
                onFallbackToPin = { showPinDialog = true }
            )
        } else {
            showPinDialog = true
        }
    }

    fun evaluateMath(expr: String): String {
        return try {
            val sanitized = expr.replace("×", "*").replace("÷", "/")
            val parts = sanitized.split(Regex("(?<=[-+*/%])|(?=[-+*/%])")).map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) return "0"
            if (parts.size == 1) return parts[0]

            var current = parts[0].toDoubleOrNull() ?: return "Error"
            var i = 1
            while (i < parts.size) {
                val op = parts[i]
                val next = if (i + 1 < parts.size) parts[i + 1].toDoubleOrNull() else null
                if (next != null) {
                    when (op) {
                        "+" -> current += next
                        "-" -> current -= next
                        "*" -> current *= next
                        "/" -> if (next != 0.0) current /= next else return "Error"
                        "%" -> current %= next
                    }
                    i += 2
                } else {
                    break
                }
            }
            if (current % 1.0 == 0.0) current.toLong().toString() else "%.4f".format(current).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "Error"
        }
    }

    fun onKeyClick(key: String) {
        vibrate()
        when (key) {
            "C" -> {
                expression = ""
                resultText = "0"
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                    resultText = if (expression.isEmpty()) "0" else evaluateMath(expression)
                }
            }
            "=" -> {
                // Check if the current expression matches PIN
                if (expression.length in 4..8 && expression.all { it.isDigit() }) {
                    val verifyResult = securityManager.verifyPin(expression)
                    when (verifyResult) {
                        is PinSecurityManager.VerificationResult.Success -> {
                            expression = ""
                            viewModel.unlockVault()
                            return
                        }
                        is PinSecurityManager.VerificationResult.DecoySuccess -> {
                            expression = ""
                            viewModel.unlockVault()
                            return
                        }
                        else -> {
                            // Proceed as math
                        }
                    }
                }
                resultText = evaluateMath(expression)
            }
            else -> {
                expression += key
                if (key.all { it.isDigit() } || key == ".") {
                    resultText = evaluateMath(expression)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCarbon)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .testTag("calculator_screen")
    ) {
        // Subtle top disguise bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculator",
                color = TextDim,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            // Long press secret trigger on history icon
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        // Tapping history just evaluates or clears
                    }
                    .padding(8.dp)
            ) {
                Text(
                    text = "DEG",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Calculator display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = expression.ifEmpty { "0" },
                color = if (expression.isEmpty()) TextDim else TextMuted,
                fontSize = if (expression.length > 12) 24.sp else 32.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Double tap secret trigger on expression
                    }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = resultText,
                color = TextWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad Grid
        val buttonRows = listOf(
            listOf("C", "%", "⌫", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "( )", "=")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            buttonRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { label ->
                        val isOperator = label in listOf("÷", "×", "-", "+", "=")
                        val isAction = label in listOf("C", "%", "⌫", "( )")

                        val bgColor = when {
                            label == "=" -> VaultEmerald
                            isOperator -> Color(0xFF2E1065)
                            isAction -> SurfaceElevated
                            else -> SurfaceCard
                        }

                        val textColor = when {
                            label == "=" -> DarkCarbon
                            isOperator -> VaultEmerald
                            isAction -> TextMuted
                            else -> TextWhite
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable {
                                    if (label == "( )") {
                                        triggerVaultUnlock()
                                    } else {
                                        onKeyClick(label)
                                    }
                                }
                                .testTag("calc_btn_$label"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = textColor,
                                fontSize = if (label.length > 1) 20.sp else 24.sp,
                                fontWeight = if (isOperator || label == "=") FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        PinKeypadDialog(
            securityManager = securityManager,
            onSuccess = {
                showPinDialog = false
                viewModel.unlockVault()
            },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showSetupDialog) {
        PinSetupDialog(
            securityManager = securityManager,
            onPinCreated = {
                showSetupDialog = false
                viewModel.unlockVault()
            },
            onDismiss = { showSetupDialog = false }
        )
    }
}
