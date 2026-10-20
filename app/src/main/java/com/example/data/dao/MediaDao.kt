package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultMediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Query("SELECT * FROM vault_media ORDER BY dateAdded DESC")
    fun getAllMedia(): Flow<List<VaultMediaItem>>

    @Query("SELECT * FROM vault_media WHERE mediaType = :type ORDER BY dateAdded DESC")
    fun getMediaByType(type: String): Flow<List<VaultMediaItem>>

    @Query("SELECT * FROM vault_media WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteMedia(): Flow<List<VaultMediaItem>>

    @Query("SELECT * FROM vault_media WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: Long): VaultMediaItem?

    @Query("SELECT * FROM vault_media WHERE uriString = :uriString LIMIT 1")
    suspend fun getMediaByUri(uriString: String): VaultMediaItem?

    @Query("SELECT * FROM vault_media WHERE fileName LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    fun searchMedia(query: String): Flow<List<VaultMediaItem>>

    @Query("SELECT COUNT(*) FROM vault_media WHERE mediaType = :type")
    fun countByType(type: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VaultMediaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VaultMediaItem>)

    @Update
    suspend fun update(item: VaultMediaItem)

    @Query("UPDATE vault_media SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Delete
    suspend fun delete(item: VaultMediaItem)

    @Query("DELETE FROM vault_media WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vault_media WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT DISTINCT folderName FROM vault_media WHERE folderName != '' ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM vault_media WHERE folderName = :folderName ORDER BY dateAdded DESC")
    fun getMediaByFolder(folderName: String): Flow<List<VaultMediaItem>>

    @Query("SELECT * FROM vault_media WHERE mediaType IN ('DOCUMENT', 'NOTE') ORDER BY dateAdded DESC")
    fun getDocumentsAndNotes(): Flow<List<VaultMediaItem>>

    @Query("UPDATE vault_media SET folderName = :folderName WHERE id IN (:ids)")
    suspend fun updateFolderForIds(ids: List<Long>, folderName: String)

    @Query("UPDATE vault_media SET isFavorite = :isFav WHERE id IN (:ids)")
    suspend fun updateFavoriteForIds(ids: List<Long>, isFav: Boolean)

    @Query("DELETE FROM vault_media")
    suspend fun clearAll()
}
