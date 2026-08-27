package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteBookUseCaseTest {

    private lateinit var useCase: DeleteBookUseCase
    private val repository = mockk<BookRepository>()

    @Before
    fun setup() {
        useCase = DeleteBookUseCase(repository)
    }

    @Test
    fun `invoke with non-existing book returns failure`() = runTest {
        coEvery { repository.getBookById(1) } returns null
        
        val result = useCase(1)
        
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_delete_book_not_found)
    }

    @Test
    fun `invoke with existing book calls delete and returns success`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true)
        coEvery { repository.getBookById(1) } returns book
        coEvery { repository.deleteBook(1) } returns Unit
        
        val result = useCase(1)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.deleteBook(1) }
    }
}
