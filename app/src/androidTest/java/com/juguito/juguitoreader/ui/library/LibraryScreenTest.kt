package com.juguito.juguitoreader.ui.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class LibraryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun libraryScreen_shows_empty_message_when_no_books() {
        val emptyState = LibraryUiState.Empty()
        
        composeTestRule.setContent {
            JuguitoReaderTheme {
                LibraryContent(
                    state = emptyState,
                    isImporting = false,
                    isSearchVisible = false,
                    showAllFoldersSheet = false,
                    onToggleSearch = {},
                    onShowAllFoldersSheet = {},
                    onOpenDrawer = {},
                    onNavigateToAddBook = {},
                    onNavigateToReadBook = {},
                    onNavigateToBookDetail = {},
                    onEvent = {},
                    onImportBook = {},
                    onDismissError = {},
                    effect = emptyFlow()
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.empty_library_title)).assertIsDisplayed()
    }

    @Test
    fun libraryScreen_shows_books_when_success_state() {
        val books = listOf(
            Book(id = 1, title = "Don Quijote", author = "Cervantes", isPhysical = false)
        )
        val successState = LibraryUiState.Success(
            allBooks = books,
            filteredBooks = books,
            folders = emptyList()
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                LibraryContent(
                    state = successState,
                    isImporting = false,
                    isSearchVisible = false,
                    showAllFoldersSheet = false,
                    onToggleSearch = {},
                    onShowAllFoldersSheet = {},
                    onOpenDrawer = {},
                    onNavigateToAddBook = {},
                    onNavigateToReadBook = {},
                    onNavigateToBookDetail = {},
                    onEvent = {},
                    onImportBook = {},
                    onDismissError = {},
                    effect = emptyFlow()
                )
            }
        }

        composeTestRule.onNodeWithText("Don Quijote").assertIsDisplayed()
    }
}
