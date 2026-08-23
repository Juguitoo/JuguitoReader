package com.juguito.juguitoreader.ui.reader

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import org.junit.Rule
import org.junit.Test

class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun readerScreen_shows_book_title_when_controls_visible() {
        val book = Book(id = 1, title = "Moby Dick", author = "Melville", isPhysical = false)
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        val progress = ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L)
        
        val successState = ReaderUiState.Success(
            book = book,
            epubContent = epubContent,
            bookSessions = emptyList(),
            readingProgress = progress,
            currentChapterIndex = 0,
            isControlsVisible = true
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                ReaderContent(
                    state = successState,
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Moby Dick").assertIsDisplayed()
    }
}
