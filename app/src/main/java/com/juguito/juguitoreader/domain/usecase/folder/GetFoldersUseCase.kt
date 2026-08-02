package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoldersUseCase @Inject constructor(
    private val repository: FolderRepository
) {

    operator fun invoke(): Flow<List<Folder>> {
        return repository.getAllFolders()
    }
}