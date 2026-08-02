package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.Genre
import kotlinx.coroutines.flow.Flow

interface GenreRepository {

    fun getAllGenres(): Flow<List<Genre>>

    suspend fun saveGenre(genre: Genre)

    suspend fun saveGenres(genres: List<Genre>)

    suspend fun deleteGenre(id: Int)

    suspend fun syncPendingGenres()
}