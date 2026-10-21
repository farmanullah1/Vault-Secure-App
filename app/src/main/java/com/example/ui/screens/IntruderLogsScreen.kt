package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.db.VaultDatabase
import com.example.data.model.IntruderEventEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.Dimens
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultDarkBackground
import com.example.ui.theme.VaultEmerald
import com.example.ui.theme.VaultOutline
import com.example.viewmodel.VaultViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Intruder Logs Screen displaying recorded unauthorized access attempts with front-camera photos.
 * Conforms strictly to Master Build Spec v3 (Section 32.5).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntruderLogsScreen(
    viewModel: VaultViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allIntruderEvents.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy  hh:mm:ss a", Locale.getDefault()) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { VaultDatabase.getInstance(context) }

    var selectedPhotoForEnlarge by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkCarbon,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Intruder Attempt Log",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("intruder_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    if (events.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.deleteIntruderPhotos()
                            },
                            modifier = Modifier.testTag("clear_intruder_logs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Logs",
                                tint = DangerRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCarbon)
            )
        }
    ) { padding ->
        if (events.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Dimens.screenEdgeExpanded),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(VaultEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = VaultEmerald,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.space4))
                Text(
                    text = "No Intrusion Attempts",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(Dimens.space2))
                Text(
                    text = "No unauthorized PIN entries or break-in attempts have been detected on this device.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimens.screenEdgeCompact),
                verticalArrangement = Arrangement.spacedBy(Dimens.itemGapLarge)
            ) {
                item {
                    Text(
                        text = "${events.size} unauthorized attempt(s) recorded:",
                        color = DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = Dimens.space1)
                    )
                }

                items(events, key = { it.id }) { event ->
                    IntruderEventCard(
                        event = event,
                        dateFormat = dateFormat,
                        onPhotoClick = { uri -> selectedPhotoForEnlarge = uri },
                        onDeleteClick = {
                            coroutineScope.launch {
                                event.photoUri?.let { uriStr ->
                                    try {
                                        val f = File(Uri.parse(uriStr).path ?: "")
                                        if (f.exists()) f.delete()
                                    } catch (e: Exception) { /* ignore */ }
                                }
                                db.intruderEventDao().deleteEvent(event.id)
                            }
                        }
                    )
                }
            }
        }
    }

    // Fullscreen photo enlargement dialog
    selectedPhotoForEnlarge?.let { photoUri ->
        Dialog(onDismissRequest = { selectedPhotoForEnlarge = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Dimens.shapeDialog)
                    .background(Color.Black)
                    .border(Dimens.border1, VaultOutline, Dimens.shapeDialog)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Intruder full photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                )

                IconButton(
                    onClick = { selectedPhotoForEnlarge = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.space2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun IntruderEventCard(
    event: IntruderEventEntity,
    dateFormat: SimpleDateFormat,
    onPhotoClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Dimens.shapeCard,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(Dimens.border1, VaultOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail or warning badge
            if (event.photoUri != null) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 76.dp)
                        .clip(Dimens.shapeThumbnail)
                        .background(VaultDarkBackground)
                        .border(Dimens.border1, VaultOutline, Dimens.shapeThumbnail)
                        .clickable { onPhotoClick(event.photoUri) }
                ) {
                    AsyncImage(
                        model = event.photoUri,
                        contentDescription = "Intruder photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.space3))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Failed Attempt #${event.attemptNumber}",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(Dimens.space2))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF262E3D), Dimens.shapeChip)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.entryPoint,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.space1))
                Text(
                    text = dateFormat.format(Date(event.timestamp)),
                    color = TextMuted,
                    fontSize = 12.sp
                )
                if (event.photoUri != null) {
                    Text(
                        text = "Front camera photo captured (Tap to enlarge)",
                        color = VaultEmerald,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "No photo (Camera permission or threshold)",
                        color = TextDim,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = TextDim,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
