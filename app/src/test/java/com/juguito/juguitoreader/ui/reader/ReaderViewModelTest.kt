package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
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
class ReaderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: ReaderViewModel
    private val getBookByIdUseCase = mockk<GetBookByIdUseCase>()
    private val getReadingProgressByIdUseCase = mockk<GetReadingProgressByIdUseCase>()
    private val updateReadingProgressUseCase = mockk<UpdateReadingProgressUseCase>()
    private val addReadingProgressUseCase = mockk<AddReadingProgressUseCase>()
    private val parseEpubUseCase = mockk<ParseEpubUseCase>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val savedStateHandle = SavedStateHandle(mapOf("bookId" to 1))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { settingsRepository.textZoomFlow } returns flowOf(100)
        every { settingsRepository.readerThemeFlow } returns flowOf("SEPIA")
        every { settingsRepository.readerBrightnessFlow } returns flowOf(0.5f)
        
        coEvery { getBookByIdUseCase(1) } returns Book(id = 1, title = "T", author = "A", isPhysical = false, localFilePath = "path")
        coEvery { getReadingProgressByIdUseCase(1) } returns ReadingProgress(bookId = 1, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Error if parse fails`() = runTest {
        coEvery { parseEpubUseCase(any(), any()) } returns Result.failure(Exception("Parse error"))
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ReaderUiState.Error::class.java)
        }
    }

    @Test
    fun `onEvent updates settings correctly`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1", "ch2"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { settingsRepository.saveTextZoom(any()) } returns Unit
        coEvery { settingsRepository.saveReaderTheme(any()) } returns Unit
        coEvery { settingsRepository.saveReaderBrightness(any()) } returns Unit
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnTextZoomChanged(120))
        viewModel.onEvent(ReaderEvent.OnThemeChanged(ReaderTheme.NIGHT))
        viewModel.onEvent(ReaderEvent.OnBrightnessChanged(0.8f))

        coVerify { settingsRepository.saveTextZoom(120) }
        coVerify { settingsRepository.saveReaderTheme("NIGHT") }
        coVerify { settingsRepository.saveReaderBrightness(0.8f) }
    }

    @Test
    fun `onEvent chapter navigation updates progress`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1", "ch2"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { updateReadingProgressUseCase(any()) } returns Result.success(Unit)
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnNextChapter)
        coVerify { updateReadingProgressUseCase(match { it.lastChapterIndex == 1 }) }

        viewModel.onEvent(ReaderEvent.OnPreviousChapter)
        coVerify { updateReadingProgressUseCase(match { it.lastChapterIndex == 0 }) }
        
        viewModel.onEvent(ReaderEvent.OnChapterSelected(1))
        coVerify { updateReadingProgressUseCase(match { it.lastChapterIndex == 1 }) }
    }

    @Test
    fun `onEvent OnScrollPositionChanged updates progress`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { updateReadingProgressUseCase(any()) } returns Result.success(Unit)
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))

        coVerify { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
    }

    @Test
    fun `onEvent OnToggleControls toggles visibility`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnToggleControls)

        viewModel.uiState.test {
            val state = awaitItem() as ReaderUiState.Success
            assertThat(state.isControlsVisible).isFalse()
        }
    }

    @Test
    fun `onEvent OnTimeRemainingChanged updates state`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnTimeRemainingChanged(15))

        viewModel.uiState.test {
            val state = awaitItem() as ReaderUiState.Success
            assertThat(state.timeRemaining).isEqualTo(15)
        }
    }
}
