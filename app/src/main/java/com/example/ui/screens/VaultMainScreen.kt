package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.ui.components.ImportProgressDialog
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.ui.util.formatFileSize
import com.example.viewmodel.Screen
import com.example.viewmodel.SortOption
import com.example.viewmodel.VaultTab
import com.example.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultMainScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { viewModel.lockVault() }

    val selectedTab by viewModel.selectedTab.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val filteredMedia by viewModel.filteredMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val selectedItemIds by viewModel.selectedItemIds.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    val activeAudioItem by viewModel.activeAudioItem.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    val gridColumns by viewModel.gridColumns.collectAsState()
    val trashCount by viewModel.trashCount.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showImportChoiceDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    var showEmptyTrashConfirm by remember { mutableStateOf(false) }
    var showPurgeConfirm by remember { mutableStateOf(false) }
    var itemToPurge by remember { mutableStateOf<VaultMediaItem?>(null) }
    var showSecureShredOption by remember { mutableStateOf(false) }
    var showPurgeBatchConfirm by remember { mutableStateOf(false) }
    var isMoveSelected by remember { mutableStateOf(false) }
    var showFirstTimeSetup by remember { mutableStateOf(!viewModel.securityManager.isFirstTimeSetupDone) }

    val isSelectionMode = selectedItemIds.isNotEmpty()

    val docsPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMedia(uris, isMoveSelected)
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMedia(uris, isMoveSelected)
        }
    }

    val photoCount = allMedia.count { it.mediaType == MediaType.PHOTO.name }
    val videoCount = allMedia.count { it.mediaType == MediaType.VIDEO.name }
    val audioCount = allMedia.count { it.mediaType == MediaType.AUDIO.name }
    val docsNotesCount = allMedia.count { it.mediaType == MediaType.DOCUMENT.name || it.mediaType == MediaType.NOTE.name }
    val allFolders by viewModel.allFolders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    var showCreateNoteDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showFabActionMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectedTab == VaultTab.TRASH) {
                        Text(
                            text = if (isSelectionMode) "${selectedItemIds.size} Selected" else "Recycle Bin",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DangerRed
                        )
                    } else if (isSelectionMode) {
                        Text(
                            text = "${selectedItemIds.size} Selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextWhite
                        )
                    } else if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search files & notes...", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = VaultEmerald
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = VaultEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.R.string.vault_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextWhite
                            )
                        }
                    }
                },
                actions = {
                    if (selectedTab == VaultTab.TRASH) {
                        if (isSelectionMode) {
                            IconButton(onClick = { viewModel.selectAll() }) {
                                Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Select All", tint = TextWhite)
                            }
                            IconButton(onClick = { viewModel.restoreSelectedFromTrash() }) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore Selected", tint = VaultEmerald)
                            }
                            IconButton(onClick = { showPurgeBatchConfirm = true }) {
                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Delete Forever", tint = DangerRed)
                            }
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Selection", tint = TextWhite)
                            }
                        } else {
                            IconButton(onClick = { showEmptyTrashConfirm = true }) {
                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "Empty Trash", tint = DangerRed)
                            }
                            IconButton(onClick = { viewModel.selectTab(VaultTab.PHOTOS) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Trash", tint = TextWhite)
                            }
                        }
                    } else if (isSelectionMode) {
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(imageVector = Icons.Default.SelectAll, contentDescription = "Select All", tint = TextWhite)
                        }
                        IconButton(onClick = { showMoveToFolderDialog = true }) {
                            Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "Move to Album", tint = VaultEmerald)
                        }
                        IconButton(onClick = { viewModel.batchFavoriteSelected(true) }) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Favorite", tint = DangerRed)
                        }
                        IconButton(onClick = { showBatchDeleteConfirm = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Selected", tint = DangerRed)
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Selection", tint = TextWhite)
                        }
                    } else {
                        // Grid column switcher (2 <-> 3 <-> 4)
                        IconButton(onClick = {
                            val nextCols = if (gridColumns == 2) 3 else if (gridColumns == 3) 4 else 2
                            viewModel.setGridColumns(nextCols)
                        }) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Grid Layout ($gridColumns cols)",
                                tint = TextWhite
                            )
                        }

                        // Recycle Bin (Trash) navigation icon
                        IconButton(onClick = { viewModel.selectTab(VaultTab.TRASH) }) {
                            BadgedBox(badge = { if (trashCount > 0) Badge { Text("$trashCount") } }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Recycle Bin",
                                    tint = TextWhite
                                )
                            }
                        }

                        // Favorite filter toggle
                        IconButton(onClick = { viewModel.toggleFavoritesOnly() }) {
                            Icon(
                                imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorites",
                                tint = if (showFavoritesOnly) DangerRed else TextWhite
                            )
                        }

                        IconButton(onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.setSearchQuery("")
                        }) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextWhite
                            )
                        }

                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = TextWhite)
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(SurfaceCard)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Newest First", color = TextWhite) },
                                    onClick = {
                                        viewModel.setSortOption(SortOption.DATE_DESC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Oldest First", color = TextWhite) },
                                    onClick = {
                                        viewModel.setSortOption(SortOption.DATE_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name (A-Z)", color = TextWhite) },
                                    onClick = {
                                        viewModel.setSortOption(SortOption.NAME_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Largest File Size", color = TextWhite) },
                                    onClick = {
                                        viewModel.setSortOption(SortOption.SIZE_DESC)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.lockVault() },
                            modifier = Modifier.testTag("lock_vault_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = DangerRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceElevated)
            )
        },
        bottomBar = {
            Column {
                // Floating Mini Audio Player Bar if audio is active
                activeAudioItem?.let { audioItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable { viewModel.navigateTo(Screen.AudioPlayer(audioItem.id)) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultEmerald.copy(alpha = 0.5f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(VaultEmerald.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = VaultEmerald, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = audioItem.fileName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "Now Playing • Tap for full player", fontSize = 11.sp, color = TextMuted)
                            }
                            IconButton(onClick = { viewModel.toggleAudioPlayback() }) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle Play",
                                    tint = VaultEmerald
                                )
                            }
                            IconButton(onClick = { viewModel.setActiveAudio(null) }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Audio", tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                NavigationBar(
                    containerColor = SurfaceElevated
                ) {
                    NavigationBarItem(
                        selected = selectedTab == VaultTab.PHOTOS,
                        onClick = { viewModel.selectTab(VaultTab.PHOTOS) },
                        icon = {
                            BadgedBox(badge = { if (photoCount > 0) Badge { Text("$photoCount") } }) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = "Photos")
                            }
                        },
                        label = { Text("Photos", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_photos")
                    )

                    NavigationBarItem(
                        selected = selectedTab == VaultTab.VIDEOS,
                        onClick = { viewModel.selectTab(VaultTab.VIDEOS) },
                        icon = {
                            BadgedBox(badge = { if (videoCount > 0) Badge { Text("$videoCount") } }) {
                                Icon(imageVector = Icons.Default.Movie, contentDescription = "Videos")
                            }
                        },
                        label = { Text("Videos", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_videos")
                    )

                    NavigationBarItem(
                        selected = selectedTab == VaultTab.AUDIO,
                        onClick = { viewModel.selectTab(VaultTab.AUDIO) },
                        icon = {
                            BadgedBox(badge = { if (audioCount > 0) Badge { Text("$audioCount") } }) {
                                Icon(imageVector = Icons.Default.Audiotrack, contentDescription = "Audio")
                            }
                        },
                        label = { Text("Audio", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_audio")
                    )

                    NavigationBarItem(
                        selected = selectedTab == VaultTab.DOCS_NOTES,
                        onClick = { viewModel.selectTab(VaultTab.DOCS_NOTES) },
                        icon = {
                            BadgedBox(badge = { if (docsNotesCount > 0) Badge { Text("$docsNotesCount") } }) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = "Notes")
                            }
                        },
                        label = { Text("Notes", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_notes")
                    )

                    NavigationBarItem(
                        selected = selectedTab == VaultTab.ALBUMS,
                        onClick = { viewModel.selectTab(VaultTab.ALBUMS) },
                        icon = {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = "Albums")
                        },
                        label = { Text("Albums", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_albums")
                    )

                    NavigationBarItem(
                        selected = selectedTab == VaultTab.SETTINGS,
                        onClick = { viewModel.selectTab(VaultTab.SETTINGS) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkCarbon,
                            selectedTextColor = VaultEmerald,
                            indicatorColor = VaultEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("tab_settings")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != VaultTab.SETTINGS && !isSelectionMode) {
                FloatingActionButton(
                    onClick = { showFabActionMenu = true },
                    containerColor = VaultEmerald,
                    contentColor = DarkCarbon,
                    modifier = Modifier.testTag("import_media_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add to Vault")
                }
            }
        },
        containerColor = DarkCarbon,
        modifier = modifier.testTag("vault_main_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Selected Folder Filter Banner
            val activeFolder = selectedFolder
            if (activeFolder != null) {
                Surface(
                    color = SurfaceCard,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = VaultEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Album: $activeFolder", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(
                            onClick = { viewModel.selectFolder(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Album Filter", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    VaultTab.ALL -> {
                        if (filteredMedia.isEmpty()) {
                            EmptyVaultState(
                                title = "Safe is Empty",
                                subtitle = "Secure photos, videos, audio, or private notes in your hidden safe.",
                                icon = Icons.Default.FolderOpen,
                                actionText = "Import Media",
                                onAction = { showImportChoiceDialog = true }
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMedia, key = { it.id }) { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    PhotoGridItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            } else {
                                                when (item.mediaType) {
                                                    MediaType.PHOTO.name -> viewModel.navigateTo(Screen.PhotoViewer(item.id))
                                                    MediaType.VIDEO.name -> viewModel.navigateTo(Screen.VideoPlayer(item.id))
                                                    MediaType.AUDIO.name -> {
                                                        viewModel.setActiveAudio(item)
                                                        viewModel.navigateTo(Screen.AudioPlayer(item.id))
                                                    }
                                                    MediaType.NOTE.name -> viewModel.navigateTo(Screen.NoteViewer(item.id))
                                                    else -> {}
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    VaultTab.PHOTOS -> {
                        if (filteredMedia.isEmpty()) {
                            EmptyVaultState(
                                title = if (showFavoritesOnly) "No Favorite Photos" else "No Secured Photos",
                                subtitle = if (showFavoritesOnly) "Star photos to keep them in quick favorites." else "Import sensitive photos into your private safe.",
                                icon = Icons.Default.Image,
                                actionText = if (showFavoritesOnly) null else "Import Photos",
                                onAction = if (showFavoritesOnly) null else { { isMoveSelected = false; filePicker.launch(arrayOf("image/*")) } }
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMedia, key = { it.id }) { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    PhotoGridItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            } else {
                                                viewModel.navigateTo(Screen.PhotoViewer(item.id))
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    VaultTab.VIDEOS -> {
                        if (filteredMedia.isEmpty()) {
                            EmptyVaultState(
                                title = if (showFavoritesOnly) "No Favorite Videos" else "No Secured Videos",
                                subtitle = if (showFavoritesOnly) "Star videos to access them instantly." else "Import and hide private video files behind .nomedia protection.",
                                icon = Icons.Default.VideoLibrary,
                                actionText = if (showFavoritesOnly) null else "Import Videos",
                                onAction = if (showFavoritesOnly) null else { { isMoveSelected = false; filePicker.launch(arrayOf("video/*")) } }
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(if (gridColumns == 2) 1 else if (gridColumns == 3) 2 else 3),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMedia, key = { it.id }) { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    VideoGridItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            } else {
                                                viewModel.navigateTo(Screen.VideoPlayer(item.id))
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    VaultTab.AUDIO -> {
                        if (filteredMedia.isEmpty()) {
                            EmptyVaultState(
                                title = if (showFavoritesOnly) "No Favorite Audio" else "No Secured Audio",
                                subtitle = "Import voice recordings or confidential music files.",
                                icon = Icons.Default.Audiotrack,
                                actionText = if (showFavoritesOnly) null else "Import Audio",
                                onAction = if (showFavoritesOnly) null else { { isMoveSelected = false; filePicker.launch(arrayOf("audio/*")) } }
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMedia, key = { it.id }) { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    AudioListItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            } else {
                                                viewModel.setActiveAudio(item)
                                                viewModel.navigateTo(Screen.AudioPlayer(item.id))
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    VaultTab.DOCS_NOTES -> {
                        if (filteredMedia.isEmpty()) {
                            EmptyVaultState(
                                title = if (showFavoritesOnly) "No Favorite Notes" else "No Documents or Notes",
                                subtitle = "Write a private note or import confidential documents.",
                                icon = Icons.Default.Description,
                                actionText = "New Private Note",
                                onAction = { showCreateNoteDialog = true }
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredMedia, key = { it.id }) { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    DocNoteListItem(
                                        item = item,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onClick = {
                                            if (isSelectionMode) {
                                                viewModel.toggleItemSelection(item.id)
                                            } else {
                                                if (item.mediaType == MediaType.NOTE.name) {
                                                    viewModel.navigateTo(Screen.NoteViewer(item.id))
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            viewModel.toggleItemSelection(item.id)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    VaultTab.ALBUMS -> {
                        val folders = allFolders.ifEmpty { listOf("Default") }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(folders) { folderName ->
                                val count = allMedia.count { it.folderName.equals(folderName, ignoreCase = true) }
                                AlbumGridCard(
                                    folderName = folderName,
                                    count = count,
                                    isSelected = selectedFolder?.equals(folderName, ignoreCase = true) == true,
                                    onClick = {
                                        viewModel.selectFolder(folderName)
                                        viewModel.selectTab(VaultTab.PHOTOS)
                                    }
                                )
                            }
                        }
                    }

                    VaultTab.TRASH -> {
                        TrashScreenContent(
                            trashList = filteredMedia,
                            selectedItemIds = selectedItemIds,
                            isSelectionMode = isSelectionMode,
                            onToggleSelection = { viewModel.toggleItemSelection(it) },
                            onRestoreItem = { viewModel.restoreItemFromTrash(it) },
                            onDeleteItem = {
                                itemToPurge = it
                                showPurgeConfirm = true
                            },
                            onEmptyTrashClick = { showEmptyTrashConfirm = true }
                        )
                    }

                    VaultTab.SETTINGS -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Import Choice Dialog: Copy vs Move
    if (showImportChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showImportChoiceDialog = false },
            title = { Text("Import to Private Safe", color = TextWhite) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how you would like to secure the selected media files:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoveSelected = false
                                showImportChoiceDialog = false
                                filePicker.launch(arrayOf("*/*"))
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Copy to Safe", color = VaultEmerald, fontWeight = FontWeight.Bold)
                            Text("Duplicates file into the vault folder. Original remains in gallery.", color = TextMuted, fontSize = 12.sp)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoveSelected = true
                                showImportChoiceDialog = false
                                filePicker.launch(arrayOf("*/*"))
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Move to Safe (Recommended)", color = VaultEmerald, fontWeight = FontWeight.Bold)
                            Text("Moves file to safe and deletes original, removing it from gallery index.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportChoiceDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Batch Delete Confirmation Dialog
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("Move ${selectedItemIds.size} Items to Trash?", color = TextWhite) },
            text = { Text("Selected items will be moved to the Recycle Bin and can be restored within 30 days.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBatchDeleteConfirm = false
                        viewModel.deleteSelectedItems()
                    }
                ) {
                    Text("Move to Trash", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Empty Trash Confirmation Dialog
    if (showEmptyTrashConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashConfirm = false },
            title = { Text("Empty Recycle Bin?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("All files in the Recycle Bin will be permanently removed. This action cannot be undone.", color = TextMuted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecureShredOption = !showSecureShredOption }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = showSecureShredOption,
                            onCheckedChange = { showSecureShredOption = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Secure Shred", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Overwrite file contents with random bytes before deletion", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmptyTrashConfirm = false
                        viewModel.emptyTrash(showSecureShredOption)
                    }
                ) {
                    Text("Permanently Delete All", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Single Item Purge Confirmation
    if (showPurgeConfirm && itemToPurge != null) {
        val target = itemToPurge!!
        AlertDialog(
            onDismissRequest = {
                showPurgeConfirm = false
                itemToPurge = null
            },
            title = { Text("Permanently Delete?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Permanently delete \"${target.fileName}\"? This cannot be recovered.", color = TextMuted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecureShredOption = !showSecureShredOption }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = showSecureShredOption,
                            onCheckedChange = { showSecureShredOption = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secure Shred (Zero-fill overwrite)", color = TextWhite, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPurgeConfirm = false
                        viewModel.permanentDeleteItem(target, showSecureShredOption)
                        itemToPurge = null
                    }
                ) {
                    Text("Delete Forever", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPurgeConfirm = false
                    itemToPurge = null
                }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Batch Purge Confirmation in Trash
    if (showPurgeBatchConfirm) {
        AlertDialog(
            onDismissRequest = { showPurgeBatchConfirm = false },
            title = { Text("Delete ${selectedItemIds.size} Items Forever?", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("These files will be permanently deleted and cannot be recovered.", color = TextMuted, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecureShredOption = !showSecureShredOption }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = showSecureShredOption,
                            onCheckedChange = { showSecureShredOption = it },
                            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Secure Shred (Zero-fill overwrite)", color = TextWhite, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPurgeBatchConfirm = false
                        viewModel.permanentlyDeleteSelected(showSecureShredOption)
                    }
                ) {
                    Text("Delete Forever", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeBatchConfirm = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Import Progress Dialog
    importProgress?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = { viewModel.dismissImportDialog() }
        )
    }

    if (showFirstTimeSetup) {
        com.example.ui.components.FirstTimeSetupDialog(
            viewModel = viewModel,
            onComplete = {
                showFirstTimeSetup = false
            }
        )
    }

    // FAB Speed-Dial / Action Menu
    if (showFabActionMenu) {
        AlertDialog(
            onDismissRequest = { showFabActionMenu = false },
            title = { Text("Add to Private Safe", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Import Photos & Videos
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showFabActionMenu = false
                                showImportChoiceDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import Photos & Videos", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Select from device gallery or internal storage", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    // Option 2: Import Files & Documents
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showFabActionMenu = false
                                docsPicker.launch(arrayOf("*/*"))
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Import Files & Documents", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("PDFs, archives, confidential office docs", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    // Option 3: Write Secret Note
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showFabActionMenu = false
                                showCreateNoteDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Write Secret Note", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Private passwords, text records, thoughts", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    // Option 4: Create Album
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showFabActionMenu = false
                                showCreateFolderDialog = true
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, tint = VaultEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Create New Album", color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Organize media into custom folders", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFabActionMenu = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Create Secret Note Dialog
    if (showCreateNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteBody by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateNoteDialog = false },
            title = { Text("New Secret Note", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = VaultEmerald
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBody,
                        onValueChange = { noteBody = it },
                        label = { Text("Note Content") },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = VaultEmerald
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = noteTitle.ifBlank { "Untitled Note" }
                        viewModel.createNote(title, noteBody, selectedFolder ?: "Default")
                        showCreateNoteDialog = false
                    }
                ) {
                    Text("Save to Vault", color = VaultEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateNoteDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Create Album Dialog
    if (showCreateFolderDialog) {
        var albumName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create New Album", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    label = { Text("Album Name") },
                    placeholder = { Text("e.g. Travel, Personal, Documents") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = VaultEmerald
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (albumName.isNotBlank()) {
                            viewModel.selectFolder(albumName.trim())
                            showCreateFolderDialog = false
                        }
                    }
                ) {
                    Text("Create", color = VaultEmerald, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Move to Album Dialog
    if (showMoveToFolderDialog) {
        var customFolder by remember { mutableStateOf("") }
        val availableFolders = (allFolders + listOf("Default", "Personal", "Work", "Archive")).distinct()

        AlertDialog(
            onDismissRequest = { showMoveToFolderDialog = false },
            title = { Text("Move ${selectedItemIds.size} Item(s) to Album", color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select destination album:", color = TextMuted, fontSize = 13.sp)

                    availableFolders.forEach { folderName ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.batchMoveSelected(folderName)
                                    showMoveToFolderDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = VaultEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(folderName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customFolder,
                        onValueChange = { customFolder = it },
                        placeholder = { Text("Or type new album name...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = VaultEmerald
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                if (customFolder.isNotBlank()) {
                    TextButton(
                        onClick = {
                            viewModel.batchMoveSelected(customFolder.trim())
                            showMoveToFolderDialog = false
                        }
                    ) {
                        Text("Move to New Album", color = VaultEmerald)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveToFolderDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoGridItem(
    item: VaultMediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) VaultEmerald else SurfaceBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("photo_item_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(item.uriString))
                    .crossfade(true)
                    .build(),
                contentDescription = item.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Selection Checkbox
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultEmerald else DarkCarbon.copy(alpha = 0.6f))
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (item.isFavorite && !isSelectionMode) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = DangerRed,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VideoGridItem(
    item: VaultMediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) VaultEmerald else SurfaceBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("video_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkCarbon),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(item.uriString))
                        .crossfade(true)
                        .build(),
                    contentDescription = item.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) VaultEmerald else DarkCarbon.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Video",
                            tint = VaultEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.fileName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sizeMb = item.fileSizeBytes / (1024 * 1024f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", sizeMb)} MB",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (item.originalDirectory.isNotEmpty()) {
                        Text(
                            text = item.originalDirectory,
                            fontSize = 10.sp,
                            color = VaultEmerald,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AudioListItem(
    item: VaultMediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) VaultEmerald else SurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("audio_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultEmerald else DarkCarbon.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultEmerald.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = null,
                        tint = VaultEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sizeKb = item.fileSizeBytes / 1024
                Text(
                    text = if (sizeKb > 1024) "${String.format("%.1f", sizeKb / 1024f)} MB • Secured" else "$sizeKb KB • Secured",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = VaultEmerald,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EmptyVaultState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextDim,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(top = 6.dp),
            lineHeight = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = DarkCarbon,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = actionText,
                    color = DarkCarbon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DocNoteListItem(
    item: VaultMediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isNote = item.mediaType == MediaType.NOTE.name
    val isMarkdown = item.fileName.endsWith(".md", ignoreCase = true)
    val isPdf = item.fileName.endsWith(".pdf", ignoreCase = true)
    val isText = item.fileName.endsWith(".txt", ignoreCase = true)

    val badgeColor = when {
        isNote -> Color(0xFFF59E0B) // Amber
        isMarkdown -> Color(0xFF38BDF8) // Sky
        isPdf -> Color(0xFFEF4444) // Coral Red
        else -> VaultEmerald
    }

    val typeLabel = when {
        isNote -> "NOTE"
        isMarkdown -> "MD"
        isPdf -> "PDF"
        isText -> "TXT"
        else -> "DOC"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) VaultEmerald else SurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("doc_note_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultEmerald else DarkCarbon.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isNote) Icons.Default.Edit else Icons.Default.Description,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = typeLabel,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                if (isNote && item.noteContent.isNotBlank()) {
                    Text(
                        text = item.noteContent.trim().replace(Regex("\\s+"), " "),
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val wordCount = item.noteContent.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
                        Text(
                            text = "$wordCount word(s)",
                            fontSize = 11.sp,
                            color = badgeColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = " • ",
                            fontSize = 11.sp,
                            color = TextDim
                        )
                        Text(
                            text = item.folderName,
                            fontSize = 11.sp,
                            color = TextDim
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatFileSize(item.fileSizeBytes),
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = " • ",
                            fontSize = 11.sp,
                            color = TextDim
                        )
                        Text(
                            text = item.folderName,
                            fontSize = 11.sp,
                            color = TextDim
                        )
                    }
                }
            }

            if (item.isFavorite) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = DangerRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumGridCard(
    folderName: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) VaultEmerald else SurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("album_card_$folderName"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VaultEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.FolderOpen else Icons.Default.Folder,
                    contentDescription = null,
                    tint = VaultEmerald,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = folderName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$count file(s)",
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun TrashScreenContent(
    trashList: List<VaultMediaItem>,
    selectedItemIds: Set<Long>,
    isSelectionMode: Boolean,
    onToggleSelection: (Long) -> Unit,
    onRestoreItem: (VaultMediaItem) -> Unit,
    onDeleteItem: (VaultMediaItem) -> Unit,
    onEmptyTrashClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (trashList.isEmpty()) {
        EmptyVaultState(
            title = "Recycle Bin is Empty",
            subtitle = "Items moved to the Recycle Bin will be stored here for up to 30 days before being automatically purged.",
            icon = Icons.Default.DeleteSweep
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${trashList.size} item(s) in Recycle Bin",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Auto-purged after 30 days of deletion",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    if (!isSelectionMode) {
                        TextButton(
                            onClick = onEmptyTrashClick,
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = DangerRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Empty Bin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(trashList, key = { it.id }) { item ->
                    val isSelected = selectedItemIds.contains(item.id)
                    TrashListItem(
                        item = item,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                onToggleSelection(item.id)
                            }
                        },
                        onLongClick = {
                            onToggleSelection(item.id)
                        },
                        onRestore = { onRestoreItem(item) },
                        onDeleteForever = { onDeleteItem(item) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TrashListItem(
    item: VaultMediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val context = LocalContext.current
    val elapsedDays = if (item.trashTimestamp > 0L) {
        (System.currentTimeMillis() - item.trashTimestamp) / (1000L * 60 * 60 * 24)
    } else {
        0L
    }
    val daysRemaining = maxOf(0L, 30L - elapsedDays)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) VaultEmerald else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("trash_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) VaultEmerald else DarkCarbon.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DarkCarbon, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCarbon),
                contentAlignment = Alignment.Center
            ) {
                when (item.mediaType) {
                    MediaType.PHOTO.name, MediaType.VIDEO.name -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(item.uriString))
                                .crossfade(true)
                                .build(),
                            contentDescription = item.fileName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (item.mediaType == MediaType.VIDEO.name) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Movie,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    MediaType.AUDIO.name -> {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = VaultEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.fileName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatFileSize(item.fileSizeBytes),
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (daysRemaining <= 1) "Expires today" else "Expires in $daysRemaining d",
                        fontSize = 11.sp,
                        color = if (daysRemaining <= 3) DangerRed else TextMuted,
                        fontWeight = if (daysRemaining <= 3) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (!isSelectionMode) {
                IconButton(
                    onClick = onRestore,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore Item",
                        tint = VaultEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteForever,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Permanently Delete",
                        tint = DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
