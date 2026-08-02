package com.juguito.juguitoreader.domain.usecase.folder

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.repository.FolderRepository
import javax.inject.Inject

class AddFolderUseCase @Inject constructor(
    private val repository: FolderRepository
) {

    suspend operator fun invoke(folder: Folder): Result<Unit> {
        if (folder.name.isBlank()){
            return Result.failure(Exception("El nombre de la carpeta no puede estar vacio."))
        } else if(folder.colorHex.isBlank()){
            return Result.failure(Exception("El color de la carpeta no puede estar vacio."))
        }

        return try{
            repository.saveFolder(folder)
            Result.success(Unit)
        } catch (e: Exception){
            return Result.failure(Exception("Error al guardar la carpeta: ${e.localizedMessage}"))
        }

    }
}