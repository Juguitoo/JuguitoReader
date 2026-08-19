package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetReadingProgressesUseCaseTest {

    private lateinit var useCase: GetReadingProgressesUseCase
    private val repository = mockk<ReadingProgressRepository>()

    @Before
    fun setup() {
        useCase = GetReadingProgressesUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getAllReadingProgress() } returns flowOf(emptyList())
        val result = useCase().first()
        assertThat(result).isEmpty()
    }
}
