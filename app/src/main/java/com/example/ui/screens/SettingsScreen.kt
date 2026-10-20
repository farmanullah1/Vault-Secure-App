package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.PinSecurityManager
import com.example.ui.components.PinChangeDialog
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.Screen
import com.example.viewmodel.VaultViewModel

@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val securityManager = viewModel.securityManager
    val hasNoMedia by viewModel.hasNoMedia.collectAsState()

    var showSafeResetDialog by remember { mutableStateOf(false) }
    var showHelplineDialog by remember { mutableStateOf(false) }
    var showAboutPrivacyDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var showDecoyPinDialog by remember { mutableStateOf(false) }

    var biometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled) }
    var flagSecureEnabled by remember { mutableStateOf(securityManager.isFlagSecureEnabled) }
    var goStraightToVault by remember { mutableStateOf(securityManager.goStraightToVault) }
    var hapticEnabled by remember { mutableStateOf(securityManager.isHapticEnabled) }
    var shuffleKeypadEnabled by remember { mutableStateOf(securityManager.isShuffleKeypadEnabled) }
    var currentAutoLock by remember { mutableIntStateOf(securityManager.autoLockSeconds) }

    val autoLockOptions = listOf(
        Pair(PinSecurityManager.AUTO_LOCK_IMMEDIATE, "Immediate (on background or screen-off)"),
        Pair(PinSecurityManager.AUTO_LOCK_1_MIN, "1 minute in background"),
        Pair(PinSecurityManager.AUTO_LOCK_2_MIN, "2 minutes in background"),
        Pair(PinSecurityManager.AUTO_LOCK_5_MIN, "5 minutes in background"),
        Pair(PinSecurityManager.AUTO_LOCK_30_MIN, "30 minutes in background"),
        Pair(PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE, "Until app is closed (re-requires PIN on reopen)")
    )

    fun getAutoLockLabel(seconds: Int): String {
        return when (seconds) {
            PinSecurityManager.AUTO_LOCK_IMMEDIATE -> "Immediate"
            PinSecurityManager.AUTO_LOCK_1_MIN -> "1 minute"
            PinSecurityManager.AUTO_LOCK_2_MIN -> "2 minutes"
            PinSecurityManager.AUTO_LOCK_5_MIN -> "5 minutes"
            PinSecurityManager.AUTO_LOCK_30_MIN -> "30 minutes"
            PinSecurityManager.AUTO_LOCK_ON_APP_CLOSE -> "Until app is closed"
            else -> "$seconds seconds"
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setVaultFolder(uri)
            Toast.makeText(context, "Vault folder configured", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vault Storage Configuration
        item {
            Text(
                text = "STORAGE & PROTECTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDim,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(VaultEmerald.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = VaultEmerald)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Vault Folder", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                            val folderUri = viewModel.repository.getVaultFolderUri()
                            val pathText = folderUri?.lastPathSegment ?: "Not configured (Default Internal)"
                            Text(text = pathText, fontSize = 12.sp, color = TextMuted, maxLines = 1)
                        }
                        Button(
                            onClick = { folderPicker.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change", color = TextWhite, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasNoMedia) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (hasNoMedia) VaultEmerald else DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasNoMedia) ".nomedia Active (Hidden from Gallery)" else ".nomedia Missing",
                                fontSize = 13.sp,
                                color = if (hasNoMedia) VaultEmerald else DangerRed,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (!hasNoMedia) {
                            TextButton(onClick = { viewModel.ensureNoMedia() }) {
                                Text("Fix Now", color = VaultEmerald)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.navigateTo(Screen.GalleryChecklist) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_checklist_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCarbon)
                    ) {
                        Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, tint = VaultEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery Visibility Checklist", color = TextWhite, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            viewModel.syncFolder()
                            Toast.makeText(context, "Vault synced with storage", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_folder_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCarbon)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = TextMuted)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Rescan & Sync Media Files", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        }

        // Security Preferences & Auto-Lock Options
        item {
            Text(
                text = "SECURITY & AUTO-LOCK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDim,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Auto-Lock Timing Option (Immediate, 1m, 2m, 5m, 30m, On App Close)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAutoLockDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Lock Timeout", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Tap to change lock timeout behavior", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Text(
                            text = getAutoLockLabel(currentAutoLock),
                            color = VaultEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("auto_lock_label")
                        )
                    }

                    // Change Master PIN
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinChangeDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Change Master PIN", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Update your 4-6 digit master vault code", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Button(
                            onClick = { showPinChangeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update", color = TextWhite, fontSize = 12.sp)
                        }
                    }

                    // Decoy PIN Setup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDecoyPinDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Decoy PIN Safe", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text(
                                    if (securityManager.isDecoyPinSetup) "Decoy PIN Active • Opens empty safe" else "Configure secondary decoy PIN",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Button(
                            onClick = { showDecoyPinDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (securityManager.isDecoyPinSetup) "Manage" else "Set Up", color = TextWhite, fontSize = 12.sp)
                        }
                    }

                    // Biometric Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Authentication", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Fingerprint / Face unlock prompt", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                securityManager.isBiometricEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            )
                        )
                    }

                    // FLAG_SECURE Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Screen Privacy (FLAG_SECURE)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Blocks screenshots & hides recent app preview", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = flagSecureEnabled,
                            onCheckedChange = {
                                flagSecureEnabled = it
                                securityManager.isFlagSecureEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            )
                        )
                    }

                    // Direct Vault Entry Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Go Straight to Vault", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Skip carrier action panel after authentication", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = goStraightToVault,
                            onCheckedChange = {
                                goStraightToVault = it
                                securityManager.goStraightToVault = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            )
                        )
                    }

                    // Haptic Feedback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Vibration, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Haptic Feedback", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Tactile response on PIN keypad taps", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = hapticEnabled,
                            onCheckedChange = {
                                hapticEnabled = it
                                securityManager.isHapticEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            )
                        )
                    }

                    // Keypad Shuffle Switch (Anti-shoulder surfing)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Shuffle Keypad Numbers", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Randomizes digit positions to prevent smudge guessing", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = shuffleKeypadEnabled,
                            onCheckedChange = {
                                shuffleKeypadEnabled = it
                                securityManager.isShuffleKeypadEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            )
                        )
                    }
                }
            }
        }

        // Carrier Helplines
        item {
            Text(
                text = "CARRIER DISGUISE HELPLINES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDim,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Helpline Numbers", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                            Text("Jazz: ${securityManager.jazzHelpline} • Zong: ${securityManager.zongHelpline}", fontSize = 12.sp, color = TextMuted)
                        }
                        Button(
                            onClick = { showHelplineDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Edit", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // About & Honest Privacy Disclosure
        item {
            Text(
                text = "HONEST PRIVACY DISCLOSURE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDim,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutPrivacyDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = VaultEmerald)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About Hidden Mode vs. Encryption", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                        Text("Important facts regarding .nomedia and storage visibility", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        }

        // Danger Zone: Safe Reset
        item {
            Text(
                text = "RESET & DATA SAFETY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DangerRed,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DangerRedContainer.copy(alpha = 0.3f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRed.copy(alpha = 0.4f)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Safe Reset App", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                    Text(
                        text = "Clears master PIN, security credentials, and app database. All media files in your storage folder remain completely untouched on disk.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Button(
                        onClick = { showSafeResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("safe_reset_button")
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Master PIN & App Data", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Auto-Lock Option Picker Dialog
    if (showAutoLockDialog) {
        AlertDialog(
            onDismissRequest = { showAutoLockDialog = false },
            title = { Text("Auto-Lock Timeout", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    autoLockOptions.forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentAutoLock = seconds
                                    securityManager.autoLockSeconds = seconds
                                    showAutoLockDialog = false
                                    Toast.makeText(context, "Auto-lock updated: ${getAutoLockLabel(seconds)}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAutoLock == seconds,
                                onClick = {
                                    currentAutoLock = seconds
                                    securityManager.autoLockSeconds = seconds
                                    showAutoLockDialog = false
                                    Toast.makeText(context, "Auto-lock updated: ${getAutoLockLabel(seconds)}", Toast.LENGTH_SHORT).show()
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = VaultEmerald)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, color = TextWhite, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAutoLockDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Change Master PIN Dialog
    if (showPinChangeDialog) {
        PinChangeDialog(
            securityManager = securityManager,
            isDecoyMode = false,
            onSuccess = {
                showPinChangeDialog = false
                Toast.makeText(context, "Master PIN updated successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPinChangeDialog = false }
        )
    }

    // Decoy PIN Dialog
    if (showDecoyPinDialog) {
        PinChangeDialog(
            securityManager = securityManager,
            isDecoyMode = true,
            onSuccess = {
                showDecoyPinDialog = false
                Toast.makeText(context, "Decoy PIN configured successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDecoyPinDialog = false }
        )
    }

    // Honest Privacy Disclosure Dialog
    if (showAboutPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showAboutPrivacyDialog = false },
            title = { Text("About Privacy & Storage Model", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "• Hidden Mode vs. Encryption:\nThis app hides media from Android's MediaStore gallery indexing using standard .nomedia markers. It does NOT encrypt the files on disk. Anyone with a file manager or PC USB cable can still read the files in the directory.",
                        color = TextWhite,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• Cloud Backups:\nPhotos previously synced to Google Photos stay in your cloud library until deleted there.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Carrier Disguise:\nThis utility is not affiliated with or endorsed by Jazz, Zong, or any telecommunications carrier.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• Zero Network Permissions:\nThis app does not request or possess the INTERNET permission. No data ever leaves your device.",
                        color = VaultEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutPrivacyDialog = false }) {
                    Text("Understood", color = VaultEmerald)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Safe Reset Confirmation Dialog
    if (showSafeResetDialog) {
        AlertDialog(
            onDismissRequest = { showSafeResetDialog = false },
            title = { Text("Safe Reset Application?", color = TextWhite) },
            text = {
                Text(
                    "This will clear the app's master PIN and local media database index.\n\nIMPORTANT: Your actual photos, videos, and music files on the SD card/storage will NOT be deleted or altered.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSafeResetDialog = false
                        viewModel.safeResetApp()
                        Toast.makeText(context, "App reset completed. Files intact on storage.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Confirm Reset", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafeResetDialog = false }) {
                    Text("Cancel", color = TextWhite)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Edit Helpline Numbers Dialog
    if (showHelplineDialog) {
        var jazzNum by remember { mutableStateOf(securityManager.jazzHelpline) }
        var zongNum by remember { mutableStateOf(securityManager.zongHelpline) }

        AlertDialog(
            onDismissRequest = { showHelplineDialog = false },
            title = { Text("Edit Carrier Helplines", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = jazzNum,
                        onValueChange = { jazzNum = it },
                        label = { Text("Jazz Helpline") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = VaultEmerald
                        )
                    )
                    OutlinedTextField(
                        value = zongNum,
                        onValueChange = { zongNum = it },
                        label = { Text("Zong Helpline") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = VaultEmerald
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        securityManager.jazzHelpline = jazzNum
                        securityManager.zongHelpline = zongNum
                        showHelplineDialog = false
                    }
                ) {
                    Text("Save", color = VaultEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelplineDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}
