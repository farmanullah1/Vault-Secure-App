package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entry points where an authentication failure can occur.
 * Conforms to Master Build Spec v3 (Section 32.1 & 32.3).
 */
enum class IntruderEntryPoint {
    COVER,
    SETTINGS,
    DELETE,
    EXPORT,
    DECOY,
    BIOMETRIC
}

/**
 * Room entity storing intruder attempt events and captured front-camera photos.
 * Stored in private vault database and never exposed to the public Android MediaStore.
 */
@Entity(tableName = "intruder_events")
data class IntruderEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val attemptNumber: Int = 1,
    val entryPoint: String = IntruderEntryPoint.COVER.name,
    val photoUri: String? = null,
    val seen: Boolean = false,
    val deviceLockedState: Boolean = false
)
