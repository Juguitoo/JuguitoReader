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
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

    private fun TestScope.loadReaderViewModel(
        epubContent: EpubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList()),
        progress: ReadingProgress? = null,
        debounceMs: Long = 10L,
        useStandardDispatcher: Boolean = false
    ): ReaderViewModel {
        if (useStandardDispatcher) {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        }

        val resolvedProgress = progress ?: ReadingProgress(
            bookId = 1,
            totalChapters = epubContent.spine.size,
            lastChapterIndex = 0,
            scrollPosition = 0f,
            lastReadAt = 0L
        )

        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { getReadingProgressByIdUseCase(1) } returns resolvedProgress

        val vm = ReaderViewModel(
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
        val job = backgroundScope.launch { vm.uiState.collect { } }
        advanceUntilIdle()
        clearMocks(updateReadingProgressUseCase, answers = false, recordedCalls = true)
        vm.progressPersistDebounceMs = debounceMs
        job.cancel()
        return vm
    }

    /** VM con el parseo del EPUB aún pendiente: el estado sigue en Loading hasta `advanceUntilIdle()`. */
    private fun TestScope.createLoadingViewModel(
        epubContent: EpubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
    ): ReaderViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { getReadingProgressByIdUseCase(1) } returns ReadingProgress(
            bookId = 1,
            totalChapters = epubContent.spine.size,
            lastChapterIndex = 0,
            scrollPosition = 0f,
            lastReadAt = 0L
        )

        return ReaderViewModel(
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
    fun `loadData reconciles stale progress with current EPUB spine`() = runTest {
        val epubContent = EpubContent(
            baseDir = "",
            spine = listOf("ch1", "ch2", "ch3"),
            chaptersTree = emptyList()
        )
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { getReadingProgressByIdUseCase(1) } returns ReadingProgress(
            bookId = 1,
            totalChapters = 10,
            lastChapterIndex = 9,
            scrollPosition = 0.8f,
            lastReadAt = 0L
        )
        val progressSlot = slot<ReadingProgress>()
        coEvery { updateReadingProgressUseCase(capture(progressSlot)) } returns Result.success(Unit)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)
        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.currentChapterIndex).isEqualTo(2)
        assertThat(state.readingProgress.totalChapters).isEqualTo(3)
        assertThat(state.readingProgress.lastChapterIndex).isEqualTo(2)
        assertThat(state.readingProgress.scrollPosition).isEqualTo(0f)
        assertThat(state.currentChapterUrl).isEqualTo(
            "https://appassets.androidplatform.net/epub/ch3"
        )
        assertThat(progressSlot.captured.lastChapterIndex).isEqualTo(2)
        coVerify(exactly = 1) { updateReadingProgressUseCase(any()) }

        job.cancel()
    }

    @Test
    fun `loadData does not persist progress when it already matches EPUB spine`() = runTest {
        val epubContent = EpubContent(
            baseDir = "",
            spine = listOf("ch1", "ch2"),
            chaptersTree = emptyList()
        )
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)
        coEvery { getReadingProgressByIdUseCase(1) } returns ReadingProgress(
            bookId = 1,
            totalChapters = 2,
            lastChapterIndex = 1,
            scrollPosition = 0.4f,
            lastReadAt = 0L
        )

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)
        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.currentChapterIndex).isEqualTo(1)
        assertThat(state.readingProgress.scrollPosition).isEqualTo(0.4f)
        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }

        job.cancel()
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
    fun `onEvent OnScrollPositionChanged updates progress after debounce`() = runTest {
        val viewModel = loadReaderViewModel(useStandardDispatcher = true)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))
        runCurrent()
        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }

        advanceTimeBy(10)
        runCurrent()

        coVerify(exactly = 1) { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
    }

    @Test
    fun `multiple scroll events debounce to single persist with latest position`() = runTest {
        val viewModel = loadReaderViewModel(useStandardDispatcher = true)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.3f))
        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.5f))
        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))
        runCurrent()

        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }

        advanceTimeBy(10)
        runCurrent()

        coVerify(exactly = 1) { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
    }

    @Test
    fun `onEvent OnScrollPositionChanged updates ui state before persist`() = runTest {
        val viewModel = loadReaderViewModel(useStandardDispatcher = true)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))
        runCurrent()

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.readingProgress.scrollPosition).isEqualTo(0.7f)
        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }
    }

    @Test
    fun `onEvent OnFinishReading flushes pending scroll progress`() = runTest {
        val viewModel = loadReaderViewModel(useStandardDispatcher = true)

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))
        runCurrent()
        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }

        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
    }

    @Test
    fun `onEvent OnBackRequested flushes pending scroll progress`() = runTest {
        val viewModel = loadReaderViewModel(
            epubContent = EpubContent(baseDir = "", spine = List(10) { "ch$it" }, chaptersTree = emptyList()),
            useStandardDispatcher = true
        )

        viewModel.onEvent(ReaderEvent.OnScrollPositionChanged(0.7f))
        runCurrent()
        coVerify(exactly = 0) { updateReadingProgressUseCase(any()) }

        viewModel.onEvent(ReaderEvent.OnBackRequested)
        advanceUntilIdle()

        coVerify(exactly = 1) { updateReadingProgressUseCase(match { it.scrollPosition == 0.7f }) }
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
    fun `READER-011 scrolling backwards adds no words but rereading forward does`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(500))
        var state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(500)

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(200))
        state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(500)
        assertThat(state.lastReportedChapterWords).isEqualTo(200)

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(500))
        state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(800)

        job.cancel()
    }

    @Test
    fun `READER-015 restored scroll position is a baseline and credits no words`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnChapterWordsBaseline(600))
        viewModel.onEvent(ReaderEvent.OnReportWordsRead(600))

        var state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.lastReportedChapterWords).isEqualTo(600)
        assertThat(state.accumulatedReadWords).isEqualTo(0)

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(750))

        state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(150)

        job.cancel()
    }

    @Test
    fun `rereading a chapter credits its words again`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1", "ch2"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(300))
        viewModel.onEvent(ReaderEvent.OnNextChapter)
        viewModel.onEvent(ReaderEvent.OnReportWordsRead(400))
        viewModel.onEvent(ReaderEvent.OnPreviousChapter)
        viewModel.onEvent(ReaderEvent.OnReportWordsRead(300))

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(1_000)
        assertThat(state.lastReportedChapterWords).isEqualTo(300)

        job.cancel()
    }

    @Test
    fun `changing chapter keeps accumulated words of the session`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1", "ch2"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnReportWordsRead(200))
        viewModel.onEvent(ReaderEvent.OnNextChapter)

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.accumulatedReadWords).isEqualTo(200)
        assertThat(state.lastReportedChapterWords).isEqualTo(0)

        job.cancel()
    }

    @Test
    fun `READER-011 saving a session resets accumulated words`() = runTest {
        val epubContent = EpubContent(baseDir = "", spine = listOf("ch1"), chaptersTree = emptyList())
        coEvery { parseEpubUseCase(any(), any()) } returns Result.success(epubContent)

        viewModel = ReaderViewModel(getBookByIdUseCase, getReadingProgressByIdUseCase, getBookDailyReadingsUseCase, updateReadingProgressUseCase, addReadingProgressUseCase, addDailyReadingUseCase, updateBookUseCase, parseEpubUseCase, settingsRepository, savedStateHandle)

        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.currentTimeProvider = { 1_000L }
        viewModel.onEvent(ReaderEvent.OnStartReading)
        viewModel.onEvent(ReaderEvent.OnReportWordsRead(400))

        viewModel.currentTimeProvider = { 121_000L }
        viewModel.onEvent(ReaderEvent.OnToggleSessionsDialog)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Success
        assertThat(state.showSessionsDialog).isTrue()
        assertThat(state.accumulatedReadWords).isEqualTo(0)

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

    @Test
    fun `READER-014 session timer starts when loading finishes if reader is already resumed`() = runTest {
        val viewModel = createLoadingViewModel()
        viewModel.currentTimeProvider = { 1_000L }

        viewModel.onEvent(ReaderEvent.OnStartReading)
        assertThat(viewModel.uiState.value).isInstanceOf(ReaderUiState.Loading::class.java)

        advanceUntilIdle()

        val slot = slot<DailyReading>()
        coEvery { addDailyReadingUseCase(capture(slot)) } returns Result.success(Unit)
        viewModel.currentTimeProvider = { 121_000L }

        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        coVerify(exactly = 1) { addDailyReadingUseCase(any()) }
        assertThat(slot.captured.timeSpentMillis).isEqualTo(120_000)
    }

    @Test
    fun `READER-014 session timer does not start if reader was paused while loading`() = runTest {
        val viewModel = createLoadingViewModel()
        viewModel.currentTimeProvider = { 1_000L }

        viewModel.onEvent(ReaderEvent.OnStartReading)
        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        viewModel.currentTimeProvider = { 121_000L }
        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        coVerify(exactly = 0) { addDailyReadingUseCase(any()) }
    }

    @Test
    fun `READER-014 repeated OnStartReading keeps the original session start`() = runTest {
        val viewModel = createLoadingViewModel()
        viewModel.currentTimeProvider = { 1_000L }

        viewModel.onEvent(ReaderEvent.OnStartReading)
        advanceUntilIdle()

        viewModel.currentTimeProvider = { 61_000L }
        viewModel.onEvent(ReaderEvent.OnStartReading)

        val slot = slot<DailyReading>()
        coEvery { addDailyReadingUseCase(capture(slot)) } returns Result.success(Unit)
        viewModel.currentTimeProvider = { 121_000L }

        viewModel.onEvent(ReaderEvent.OnFinishReading)
        advanceUntilIdle()

        assertThat(slot.captured.timeSpentMillis).isEqualTo(120_000)
    }

    @Test
    fun `READER-012 render process gone recreates the WebView and then fails with Error`() = runTest {
        val viewModel = createLoadingViewModel()
        val job = backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onEvent(ReaderEvent.OnRenderProcessGone)
        advanceUntilIdle()
        assertThat((viewModel.uiState.value as ReaderUiState.Success).webViewInstanceKey).isEqualTo(1)

        viewModel.onEvent(ReaderEvent.OnRenderProcessGone)
        advanceUntilIdle()
        assertThat((viewModel.uiState.value as ReaderUiState.Success).webViewInstanceKey).isEqualTo(2)

        viewModel.onEvent(ReaderEvent.OnRenderProcessGone)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value).isInstanceOf(ReaderUiState.Error::class.java)

        job.cancel()
    }
}
