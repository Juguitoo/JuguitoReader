package com.juguito.juguitoreader.ui.reader

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress
import org.junit.Test

class EpubChapterUrlTest {

    @Test
    fun `epubChapterUrl uses synthetic https origin and epub prefix`() {
        val url = epubChapterUrl("OEBPS/ch1.xhtml")

        assertThat(url).isEqualTo("https://appassets.androidplatform.net/epub/OEBPS/ch1.xhtml")
    }

    @Test
    fun `epubChapterUrl encodes path segments and keeps slashes`() {
        val url = epubChapterUrl("OEBPS/Mi cap.xhtml")

        assertThat(url).isEqualTo("https://appassets.androidplatform.net/epub/OEBPS/Mi%20cap.xhtml")
        assertThat(url).contains("/epub/OEBPS/")
    }

    @Test
    fun `epubChapterUrl drops empty segments from leading slash`() {
        val url = epubChapterUrl("/OEBPS/ch1.xhtml")

        assertThat(url).isEqualTo("https://appassets.androidplatform.net/epub/OEBPS/ch1.xhtml")
        assertThat(url).doesNotContain("/epub//")
    }

    @Test
    fun `epubChapterUrl does not include filesystem path or file scheme`() {
        val url = epubChapterUrl("OEBPS/ch1.xhtml")

        assertThat(url).doesNotContain("file:")
        assertThat(url).doesNotContain("cache")
        assertThat(url).doesNotContain("/data/")
    }

    @Test
    fun `currentChapterUrl delegates to helper without baseDir`() {
        val success = ReaderUiState.Success(
            book = Book(id = 1, title = "Test", author = "A", isPhysical = false),
            epubContent = EpubContent(
                baseDir = "/data/user/0/com.juguito.juguitoreader/cache/reader/1",
                spine = listOf("OEBPS/ch1.xhtml"),
                chaptersTree = emptyList()
            ),
            readingProgress = ReadingProgress(
                bookId = 1,
                totalChapters = 1,
                lastChapterIndex = 0,
                scrollPosition = 0f,
                lastReadAt = 0L
            ),
            bookSessions = emptyList(),
            currentChapterIndex = 0
        )

        assertThat(success.currentChapterUrl).isEqualTo("https://appassets.androidplatform.net/epub/OEBPS/ch1.xhtml")
        assertThat(success.currentChapterUrl).doesNotContain(success.epubContent.baseDir)
        assertThat(success.currentChapterUrl).doesNotContain("file:")
    }
}
