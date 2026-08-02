package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getAllFolders(): Flow<List<Folder>>

    suspend fun saveFolder(folder: Folder)

    suspend fun saveFolders(folders: List<Folder>)

    suspend fun deleteFolder(id: Int)

    suspend fun syncPendingFolders()
}