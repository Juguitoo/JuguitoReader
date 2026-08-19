package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddGenresUseCaseTest {

    private lateinit var useCase: AddGenresUseCase
    private val repository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = AddGenresUseCase(repository)
    }

    @Test
    fun `invoke with empty list returns Success`() = runTest {
        val result = useCase(emptyList())
        assertThat(result).isEqualTo(AddGenresResult.Success)
    }

    @Test
    fun `invoke filters duplicates and invalid names`() = runTest {
        val genres = listOf(
            Genre(name = "Action"), // Valid
            Genre(name = "Action"), // Duplicate
            Genre(name = "")        // Invalid
        )
        coEvery { repository.getAllGenreNames() } returns listOf("Action")
        
        val result = useCase(genres)
        
        assertThat(result).isInstanceOf(AddGenresResult.Error::class.java)
        coVerify(exactly = 0) { repository.saveGenres(any()) }
    }

    @Test
    fun `invoke with new valid genres returns Success`() = runTest {
        val genres = listOf(Genre(name = "Horror"))
        coEvery { repository.getAllGenreNames() } returns emptyList()
        coEvery { repository.saveGenres(any()) } returns Unit
        
        val result = useCase(genres)
        
        assertThat(result).isEqualTo(AddGenresResult.Success)
        coVerify { repository.saveGenres(any()) }
    }
}
