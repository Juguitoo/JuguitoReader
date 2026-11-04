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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddBookmarkUseCaseTest {

    private lateinit var useCase: AddBookmarkUseCase
    private val repository = mockk<BookmarkRepository>()

    @Before
    fun setup() {
        useCase = AddBookmarkUseCase(repository)
        coEvery { repository.getBookBookmarks(1) } returns flowOf(emptyList())
        coEvery { repository.insertBookmark(any()) } returns 7L
    }

    @Test
    fun `position draft inserts a bookmark without color or note`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.insertBookmark(capture(slot)) } returns 7L

        val result = useCase(position())

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(7)
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.BOOKMARK)
        assertThat(slot.captured.color).isNull()
        assertThat(slot.captured.noteText).isNull()
        assertThat(slot.captured.id).isEqualTo(0)
    }

    @Test
    fun `highlight draft inserts a highlight`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.insertBookmark(capture(slot)) } returns 7L

        val result = useCase(highlight())

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.HIGHLIGHT)
        assertThat(slot.captured.color).isEqualTo(BookmarkColor.AMBER)
        assertThat(slot.captured.noteText).isNull()
    }

    @Test
    fun `note draft inserts a note with trimmed text`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.insertBookmark(capture(slot)) } returns 7L

        val result = useCase(note(text = "  margen  "))

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.NOTE)
        assertThat(slot.captured.noteText).isEqualTo("margen")
    }

    @Test
    fun `blank note is inserted as an empty note`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.insertBookmark(capture(slot)) } returns 7L

        val result = useCase(note(text = "   "))

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.NOTE)
        assertThat(slot.captured.noteText).isEqualTo("")
    }

    @Test
    fun `highlight without a phrase returns failure`() = runTest {
        val result = useCase(highlight().copy(quote = "  "))

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_empty_text)
        coVerify(exactly = 0) { repository.insertBookmark(any()) }
    }

    @Test
    fun `position on an image stores the image src`() = runTest {
        val slot = slot<Bookmark>()
        coEvery { repository.insertBookmark(capture(slot)) } returns 7L

        val result = useCase(
            NewBookmark.Position(
                bookId = 1,
                chapterHref = "chapter.xhtml",
                quote = "",
                prefix = "",
                suffix = "",
                scrollPosition = 0.4f,
                imageSrc = " images/map.jpg "
            )
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.kind).isEqualTo(BookmarkKind.BOOKMARK)
        assertThat(slot.captured.quote).isEqualTo("")
        assertThat(slot.captured.imageSrc).isEqualTo("images/map.jpg")
    }

    @Test
    fun `position with no phrase and no image returns failure`() = runTest {
        val result = useCase(
            NewBookmark.Position(
                bookId = 1,
                chapterHref = "chapter.xhtml",
                quote = " ",
                prefix = "",
                suffix = "",
                scrollPosition = 0.4f
            )
        )

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_empty_text)
        coVerify(exactly = 0) { repository.insertBookmark(any()) }
    }

    @Test
    fun `quote over the cap returns failure`() = runTest {
        val result = useCase(position(quote = "a".repeat(MAX_BOOKMARK_QUOTE + 1)))

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_too_long)
    }

    @Test
    fun `same position anchor returns duplicate`() = runTest {
        coEvery { repository.getBookBookmarks(1) } returns flowOf(
            listOf(stored(kind = BookmarkKind.BOOKMARK, quote = "frase", prefix = "antes"))
        )

        val result = useCase(position())

        assertThat(failureId(result)).isEqualTo(R.string.error_bookmark_duplicate)
        coVerify(exactly = 0) { repository.insertBookmark(any()) }
    }

    private fun position(quote: String = "frase") = NewBookmark.Position(
        bookId = 1,
        chapterHref = "chapter.xhtml",
        quote = quote,
        prefix = "antes",
        suffix = "despues",
        scrollPosition = 0.2f
    )

    private fun highlight() = NewBookmark.Highlight(
        bookId = 1,
        chapterHref = "chapter.xhtml",
        quote = "frase",
        prefix = "antes",
        suffix = "despues",
        scrollPosition = 0.2f,
        color = BookmarkColor.AMBER
    )

    private fun note(text: String) = NewBookmark.Note(
        bookId = 1,
        chapterHref = "chapter.xhtml",
        quote = "frase",
        prefix = "antes",
        suffix = "despues",
        scrollPosition = 0.2f,
        color = BookmarkColor.BLUE,
        noteText = text
    )

    private fun stored(kind: BookmarkKind, quote: String, prefix: String) = Bookmark(
        id = 3,
        bookId = 1,
        kind = kind,
        chapterHref = "chapter.xhtml",
        quote = quote,
        prefix = prefix,
        suffix = "despues",
        imageSrc = null,
        color = null,
        noteText = null,
        scrollPosition = 0.2f,
        createdAt = 1L
    )

    private fun failureId(result: Result<Int>): Int {
        assertThat(result.isFailure).isTrue()
        return (result.exceptionOrNull() as JuguitoException).resId
    }
}
