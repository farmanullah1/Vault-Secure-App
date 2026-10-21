package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.example.data.dao.OperationJournalDao
import com.example.data.model.OperationJournalEntity
import java.util.UUID

data class ImportProgress(
    val current: Int,
    val total: Int,
    val currentFileName: String,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)

class VaultRepository(
    private val context: Context,
    private val mediaDao: MediaDao,
    private val operationJournalDao: OperationJournalDao,
    private val securityManager: PinSecurityManager
) {

    val allMedia: Flow<List<VaultMediaItem>> = mediaDao.getAllMedia()
    val trashMedia: Flow<List<VaultMediaItem>> = mediaDao.getTrashMedia()
    val trashCount: Flow<Int> = mediaDao.countTrash()

    fun getMediaByType(type: String): Flow<List<VaultMediaItem>> {
        return mediaDao.getMediaByType(type)
    }

    suspend fun getMediaById(id: Long): VaultMediaItem? = withContext(Dispatchers.IO) {
        mediaDao.getMediaById(id)
    }

    suspend fun toggleFavorite(item: VaultMediaItem) = withContext(Dispatchers.IO) {
        mediaDao.updateFavorite(item.id, !item.isFavorite)
    }

    suspend fun updateLastPlayedPosition(id: Long, positionMs: Long) = withContext(Dispatchers.IO) {
        mediaDao.updateLastPlayedPosition(id, positionMs)
    }

    // Move to Recycle Bin (Trash)
    suspend fun moveToTrash(item: VaultMediaItem) = withContext(Dispatchers.IO) {
        mediaDao.moveToTrash(item.id, System.currentTimeMillis())
    }

    suspend fun moveToTrashBatch(items: List<VaultMediaItem>) = withContext(Dispatchers.IO) {
        val ids = items.map { it.id }
        mediaDao.moveToTrashBatch(ids, System.currentTimeMillis())
    }

    // Restore from Trash
    suspend fun restoreFromTrash(item: VaultMediaItem) = withContext(Dispatchers.IO) {
        mediaDao.restoreFromTrash(item.id)
    }

    suspend fun restoreFromTrashBatch(items: List<VaultMediaItem>) = withContext(Dispatchers.IO) {
        val ids = items.map { it.id }
        mediaDao.restoreFromTrashBatch(ids)
    }

    // Permanent Deletion with optional Secure Shredder (Zero-fill overwrite)
    suspend fun permanentDelete(item: VaultMediaItem, secureShred: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        if (secureShred) {
            shredDocument(item)
        }
        deleteDocumentFromStorage(item.uriString)
        mediaDao.delete(item)
        true
    }

    suspend fun permanentDeleteBatch(items: List<VaultMediaItem>, secureShred: Boolean = false): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (item in items) {
            if (secureShred) {
                shredDocument(item)
            }
            deleteDocumentFromStorage(item.uriString)
            mediaDao.delete(item)
            count++
        }
        count
    }

    suspend fun emptyTrash(secureShred: Boolean = false): Int = withContext(Dispatchers.IO) {
        val trashItems = mediaDao.getTrashMediaList()
        var count = 0
        for (item in trashItems) {
            if (secureShred) {
                shredDocument(item)
            }
            deleteDocumentFromStorage(item.uriString)
            count++
        }
        mediaDao.emptyTrash()
        count
    }

    private fun deleteDocumentFromStorage(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: Exception) {
            try {
                DocumentFile.fromSingleUri(context, Uri.parse(uriString))?.delete()
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }

    private fun shredDocument(item: VaultMediaItem) {
        try {
            val uri = Uri.parse(item.uriString)
            val size = item.fileSizeBytes
            if (size > 0 && !uri.scheme.equals("note", ignoreCase = true)) {
                context.contentResolver.openOutputStream(uri, "rwt")?.use { out ->
                    val buffer = ByteArray(8192)
                    SecureRandom().nextBytes(buffer)
                    var written = 0L
                    while (written < size) {
                        val toWrite = minOf(buffer.size.toLong(), size - written).toInt()
                        out.write(buffer, 0, toWrite)
                        written += toWrite
                    }
                    out.flush()
                }
            }
        } catch (e: Exception) {
            // Best effort overwrite
        }
    }

    // Auto-purge trash older than 30 days
    suspend fun purgeOldTrash() = withContext(Dispatchers.IO) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
        mediaDao.purgeTrashOlderThan(thirtyDaysAgo)
    }

    // Backward-compatible deleteMedia moves to trash
    suspend fun deleteMedia(item: VaultMediaItem): Boolean = withContext(Dispatchers.IO) {
        moveToTrash(item)
        true
    }

    suspend fun deleteBatch(items: List<VaultMediaItem>): Int = withContext(Dispatchers.IO) {
        moveToTrashBatch(items)
        items.size
    }

    fun getVaultFolderUri(): Uri? {
        val uriStr = securityManager.vaultFolderUri ?: return null
        return Uri.parse(uriStr)
    }

    fun setVaultFolderUri(uri: Uri) {
        try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        securityManager.vaultFolderUri = uri.toString()
    }

    suspend fun checkNoMediaStatus(): Boolean = withContext(Dispatchers.IO) {
        val folderUri = getVaultFolderUri() ?: return@withContext false
        val dir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false
        if (!dir.exists() || !dir.isDirectory) return@withContext false

        val existing = dir.findFile(".nomedia")
        existing != null && existing.exists()
    }

    suspend fun ensureNoMediaFile(): Boolean = withContext(Dispatchers.IO) {
        val folderUri = getVaultFolderUri() ?: return@withContext false
        val dir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext false
        if (!dir.exists() || !dir.isDirectory) return@withContext false

        val existing = dir.findFile(".nomedia")
        if (existing != null && existing.exists()) {
            return@withContext true
        }

        try {
            val newFile = dir.createFile("application/octet-stream", ".nomedia")
            if (newFile != null) {
                context.contentResolver.openOutputStream(newFile.uri)?.use { out ->
                    out.write(ByteArray(0))
                }
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    suspend fun importFiles(
        sourceUris: List<Uri>,
        isMove: Boolean,
        onProgress: (ImportProgress) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val folderUri = getVaultFolderUri() ?: throw IllegalStateException("Vault folder not configured")
        val targetDir = DocumentFile.fromTreeUri(context, folderUri)
            ?: throw IllegalStateException("Cannot access vault folder")

        // Note: Initial scan & import do NOT auto-create .nomedia per Spec 2.1 Conflict Resolution #2 & Spec 14.8.
        // Hiding from gallery via .nomedia is an explicit user-confirmed action.

        var successCount = 0
        val total = sourceUris.size

        for ((index, sourceUri) in sourceUris.withIndex()) {
            val nameAndSize = getFileNameAndSize(sourceUri)
            val fileName = nameAndSize.first ?: "media_${System.currentTimeMillis()}"
            val sourceSize = nameAndSize.second
            val mimeType = context.contentResolver.getType(sourceUri) ?: getMimeTypeFromExtension(fileName)

            val pathSegments = sourceUri.pathSegments
            val directoryGuess = if (pathSegments.size > 1) {
                pathSegments[pathSegments.size - 2]
            } else {
                "Internal Storage"
            }

            onProgress(
                ImportProgress(
                    current = index + 1,
                    total = total,
                    currentFileName = fileName
                )
            )

            val opId = UUID.randomUUID().toString()
            val opType = if (isMove) OperationJournalEntity.TYPE_IMPORT_MOVE else OperationJournalEntity.TYPE_IMPORT_COPY
            val journalEntry = OperationJournalEntity(
                operationId = opId,
                operationType = opType,
                sourceUri = sourceUri.toString(),
                fileSizeBytes = sourceSize,
                stage = OperationJournalEntity.STAGE_INITIALIZED
            )
            operationJournalDao.insert(journalEntry)

            var newDoc: DocumentFile? = null
            try {
                newDoc = targetDir.createFile(mimeType, fileName)
                if (newDoc == null) {
                    operationJournalDao.updateStage(
                        operationId = opId,
                        stage = OperationJournalEntity.STAGE_FAILED,
                        bytesTransferred = 0L,
                        sanitizedError = "Failed to create destination file in vault folder"
                    )
                    continue
                }

                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_COPYING,
                    bytesTransferred = 0L
                )

                var bytesCopied: Long = 0
                context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                    context.contentResolver.openOutputStream(newDoc.uri)?.use { output: OutputStream ->
                        bytesCopied = input.copyTo(output)
                        output.flush()
                    }
                }

                // Stage: VERIFYING (Spec 14.5: size verify before finalizing or deleting source)
                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_VERIFYING,
                    bytesTransferred = bytesCopied
                )

                val verificationPassed = if (sourceSize > 0L) {
                    bytesCopied == sourceSize
                } else {
                    bytesCopied > 0L
                }

                if (!verificationPassed) {
                    // Verification failed: Remove partial destination, never touch source!
                    try { newDoc.delete() } catch (_: Exception) {}
                    operationJournalDao.updateStage(
                        operationId = opId,
                        stage = OperationJournalEntity.STAGE_FAILED,
                        bytesTransferred = bytesCopied,
                        sanitizedError = "Destination size mismatch verification failed. Source preserved."
                    )
                    continue
                }

                // Source deletion ONLY after destination is verified (Spec 14.5, PV-STO-002)
                if (isMove) {
                    try {
                        DocumentsContract.deleteDocument(context.contentResolver, sourceUri)
                    } catch (e: Exception) {
                        try {
                            val singleDoc = DocumentFile.fromSingleUri(context, sourceUri)
                            singleDoc?.delete()
                        } catch (e2: Exception) {
                            // Source may be read-only
                        }
                    }
                    operationJournalDao.updateStage(
                        operationId = opId,
                        stage = OperationJournalEntity.STAGE_SOURCE_DELETED,
                        bytesTransferred = bytesCopied
                    )
                }

                val mediaType = MediaType.fromMimeType(mimeType)
                val item = VaultMediaItem(
                    uriString = newDoc.uri.toString(),
                    originalPathOrUri = sourceUri.toString(),
                    originalDirectory = directoryGuess,
                    fileName = newDoc.name ?: fileName,
                    mimeType = mimeType,
                    mediaType = mediaType.name,
                    fileSizeBytes = bytesCopied,
                    dateAdded = System.currentTimeMillis()
                )
                mediaDao.insert(item)

                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_COMPLETED,
                    bytesTransferred = bytesCopied
                )
                successCount++
            } catch (e: Exception) {
                // Keep source, record sanitized error
                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_FAILED,
                    bytesTransferred = 0L,
                    sanitizedError = e.javaClass.simpleName
                )
            }
        }

        onProgress(
            ImportProgress(
                current = total,
                total = total,
                currentFileName = "",
                isFinished = true
            )
        )
        successCount
    }

    suspend fun restoreOrExportMedia(
        item: VaultMediaItem,
        destinationTreeUri: Uri,
        deleteFromVault: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        val opId = UUID.randomUUID().toString()
        val opType = if (deleteFromVault) OperationJournalEntity.TYPE_RESTORE else OperationJournalEntity.TYPE_EXPORT
        val journalEntry = OperationJournalEntity(
            operationId = opId,
            operationType = opType,
            sourceUri = item.uriString,
            fileSizeBytes = item.fileSizeBytes,
            stage = OperationJournalEntity.STAGE_INITIALIZED
        )
        operationJournalDao.insert(journalEntry)

        try {
            val destDir = DocumentFile.fromTreeUri(context, destinationTreeUri) ?: return@withContext false
            val newFile = destDir.createFile(item.mimeType, item.fileName) ?: return@withContext false

            operationJournalDao.updateStage(
                operationId = opId,
                stage = OperationJournalEntity.STAGE_COPYING,
                bytesTransferred = 0L
            )

            val sourceUri = Uri.parse(item.uriString)
            var bytesCopied = 0L
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    bytesCopied = input.copyTo(output)
                    output.flush()
                }
            }

            // Verify before deleting from vault if restore was requested
            val verified = if (item.fileSizeBytes > 0L) bytesCopied == item.fileSizeBytes else bytesCopied > 0L
            if (!verified) {
                try { newFile.delete() } catch (_: Exception) {}
                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_FAILED,
                    bytesTransferred = bytesCopied,
                    sanitizedError = "Export verification failed"
                )
                return@withContext false
            }

            if (deleteFromVault) {
                deleteMedia(item)
                operationJournalDao.updateStage(
                    operationId = opId,
                    stage = OperationJournalEntity.STAGE_SOURCE_DELETED,
                    bytesTransferred = bytesCopied
                )
            }

            operationJournalDao.updateStage(
                operationId = opId,
                stage = OperationJournalEntity.STAGE_COMPLETED,
                bytesTransferred = bytesCopied
            )
            true
        } catch (e: Exception) {
            operationJournalDao.updateStage(
                operationId = opId,
                stage = OperationJournalEntity.STAGE_FAILED,
                bytesTransferred = 0L,
                sanitizedError = e.javaClass.simpleName
            )
            false
        }
    }

    /**
     * Strictly read-only scan of vault folder per Spec 2.1 Conflict Resolution #2 & Spec 14.8.
     * Never creates or modifies files.
     */
    suspend fun syncVaultFolder(): Int = withContext(Dispatchers.IO) {
        val folderUri = getVaultFolderUri() ?: return@withContext 0
        val dir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext 0
        if (!dir.exists() || !dir.isDirectory) return@withContext 0

        val files = dir.listFiles()
        var newCount = 0
        for (file in files) {
            val name = file.name ?: continue
            if (name == ".nomedia" || file.isDirectory) continue

            val existing = mediaDao.getMediaByUri(file.uri.toString())
            if (existing == null) {
                val mime = file.type ?: getMimeTypeFromExtension(name)
                val type = MediaType.fromMimeType(mime)
                val item = VaultMediaItem(
                    uriString = file.uri.toString(),
                    originalPathOrUri = "",
                    originalDirectory = "Secured Vault",
                    fileName = name,
                    mimeType = mime,
                    mediaType = type.name,
                    fileSizeBytes = file.length(),
                    dateAdded = file.lastModified()
                )
                mediaDao.insert(item)
                newCount++
            }
        }
        newCount
    }

    /**
     * Reconciles any unfinished operations from process death or unexpected interruption.
     * Fulfills Spec 14.6 & PV-STO-003.
     */
    suspend fun reconcilePendingOperations(): Int = withContext(Dispatchers.IO) {
        val pending = operationJournalDao.getPendingOperations()
        var reconciledCount = 0
        for (op in pending) {
            when (op.stage) {
                OperationJournalEntity.STAGE_INITIALIZED,
                OperationJournalEntity.STAGE_COPYING -> {
                    // Interrupted before destination was verified:
                    // Source was not deleted. Mark failed for audit and preserve source.
                    operationJournalDao.updateStage(
                        operationId = op.operationId,
                        stage = OperationJournalEntity.STAGE_FAILED,
                        bytesTransferred = op.bytesTransferred,
                        sanitizedError = "Interrupted transfer before verification. Source preserved."
                    )
                    reconciledCount++
                }
                OperationJournalEntity.STAGE_VERIFYING,
                OperationJournalEntity.STAGE_SOURCE_DELETED -> {
                    // Destination verification or move reached; mark completed.
                    operationJournalDao.updateStage(
                        operationId = op.operationId,
                        stage = OperationJournalEntity.STAGE_COMPLETED,
                        bytesTransferred = op.bytesTransferred,
                        sanitizedError = null
                    )
                    reconciledCount++
                }
            }
        }
        reconciledCount
    }

    suspend fun safeResetDatabase() = withContext(Dispatchers.IO) {
        mediaDao.clearAll()
        securityManager.safeResetAuth()
    }

    val allFolders: Flow<List<String>> = mediaDao.getAllFolders()

    fun getMediaByFolder(folderName: String): Flow<List<VaultMediaItem>> = mediaDao.getMediaByFolder(folderName)

    fun getDocumentsAndNotes(): Flow<List<VaultMediaItem>> = mediaDao.getDocumentsAndNotes()

    suspend fun saveNote(title: String, content: String, folderName: String = "Default"): Long = withContext(Dispatchers.IO) {
        val fileName = if (title.endsWith(".txt") || title.endsWith(".md")) title else "$title.txt"
        val noteItem = VaultMediaItem(
            uriString = "note://${System.currentTimeMillis()}",
            fileName = fileName,
            mimeType = "text/plain",
            mediaType = MediaType.NOTE.name,
            fileSizeBytes = content.toByteArray().size.toLong(),
            dateAdded = System.currentTimeMillis(),
            folderName = folderName,
            noteContent = content
        )
        mediaDao.insert(noteItem)
    }

    suspend fun updateNote(id: Long, title: String, content: String) = withContext(Dispatchers.IO) {
        val existing = mediaDao.getMediaById(id) ?: return@withContext
        val fileName = if (title.endsWith(".txt") || title.endsWith(".md")) title else "$title.txt"
        val updated = existing.copy(
            fileName = fileName,
            noteContent = content,
            fileSizeBytes = content.toByteArray().size.toLong()
        )
        mediaDao.update(updated)
    }

    suspend fun batchMoveToFolder(ids: List<Long>, folderName: String) = withContext(Dispatchers.IO) {
        mediaDao.updateFolderForIds(ids, folderName)
    }

    suspend fun batchSetFavorite(ids: List<Long>, isFav: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.updateFavoriteForIds(ids, isFav)
    }

    private fun getFileNameAndSize(uri: Uri): Pair<String?, Long> {
        var name: String? = null
        var size: Long = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex)
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            name = uri.lastPathSegment
        }
        return Pair(name, size)
    }

    private fun getMimeTypeFromExtension(name: String): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(name)
        if (!extension.isNullOrEmpty()) {
            val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
            if (type != null) return type
        }
        return "application/octet-stream"
    }
}
