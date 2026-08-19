package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class UpdateReadingProgressUseCaseTest {

    private lateinit var useCase: UpdateReadingProgressUseCase
    private val repository = mockk<ReadingProgressRepository>()

    @Before
    fun setup() {
        useCase = UpdateReadingProgressUseCase(repository)
    }

    @Test
    fun `invoke calls saveReadingProgress`() = runBlocking {
        val progress = ReadingProgress(bookId = 1, lastChapterIndex = 1, scrollPosition = 0.5f, lastReadAt = 100L)
        coEvery { repository.saveReadingProgress(any()) } returns 1L
        
        val result = useCase(progress)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.saveReadingProgress(progress) }
    }
}
