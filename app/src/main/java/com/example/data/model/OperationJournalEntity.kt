package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted journal record for long-running or destructive file operations.
 * Fulfills Private Vault Master Build Specification (v4, merged) Section 14.6 & PV-STO-003.
 *
 * Tracks stages: INITIALIZED -> COPYING -> VERIFYING -> SOURCE_DELETED -> COMPLETED / FAILED / ROLLED_BACK.
 */
@Entity(tableName = "operation_journal")
data class OperationJournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationId: String,
    val operationType: String, // "IMPORT_COPY", "IMPORT_MOVE", "EXPORT", "RESTORE", "TRASH", "PERMANENT_DELETE"
    val sourceUri: String,
    val destinationUri: String? = null,
    val fileSizeBytes: Long = 0L,
    val bytesTransferred: Long = 0L,
    val startTimeMs: Long = System.currentTimeMillis(),
    val stage: String = STAGE_INITIALIZED,
    val sanitizedError: String? = null
) {
    companion object {
        const val STAGE_INITIALIZED = "INITIALIZED"
        const val STAGE_COPYING = "COPYING"
        const val STAGE_VERIFYING = "VERIFYING"
        const val STAGE_SOURCE_DELETED = "SOURCE_DELETED"
        const val STAGE_COMPLETED = "COMPLETED"
        const val STAGE_FAILED = "FAILED"
        const val STAGE_ROLLED_BACK = "ROLLED_BACK"

        const val TYPE_IMPORT_COPY = "IMPORT_COPY"
        const val TYPE_IMPORT_MOVE = "IMPORT_MOVE"
        const val TYPE_EXPORT = "EXPORT"
        const val TYPE_RESTORE = "RESTORE"
        const val TYPE_TRASH = "TRASH"
        const val TYPE_PERMANENT_DELETE = "PERMANENT_DELETE"
    }
}
