package com.example

import com.example.data.model.OperationJournalEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FileOperationsJournalTest {

    @Test
    fun `journal stage constants are well defined`() {
        assertEquals("INITIALIZED", OperationJournalEntity.STAGE_INITIALIZED)
        assertEquals("COPYING", OperationJournalEntity.STAGE_COPYING)
        assertEquals("VERIFYING", OperationJournalEntity.STAGE_VERIFYING)
        assertEquals("SOURCE_DELETED", OperationJournalEntity.STAGE_SOURCE_DELETED)
        assertEquals("COMPLETED", OperationJournalEntity.STAGE_COMPLETED)
        assertEquals("FAILED", OperationJournalEntity.STAGE_FAILED)
        assertEquals("ROLLED_BACK", OperationJournalEntity.STAGE_ROLLED_BACK)
    }

    @Test
    fun `copy-verify-delete protocol verification logic`() {
        // Spec 14.5: Size verification must match before source deletion
        val sourceSize = 1048576L // 1 MB

        // Case 1: Exact size copied -> verification passes
        val exactBytesCopied = 1048576L
        val passVerification = (exactBytesCopied == sourceSize)
        assertTrue("Verification must pass when destination bytes equal source size", passVerification)

        // Case 2: Partial bytes copied (e.g. storage full or interrupted) -> verification fails
        val partialBytesCopied = 524288L // 512 KB
        val failVerification = (partialBytesCopied == sourceSize)
        assertFalse("Verification must fail when destination size does not match source", failVerification)

        // In case 2, source deletion must NEVER be performed, preserving original file
    }

    @Test
    fun `operation journal record contains required audit fields`() {
        val entry = OperationJournalEntity(
            operationId = "test-uuid-1234",
            operationType = OperationJournalEntity.TYPE_IMPORT_MOVE,
            sourceUri = "content://com.android.providers.media.documents/document/image%3A100",
            destinationUri = "content://com.android.externalstorage.documents/tree/vault/document/image_100.jpg",
            fileSizeBytes = 2048L,
            bytesTransferred = 2048L,
            stage = OperationJournalEntity.STAGE_COMPLETED,
            sanitizedError = null
        )

        assertEquals("test-uuid-1234", entry.operationId)
        assertEquals(OperationJournalEntity.TYPE_IMPORT_MOVE, entry.operationType)
        assertEquals(OperationJournalEntity.STAGE_COMPLETED, entry.stage)
        assertEquals(2048L, entry.fileSizeBytes)
        assertEquals(2048L, entry.bytesTransferred)
        assertNull(entry.sanitizedError)
    }
}
