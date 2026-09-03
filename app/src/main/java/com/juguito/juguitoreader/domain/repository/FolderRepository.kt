package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getAllFolders(): Flow<List<Folder>>

    suspend fun getFolderById(id: Int): Folder?

    suspend fun getFolderByName(name: String): Folder?

    suspend fun insertFolder(folder: Folder): Long

    suspend fun insertFolders(folders: List<Folder>)

    suspend fun updateFolder(folder: Folder)

    suspend fun updateFolders(folders: List<Folder>)

    suspend fun deleteFolder(id: Int)
}