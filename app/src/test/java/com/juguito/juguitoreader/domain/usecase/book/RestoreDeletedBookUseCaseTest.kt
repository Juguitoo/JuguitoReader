package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.DeletedBookSnapshot
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RestoreDeletedBookUseCaseTest {

    private lateinit var useCase: RestoreDeletedBookUseCase
    private val bookRepository = mockk<BookRepository>()
    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = RestoreDeletedBookUseCase(bookRepository, folderRepository, genreRepository)
    }

    @Test
    fun `invoke restores book with resolved folder and genre ids`() = runTest {
        val folder = Folder(id = 5, name = "Sci-Fi", colorHex = "#FFF")
        val genre = Genre(id = 3, name = "Fantasy")
        val book = Book(
            id = 1,
            title = "Title",
            author = "Author",
            isPhysical = false,
            folders = listOf(folder),
            genres = listOf(genre)
        )
        val dailyReading = DailyReading(
            bookId = 1,
            date = "2024-01-01",
            timeSpentMillis = 100,
            reachedPercentage = 0.5f,
            readingSpeed = 200
        )
        val snapshot = DeletedBookSnapshot(book = book, dailyReadings = listOf(dailyReading))

        coEvery { folderRepository.getFolderById(5) } returns folder
        coEvery { genreRepository.getGenreById(3) } returns genre
        coEvery { bookRepository.restoreDeletedBook(any(), any(), any()) } returns Unit

        val result = useCase(snapshot)

        assertThat(result.isSuccess).isTrue()
        coVerify {
            bookRepository.restoreDeletedBook(snapshot, listOf(5), listOf(3))
        }
    }

    @Test
    fun `invoke with empty relations calls restoreDeletedBook with empty id lists`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = false)
        val snapshot = DeletedBookSnapshot(book = book, dailyReadings = emptyList())

        coEvery { bookRepository.restoreDeletedBook(any(), any(), any()) } returns Unit

        val result = useCase(snapshot)

        assertThat(result.isSuccess).isTrue()
        coVerify {
            bookRepository.restoreDeletedBook(snapshot, emptyList(), emptyList())
        }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = false)
        val snapshot = DeletedBookSnapshot(book = book, dailyReadings = emptyList())

        coEvery { bookRepository.restoreDeletedBook(any(), any(), any()) } throws Exception("DB error")

        val result = useCase(snapshot)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_restore_book)
    }
}
