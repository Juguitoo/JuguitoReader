package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteReadingProgressUseCaseTest {

    private lateinit var useCase: DeleteReadingProgressUseCase
    private val repository = mockk<ReadingProgressRepository>()

    @Before
    fun setup() {
        useCase = DeleteReadingProgressUseCase(repository)
    }

    @Test
    fun `invoke calls delete on repository`() = runTest {
        coEvery { repository.deleteProgress(1) } returns Unit
        val result = useCase(1)
        assertThat(result.isSuccess).isTrue()
    }
}
