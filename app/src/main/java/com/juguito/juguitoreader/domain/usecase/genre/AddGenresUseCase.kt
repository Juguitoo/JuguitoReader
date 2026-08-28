package com.juguito.juguitoreader.domain.usecase.genre

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import com.juguito.juguitoreader.ui.common.UiText
import javax.inject.Inject

class AddGenresUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    private val validNameRegex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9 \\-&']+$".toRegex()

    suspend operator fun invoke(genres: List<Genre>): AddGenresResult {
        if (genres.isEmpty()) return AddGenresResult.Success

        val validGenres = mutableListOf<Genre>()
        val skippedGenres = mutableListOf<String>()
        val existingGenreNames = repository.getAllGenreNames().map { it.lowercase() }.toSet()

        genres.forEach { genre ->
            val isFormatValid = genre.name.isNotBlank() && genre.name.matches(validNameRegex)
            val isAlreadySaved = existingGenreNames.contains(genre.name.lowercase())

            if (isFormatValid && !isAlreadySaved) {
                validGenres.add(genre)
            } else {
                skippedGenres.add(genre.name)
            }
        }

        if (validGenres.isEmpty() && skippedGenres.isNotEmpty()) {
            return AddGenresResult.Error(UiText.StringResource(R.string.something_went_wrong))
        }

        return try {
            repository.insertGenres(validGenres)

            if (skippedGenres.isNotEmpty()) {
                AddGenresResult.PartialSuccess(skippedGenres)
            } else {
                AddGenresResult.Success
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AddGenresResult.Error(UiText.StringResource(R.string.something_went_wrong))
        }
    }
}

sealed interface AddGenresResult {
    data object Success : AddGenresResult
    data class PartialSuccess(val skippedGenres: List<String>) : AddGenresResult
    data class Error(val message: UiText) : AddGenresResult
}
