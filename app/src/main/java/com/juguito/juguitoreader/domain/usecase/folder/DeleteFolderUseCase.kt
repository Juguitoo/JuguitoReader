package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
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
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_folder_error))
        }
    }
}
