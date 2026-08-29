package com.juguito.juguitoreader.domain.usecase.reader

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.utils.EpubParser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ParseEpubUseCaseTest {

    private lateinit var useCase: ParseEpubUseCase
    private val context = mockk<Context>()
    private val bookRepository = mockk<BookRepository>()

    private val epubContent = EpubContent(
        baseDir = "/cache/reader/1",
        spine = listOf("chapter1.xhtml"),
        chaptersTree = emptyList()
    )

    @Before
    fun setup() {
        useCase = ParseEpubUseCase(context, bookRepository)
        mockkObject(EpubParser)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `invoke with non-existing book returns failure`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns null
        val result = useCase(1, "path")
        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_epub_not_found)
    }

    @Test
    fun `invoke with existing book returns parsed content`() = runTest {
        coEvery { bookRepository.getBookById(1) } returns Book(
            id = 1,
            title = "Title",
            author = "Author",
            isPhysical = false
        )
        every { EpubParser.extractFullContent(any(), 1, "path") } returns epubContent

        val result = useCase(1, "path")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(epubContent)
    }
}
