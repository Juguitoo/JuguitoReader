package com.juguito.juguitoreader.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_shows_loading_indicator() {
        composeTestRule.setContent {
            JuguitoReaderTheme {
                HomeContent(
                    state = HomeUiState.Loading,
                    snackbarMessage = null,
                    onClearSnackbarMessage = {},
                    onNavigateToAddBook = {},
                    onNavigateToBookDetail = {},
                    onNavigateToReadBook = {},
                    onOpenDrawer = {},
                    onEvent = {},
                    effect = emptyFlow()
                )
            }
        }
    }

    @Test
    fun homeScreen_shows_empty_message() {
        composeTestRule.setContent {
            JuguitoReaderTheme {
                HomeContent(
                    state = HomeUiState.Empty,
                    snackbarMessage = null,
                    onClearSnackbarMessage = {},
                    onNavigateToAddBook = {},
                    onNavigateToBookDetail = {},
                    onNavigateToReadBook = {},
                    onOpenDrawer = {},
                    onEvent = {},
                    effect = emptyFlow()
                )
            }
        }
        composeTestRule.onNodeWithText("Tu biblioteca está vacía", ignoreCase = true).assertIsDisplayed()
    }
}
