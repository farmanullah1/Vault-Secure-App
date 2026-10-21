package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.IntruderEventDao
import com.example.data.dao.MediaDao
import com.example.data.model.IntruderEventEntity
import com.example.data.model.VaultMediaItem

@Database(entities = [VaultMediaItem::class, IntruderEventEntity::class], version = 5, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {

    abstract fun mediaDao(): MediaDao
    abstract fun intruderEventDao(): IntruderEventDao

    companion object {
        @Volatile
        private var INSTANCE: VaultDatabase? = null

        fun getInstance(context: Context): VaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VaultDatabase::class.java,
                    "sim_vault_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
