package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class UpdateGenreUseCase @Inject constructor(
    private val repository: GenreRepository
) {

    suspend operator fun invoke(genre: Genre): Result<Unit> {
        if (genre.name.isBlank()){
            return Result.failure(Exception("El nombre del género no puede estar vacío."))
        }

        val existingGenre = repository.getGenreByName(genre.name)
        if (existingGenre != null && existingGenre.id != genre.id) {
            return Result.failure(Exception("Ya existe otro género llamado '${genre.name}'."))
        }

        return try {
            repository.saveGenre(genre)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar el género: ${e.localizedMessage}"))
        }
    }
}
