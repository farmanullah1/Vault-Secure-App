package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricHelper
import com.example.ui.components.PinKeypadDialog
import com.example.ui.components.PinSetupDialog
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald
import com.example.viewmodel.VaultViewModel

data class DecoyNote(
    val id: Int,
    val title: String,
    val snippet: String,
    val date: String,
    val category: String,
    val isPinned: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun NotesCoverScreen(
    viewModel: VaultViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val securityManager = viewModel.securityManager

    var showPinDialog by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activeDecoyNote by remember { mutableStateOf<DecoyNote?>(null) }
    var searchClickCount by remember { mutableStateOf(0) }

    val decoyNotes = remember {
        listOf(
            DecoyNote(
                id = 1,
                title = "Weekly Grocery & Kitchen",
                snippet = "• Whole milk & oat milk\n• Extra virgin olive oil\n• Free-range eggs\n• Fresh spinach & avocados\n• Brown rice & whole grain bread",
                date = "Yesterday",
                category = "Personal",
                isPinned = true
            ),
            DecoyNote(
                id = 2,
                title = "Weekend Home Projects",
                snippet = "1. Organize study desk cable management\n2. Service air purifier filter\n3. Back up laptop work directory\n4. Order replacement water filter",
                date = "Oct 18",
                category = "Personal",
                isPinned = true
            ),
            DecoyNote(
                id = 3,
                title = "Meeting Highlights & Follow-up",
                snippet = "Sync with project design leads regarding mobile app responsive navigation. Confirm target SDK 36 compatibility and final release cut timeline.",
                date = "Oct 16",
                category = "Work"
            ),
            DecoyNote(
                id = 4,
                title = "Book Recommendations",
                snippet = "• Clean Architecture (Robert Martin)\n• Designing Data-Intensive Applications\n• The Pragmatic Programmer\n• Atomic Habits",
                date = "Oct 12",
                category = "Ideas"
            ),
            DecoyNote(
                id = 5,
                title = "Gym Workout Routine",
                snippet = "Mon: Chest & Triceps (Bench press, incline dumbbell, cable pushdown)\nWed: Back & Biceps (Pullups, lat pull, barbell rows)\nFri: Legs & Shoulders (Squats, lunges, OHP)",
                date = "Oct 05",
                category = "Personal"
            )
        )
    }

    fun vibrate() {
        if (securityManager.isHapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    fun triggerVaultUnlock() {
        vibrate()
        if (!securityManager.isPinSetup) {
            showSetupDialog = true
            return
        }

        val activity = context as? FragmentActivity
        if (activity != null && securityManager.isBiometricEnabled && BiometricHelper.isBiometricAvailable(context)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Unlock Private Vault",
                subtitle = "Verify biometric identity to open vault",
                onSuccess = { viewModel.unlockVault() },
                onError = { showPinDialog = true },
                onFallbackToPin = { showPinDialog = true }
            )
        } else {
            showPinDialog = true
        }
    }

    val filteredNotes = decoyNotes.filter { note ->
        (selectedCategory == "All" || note.category.equals(selectedCategory, ignoreCase = true)) &&
        (searchQuery.isBlank() || note.title.contains(searchQuery, ignoreCase = true) || note.snippet.contains(searchQuery, ignoreCase = true))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notes_cover_screen"),
        containerColor = DarkCarbon,
        topBar = {
            TopAppBar(
                title = {
                    // Secret Trigger 1: Long press on "My Notes" title unlocks vault
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { /* Normal tap: no-op or scroll to top */ },
                                onLongClick = { triggerVaultUnlock() }
                            )
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                            .testTag("notes_title_secret_trigger")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = VaultEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "My Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextWhite
                        )
                    }
                },
                actions = {
                    // Secret Trigger 2: 4 rapid taps on search icon triggers unlock
                    IconButton(onClick = {
                        searchClickCount++
                        if (searchClickCount >= 4) {
                            searchClickCount = 0
                            triggerVaultUnlock()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted
                        )
                    }

                    IconButton(onClick = { /* Decoy menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCarbon)
            )
        },
        floatingActionButton = {
            // Secret Trigger 3: Long press FAB unlocks vault
            FloatingActionButton(
                onClick = {
                    // Open a new blank decoy note preview
                    activeDecoyNote = DecoyNote(
                        id = 999,
                        title = "New Note",
                        snippet = "",
                        date = "Just now",
                        category = "Personal"
                    )
                },
                containerColor = VaultEmerald,
                contentColor = DarkCarbon,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            activeDecoyNote = DecoyNote(
                                id = 999,
                                title = "New Note",
                                snippet = "",
                                date = "Just now",
                                category = "Personal"
                            )
                        },
                        onLongClick = { triggerVaultUnlock() }
                    )
                    .testTag("notes_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search personal notes...", color = TextDim, fontSize = 14.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextDim, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextDim, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = VaultEmerald.copy(alpha = 0.5f),
                    unfocusedBorderColor = SurfaceBorder,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("All", "Personal", "Work", "Ideas")
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VaultEmerald,
                            selectedLabelColor = DarkCarbon,
                            containerColor = SurfaceCard,
                            labelColor = TextMuted
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) VaultEmerald else SurfaceBorder
                        )
                    )
                }
            }

            // Notes List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeDecoyNote = note },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (note.isPinned) VaultEmerald.copy(alpha = 0.35f) else SurfaceBorder
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (note.isPinned) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Pinned",
                                        tint = VaultEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.snippet,
                                color = TextMuted,
                                fontSize = 13.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.category,
                                    color = TextDim,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = note.date,
                                    color = TextDim,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Decoy Note Viewer / Edit dialog to make the disguise 100% convincing
    activeDecoyNote?.let { note ->
        var noteTitle by remember(note) { mutableStateOf(note.title) }
        var noteBody by remember(note) { mutableStateOf(note.snippet) }

        AlertDialog(
            onDismissRequest = { activeDecoyNote = null },
            title = {
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    placeholder = { Text("Title", color = TextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = noteBody,
                    onValueChange = { noteBody = it },
                    placeholder = { Text("Note content...", color = TextDim) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { activeDecoyNote = null }) {
                    Text("Done", color = VaultEmerald)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDecoyNote = null }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
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
