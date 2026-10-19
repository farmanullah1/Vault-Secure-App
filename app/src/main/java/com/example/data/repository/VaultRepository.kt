package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.example.data.dao.MediaDao
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.security.PinSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

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
    private val securityManager: PinSecurityManager
) {

    val allMedia: Flow<List<VaultMediaItem>> = mediaDao.getAllMedia()

    fun getMediaByType(type: String): Flow<List<VaultMediaItem>> {
        return mediaDao.getMediaByType(type)
    }

    suspend fun getMediaById(id: Long): VaultMediaItem? = withContext(Dispatchers.IO) {
        mediaDao.getMediaById(id)
    }

    suspend fun toggleFavorite(item: VaultMediaItem) = withContext(Dispatchers.IO) {
        mediaDao.updateFavorite(item.id, !item.isFavorite)
    }

    suspend fun deleteMedia(item: VaultMediaItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(item.uriString)
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: Exception) {
            try {
                DocumentFile.fromSingleUri(context, Uri.parse(item.uriString))?.delete()
            } catch (e2: Exception) {
                // Ignore
            }
        }
        mediaDao.delete(item)
        true
    }

    suspend fun deleteBatch(items: List<VaultMediaItem>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (item in items) {
            try {
                DocumentsContract.deleteDocument(context.contentResolver, Uri.parse(item.uriString))
            } catch (e: Exception) {
                try {
                    DocumentFile.fromSingleUri(context, Uri.parse(item.uriString))?.delete()
                } catch (e2: Exception) {
                    // Ignore
                }
            }
            mediaDao.delete(item)
            count++
        }
        count
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

        ensureNoMediaFile()

        var successCount = 0
        val total = sourceUris.size

        for ((index, sourceUri) in sourceUris.withIndex()) {
            val nameAndSize = getFileNameAndSize(sourceUri)
            val fileName = nameAndSize.first ?: "media_${System.currentTimeMillis()}"
            val sourceSize = nameAndSize.second
            val mimeType = context.contentResolver.getType(sourceUri) ?: getMimeTypeFromExtension(fileName)

            // Extract original directory or path representation
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

            try {
                val newDoc = targetDir.createFile(mimeType, fileName) ?: continue

                var bytesCopied: Long = 0
                context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                    context.contentResolver.openOutputStream(newDoc.uri)?.use { output: OutputStream ->
                        bytesCopied = input.copyTo(output)
                    }
                }

                val mediaType = MediaType.fromMimeType(mimeType)
                val item = VaultMediaItem(
                    uriString = newDoc.uri.toString(),
                    originalPathOrUri = sourceUri.toString(),
                    originalDirectory = directoryGuess,
                    fileName = newDoc.name ?: fileName,
                    mimeType = mimeType,
                    mediaType = mediaType.name,
                    fileSizeBytes = if (bytesCopied > 0) bytesCopied else sourceSize,
                    dateAdded = System.currentTimeMillis()
                )
                mediaDao.insert(item)
                successCount++

                if (isMove && bytesCopied > 0) {
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        try {
            val destDir = DocumentFile.fromTreeUri(context, destinationTreeUri) ?: return@withContext false
            val newFile = destDir.createFile(item.mimeType, item.fileName) ?: return@withContext false

            val sourceUri = Uri.parse(item.uriString)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    input.copyTo(output)
                }
            }

            if (deleteFromVault) {
                deleteMedia(item)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncVaultFolder(): Int = withContext(Dispatchers.IO) {
        val folderUri = getVaultFolderUri() ?: return@withContext 0
        val dir = DocumentFile.fromTreeUri(context, folderUri) ?: return@withContext 0
        if (!dir.exists() || !dir.isDirectory) return@withContext 0

        ensureNoMediaFile()

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

    suspend fun safeResetDatabase() = withContext(Dispatchers.IO) {
        mediaDao.clearAll()
        securityManager.safeResetAuth()
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
