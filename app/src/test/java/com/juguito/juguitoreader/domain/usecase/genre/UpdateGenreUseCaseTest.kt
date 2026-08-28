package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateGenreUseCaseTest {

    private lateinit var useCase: UpdateGenreUseCase
    private val repository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = UpdateGenreUseCase(repository)
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val result = useCase(Genre(id = 1, name = ""))
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `invoke with duplicate name from another genre returns failure`() = runTest {
        val genre = Genre(id = 1, name = "Taken")
        coEvery { repository.getGenreByName("Taken") } returns Genre(id = 2, name = "Taken")

        val result = useCase(genre)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_genre_exists)
    }

    @Test
    fun `invoke with valid data calls updateGenre and returns success`() = runTest {
        val genre = Genre(id = 1, name = "G1")
        coEvery { repository.getGenreByName("G1") } returns genre
        coEvery { repository.updateGenre(any()) } returns Unit

        val result = useCase(genre)

        assertThat(result.isSuccess).isTrue()
        coVerify { repository.updateGenre(genre) }
    }
}
