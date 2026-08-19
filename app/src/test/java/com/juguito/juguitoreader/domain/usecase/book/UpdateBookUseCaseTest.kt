package com.juguito.juguitoreader.domain.usecase.book

import com.google.common.truth.Truth.assertThat
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
    fun `invoke with valid book calls repository save`() = runTest {
        val book = Book(id = 1, title = "Title", author = "Author", isPhysical = true)
        coEvery { bookRepository.saveBook(any()) } returns 1L
        coEvery { bookRepository.addCrossReferences(any(), any(), any()) } returns Unit
        
        val result = useCase(book)
        
        assertThat(result.isSuccess).isTrue()
        coVerify { bookRepository.saveBook(any()) }
    }
}
