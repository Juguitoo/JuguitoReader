package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ImportBookFromUriUseCaseTest {

    private lateinit var useCase: ImportBookFromUriUseCase
    private val getBookFromEpubUseCase = mockk<GetBookFromEpubUseCase>()
    private val addBookUseCase = mockk<AddBookUseCase>()
    private val context = mockk<Context>()
    private val uri = mockk<Uri>()

    @Before
    fun setup() {
        useCase = ImportBookFromUriUseCase(getBookFromEpubUseCase, addBookUseCase)
    }

    @Test
    fun `invoke calls getBookFromEpub and addBook`() = runTest {
        val book = Book(title = "T", author = "A", isPhysical = false)
        coEvery { getBookFromEpubUseCase(any(), any()) } returns book
        coEvery { addBookUseCase(any()) } returns Result.success(Unit)

        val result = useCase(context, uri)

        assertThat(result.isSuccess).isTrue()
        coVerify { getBookFromEpubUseCase(context, uri) }
        coVerify { addBookUseCase(book) }
    }
}
