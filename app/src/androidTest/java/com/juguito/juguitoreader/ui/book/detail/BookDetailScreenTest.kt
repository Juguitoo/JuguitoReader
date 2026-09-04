package com.juguito.juguitoreader.ui.book.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.book.state.BookDraftState
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class BookDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookDetailScreen_shows_book_info_in_success_state() {
        val book = Book(id = 1, title = "Don Quijote", author = "Cervantes", isPhysical = true)
        val successState = BookDetailUiState.Success(
            book = book,
            bookDraft = BookDraftState(title = "Don Quijote", author = "Cervantes", isPhysical = true)
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                BookDetailContent(
                    state = successState,
                    onEvent = {},
                    onNavigateBack = {},
                    onNavigateToAddFolder = {},
                    onLoadBook = {},
                    effect = emptyFlow()
                )
            }
        }

        composeTestRule.onNodeWithText("Cervantes", useUnmergedTree = true, substring = true).assertExists()
    }

    @Test
    fun bookDetailScreen_shows_edit_mode_when_toggled() {
        val book = Book(id = 1, title = "Don Quijote", author = "Cervantes", isPhysical = true)
        val successState = BookDetailUiState.Success(
            book = book,
            bookDraft = BookDraftState(title = "Don Quijote", author = "Cervantes", isPhysical = true),
            isEditMode = true
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                BookDetailContent(
                    state = successState,
                    onEvent = {},
                    onNavigateBack = {},
                    onNavigateToAddFolder = {},
                    onLoadBook = {},
                    effect = emptyFlow()
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.edit_book)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.change)).assertDoesNotExist()
    }
}
