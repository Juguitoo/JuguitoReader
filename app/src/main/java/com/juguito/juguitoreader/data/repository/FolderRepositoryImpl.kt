package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.FolderDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDAO: FolderDAO
): FolderRepository {
    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDAO.getAllFolders().map{ entities -> entities.map{ it.toDomain() } }
    }

    override suspend fun getFolderById(id: Int): Folder? {
        return folderDAO.getFolderById(id)?.toDomain()
    }

    override suspend fun getFolderByName(name: String): Folder? {
        return folderDAO.getFolderByName(name)?.toDomain()
    }

    override suspend fun saveFolder(folder: Folder) {
        folderDAO.insertFolder(folder.toEntity())
    }

    override suspend fun saveFolders(folders: List<Folder>) {
        folderDAO.insertFolders(folders.map{ it.toEntity() })
    }

    override suspend fun deleteFolder(id: Int) {
        folderDAO.deleteFolderById(id)
    }

    override suspend fun syncPendingFolders() {
        TODO("Not yet implemented")
    }
}