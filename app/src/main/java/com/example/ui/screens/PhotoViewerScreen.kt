package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.VaultMediaItem
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    mediaList: List<VaultMediaItem>,
    initialMediaId: Long,
    onBack: () -> Unit,
    onDelete: (VaultMediaItem) -> Unit,
    onToggleFavorite: (VaultMediaItem) -> Unit,
    onRestore: ((VaultMediaItem, Uri) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    BackHandler { onBack() }

    val safeList = remember(mediaList) {
        if (mediaList.isEmpty()) emptyList() else mediaList
    }

    val initialIndex = remember(safeList, initialMediaId) {
        val idx = safeList.indexOfFirst { it.id == initialMediaId }
        if (idx >= 0) idx else 0
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { safeList.size }
    )

    val currentItem = safeList.getOrNull(pagerState.currentPage)

    var showInfoSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var isSlideshowActive by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    // Reset rotation when swiping to a different photo
    LaunchedEffect(pagerState.currentPage) {
        rotationAngle = 0f
    }

    // Slideshow auto-advance
    LaunchedEffect(isSlideshowActive, pagerState.currentPage, safeList.size) {
        if (isSlideshowActive && safeList.size > 1) {
            delay(3500L)
            val nextPage = (pagerState.currentPage + 1) % safeList.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    val restoreFolderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        if (treeUri != null && onRestore != null && currentItem != null) {
            onRestore(currentItem, treeUri)
            Toast.makeText(context, "Exported file to selected directory", Toast.LENGTH_SHORT).show()
        }
    }

    if (currentItem == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentItem.fileName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pagerState.currentPage + 1} of ${safeList.size} • Private Safe",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("photo_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    // Slideshow Toggle
                    IconButton(onClick = { isSlideshowActive = !isSlideshowActive }) {
                        Icon(
                            imageVector = if (isSlideshowActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Slideshow",
                            tint = if (isSlideshowActive) VaultEmerald else TextWhite
                        )
                    }

                    // Rotate 90° Clockwise
                    IconButton(onClick = { rotationAngle = (rotationAngle + 90f) % 360f }) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "Rotate",
                            tint = TextWhite
                        )
                    }

                    // Favorite Toggle
                    IconButton(onClick = { onToggleFavorite(currentItem) }) {
                        Icon(
                            imageVector = if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (currentItem.isFavorite) DangerRed else TextWhite
                        )
                    }

                    // Secure Share
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextWhite)
                    }

                    // Export / Restore to Storage
                    IconButton(onClick = { restoreFolderPicker.launch(null) }) {
                        Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "Export to storage", tint = VaultEmerald)
                    }

                    // Metadata Details
                    IconButton(onClick = { showInfoSheet = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Details", tint = TextWhite)
                    }

                    // Delete / Move to Trash
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCarbon.copy(alpha = 0.9f))
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val item = safeList[page]
                ZoomablePhotoImage(
                    item = item,
                    rotationDegrees = if (page == pagerState.currentPage) rotationAngle else 0f
                )
            }

            // Slideshow Indicator Badge
            AnimatedVisibility(
                visible = isSlideshowActive,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkCarbon.copy(alpha = 0.85f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(VaultEmerald)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(VaultEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Slideshow Playing",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Share Dialog with EXIF metadata scrubbing note
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Photo", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Notice: This file will leave the vault and will no longer be protected by your PIN or app lock. The receiving app is beyond Private Vault's control. A temporary read grant will be granted.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareDialog = false
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = currentItem.mimeType.ifEmpty { "image/*" }
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(currentItem.uriString))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Secure Photo"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald)
                ) {
                    Text("Share", color = DarkCarbon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // EXIF Metadata Bottom Sheet
    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            containerColor = SurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = "File Metadata & Origin Mapping", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Spacer(modifier = Modifier.height(16.dp))

                val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(currentItem.dateAdded))
                val sizeKb = currentItem.fileSizeBytes / 1024
                val sizeStr = if (sizeKb > 1024) "${String.format("%.1f", sizeKb / 1024f)} MB" else "$sizeKb KB"

                DetailRow("File Name", currentItem.fileName)
                DetailRow("Date Added", dateStr)
                DetailRow("File Size", sizeStr)
                DetailRow("Format (MIME)", currentItem.mimeType)
                DetailRow("Original Source Mapping", if (currentItem.originalPathOrUri.isNotEmpty()) currentItem.originalPathOrUri else "Direct Import")
                DetailRow("Original Directory", currentItem.originalDirectory.ifEmpty { "Camera / Gallery" })
                DetailRow("Storage Location", ".nomedia protected vault folder")

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        showInfoSheet = false
                        restoreFolderPicker.launch(null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = null, tint = DarkCarbon)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export / Restore to Storage Folder", color = DarkCarbon, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Delete / Move to Trash Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Move to Trash?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = { Text("This file will be moved to the Recycle Bin and can be restored within 30 days.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(currentItem)
                        if (safeList.size <= 1) {
                            onBack()
                        }
                    }
                ) {
                    Text("Move to Trash", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

/**
 * High-performance zoomable photo image supporting double-tap animated snap and smooth pan
 */
@Composable
fun ZoomablePhotoImage(
    item: VaultMediaItem,
    rotationDegrees: Float = 0f
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scaleAnim.value * zoomChange).coerceIn(1f, 5f)
        coroutineScope.launch {
            scaleAnim.snapTo(newScale)
        }
        if (newScale > 1f) {
            val maxOffset = 600f * (newScale - 1f)
            val newX = (offset.x + panChange.x).coerceIn(-maxOffset, maxOffset)
            val newY = (offset.y + panChange.y).coerceIn(-maxOffset, maxOffset)
            offset = Offset(newX, newY)
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            if (scaleAnim.value > 1.2f) {
                                scaleAnim.animateTo(1f, tween(250))
                                offset = Offset.Zero
                            } else {
                                scaleAnim.animateTo(2.8f, tween(250))
                            }
                        }
                    }
                )
            }
            .transformable(state = transformState),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(Uri.parse(item.uriString))
                .crossfade(true)
                .build(),
            contentDescription = item.fileName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scaleAnim.value,
                    scaleY = scaleAnim.value,
                    rotationZ = rotationDegrees,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .testTag("full_photo_image")
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
    }
}
