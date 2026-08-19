package com.juguito.juguitoreader.ui.management

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class ManagementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun managementScreen_shows_tabs() {
        val state = ManagementUiState()

        composeTestRule.setContent {
            JuguitoReaderTheme {
                ManagementContent(
                    state = state,
                    onEvent = {},
                    managementMessage = null,
                    onClearManagementMessage = {},
                    onNavigateBack = {},
                    onNavigateToEditFolder = {},
                    onNavigateToAddFolder = {},
                    effect = emptyFlow()
                )
            }
        }

        composeTestRule.onNodeWithText("Carpetas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Géneros").assertIsDisplayed()
    }
}
