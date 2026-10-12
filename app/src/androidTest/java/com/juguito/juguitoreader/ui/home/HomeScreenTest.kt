package com.juguito.juguitoreader.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
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

    @Test
    fun homeScreen_success_with_empty_shelves_shows_readable_empty() {
        composeTestRule.setContent {
            JuguitoReaderTheme {
                HomeContent(
                    state = HomeUiState.Success(
                        readingBooks = emptyList(),
                        pendingBooks = emptyList(),
                        stats = StatsUiState(totalBooksCount = 2)
                    ),
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
        composeTestRule.onNodeWithText(context.getString(R.string.empty_home_readable_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.empty_library_title)).assertDoesNotExist()
    }

    @Test
    fun homeScreen_shows_featured_book_and_hides_empty_sections() {
        val reading = Book(
            id = 1,
            title = "Ahora mismo",
            author = "Autora",
            isPhysical = false,
            localFilePath = "a.epub",
            status = BookStatus.READING
        )
        val pending = Book(
            id = 2,
            title = "Luego",
            author = "Otro",
            isPhysical = false,
            localFilePath = "b.epub",
            status = BookStatus.PENDING
        )
        composeTestRule.setContent {
            JuguitoReaderTheme {
                HomeContent(
                    state = HomeUiState.Success(
                        readingBooks = listOf(reading),
                        pendingBooks = listOf(pending),
                        stats = StatsUiState()
                    ),
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
        composeTestRule.onNodeWithText("Ahora mismo").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.active_reading_label).uppercase()
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.read_book)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.active_readings)).assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.home_favourites_title)).assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.home_pending_title)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Luego").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.add_book)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.import_epub)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeScreen_shows_other_readings_and_favourite_rating() {
        val current = Book(
            id = 1,
            title = "Principal",
            author = "A",
            isPhysical = false,
            localFilePath = "a.epub",
            status = BookStatus.READING
        )
        val other = Book(
            id = 2,
            title = "Segundo",
            author = "B",
            isPhysical = false,
            localFilePath = "b.epub",
            status = BookStatus.READING
        )
        val favourite = Book(
            id = 3,
            title = "Querido",
            author = "C",
            isPhysical = true,
            status = BookStatus.FINISHED,
            isFavourite = true,
            rating = 8f
        )
        composeTestRule.setContent {
            JuguitoReaderTheme {
                HomeContent(
                    state = HomeUiState.Success(
                        readingBooks = listOf(current, other),
                        pendingBooks = emptyList(),
                        favouriteBooks = listOf(favourite),
                        stats = StatsUiState()
                    ),
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
        composeTestRule.onNodeWithText("Principal").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.active_readings)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Segundo").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.home_favourites_title)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNode(hasContentDescription("Querido", substring = true)).performScrollTo().assertExists()
        composeTestRule.onNode(hasContentDescription("8 / 10", substring = true)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.home_pending_title)).assertDoesNotExist()
    }
}
