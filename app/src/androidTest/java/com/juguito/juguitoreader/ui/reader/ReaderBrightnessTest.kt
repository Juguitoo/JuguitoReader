package com.juguito.juguitoreader.ui.reader

import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.ui.reader.components.ReaderControls
import com.juguito.juguitoreader.ui.theme.JuguitoReaderTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class ReaderBrightnessTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readerControls_appliesBrightnessToWindow() {
        val brightness = 0.25f
        composeTestRule.setContent {
            JuguitoReaderTheme {
                ReaderControls(
                    state = successState(brightness = brightness),
                    onEvent = {},
                    onOpenDrawer = {}
                )
            }
        }

        composeTestRule.waitForIdle()

        val appliedBrightness = composeTestRule.activity.window.attributes.screenBrightness
        assertThat(abs(appliedBrightness - brightness)).isLessThan(0.01f)
    }

    @Test
    fun readerControls_updatesBrightnessWhenStateChanges() {
        var brightness by mutableFloatStateOf(0.8f)

        composeTestRule.setContent {
            JuguitoReaderTheme {
                ReaderControls(
                    state = successState(brightness = brightness),
                    onEvent = {},
                    onOpenDrawer = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        assertThat(composeTestRule.activity.window.attributes.screenBrightness).isWithin(0.01f).of(0.8f)

        composeTestRule.runOnUiThread { brightness = 0.15f }
        composeTestRule.waitForIdle()
        assertThat(composeTestRule.activity.window.attributes.screenBrightness).isWithin(0.01f).of(0.15f)
    }

    @Test
    fun restoreReaderWindow_resetsBrightnessOverride() {
        var mounted by mutableStateOf(true)

        composeTestRule.setContent {
            val activity = LocalActivity.current as ComponentActivity
            LaunchedEffect(Unit) {
                activity.window.attributes = activity.window.attributes.apply {
                    screenBrightness = 0.2f
                }
            }
            if (mounted) {
                DisposableEffect(activity) {
                    val readerWindow = activity.window
                    readerWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose { restoreReaderWindow(readerWindow) }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnUiThread { mounted = false }
        composeTestRule.waitForIdle()

        val window = composeTestRule.activity.window
        assertThat(window.attributes.screenBrightness)
            .isWithin(0.001f)
            .of(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE)
        assertThat(window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            .isEqualTo(0)
    }

    private fun successState(brightness: Float): ReaderUiState.Success {
        val context = composeTestRule.activity
        val baseDir = File(context.filesDir, "reader-brightness-test").apply { mkdirs() }.absolutePath
        return ReaderUiState.Success(
            book = Book(id = 1, title = "Test Book", author = "Author", isPhysical = false),
            epubContent = EpubContent(baseDir = baseDir, spine = listOf("ch1"), chaptersTree = emptyList()),
            bookSessions = emptyList(),
            readingProgress = ReadingProgress(
                bookId = 1,
                totalChapters = 1,
                lastChapterIndex = 0,
                scrollPosition = 0f,
                lastReadAt = 0L
            ),
            currentChapterIndex = 0,
            isControlsVisible = true,
            brightness = brightness
        )
    }
}
