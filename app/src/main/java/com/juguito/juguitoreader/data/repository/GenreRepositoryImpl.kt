package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.GenreDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GenreRepositoryImpl @Inject constructor(
    private val genreDAO: GenreDAO
): GenreRepository {
    override fun getAllGenres(): Flow<List<Genre>> {
        return genreDAO.getAllGenres().map{ entities -> entities.map{ it.toDomain() } }
    }

    override suspend fun saveGenre(genre: Genre) {
        genreDAO.insertGenre(genre.toEntity())
    }

    override suspend fun saveGenres(genres: List<Genre>) {
        genreDAO.insertGenres(genres.map { it.toEntity() })
    }

    override suspend fun deleteGenre(id: Int) {
        genreDAO.deleteGenreById(id)
    }

    override suspend fun syncPendingGenres() {
        TODO("Not yet implemented")
    }


}