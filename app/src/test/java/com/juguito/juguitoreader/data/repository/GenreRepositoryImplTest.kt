package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.dao.GenreDAO
import com.juguito.juguitoreader.data.local.entity.GenreEntity
import com.juguito.juguitoreader.data.local.entity.GenreWithCountEntity
import com.juguito.juguitoreader.domain.model.Genre
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GenreRepositoryImplTest {

    private lateinit var repository: GenreRepositoryImpl
    private val genreDAO = mockk<GenreDAO>()

    @Before
    fun setup() {
        repository = GenreRepositoryImpl(genreDAO)
    }

    @Test
    fun `getAllGenres maps entities to domain`() = runTest {
        val genreEntity = GenreEntity(id = 1, name = "Fantasy", createdAt = 0L)
        val genreWithCount = GenreWithCountEntity(genre = genreEntity, bookCount = 5)

        every { genreDAO.getGenresWithBookCount() } returns flowOf(listOf(genreWithCount))

        val result = repository.getAllGenres().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Fantasy")
    }

    @Test
    fun `getGenreByName returns domain genre`() = runTest {
        val genreEntity = GenreEntity(id = 1, name = "Sci-Fi", createdAt = 0L)
        coEvery { genreDAO.getGenreByName("Sci-Fi") } returns genreEntity

        val result = repository.getGenreByName("Sci-Fi")

        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Sci-Fi")
    }

    @Test
    fun `getAllGenreNames returns list of names`() = runTest {
        every { genreDAO.getAllGenreNames() } returns listOf("A", "B")

        val result = repository.getAllGenreNames()

        assertThat(result).containsExactly("A", "B")
    }

    @Test
    fun `insertGenre calls insertGenre on DAO`() = runTest {
        val genre = Genre(name = "Horror")
        coEvery { genreDAO.insertGenre(any()) } returns 1L

        val id = repository.insertGenre(genre)

        assertThat(id).isEqualTo(1L)
        coVerify { genreDAO.insertGenre(any()) }
    }

    @Test
    fun `insertGenres calls insertGenres on DAO`() = runTest {
        val genres = listOf(Genre(name = "G1"))
        coEvery { genreDAO.insertGenres(any()) } returns Unit

        repository.insertGenres(genres)

        coVerify { genreDAO.insertGenres(any()) }
    }

    @Test
    fun `updateGenre calls updateGenre on DAO`() = runTest {
        val genre = Genre(id = 1, name = "Renamed")
        coEvery { genreDAO.updateGenre(any()) } returns Unit

        repository.updateGenre(genre)

        coVerify { genreDAO.updateGenre(any()) }
    }

    @Test
    fun `deleteGenre calls deleteGenreById on DAO`() = runTest {
        coEvery { genreDAO.deleteGenreById(1) } returns Unit

        repository.deleteGenre(1)

        coVerify { genreDAO.deleteGenreById(1) }
    }
}
