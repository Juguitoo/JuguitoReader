package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.utils.FileUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ImportBookFromUriUseCaseTest {

    private lateinit var useCase: ImportBookFromUriUseCase
    private val getBookFromEpubUseCase = mockk<GetBookFromEpubUseCase>()
    private val addBookUseCase = mockk<AddBookUseCase>()
    private val context = mockk<Context>()
    private val uri = mockk<Uri>()

    private val importedBook = Book(
        title = "T",
        author = "A",
        isPhysical = false,
        coverUrl = "/data/files/cover.jpg",
        localFilePath = "/data/files/book.epub",
    )

    @Before
    fun setup() {
        useCase = ImportBookFromUriUseCase(getBookFromEpubUseCase, addBookUseCase)
        mockkObject(FileUtils)
        every { FileUtils.deleteFileFromInternalStorage(any(), any()) } just runs
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke imports with persistFiles true and saves book`() = runTest {
        coEvery { getBookFromEpubUseCase(context, uri, persistFiles = true) } returns importedBook
        coEvery { addBookUseCase(importedBook) } returns Result.success(Unit)

        val result = useCase(context, uri)

        assertThat(result.isSuccess).isTrue()
        coVerify { getBookFromEpubUseCase(context, uri, persistFiles = true) }
        coVerify { addBookUseCase(importedBook) }
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), any()) }
    }

    @Test
    fun `invoke deletes copied files when addBook returns failure`() = runTest {
        coEvery { getBookFromEpubUseCase(context, uri, persistFiles = true) } returns importedBook
        coEvery { addBookUseCase(importedBook) } returns Result.failure(
            JuguitoException(R.string.error_save_book)
        )

        val result = useCase(context, uri)

        assertThat(result.isFailure).isTrue()
        verify { FileUtils.deleteFileFromInternalStorage(context, "/data/files/book.epub") }
        verify { FileUtils.deleteFileFromInternalStorage(context, "/data/files/cover.jpg") }
    }

    @Test
    fun `invoke does not delete files when getBookFromEpub throws before book exists`() = runTest {
        coEvery { getBookFromEpubUseCase(context, uri, persistFiles = true) } throws
            JuguitoException(R.string.error_copy_epub)

        val result = useCase(context, uri)

        assertThat(result.isFailure).isTrue()
        assertThat((result.exceptionOrNull() as JuguitoException).resId)
            .isEqualTo(R.string.error_import_book)
        verify(exactly = 0) { FileUtils.deleteFileFromInternalStorage(any(), any()) }
    }
}
