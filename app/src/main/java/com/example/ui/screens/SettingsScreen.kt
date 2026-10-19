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
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
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
    var biometricEnabled by remember { mutableStateOf(securityManager.isBiometricEnabled) }
    var autoLockSec by remember { mutableIntStateOf(securityManager.autoLockSeconds) }

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
                        Text("Open Gallery Visibility Checklist", color = TextWhite, fontSize = 13.sp)
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

        // Security Preferences
        item {
            Text(
                text = "SECURITY PREFERENCES",
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Biometric Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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

                    // Auto Lock Timeout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Lock Timing", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                Text("Lock immediately when leaving app", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        Text("Immediate", color = VaultEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cover Screen Customization
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
                        text = "Clears master PIN, app database, and security keys. All media files in your storage folder are preserved and completely untouched.",
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
