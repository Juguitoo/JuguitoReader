package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
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
    fun `invoke with valid data returns success`() = runTest {
        val genre = Genre(id = 1, name = "G1")
        coEvery { repository.getGenreByName("G1") } returns genre
        coEvery { repository.saveGenre(any()) } returns 1L
        val result = useCase(genre)
        assertThat(result.isSuccess).isTrue()
    }
}
