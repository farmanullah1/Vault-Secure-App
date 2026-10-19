package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryChecklistScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val hasNoMedia by viewModel.hasNoMedia.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery Visibility Checklist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("checklist_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceElevated)
            )
        },
        containerColor = DarkCarbon
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transparency Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = VaultEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "How Media Hiding Works",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "A .nomedia file tells Android's MediaStore indexer (used by Google Photos, WhatsApp, and Instagram pickers) to ignore the folder. It hides files from gallery feeds, but does not encrypt files on disk.",
                                fontSize = 13.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Step 1: Live .nomedia status
            item {
                ChecklistCard(
                    stepNumber = "1",
                    title = "Verify .nomedia Presence",
                    description = if (hasNoMedia) {
                        "Verified: The .nomedia file is present and active inside your designated vault directory."
                    } else {
                        "Warning: No .nomedia file was detected. Standard gallery apps may still scan this folder."
                    },
                    icon = if (hasNoMedia) Icons.Default.Check else Icons.Default.Warning,
                    statusText = if (hasNoMedia) ".nomedia Active" else "Missing .nomedia",
                    isSuccess = hasNoMedia,
                    actionButton = if (!hasNoMedia) {
                        {
                            Button(
                                onClick = { viewModel.ensureNoMedia() },
                                colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Create .nomedia Now", color = DarkCarbon, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else null
                )
            }

            // Step 2: Restart phone
            item {
                ChecklistCard(
                    stepNumber = "2",
                    title = "Device Restart (Rescan MediaStore)",
                    description = "Files already indexed before moving into the vault may temporarily linger in gallery caches. Restarting your phone automatically prompts the system media provider to refresh and drop the hidden folder.",
                    icon = Icons.Default.RestartAlt,
                    statusText = "Recommended",
                    isSuccess = true
                )
            }

            // Step 3: Cache clearing
            item {
                ChecklistCard(
                    stepNumber = "3",
                    title = "Clear Media Storage Cache",
                    description = "If thumbnails still appear in Google Photos or WhatsApp pickers after restart: Navigate to Device Settings → Apps → Show System Apps → Media Storage → Storage → Clear Cache.",
                    icon = Icons.Default.Cached,
                    statusText = "Optional Troubleshooting",
                    isSuccess = true
                )
            }

            // Step 4: Cloud backup
            item {
                ChecklistCard(
                    stepNumber = "4",
                    title = "Confirm Cloud Backup Is Off",
                    description = "Check Google Photos or OneDrive settings. Ensure cloud backup is disabled for this specific device folder; any media uploaded before being secured in the vault will remain in your cloud account until manually deleted.",
                    icon = Icons.Default.CloudDone,
                    statusText = "Privacy Verification",
                    isSuccess = true
                )
            }

            // Step 5: Verification in third-party pickers
            item {
                ChecklistCard(
                    stepNumber = "5",
                    title = "Verify in Social App Pickers",
                    description = "Open the media picker in WhatsApp or Instagram to confirm the vault folder is omitted from recent photos and device album listings.",
                    icon = Icons.Default.FolderSpecial,
                    statusText = "Final Check",
                    isSuccess = true
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChecklistCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    statusText: String,
    isSuccess: Boolean,
    actionButton: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSuccess) VaultEmerald.copy(alpha = 0.2f) else DangerRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) VaultEmerald else DangerRed,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCarbon)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSuccess) VaultEmerald else DangerRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp
            )

            if (actionButton != null) {
                actionButton()
            }
        }
    }
}
