package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetBookByIdUseCaseTest {

    private lateinit var useCase: GetBookByIdUseCase
    private val repository = mockk<BookRepository>()

    @Before
    fun setup() {
        useCase = GetBookByIdUseCase(repository)
    }

    @Test
    fun `invoke returns book from repository`() = runTest {
        val book = Book(id = 1, title = "T", author = "A", isPhysical = true)
        coEvery { repository.getBookById(1) } returns book
        
        val result = useCase(1)
        
        assertThat(result).isEqualTo(book)
    }
}
