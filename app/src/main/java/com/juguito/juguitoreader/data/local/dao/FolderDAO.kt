package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("SELECT * FROM folders WHERE id = :idFolder")
    suspend fun getFolderById(idFolder: Int): FolderEntity?

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Int)

    @Query("SELECT * FROM folders WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedFolders(): List<FolderEntity>
}