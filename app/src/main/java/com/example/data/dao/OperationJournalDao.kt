package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OperationJournalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for operation journals.
 * Fulfills Spec 14.6 (Operation journal & recovery).
 */
@Dao
interface OperationJournalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: OperationJournalEntity): Long

    @Update
    suspend fun update(journal: OperationJournalEntity)

    @Query("UPDATE operation_journal SET stage = :stage, bytesTransferred = :bytesTransferred, sanitizedError = :sanitizedError WHERE operationId = :operationId")
    suspend fun updateStage(
        operationId: String,
        stage: String,
        bytesTransferred: Long,
        sanitizedError: String? = null
    )

    @Query("SELECT * FROM operation_journal WHERE stage NOT IN ('COMPLETED', 'ROLLED_BACK') ORDER BY startTimeMs DESC")
    suspend fun getPendingOperations(): List<OperationJournalEntity>

    @Query("SELECT * FROM operation_journal WHERE operationId = :operationId LIMIT 1")
    suspend fun getByOperationId(operationId: String): OperationJournalEntity?

    @Query("SELECT * FROM operation_journal ORDER BY startTimeMs DESC LIMIT :limit")
    fun getRecentOperations(limit: Int = 50): Flow<List<OperationJournalEntity>>

    @Query("DELETE FROM operation_journal WHERE stage = 'COMPLETED' AND startTimeMs < :olderThanMs")
    suspend fun purgeOldCompleted(olderThanMs: Long)

    @Query("DELETE FROM operation_journal")
    suspend fun clearAll()
}
