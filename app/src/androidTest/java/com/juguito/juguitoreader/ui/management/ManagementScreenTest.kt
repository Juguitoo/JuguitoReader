package com.juguito.juguitoreader.ui.management

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
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
                    onOpenDrawer = {},
                    onNavigateToEditFolder = {},
                    onNavigateToAddFolder = {},
                    effect = emptyFlow()
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(context.getString(R.string.folders)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.genres)).assertIsDisplayed()
    }
}
