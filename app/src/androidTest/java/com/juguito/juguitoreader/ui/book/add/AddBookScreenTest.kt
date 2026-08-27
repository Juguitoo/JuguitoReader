package com.juguito.juguitoreader.ui.book.add

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
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
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.onNodeWithText(context.getString(R.string.add_book_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.title_label)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.author_label)).assertIsDisplayed()
    }
}
