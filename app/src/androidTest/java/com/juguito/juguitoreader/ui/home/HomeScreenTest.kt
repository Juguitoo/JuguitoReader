package com.juguito.juguitoreader.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.empty_library_title)).assertIsDisplayed()
    }
}
