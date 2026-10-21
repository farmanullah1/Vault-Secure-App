package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.security.BiometricHelper
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultAccentBrush
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.Screen
import com.example.viewmodel.VaultViewModel

/**
 * First-Time Permissions & Access Setup Screen.
 * Conforms to Master Spec v3 (Section 9.8).
 * Prompts the user once properly with honest rationales for all required and optional permissions.
 */
@Composable
fun FirstTimePermissionsScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val securityManager = viewModel.securityManager

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    var selectedFolderUri by remember {
        mutableStateOf(securityManager.vaultFolderUri)
    }

    var biometricEnabled by remember {
        mutableStateOf(securityManager.isBiometricEnabled)
    }

    val isBiometricAvailable = remember {
        BiometricHelper.isBiometricAvailable(context)
    }

    val isBatteryOptimizedIgnored = remember {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setVaultFolder(uri)
            selectedFolderUri = uri.toString()
            Toast.makeText(context, "Vault folder configured", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("first_time_permissions_screen"),
        color = Color(0xFF090A0C)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(VaultEmerald.copy(alpha = 0.15f))
                            .border(1.dp, VaultEmerald.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = VaultEmerald,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Set Up Private Safe",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Text(
                        text = "Review and grant the permissions Private Safe needs to protect your files on your device.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        lineHeight = 18.sp
                    )
                }
            }

            // Zero-Internet Guarantee Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = VaultEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Private Safe never asks for internet, contacts, or location. Everything stays offline on this device.",
                            fontSize = 12.sp,
                            color = TextWhite,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1. Vault Storage Folder (SAF)
            item {
                PermissionItemCard(
                    icon = Icons.Default.Folder,
                    title = "Vault Storage Folder",
                    badge = if (selectedFolderUri != null) "Configured" else "Required",
                    isGranted = selectedFolderUri != null,
                    description = "Choose or create a folder on your SD card or internal storage. A .nomedia file will be placed inside to hide files from your Android gallery.",
                    actionButton = {
                        Button(
                            onClick = { folderPicker.launch(null) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedFolderUri != null) SurfaceBorder else VaultEmerald
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("setup_folder_button")
                        ) {
                            Text(
                                text = if (selectedFolderUri != null) "Change" else "Choose Folder",
                                color = TextWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }

            // 2. Notifications Permission (Android 13+)
            item {
                PermissionItemCard(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    badge = if (hasNotificationPermission) "Granted" else "Recommended",
                    isGranted = hasNotificationPermission,
                    description = "Required on Android 13+ to show file import progress and background music playback controls in the status bar.",
                    actionButton = {
                        if (!hasNotificationPermission) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        hasNotificationPermission = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("setup_notifications_button")
                            ) {
                                Text("Grant", color = TextWhite, fontSize = 12.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VaultEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Granted", color = VaultEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }

            // 3. Biometric Authentication
            item {
                PermissionItemCard(
                    icon = Icons.Default.Fingerprint,
                    title = "Fingerprint / Face Unlock",
                    badge = if (biometricEnabled && isBiometricAvailable) "Enabled" else "Optional",
                    isGranted = biometricEnabled && isBiometricAvailable,
                    description = "Enables fast biometric unlocking in addition to your master PIN. You can change this anytime in Settings.",
                    actionButton = {
                        Switch(
                            checked = biometricEnabled && isBiometricAvailable,
                            onCheckedChange = {
                                if (isBiometricAvailable) {
                                    biometricEnabled = it
                                    securityManager.isBiometricEnabled = it
                                } else {
                                    Toast.makeText(context, "Biometric hardware not enrolled on device", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkCarbon,
                                checkedTrackColor = VaultEmerald
                            ),
                            modifier = Modifier.testTag("setup_biometric_switch")
                        )
                    }
                )
            }

            // 4. Battery Optimization Exemption
            item {
                PermissionItemCard(
                    icon = if (isBatteryOptimizedIgnored) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
                    title = "Battery Optimization",
                    badge = if (isBatteryOptimizedIgnored) "Unrestricted" else "Optional",
                    isGranted = isBatteryOptimizedIgnored,
                    description = "Prevents Android from killing long file copy jobs, video scans, and audio playback when the screen is turned off.",
                    actionButton = {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallback = Intent(Settings.ACTION_SETTINGS)
                                    context.startActivity(fallback)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("setup_battery_button")
                        ) {
                            Text("Configure", color = TextWhite, fontSize = 12.sp)
                        }
                    }
                )
            }

            // Complete Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        securityManager.isFirstTimeSetupDone = true
                        viewModel.ensureNoMedia()
                        viewModel.navigateTo(Screen.VaultMain)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultAccentBrush)
                        .testTag("continue_to_vault_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Continue to Private Safe",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String,
    isGranted: Boolean,
    description: String,
    actionButton: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                        .background(if (isGranted) VaultEmerald.copy(alpha = 0.2f) else DarkCarbon),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) VaultEmerald else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) VaultEmerald else TextDim
                    )
                }

                actionButton()
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }
    }
}
