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
        return genreDAO.getGenresWithBookCount().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getAllGenreNames(): List<String> {
        return genreDAO.getAllGenreNames()
    }

    override suspend fun getGenreById(id: Int): Genre? {
        return genreDAO.getGenreById(id)?.toDomain()
    }

    override suspend fun getGenreByName(name: String): Genre? {
        return genreDAO.getGenreByName(name)?.toDomain()
    }

    override suspend fun insertGenre(genre: Genre): Long {
        return genreDAO.insertGenre(genre.toEntity())
    }

    override suspend fun insertGenres(genres: List<Genre>) {
        genreDAO.insertGenres(genres.map { it.toEntity() })
    }

    override suspend fun updateGenre(genre: Genre) {
        genreDAO.updateGenre(genre.toEntity())
    }

    override suspend fun updateGenres(genres: List<Genre>) {
        genreDAO.updateGenres(genres.map { it.toEntity() })
    }

    override suspend fun deleteGenre(id: Int) {
        genreDAO.deleteGenreById(id)
    }
}