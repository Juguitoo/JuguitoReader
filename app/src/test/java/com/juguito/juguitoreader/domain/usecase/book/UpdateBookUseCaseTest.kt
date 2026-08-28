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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateBookUseCaseTest {

    private lateinit var useCase: UpdateBookUseCase
    private val bookRepository = mockk<BookRepository>()
    private val folderRepository = mockk<FolderRepository>()
    private val genreRepository = mockk<GenreRepository>()

    @Before
    fun setup() {
        useCase = UpdateBookUseCase(bookRepository, folderRepository, genreRepository)
    }

    @Test
    fun `invoke with blank title returns failure`() = runTest {
        val result = useCase(Book(id = 1, title = "", author = "Author", isPhysical = true))
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_title_empty)
    }

    @Test
    fun `invoke with blank author returns failure`() = runTest {
        val result = useCase(Book(id = 1, title = "Title", author = "", isPhysical = true))
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_author_empty)
    }

    @Test
    fun `invoke with valid book calls updateBook and returns success`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = true)
        coEvery { bookRepository.updateBook(any()) } returns Unit
        coEvery { bookRepository.syncCrossReferences(any(), any(), any()) } returns Unit

        val result = useCase(book)

        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.updateBook(book) }
        coVerify { bookRepository.syncCrossReferences(1, any(), any()) }
    }
}
