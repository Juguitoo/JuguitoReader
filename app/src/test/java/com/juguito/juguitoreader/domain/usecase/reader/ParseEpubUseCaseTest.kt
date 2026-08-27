package com.juguito.juguitoreader.domain.usecase.reader

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ParseEpubUseCaseTest {

    private lateinit var useCase: ParseEpubUseCase
    private val context = mockk<Context>()
    private val bookRepository = mockk<BookRepository>()

    @Before
    fun setup() {
        useCase = ParseEpubUseCase(context, bookRepository)
    }

    @Test
    fun `invoke with non-existing book returns failure`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns null
        val result = useCase(1, "path")
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_epub_not_found)
    }
}
