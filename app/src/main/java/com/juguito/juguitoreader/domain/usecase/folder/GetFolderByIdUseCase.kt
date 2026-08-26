package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import javax.inject.Inject

class GetFolderByIdUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folderId: Int): Folder? {
        return repository.getFolderById(folderId)
    }
}