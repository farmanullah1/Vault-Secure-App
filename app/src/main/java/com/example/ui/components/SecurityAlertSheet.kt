package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.IntruderEventEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Dimens
import com.example.ui.theme.PostAuthSheetBackground
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultDarkBackground
import com.example.ui.theme.VaultOutline
import com.example.ui.theme.VaultViolet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Security Alert Sheet shown to the real owner after successful unlock if wrong attempts occurred.
 * Conforms strictly to Master Build Spec v3 (Section 32.4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAlertSheet(
    unseenEvents: List<IntruderEventEntity>,
    onDismiss: () -> Unit,
    onViewLog: () -> Unit,
    onDeletePhotos: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (unseenEvents.isEmpty()) return

    val latestEvent = unseenEvents.first()
    val attemptCount = unseenEvents.size
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(latestEvent.timestamp) { dateFormat.format(Date(latestEvent.timestamp)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PostAuthSheetBackground,
        shape = Dimens.shapeSheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = Dimens.dragHandleMargin)
                    .size(width = Dimens.dragHandleWidth, height = Dimens.dragHandleHeight)
                    .background(Color(0xFF3A3C41), CircleShape)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.dialogPadding, vertical = Dimens.space3)
        ) {
            // Header: Warning Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.space3)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF2A1515), CircleShape)
                        .border(Dimens.border1, DangerRed.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Alert",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = if (attemptCount == 1) "1 wrong attempt while you were away" else "$attemptCount wrong attempts while you were away",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Latest: $formattedTime  •  Via ${latestEvent.entryPoint}",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.space4))

            // Photo thumbnails (up to 3 photos)
            val photos = unseenEvents.mapNotNull { it.photoUri }.take(3)
            if (photos.isNotEmpty()) {
                Text(
                    text = "Captured Photos (${photos.size}):",
                    color = TextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Dimens.space2))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.itemGapLarge),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos) { photoUri ->
                        Box(
                            modifier = Modifier
                                .size(width = 84.dp, height = 112.dp)
                                .clip(Dimens.shapeThumbnail)
                                .background(VaultDarkBackground)
                                .border(Dimens.border1, VaultOutline, Dimens.shapeThumbnail)
                        ) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Intruder photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.space4))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.space2)
            ) {
                // View Log button
                Button(
                    onClick = {
                        onDismiss()
                        onViewLog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultViolet),
                    shape = Dimens.shapeButton,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonSecondaryHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Log", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                // Delete Photos button
                OutlinedButton(
                    onClick = onDeletePhotos,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DangerRed.copy(alpha = 0.5f))),
                    shape = Dimens.shapeButton,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.buttonSecondaryHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.space2))

            // Dismiss Button
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2024)),
                shape = Dimens.shapeButton,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonSecondaryHeight)
            ) {
                Text("Dismiss", color = TextMuted, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(Dimens.space3))
        }
    }
}
