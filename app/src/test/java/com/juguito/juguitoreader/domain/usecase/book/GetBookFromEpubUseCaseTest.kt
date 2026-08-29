package com.juguito.juguitoreader.domain.usecase.book

import android.content.Context
import android.net.Uri
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.utils.EpubMetaData
import com.juguito.juguitoreader.utils.EpubParser
import com.juguito.juguitoreader.utils.FileUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GetBookFromEpubUseCaseTest {

    private lateinit var useCase: GetBookFromEpubUseCase
    private val context = mockk<Context>()
    private val uri = mockk<Uri>()

    private val metadata = EpubMetaData(
        title = "Test",
        author = "Author",
        series = null,
        seriesOrder = null,
        genres = emptyList(),
        publisher = null,
        coverUrl = null
    )

    @Before
    fun setup() {
        useCase = GetBookFromEpubUseCase()
        mockkObject(EpubParser)
        mockkObject(FileUtils)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke extracts metadata and returns book with internal path`() {
        every { EpubParser.extractMetadata(any(), any()) } returns metadata
        every { FileUtils.saveBookToInternalStorage(any(), any()) } returns "internal/path"

        val book = useCase(context, uri)

        assertThat(book.title).isEqualTo("Test")
        assertThat(book.author).isEqualTo("Author")
        assertThat(book.localFilePath).isEqualTo("internal/path")
    }

    @Test
    fun `invoke throws when copy to internal storage fails without falling back to content uri`() {
        every { EpubParser.extractMetadata(any(), any()) } returns metadata
        every { FileUtils.saveBookToInternalStorage(any(), any()) } returns null
        every { uri.toString() } returns "content://com.android.providers.downloads/document/42"

        val exception = assertThrows(JuguitoException::class.java) {
            useCase(context, uri)
        }

        assertThat(exception.resId).isEqualTo(R.string.error_copy_epub)
    }
}
