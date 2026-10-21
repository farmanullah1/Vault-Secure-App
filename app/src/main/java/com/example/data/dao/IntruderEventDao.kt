package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.IntruderEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Intruder Events.
 * Conforms to Master Build Spec v3 (Section 32.3).
 */
@Dao
interface IntruderEventDao {

    @Query("SELECT * FROM intruder_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IntruderEventEntity>>

    @Query("SELECT * FROM intruder_events WHERE seen = 0 ORDER BY timestamp DESC")
    fun getUnseenEvents(): Flow<List<IntruderEventEntity>>

    @Query("SELECT * FROM intruder_events WHERE seen = 0 ORDER BY timestamp DESC")
    suspend fun getUnseenEventsSync(): List<IntruderEventEntity>

    @Query("SELECT COUNT(*) FROM intruder_events WHERE seen = 0")
    fun getUnseenCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IntruderEventEntity): Long

    @Query("UPDATE intruder_events SET seen = 1 WHERE seen = 0")
    suspend fun markAllAsSeen()

    @Query("DELETE FROM intruder_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM intruder_events")
    suspend fun clearAll()

    @Query("DELETE FROM intruder_events WHERE id NOT IN (SELECT id FROM intruder_events ORDER BY timestamp DESC LIMIT :limit)")
    suspend fun pruneOldEvents(limit: Int)
}
