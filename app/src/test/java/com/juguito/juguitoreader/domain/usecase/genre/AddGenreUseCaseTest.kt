package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddGenreUseCaseTest {

    private lateinit var useCase: AddGenreUseCase
    private val repository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = AddGenreUseCase(repository)
    }

    @Test
    fun `invoke with blank name returns failure`() = runBlocking {
        val result = useCase(Genre(name = ""))

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_genre_empty)
    }

    @Test
    fun `invoke with existing name returns failure`() = runBlocking {
        val genre = Genre(name = "Fantasy")
        coEvery { repository.getGenreByName("Fantasy") } returns genre

        val result = useCase(genre)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_genre_exists)
    }

    @Test
    fun `invoke with valid genre calls insertGenre and returns success`() = runBlocking {
        val genre = Genre(name = "Fantasy")
        coEvery { repository.getGenreByName("Fantasy") } returns null
        coEvery { repository.insertGenre(any()) } returns 1L

        val result = useCase(genre)

        assertThat(result.isSuccess).isTrue()
        coVerify { repository.insertGenre(any()) }
    }
}
