package com.juguito.juguitoreader.domain.usecase.bookmark

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.repository.BookmarkRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateBookmarkUseCaseTest {

    private lateinit var useCase: UpdateBookmarkUseCase
    private val repository = mockk<BookmarkRepository>()

    @Before
    fun setup() {
        useCase = UpdateBookmarkUseCase(repository)
        coEvery { repository.updateBookmark(any()) } returns Unit
    }

    @Test
    fun `highlight with text becomes a note`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.updateBookmark(capture(slot)) } returns Unit

        val result = useCase(stored(kind = BookmarkKind.HIGHLIGHT, noteText = "  margen  "))

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.NOTE)
        assertThat(slot.captured.noteText).isEqualTo("margen")
        assertThat(slot.captured.color).isEqualTo(BookmarkColor.AMBER)
        assertThat(slot.captured.quote).isEqualTo("frase")
        assertThat(slot.captured.id).isEqualTo(4)
    }

    @Test
    fun `blank note text becomes a highlight`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.updateBookmark(capture(slot)) } returns Unit

        val result = useCase(stored(kind = BookmarkKind.NOTE, noteText = "   "))

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.HIGHLIGHT)
        assertThat(slot.captured.noteText).isNull()
    }

    @Test
    fun `position bookmark is rejected`() = runTest {
        val result = useCase(stored(kind = BookmarkKind.BOOKMARK, noteText = "margen"))

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_position)
        coVerify(exactly = 0) { repository.updateBookmark(any()) }
    }

    @Test
    fun `missing color is rejected`() = runTest {
        val result = useCase(stored(kind = BookmarkKind.HIGHLIGHT, noteText = null, color = null))

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_color)
        coVerify(exactly = 0) { repository.updateBookmark(any()) }
    }

    private fun stored(
        kind: BookmarkKind,
        noteText: String?,
        color: BookmarkColor? = BookmarkColor.AMBER
    ) = Bookmark(
        id = 4,
        bookId = 1,
        kind = kind,
        chapterHref = "chapter.xhtml",
        quote = "frase",
        prefix = "antes",
        suffix = "despues",
        imageSrc = null,
        color = color,
        noteText = noteText,
        scrollPosition = 0.2f,
        createdAt = 1L
    )

    private fun failureId(result: Result<Bookmark>): Int {
        assertThat(result.isFailure).isTrue()
        return (result.exceptionOrNull() as JuguitoException).resId
    }
}
