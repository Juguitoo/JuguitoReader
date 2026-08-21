package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddReadingProgressUseCaseTest {

    private lateinit var useCase: AddReadingProgressUseCase
    private val repository = mockk<ReadingProgressRepository>()

    @Before
    fun setup() {
        useCase = AddReadingProgressUseCase(repository)
    }

    @Test
    fun `invoke calls saveReadingProgress`() = runTest {
        val p = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L)
        coEvery { repository.saveReadingProgress(any()) } returns 1L
        val result = useCase(p)
        assertThat(result.isSuccess).isTrue()
    }
}
