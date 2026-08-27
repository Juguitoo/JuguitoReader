package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.EpubContent
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.AddDailyReadingUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetBookDailyReadingsUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val getBookDailyReadingsUseCase = mockk<GetBookDailyReadingsUseCase>()
    private val updateReadingProgressUseCase = mockk<UpdateReadingProgressUseCase>()
    private val addReadingProgressUseCase = mockk<AddReadingProgressUseCase>()
    private val addDailyReadingUseCase = mockk<AddDailyReadingUseCase>()
    private val updateBookUseCase = mockk<UpdateBookUseCase>()
    private val parseEpubUseCase = mockk<ParseEpubUseCase>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val savedStateHandle = SavedStateHandle(mapOf("bookId" to 1))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { settingsRepository.textZoomFlow } returns flowOf(100)
        every { settingsRepository.readerThemeFlow } returns flowOf("SEPIA")
        every { settingsRepository.readerBrightnessFlow } returns flowOf(0.5f)
        every { settingsRepository.promptStatusChangeFlow } returns flowOf(true)
        every { settingsRepository.autoStartReadingFlow } returns flowOf(false)
        every { settingsRepository.autoFinishReadingFlow } returns flowOf(false)
        
        coEvery { getBookByIdUseCase(1) } returns Book(id = 1, title = "T", author = "A", isPhysical = false, status = BookStatus.PENDING, localFilePath = "path")
        coEvery { getReadingProgressByIdUseCase(1) } returns ReadingProgress(bookId = 1, totalChapters = 10, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L)

        coEvery { updateReadingProgressUseCase(any()) } returns Result.success(Unit)
        coEvery { addReadingProgressUseCase(any()) } returns Result.success(Unit)
        coEvery { addDailyReadingUseCase(any()) } returns Result.success(Unit)
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)
        coEvery { getBookDailyReadingsUseCase(any()) } returns flowOf(listOf(DailyReading(1, "0000-00-00", 300, 10f, readingSpeed = 0)))

        viewModel = ReaderViewModel(
            getBookByIdUseCase,
            getReadingProgressByIdUseCase,
            getBookDailyReadingsUseCase,
            updateReadingProgressUseCase,
            addReadingProgressUseCase,
            addDailyReadingUseCase,
            updateBookUseCase,
            parseEpubUseCase,
            settingsRepository,
            savedStateHandle
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Error if parse fails`() = runTest {
        coEvery { parseEpubUseCase(any(), any()) } returns Result.failure(Exception("Parse error"))
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

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
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

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
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

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
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))

        coVerify { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
    }

    @Test
    fun `onEvent OnToggleControls toggles visibility`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

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
        
        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnTimeRemainingChanged(15))

        viewModel.uiState.test {
            val state = awaitItem() as ReaderUiState.Success
            assertThat(state.timeRemaining).isEqualTo(15)
        }
    }

    @Test
    fun `onEvent OnBackRequested with 20 percent increment`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = List(10) {"ch$it"}, chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase,updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnChapterSelected(2))

        viewModel.onEvent(ReaderEvent.OnBackRequested)

        viewModel.uiState.test {
            val state = awaitItem() as ReaderUiState.Success
            assertThat(state.showStatusPrompt).isEqualTo(true)
        }
    }

    @Test
    fun `onEvent OnBackRequested with 15 percent increment`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = List(10) {"ch$it"}, chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnChapterSelected(1))

        viewModel.onEvent(ReaderEvent.OnBackRequested)

        viewModel.uiState.test {
            val state = awaitItem() as ReaderUiState.Success
            assertThat(state.showStatusPrompt).isEqualTo(false)
        }
    }

    @Test
    fun `onEvent OnStatusPromptResult accept`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = List(10) { "ch$it" }, chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnChapterSelected(2))
        viewModel.onEvent(ReaderEvent.OnBackRequested)

        viewModel.effect.test {
            viewModel.onEvent(ReaderEvent.OnStatusPromptResult(changeToReading = true))

            coVerify(exactly = 1) { updateBookUseCase(match { it.status == BookStatus.READING }) }
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `onEvent OnStatusPromptResult reject`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = List(10) { "ch$it" }, chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { updateBookUseCase(any()) } returns Result.success(Unit)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        viewModel.onEvent(ReaderEvent.OnChapterSelected(2))
        viewModel.onEvent(ReaderEvent.OnBackRequested)

        viewModel.effect.test {
            viewModel.onEvent(ReaderEvent.OnStatusPromptResult(changeToReading = false))

            coVerify(exactly = 0) { updateBookUseCase(any()) }
            assertThat(awaitItem()).isEqualTo(UiEffect.NavigateBack)
        }
    }

    @Test
    fun `OnReportWordsRead calculates delta correctly and accumulates`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(100))
        var state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.lastReportedChapterWords).isEqualTo(100)
        assertThat(state.accumulatedReadWords).isEqualTo(100)

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(150))
        state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.lastReportedChapterWords).isEqualTo(150)
        assertThat(state.accumulatedReadWords).isEqualTo(150)

        job.cancel()
    }

    @Test
    fun `updateChapter resets lastReportedChapterWords but keeps accumulatedReadWords`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1", "ch2"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(200))
        viewModel.onEvent(ReaderEvent.OnNextChapter)

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.lastReportedChapterWords).isEqualTo(0)
        assertThat(state.accumulatedReadWords).isEqualTo(200)

        job.cancel()
    }

    @Test
    fun `saveCurrentReadingSession calculates reading speed correctly`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.currentTimeProvider = { 1000L }
        viewModel.onEvent(ReaderEvent.OnStartReading)
        viewModel.onEvent(ReaderEvent.OnReportWordsRead(400))

        viewModel.currentTimeProvider = { 121_000L }

        val slot = slot<DailyReading>()
        coEvery { addDailyReadingUseCase.invoke(capture(slot)) } returns Result.success(Unit)

        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        assertThat(slot.captured.timeSpentMillis).isEqualTo(120_000)
        assertThat(slot.captured.readingSpeed).isEqualTo(200)

        job.cancel()
    }
}
