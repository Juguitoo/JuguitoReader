package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.domain.repository.FolderRepository
import javax.inject.Inject

class DeleteFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folderId: Int): Result<Unit>{
        return try{
            repository.deleteFolder(folderId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al borrar la carpeta: ${e.localizedMessage}"))
        }
    }
}