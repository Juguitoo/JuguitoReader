package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.FolderEntity
import com.juguito.juguitoreader.data.local.entity.FolderWithCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("SELECT * FROM folders WHERE id = :idFolder")
    suspend fun getFolderById(idFolder: Int): FolderEntity?

    @Query("SELECT * FROM folders WHERE name = :folderName")
    suspend fun getFolderByName(folderName: String): FolderEntity?

    @Query("""
        SELECT 
            f.*, 
            (SELECT COUNT(*) FROM book_folders bg WHERE bg.folder_id = f.id) AS bookCount
        FROM folders f
        ORDER BY f.name ASC
    """)
    fun getFoldersWithBookCount(): Flow<List<FolderWithCountEntity>>

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Int)

    @Query("SELECT * FROM folders WHERE sync_status != 'SYNCED'")
    suspend fun getUnsyncedFolders(): List<FolderEntity>
}