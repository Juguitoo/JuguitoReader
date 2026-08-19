package com.juguito.juguitoreader.ui.book.add

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class AddBookScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addBookScreen_shows_form_fields() {
        val state = AddBookUiState()

        composeTestRule.setContent {
            JuguitoReaderTheme {
                AddBookContent(
                    state = state,
                    onEvent = {},
                    onNavigateBack = {},
                    onNavigateToAddFolder = {},
                    onBookSavedSuccessfully = {},
                    effect = emptyFlow()
                )
            }
        }

        composeTestRule.onNodeWithText("Nuevo libro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Título").assertIsDisplayed()
        composeTestRule.onNodeWithText("Autor").assertIsDisplayed()
    }
}
