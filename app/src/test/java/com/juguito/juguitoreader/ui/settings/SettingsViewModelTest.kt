package com.juguito.juguitoreader.ui.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.theme.AppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: SettingsViewModel
    private val repository = mockk<SettingsRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.appThemeFlow } returns flowOf("JUGUITO")
        every { repository.readerThemeFlow } returns flowOf("SEPIA")
        every { repository.textZoomFlow } returns flowOf(100)
        every { repository.languageFlow } returns flowOf("SPANISH")
        every { repository.autoStartReadingFlow } returns flowOf(false)
        every { repository.autoFinishReadingFlow } returns flowOf(false)
        every { repository.promptStatusChangeFlow } returns flowOf(true)
        
        viewModel = SettingsViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState contains correct default values`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.appTheme).isEqualTo(AppTheme.JUGUITO)
            assertThat(state.readerTheme).isEqualTo(ReaderTheme.SEPIA)
            assertThat(state.textZoom).isEqualTo(100)
            assertThat(state.autoFinish).isEqualTo(false)
            assertThat(state.autoStart).isEqualTo(false)
        }
    }

    @Test
    fun `onEvent OnAppThemeChanged calls repository`() = runTest {
        coEvery { repository.saveAppTheme(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnAppThemeChanged(AppTheme.NEON))
        coVerify { repository.saveAppTheme("NEON") }
    }

    @Test
    fun `onEvent OnReaderThemeChanged calls repository`() = runTest {
        coEvery { repository.saveReaderTheme(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnReaderThemeChanged(ReaderTheme.NIGHT))
        coVerify { repository.saveReaderTheme("NIGHT") }
    }

    @Test
    fun `onEvent OnTextZoomChanged calls repository`() = runTest {
        coEvery { repository.saveTextZoom(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnTextZoomChanged(150))
        coVerify { repository.saveTextZoom(150) }
    }

    @Test
    fun `onEvent OnLanguageChanged calls repository`() = runTest {
        coEvery { repository.saveLanguage(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnLanguageChanged(Language.ENGLISH))
        coVerify { repository.saveLanguage("ENGLISH") }
    }

    @Test
    fun `onEvent OnAutoPendingToReadingChanged calls repository`() = runTest {
        coEvery { repository.saveAutoStartReading(any()) } returns Unit
        coEvery { repository.savePromptStatusChange(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnAutoPendingToReadingChanged(true))
        coVerify { repository.saveAutoStartReading(true) }
    }

    @Test
    fun `onEvent OnAutoFinishReadingChanged calls repository`() = runTest {
        coEvery { repository.saveAutoFinishReading(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnAutoFinishChanged(true))
        coVerify { repository.saveAutoFinishReading(true) }
    }

    @Test
    fun `onEvent OnPromptStatusChangeChanged calls repository`() = runTest {
        coEvery { repository.savePromptStatusChange(any()) } returns Unit
        viewModel.onEvent(SettingsEvent.OnPromptStatusChangeChanged(true))
        coVerify { repository.savePromptStatusChange(true) }
    }
}
