package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class DeleteGenreUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    suspend operator fun invoke(genreId: Int): Result<Unit> {
        return try{
            repository.deleteGenre(genreId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_genre_error))
        }
    }
}
