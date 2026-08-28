package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import javax.inject.Inject

class UpdateFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder): Result<Unit> {
        if (folder.name.isBlank()){
            return Result.failure(JuguitoException(R.string.name_empty_error))
        }

        val existingFolder = repository.getFolderByName(folder.name)
        if (existingFolder != null && existingFolder.id != folder.id) {
            return Result.failure(JuguitoException(R.string.error_folder_exists, folder.name))
        }

        return try {
            repository.updateFolder(folder)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_save_folder))
        }
    }
}

