package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import androidx.room.withTransaction
import com.juguito.juguitoreader.data.local.JuguitoReaderDatabase
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.dao.DailyReadingDAO
import com.juguito.juguitoreader.data.local.dao.ReadingProgressDAO
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookWithDetails
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.DeletedBookSnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookRepositoryImplTest {

    private lateinit var repository: BookRepositoryImpl
    private val bookDAO = mockk<BookDAO>()
    private val readingProgressDAO = mockk<ReadingProgressDAO>()
    private val dailyReadingDAO = mockk<DailyReadingDAO>()
    private val database = mockk<JuguitoReaderDatabase>()

    @Before
    fun setup() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction<Unit>(any()) } coAnswers {
            arg<suspend () -> Unit>(1).invoke()
        }
        repository = BookRepositoryImpl(
            bookDAO = bookDAO,
            readingProgressDAO = readingProgressDAO,
            dailyReadingDAO = dailyReadingDAO,
            database = database
        )
    }

    @After
    fun teardown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `getAllBooks maps entities to domain`() = runTest {
        val bookEntity = BookEntity(id = 1, title = "Title", author = "Author", isPhysical = false, createdAt = 0L)
        val bookWithDetails = BookWithDetails(book = bookEntity, folders = emptyList(), genres = emptyList(), readingProgress = null)

        every { bookDAO.getAllBooks() } returns flowOf(listOf(bookWithDetails))

        val result = repository.getAllBooks().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Title")
    }

    @Test
    fun `getBookById returns domain book`() = runTest {
        val bookEntity = BookEntity(id = 1, title = "Title", author = "Author", isPhysical = false, createdAt = 0L)
        val bookWithDetails = BookWithDetails(book = bookEntity, folders = emptyList(), genres = emptyList(), readingProgress = null)
        coEvery { bookDAO.getBookById(1) } returns bookWithDetails

        val result = repository.getBookById(1)

        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo("Title")
    }

    @Test
    fun `insertBook calls insertBook on DAO`() = runTest {
        val book = Book(title = "New Book", author = "Author", isPhysical = false)
        coEvery { bookDAO.insertBook(any()) } returns 100L

        val id = repository.insertBook(book)

        assertThat(id).isEqualTo(100L)
        coVerify { bookDAO.insertBook(any()) }
    }

    @Test
    fun `insertBooks calls insertBooks on DAO`() = runTest {
        val books = listOf(Book(title = "Book 1", author = "A", isPhysical = false))
        coEvery { bookDAO.insertBooks(any()) } returns Unit

        repository.insertBooks(books)

        coVerify { bookDAO.insertBooks(any()) }
    }

    @Test
    fun `updateBook calls updateBook on DAO`() = runTest {
        val book = Book(id = 1, title = "Updated", author = "Author", isPhysical = false)
        coEvery { bookDAO.updateBook(any()) } returns Unit

        repository.updateBook(book)

        coVerify { bookDAO.updateBook(any()) }
    }

    @Test
    fun `syncCrossReferences calls syncBookCrossRefs on DAO`() = runTest {
        coEvery { bookDAO.syncBookCrossRefs(any(), any(), any()) } returns Unit

        repository.syncCrossReferences(1, listOf(1), listOf(2))

        coVerify { bookDAO.syncBookCrossRefs(1, listOf(1), listOf(2)) }
    }

    @Test
    fun `deleteBook calls deleteBookById on DAO`() = runTest {
        coEvery { bookDAO.deleteBookById(1) } returns Unit

        repository.deleteBook(1)

        coVerify { bookDAO.deleteBookById(1) }
    }

    @Test
    fun `restoreDeletedBook inserts book syncs cross refs and daily readings in transaction`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = false)
        val dailyReading = DailyReading(
            bookId = 1,
            date = "2024-01-01",
            timeSpentMillis = 100,
            reachedPercentage = 0.5f,
            readingSpeed = 200
        )
        val snapshot = DeletedBookSnapshot(book = book, dailyReadings = listOf(dailyReading))

        coEvery { bookDAO.insertBook(any()) } returns 1L
        coEvery { bookDAO.syncBookCrossRefs(any(), any(), any()) } returns Unit
        coEvery { dailyReadingDAO.insert(any()) } returns 1L

        repository.restoreDeletedBook(snapshot, listOf(5), listOf(3))

        coVerify { bookDAO.insertBook(any()) }
        coVerify { bookDAO.syncBookCrossRefs(1, listOf(5), listOf(3)) }
        coVerify { dailyReadingDAO.insert(any()) }
    }
}
