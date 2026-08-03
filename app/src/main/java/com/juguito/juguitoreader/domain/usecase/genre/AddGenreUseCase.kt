package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class AddGenreUseCase @Inject constructor(
    private val repository: GenreRepository
) {

    suspend operator fun invoke(genre: Genre): Result<Unit> {
        if (genre.name.isBlank()) {
            return Result.failure(Exception("El nombre del género no puede estar vacio."))
        }

        val existingGenre = repository.getGenreByName(genre.name)
        if (existingGenre != null) {
            return Result.failure(Exception("Ya existe un género llamado '${genre.name}'."))
        }

        return try {
            repository.saveGenre(genre)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error al guardar el género: ${e.localizedMessage}"))
        }
    }
}