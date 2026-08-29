package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {

    fun getAllGenres(): Flow<List<Genre>>

    suspend fun getAllGenreNames(): List<String>

    suspend fun getGenreById(id: Int): Genre?

    suspend fun getGenreByName(name: String): Genre?

    suspend fun insertGenre(genre: Genre): Long

    suspend fun insertGenres(genres: List<Genre>)

    suspend fun updateGenre(genre: Genre)

    suspend fun updateGenres(genres: List<Genre>)

    suspend fun deleteGenre(id: Int)

    suspend fun syncPendingGenres()
}