package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import javax.inject.Inject

class AddGenresUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    private val validNameRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 \\-&']+$".toRegex()

    suspend operator fun invoke(genres: List<Genre>): AddGenresResult {
        if (genres.isEmpty()) return AddGenresResult.Success

        val validGenres = mutableListOf<Genre>()
        val skippedGenres = mutableListOf<String>()

        genres.forEach { genre ->
            if (genre.name.isNotBlank() && genre.name.matches(validNameRegex)) {
                validGenres.add(genre)
            } else {
                skippedGenres.add(genre.name)
            }
        }

        if (validGenres.isEmpty() && skippedGenres.isNotEmpty()) {
            return AddGenresResult.Error("Ningún género tenía un formato válido.")
        }

        return try {
            repository.saveGenres(validGenres)

            if (skippedGenres.isNotEmpty()) {
                AddGenresResult.PartialSuccess(skippedGenres)
            } else {
                AddGenresResult.Success
            }
        } catch (e: Exception) {
            AddGenresResult.Error("Error al guardar en la base de datos: ${e.localizedMessage}")
        }
    }
}

sealed interface AddGenresResult {
    data object Success : AddGenresResult
    data class PartialSuccess(val skippedGenres: List<String>) : AddGenresResult
    data class Error(val message: String) : AddGenresResult
}