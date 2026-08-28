package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.FolderRepository
import com.juguito.juguitoreader.domain.repository.GenreRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class AddBookUseCaseTest {

    private lateinit var addBookUseCase: AddBookUseCase
    private val bookRepository = mockk<BookRepository>()
    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        addBookUseCase = AddBookUseCase(bookRepository, folderRepository, genreRepository)
    }

    @Test
    fun `invoke with empty title returns failure`() = runBlocking {
        val book = Book(title = "", author = "Author", isPhysical = false)
        val result = addBookUseCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_title_empty)
    }

    @Test
    fun `invoke with empty author returns failure`() = runBlocking {
        val book = Book(title = "Title", author = "", isPhysical = false)
        val result = addBookUseCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_author_empty)
    }

    @Test
    fun `invoke with valid book calls insertBook and returns success`() = runBlocking {
        val book = Book(title = "Valid Title", author = "Valid Author", isPhysical = false)

        coEvery { bookRepository.insertBook(any()) } returns 1L
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = addBookUseCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { bookRepository.insertBook(any()) }
        coVerify(exactly = 1) { bookRepository.syncCrossReferences(1, any(), any()) }
    }

    @Test
    fun `invoke with exception in repository returns failure`() = runBlocking {
        val book = Book(title = "Title", author = "Author", isPhysical = false)
        coEvery { bookRepository.insertBook(any()) } throws Exception("DB Error")

        val result = addBookUseCase(book)

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_save_book)
    }
}
