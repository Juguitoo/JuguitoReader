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
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
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
        coverUrl = "/cache/covers/cover.jpg",
    )

    @Before
    fun setup() {
        useCase = GetBookFromEpubUseCase()
        mockkObject(EpubParser)
        mockkObject(FileUtils)
        every { uri.toString() } returns "content://com.android.providers.downloads/document/42"
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke with persistFiles false keeps epub uri in draft`() = runTest {
        every { EpubParser.extractMetadata(context, uri, false) } returns metadata

        val book = useCase(context, uri, persistFiles = false)

        assertThat(book.title).isEqualTo("Test")
        assertThat(book.author).isEqualTo("Author")
        assertThat(book.localFilePath).isEqualTo("content://com.android.providers.downloads/document/42")
        assertThat(book.coverUrl).isEqualTo("/cache/covers/cover.jpg")
        verify(exactly = 0) { FileUtils.saveEpubBookToInternalStorage(any(), any()) }
    }

    @Test
    fun `invoke with persistFiles true copies epub to internal storage`() = runTest {
        every { EpubParser.extractMetadata(context, uri, true) } returns metadata
        every { FileUtils.saveEpubBookToInternalStorage(context, uri) } returns "/data/files/book.epub"

        val book = useCase(context, uri, persistFiles = true)

        assertThat(book.localFilePath).isEqualTo("/data/files/book.epub")
        verify(exactly = 1) { FileUtils.saveEpubBookToInternalStorage(context, uri) }
    }

    @Test
    fun `invoke with persistFiles true propagates epub copy failure`() = runTest {
        every { EpubParser.extractMetadata(context, uri, true) } returns metadata
        every {
            FileUtils.saveEpubBookToInternalStorage(context, uri)
        } throws JuguitoException(R.string.error_copy_epub)

        val exception = runCatching { useCase(context, uri, persistFiles = true) }.exceptionOrNull()

        assertThat(exception).isInstanceOf(JuguitoException::class.java)
        assertThat((exception as JuguitoException).resId).isEqualTo(R.string.error_copy_epub)
        verify { FileUtils.deleteFileFromInternalStorage(context, metadata.coverUrl) }
    }
}
