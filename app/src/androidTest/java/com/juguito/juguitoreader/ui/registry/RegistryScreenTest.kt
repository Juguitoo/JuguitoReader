package com.juguito.juguitoreader.ui.registry

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import org.junit.Rule
import org.junit.Test

class RegistryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun registryScreen_shows_table_headers() {
        val state = RegistryUiState()

        composeTestRule.setContent {
            JuguitoReaderTheme {
                RegistryContent(
                    state = state,
                    onEvent = {},
                    onOpenDrawer = {},
                    onNavigateToAddBook = {},
                    onNavigateToBookDetail = {}
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.book_col), ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.status_col), ignoreCase = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.rating_col), ignoreCase = true).assertExists()
    }

    @Test
    fun registryScreen_shows_book_in_list() {
        val book = Book(id = 1, title = "1984", author = "Orwell", isPhysical = true)
        val state = RegistryUiState(
            books = listOf(book),
            filteredBooks = listOf(book)
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                RegistryContent(
                    state = state,
                    onEvent = {},
                    onOpenDrawer = {},
                    onNavigateToAddBook = {},
                    onNavigateToBookDetail = {}
                )
            }
        }

        composeTestRule.onNodeWithText("1984").assertIsDisplayed()
        composeTestRule.onNodeWithText("Orwell").assertIsDisplayed()
    }

    @Test
    fun registryScreen_shows_error_instead_of_empty_table() {
        val state = RegistryUiState(
            errorMessage = "Load failed",
            isLoading = false
        )

        composeTestRule.setContent {
            JuguitoReaderTheme {
                RegistryContent(
                    state = state,
                    onEvent = {},
                    onOpenDrawer = {},
                    onNavigateToAddBook = {},
                    onNavigateToBookDetail = {}
                )
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.something_went_wrong)).assertIsDisplayed()
        composeTestRule.onNodeWithText("Load failed").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.retry)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.book_col), ignoreCase = true).assertDoesNotExist()
    }
}
