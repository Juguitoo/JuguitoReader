package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.dao.BookDAO
import com.juguito.juguitoreader.data.local.entity.BookEntity
import com.juguito.juguitoreader.data.local.entity.BookWithDetails
import com.juguito.juguitoreader.domain.model.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BookRepositoryImplTest {

    private lateinit var repository: BookRepositoryImpl
    private val bookDAO = mockk<BookDAO>()

    @Before
    fun setup() {
        repository = BookRepositoryImpl(bookDAO)
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
    fun `addCrossReferences calls DAO methods`() = runTest {
        coEvery { bookDAO.insertBookFolderCrossRefs(any()) } returns Unit
        coEvery { bookDAO.insertBookGenreCrossRefs(any()) } returns Unit

        repository.addCrossReferences(1, listOf(1), listOf(1))

        coVerify { bookDAO.insertBookFolderCrossRefs(any()) }
        coVerify { bookDAO.insertBookGenreCrossRefs(any()) }
    }

    @Test
    fun `deleteBook calls deleteBookById on DAO`() = runTest {
        coEvery { bookDAO.deleteBookById(1) } returns Unit

        repository.deleteBook(1)

        coVerify { bookDAO.deleteBookById(1) }
    }
}
