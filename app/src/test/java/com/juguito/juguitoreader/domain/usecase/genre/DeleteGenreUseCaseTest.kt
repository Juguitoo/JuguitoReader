package com.juguito.juguitoreader.domain.usecase.genre

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteGenreUseCaseTest {

    private lateinit var useCase: DeleteGenreUseCase
    private val repository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = DeleteGenreUseCase(repository)
    }

    @Test
    fun `invoke calls delete on repository`() = runTest {
        coEvery { repository.deleteGenre(1) } returns Unit
        val result = useCase(1)
        assertThat(result.isSuccess).isTrue()
    }
}
