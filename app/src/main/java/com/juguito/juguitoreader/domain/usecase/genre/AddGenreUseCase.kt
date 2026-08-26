package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class AddGenreUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    suspend operator fun invoke(genre: Genre): Result<Unit> {
        if (genre.name.isBlank()) {
            return Result.failure(JuguitoException(R.string.error_genre_empty))
        }

        val existingGenre = repository.getGenreByName(genre.name)
        if (existingGenre != null) {
            return Result.failure(JuguitoException(R.string.error_genre_exists, genre.name))
        }

        return try {
            repository.saveGenre(genre)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.something_went_wrong))
        }
    }
}
