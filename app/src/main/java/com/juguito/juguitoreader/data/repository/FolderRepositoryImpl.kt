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
        return folderDAO.getFoldersWithBookCount().map { list ->
            list.map { it.toDomain() }
        }
    }
    override suspend fun getFolderById(id: Int): Folder? {
        return folderDAO.getFolderById(id)?.toDomain()
    }

    override suspend fun getFolderByName(name: String): Folder? {
        return folderDAO.getFolderByName(name)?.toDomain()
    }

    override suspend fun insertFolder(folder: Folder): Long {
        return folderDAO.insertFolder(folder.toEntity())
    }

    override suspend fun insertFolders(folders: List<Folder>) {
        folderDAO.insertFolders(folders.map{ it.toEntity() })
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDAO.updateFolder(folder.toEntity())
    }

    override suspend fun updateFolders(folders: List<Folder>) {
        folderDAO.updateFolders(folders.map { it.toEntity() })
    }

    override suspend fun deleteFolder(id: Int) {
        folderDAO.deleteFolderById(id)
    }
}