package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetGenresUseCaseTest {

    private lateinit var useCase: GetGenresUseCase
    private val repository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = GetGenresUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getAllGenres() } returns flowOf(emptyList())
        
        val result = useCase().first()
        
        assertThat(result).isEmpty()
    }
}
