package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
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
    fun `invoke with valid genre calls save`() = runBlocking {
        val genre = Genre(name = "Fantasy")
        coEvery { repository.getGenreByName("Fantasy") } returns null
        coEvery { repository.saveGenre(any()) } returns 1L
        
        val result = useCase(genre)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.saveGenre(any()) }
    }
}
