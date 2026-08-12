package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import javax.inject.Inject

class UpdateFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folder: Folder): Result<Unit> {
        if (folder.name.isBlank()){
            return Result.failure(Exception("El nombre de la carpeta no puede estar vacío."))
        }

        val existingFolder = repository.getFolderByName(folder.name)
        if (existingFolder != null && existingFolder.id != folder.id) {
            return Result.failure(Exception("Ya existe otra carpeta llamada '${folder.name}'."))
        }

        return try {
            repository.saveFolder(folder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar la carpeta: ${e.localizedMessage}"))
        }
    }
}
