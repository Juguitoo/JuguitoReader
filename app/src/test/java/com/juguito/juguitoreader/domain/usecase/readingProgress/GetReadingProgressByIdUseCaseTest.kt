package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetReadingProgressByIdUseCaseTest {

    private lateinit var useCase: GetReadingProgressByIdUseCase
    private val repository = mockk<ReadingProgressRepository>()

    @Before
    fun setup() {
        useCase = GetReadingProgressByIdUseCase(repository)
    }

    @Test
    fun `invoke returns progress from repository`() = runTest {
        coEvery { repository.getReadingProgressById(1) } returns null
        
        val result = useCase(1)
        
        assertThat(result).isNull()
    }
}
