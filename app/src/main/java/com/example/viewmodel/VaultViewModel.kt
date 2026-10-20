package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.VaultDatabase
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.data.repository.ImportProgress
import com.example.data.repository.VaultRepository
import com.example.security.PinSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.security.DisguiseMode

sealed class Screen {
    object SimCover : Screen()
    object CalculatorCover : Screen()
    object NotesCover : Screen()
    object VaultMain : Screen()
    data class PhotoViewer(val mediaId: Long) : Screen()
    data class VideoPlayer(val mediaId: Long) : Screen()
    data class AudioPlayer(val mediaId: Long) : Screen()
    data class NoteViewer(val mediaId: Long) : Screen()
    object GalleryChecklist : Screen()
    object IntruderLogs : Screen()
}

enum class VaultTab {
    ALL,
    PHOTOS,
    VIDEOS,
    AUDIO,
    DOCS_NOTES,
    ALBUMS,
    SETTINGS
}

enum class SortOption {
    DATE_DESC,
    DATE_ASC,
    NAME_ASC,
    SIZE_DESC
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    val securityManager = PinSecurityManager(application)
    private val database = VaultDatabase.getInstance(application)
    val repository = VaultRepository(application, database.mediaDao(), securityManager)

    private val _currentScreen = MutableStateFlow<Screen>(
        when (securityManager.disguiseMode) {
            DisguiseMode.CALCULATOR -> Screen.CalculatorCover
            DisguiseMode.NOTES -> Screen.NotesCover
            else -> Screen.SimCover
        }
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedTab = MutableStateFlow(VaultTab.PHOTOS)
    val selectedTab: StateFlow<VaultTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _hasNoMedia = MutableStateFlow(false)
    val hasNoMedia: StateFlow<Boolean> = _hasNoMedia.asStateFlow()

    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()

    private val _activeAudioItem = MutableStateFlow<VaultMediaItem?>(null)
    val activeAudioItem: StateFlow<VaultMediaItem?> = _activeAudioItem.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _selectedItemIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedItemIds: StateFlow<Set<Long>> = _selectedItemIds.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    val allFolders: StateFlow<List<String>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedia: StateFlow<List<VaultMediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMedia: StateFlow<List<VaultMediaItem>> = combine(
        allMedia,
        _selectedTab,
        _searchQuery,
        _sortOption,
        combine(_showFavoritesOnly, _selectedFolder) { favs, folder -> Pair(favs, folder) }
    ) { mediaList, tab, query, sort, favsAndFolder ->
        val favsOnly = favsAndFolder.first
        val folder = favsAndFolder.second

        val typeFiltered = when (tab) {
            VaultTab.ALL -> mediaList
            VaultTab.PHOTOS -> mediaList.filter { it.mediaType == MediaType.PHOTO.name }
            VaultTab.VIDEOS -> mediaList.filter { it.mediaType == MediaType.VIDEO.name }
            VaultTab.AUDIO -> mediaList.filter { it.mediaType == MediaType.AUDIO.name }
            VaultTab.DOCS_NOTES -> mediaList.filter { it.mediaType == MediaType.DOCUMENT.name || it.mediaType == MediaType.NOTE.name }
            VaultTab.ALBUMS -> if (folder != null) mediaList.filter { it.folderName.equals(folder, ignoreCase = true) } else mediaList
            else -> emptyList()
        }

        val folderFiltered = if (folder != null && tab != VaultTab.ALBUMS) {
            typeFiltered.filter { it.folderName.equals(folder, ignoreCase = true) }
        } else {
            typeFiltered
        }

        val favFiltered = if (favsOnly) {
            folderFiltered.filter { it.isFavorite }
        } else {
            folderFiltered
        }

        val searched = if (query.isBlank()) {
            favFiltered
        } else {
            favFiltered.filter {
                it.fileName.contains(query, ignoreCase = true) ||
                it.noteContent.contains(query, ignoreCase = true) ||
                it.folderName.contains(query, ignoreCase = true)
            }
        }

        when (sort) {
            SortOption.DATE_DESC -> searched.sortedByDescending { it.dateAdded }
            SortOption.DATE_ASC -> searched.sortedBy { it.dateAdded }
            SortOption.NAME_ASC -> searched.sortedBy { it.fileName.lowercase() }
            SortOption.SIZE_DESC -> searched.sortedByDescending { it.fileSizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshNoMediaStatus()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectTab(tab: VaultTab) {
        _selectedTab.value = tab
        _selectedItemIds.value = emptySet()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleItemSelection(id: Long) {
        val current = _selectedItemIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItemIds.value = current
    }

    fun clearSelection() {
        _selectedItemIds.value = emptySet()
    }

    fun selectAll() {
        _selectedItemIds.value = filteredMedia.value.map { it.id }.toSet()
    }

    fun deleteSelectedItems() {
        val idsToDelete = _selectedItemIds.value
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch {
            val itemsToDelete = allMedia.value.filter { idsToDelete.contains(it.id) }
            repository.deleteBatch(itemsToDelete)
            _selectedItemIds.value = emptySet()
        }
    }

    fun selectFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun batchMoveSelected(folderName: String) {
        val ids = _selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.batchMoveToFolder(ids, folderName)
            _selectedItemIds.value = emptySet()
        }
    }

    fun batchFavoriteSelected(isFav: Boolean) {
        val ids = _selectedItemIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            repository.batchSetFavorite(ids, isFav)
            _selectedItemIds.value = emptySet()
        }
    }

    fun createNote(title: String, content: String, folder: String = "Default") {
        viewModelScope.launch {
            repository.saveNote(title, content, folder)
        }
    }

    fun updateNote(id: Long, title: String, content: String) {
        viewModelScope.launch {
            repository.updateNote(id, title, content)
        }
    }

    fun unlockVault() {
        _isUnlocked.value = true
        _currentScreen.value = Screen.VaultMain
        refreshNoMediaStatus()
        syncFolder()
    }

    fun lockVault() {
        _isUnlocked.value = false
        _activeAudioItem.value = null
        _isAudioPlaying.value = false
        _selectedItemIds.value = emptySet()
        securityManager.isDecoySessionActive = false
        _currentScreen.value = when (securityManager.disguiseMode) {
            DisguiseMode.CALCULATOR -> Screen.CalculatorCover
            DisguiseMode.NOTES -> Screen.NotesCover
            else -> Screen.SimCover
        }
    }

    fun refreshNoMediaStatus() {
        viewModelScope.launch {
            _hasNoMedia.value = repository.checkNoMediaStatus()
        }
    }

    fun ensureNoMedia() {
        viewModelScope.launch {
            _hasNoMedia.value = repository.ensureNoMediaFile()
        }
    }

    fun setVaultFolder(uri: Uri) {
        repository.setVaultFolderUri(uri)
        refreshNoMediaStatus()
        syncFolder()
    }

    fun syncFolder() {
        viewModelScope.launch {
            repository.syncVaultFolder()
            refreshNoMediaStatus()
        }
    }

    fun importMedia(uris: List<Uri>, isMove: Boolean) {
        viewModelScope.launch {
            repository.importFiles(uris, isMove) { progress ->
                _importProgress.value = progress
            }
        }
    }

    fun dismissImportDialog() {
        _importProgress.value = null
    }

    fun deleteItem(item: VaultMediaItem) {
        viewModelScope.launch {
            repository.deleteMedia(item)
            if (_activeAudioItem.value?.id == item.id) {
                _activeAudioItem.value = null
                _isAudioPlaying.value = false
            }
        }
    }

    fun restoreMedia(item: VaultMediaItem, destinationUri: Uri, deleteFromVault: Boolean) {
        viewModelScope.launch {
            repository.restoreOrExportMedia(item, destinationUri, deleteFromVault)
        }
    }

    fun toggleFavorite(item: VaultMediaItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun setActiveAudio(item: VaultMediaItem?) {
        _activeAudioItem.value = item
        _isAudioPlaying.value = item != null
    }

    fun toggleAudioPlayback() {
        _isAudioPlaying.value = !_isAudioPlaying.value
    }

    fun safeResetApp() {
        viewModelScope.launch {
            repository.safeResetDatabase()
            lockVault()
        }
    }
}
