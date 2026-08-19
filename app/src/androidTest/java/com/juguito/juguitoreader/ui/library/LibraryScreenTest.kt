package com.juguito.juguitoreader.ui.library

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeTestRule.onNodeWithText("Tu biblioteca está vacía", ignoreCase = true).assertIsDisplayed()
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
