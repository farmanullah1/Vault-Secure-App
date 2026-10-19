package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_media")
data class VaultMediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,              // Secure vault SAF URI
    val originalPathOrUri: String = "", // Original source path or URI mapping
    val originalDirectory: String = "", // Original directory name (e.g. Camera, WhatsApp, Downloads)
    val fileName: String,
    val mimeType: String,
    val mediaType: String,              // "PHOTO", "VIDEO", "AUDIO", "OTHER"
    val fileSizeBytes: Long,
    val durationMs: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis(),
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val relativePath: String = ""
)
