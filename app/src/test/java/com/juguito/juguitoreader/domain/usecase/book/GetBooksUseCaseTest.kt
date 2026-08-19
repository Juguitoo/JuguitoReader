package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetBooksUseCaseTest {

    private lateinit var getBooksUseCase: GetBooksUseCase
    private val repository = mockk<BookRepository>()

    @Before
    fun setup() {
        getBooksUseCase = GetBooksUseCase(repository)
    }

    @Test
    fun `invoke returns flow of books from repository`() = runBlocking {
        val books = listOf(
            Book(id = 1, title = "Book 1", author = "Author 1", isPhysical = false),
            Book(id = 2, title = "Book 2", author = "Author 2", isPhysical = false)
        )
        every { repository.getAllBooks() } returns flowOf(books)
        
        val result = getBooksUseCase().first()
        
        assertThat(result).hasSize(2)
        assertThat(result[0].title).isEqualTo("Book 1")
        assertThat(result[1].title).isEqualTo("Book 2")
    }
}
