package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateReadingProgressUseCaseTest {

    private lateinit var progressRepository: ReadingProgressRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: UpdateReadingProgressUseCase

    @Before
    fun setup() {
        progressRepository = mockk(relaxed = true)
        bookRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        useCase = UpdateReadingProgressUseCase(progressRepository, bookRepository, settingsRepository)
    }

    @Test
    fun `Si autoStart esta activado y el libro es PENDING, cambia a READING al actualizar progreso`() = runTest {
        val book = Book(id = 1, title = "Test", author = "Autor", isPhysical = false, status = BookStatus.PENDING)
        val progress = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 1, scrollPosition = 0f, lastReadAt = 123L)

        coEvery { bookRepository.getBookById(1) } returns book
        every { settingsRepository.autoStartReadingFlow } returns flowOf(true)
        every { settingsRepository.autoFinishReadingFlow } returns flowOf(true)

        val result = useCase(progress)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            bookRepository.saveBook(match { it.status == BookStatus.READING })
        }
    }

    @Test
    fun `Si autoFinish esta activado y llega al 100 por ciento, cambia a FINISHED`() = runTest {
        val book = Book(id = 1, title = "Test", author = "Autor", isPhysical = false, status = BookStatus.READING)
        val progress = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 9, scrollPosition = 1.0f, lastReadAt = 123L)

        coEvery { bookRepository.getBookById(1) } returns book
        every { settingsRepository.autoStartReadingFlow } returns flowOf(true)
        every { settingsRepository.autoFinishReadingFlow } returns flowOf(true)

        useCase(progress)

        coVerify(exactly = 1) {
            bookRepository.saveBook(match { it.status == BookStatus.FINISHED })
        }
    }

    @Test
    fun `Si autoFinish esta activado pero no llega al 100 por ciento, se queda en READING`() = runTest {
        val book = Book(id = 1, title = "Test", author = "Autor", isPhysical = false, status = BookStatus.READING)
        val progress = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 9, scrollPosition = 0.9f, lastReadAt = 123L)

        coEvery { bookRepository.getBookById(1) } returns book
        every { settingsRepository.autoStartReadingFlow } returns flowOf(true)
        every { settingsRepository.autoFinishReadingFlow } returns flowOf(true)

        useCase(progress)

        coVerify(exactly = 0) { bookRepository.saveBook(any()) }
    }

    @Test
    fun `Si las automatizaciones estan desactivadas, el estado no cambia nunca`() = runTest {
        val book = Book(id = 1, title = "Test", author = "Autor", isPhysical = false, status = BookStatus.PENDING)
        val progress = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 9, scrollPosition = 1.0f, lastReadAt = 123L)

        coEvery { bookRepository.getBookById(1) } returns book
        every { settingsRepository.autoStartReadingFlow } returns flowOf(false)
        every { settingsRepository.autoFinishReadingFlow } returns flowOf(false)

        useCase(progress)

        coVerify(exactly = 0) { bookRepository.saveBook(any()) }
    }
}