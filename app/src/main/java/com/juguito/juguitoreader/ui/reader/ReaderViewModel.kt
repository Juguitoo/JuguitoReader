package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.AddDailyReadingUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetBookDailyReadingsUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
import com.juguito.juguitoreader.ui.common.asUiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val PROGRESS_PERSIST_DEBOUNCE_MS = 2_000L

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getReadingProgressByIdUseCase: GetReadingProgressByIdUseCase,
    private val getBookDailyReadingsUseCase: GetBookDailyReadingsUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val addReadingProgressUseCase: AddReadingProgressUseCase,
    private val addDailyReadingUseCase: AddDailyReadingUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val parseEpubUseCase: ParseEpubUseCase,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _internalState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = combine(
        _internalState,
        settingsRepository.textZoomFlow,
        settingsRepository.readerThemeFlow,
        settingsRepository.readerBrightnessFlow
    ) { state, zoom, themeStr, brightness ->
        if (state is ReaderUiState.Success) {
            val theme = runCatching { ReaderTheme.valueOf(themeStr) }.getOrDefault(ReaderTheme.SEPIA)
            state.copy(
                textZoom = zoom,
                theme = theme,
                brightness = brightness
            )
        } else {
            state
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderUiState.Loading
    )

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])
    private var initialPercentage: Int = -1
    private var sessionStartTimeMillis: Long = 0L
    internal var currentTimeProvider: () -> Long = { System.currentTimeMillis() }

    private var persistProgressJob: Job? = null
    private var lastPersistedProgress: ReadingProgress? = null
    internal var progressPersistDebounceMs: Long = PROGRESS_PERSIST_DEBOUNCE_MS

    init {
        loadData()
    }

    private fun loadData() {
        _internalState.value = ReaderUiState.Loading

        viewModelScope.launch {
            try {
                val themeStr = settingsRepository.readerThemeFlow.first()
                val zoom = settingsRepository.textZoomFlow.first()
                val brightness = settingsRepository.readerBrightnessFlow.first()
                val initialTheme = runCatching { ReaderTheme.valueOf(themeStr) }.getOrDefault(ReaderTheme.SEPIA)

                val book = getBookByIdUseCase.invoke(bookId)
                    ?: throw Exception("error_epub_not_found")
                val localPath = book.localFilePath
                    ?: throw Exception("error_epub_not_found")

                val parseResult = parseEpubUseCase.invoke(bookId, localPath)
                parseResult.fold(
                    onSuccess = { content ->
                        var progress = getReadingProgressByIdUseCase.invoke(bookId)
                        if (progress == null) {
                            val newProgress = ReadingProgress(bookId, content.spine.size ,0, 0f, currentTimeProvider())
                            addReadingProgressUseCase.invoke(newProgress)
                            progress = newProgress
                        }

                        val safeChapterIndex = progress.lastChapterIndex.coerceIn(0, content.spine.lastIndex)
                        val chapterChanged = safeChapterIndex != progress.lastChapterIndex
                        val totalChaptersChanged = progress.totalChapters != content.spine.size

                        if (chapterChanged || totalChaptersChanged) {
                            progress = progress.copy(
                                totalChapters = content.spine.size,
                                lastChapterIndex = safeChapterIndex,
                                scrollPosition = if (chapterChanged) 0f else progress.scrollPosition,
                                lastReadAt = currentTimeProvider()
                            )
                            updateReadingProgressUseCase.invoke(progress)
                        }

                        initialPercentage = progress.percentage

                        _internalState.value = ReaderUiState.Success(
                            book = book,
                            epubContent = content,
                            readingProgress = progress,
                            currentChapterIndex = progress.lastChapterIndex,
                            isControlsVisible = true,
                            textZoom = zoom,
                            theme = initialTheme,
                            brightness = brightness,
                            bookSessions = emptyList()
                        )

                        viewModelScope.launch {
                            getBookDailyReadingsUseCase.invoke(bookId).collect { sessions ->
                                val currentState = _internalState.value
                                if (currentState is ReaderUiState.Success) _internalState.value = currentState.copy(bookSessions = sessions)
                            }
                        }
                    },
                    onFailure = { exception ->
                        _internalState.value = ReaderUiState.Error(exception.asUiText())
                    }
                )
            } catch (e: Exception) {
                _internalState.value = ReaderUiState.Error(e.asUiText())
            }
        }
    }

    fun onEvent(event: ReaderEvent) {
        val currentState = _internalState.value
        if (currentState !is ReaderUiState.Success) return

        when (event) {
            is ReaderEvent.OnChapterSelected -> {
                updateChapter(currentState, event.index)
            }
            is ReaderEvent.OnNextChapter -> {
                updateChapter(currentState, currentState.currentChapterIndex + 1)
            }
            is ReaderEvent.OnPreviousChapter -> {
                updateChapter(currentState, currentState.currentChapterIndex - 1)
            }
            is ReaderEvent.OnToggleControls -> {
                _internalState.value = currentState.copy(isControlsVisible = !currentState.isControlsVisible)
            }
            is ReaderEvent.OnTextZoomChanged -> {
                _internalState.value = currentState.copy(textZoom = event.zoom)
                viewModelScope.launch { settingsRepository.saveTextZoom(event.zoom) }
            }
            is ReaderEvent.OnThemeChanged -> {
                _internalState.value = currentState.copy(theme = event.theme)
                viewModelScope.launch { settingsRepository.saveReaderTheme(event.theme.name) }
            }
            is ReaderEvent.OnBrightnessChanged -> {
                _internalState.value = currentState.copy(brightness = event.brightness)
                viewModelScope.launch { settingsRepository.saveReaderBrightness(event.brightness) }
            }
            is ReaderEvent.OnScrollPositionChanged -> {
                val updatedProgress = currentState.readingProgress.copy(scrollPosition = event.scrollPosition, lastReadAt = currentTimeProvider())
                _internalState.value = currentState.copy(readingProgress = updatedProgress)
                scheduleProgressPersist(updatedProgress)
            }
            is ReaderEvent.OnTimeRemainingChanged -> {
                _internalState.value = currentState.copy(timeRemaining = event.minutes)
            }
            is ReaderEvent.OnBackRequested -> {
                handleBackRequest(currentState)
            }
            is ReaderEvent.OnStatusPromptResult -> {
                handlePromptResult(currentState, event.changeToReading)
            }
            is ReaderEvent.OnFinishReading -> {
                flushProgressPersist()
                saveCurrentReadingSession(currentState)
            }
            is ReaderEvent.OnStartReading -> {
                sessionStartTimeMillis = currentTimeProvider()
            }
            is ReaderEvent.OnToggleSessionsDialog -> {
                val willShow = !currentState.showSessionsDialog

                if (willShow) {
                    saveCurrentReadingSession(currentState)
                    sessionStartTimeMillis = currentTimeProvider()
                    _internalState.value = currentState.copy(showSessionsDialog = true)
                } else {
                    _internalState.value = currentState.copy(showSessionsDialog = false)
                }
            }
            is ReaderEvent.OnReportWordsRead -> {
                val delta = event.words - currentState.lastReportedChapterWords
                val newAccumulated = if (delta > 0) currentState.accumulatedReadWords + delta else currentState.accumulatedReadWords
                _internalState.value = currentState.copy(
                    lastReportedChapterWords = event.words,
                    accumulatedReadWords = newAccumulated
                )
            }
        }
    }

    private fun updateChapter(currentState: ReaderUiState.Success, newIndex: Int) {
        persistProgressJob?.cancel()
        persistProgressJob = null
        val spineSize = currentState.epubContent.spine.size

        if (newIndex !in 0..<spineSize) return

        val updatedProgress = currentState.readingProgress.copy(
            lastChapterIndex = newIndex,
            scrollPosition = 0f,
            lastReadAt = currentTimeProvider()
        )

        _internalState.value = currentState.copy(
            currentChapterIndex = newIndex,
            readingProgress = updatedProgress,
            lastReportedChapterWords = 0
        )

        viewModelScope.launch {
            persistProgress(updatedProgress)
        }
    }

    private fun handleBackRequest(currentState: ReaderUiState.Success) {
        flushProgressPersist()
        if (sessionStartTimeMillis > 0L) saveCurrentReadingSession(currentState)
        val currentPercentage = currentState.readingProgress.percentage
        val sessionDelta = currentPercentage - initialPercentage

        viewModelScope.launch {
            val shouldPrompt = settingsRepository.promptStatusChangeFlow.first()
            val autoStart = settingsRepository.autoStartReadingFlow.first()
            if (currentState.book.status == BookStatus.PENDING && sessionDelta >= 15 && !autoStart && shouldPrompt) {
                _internalState.value = currentState.copy(showStatusPrompt = true)
            } else {
                _effect.send(UiEffect.NavigateBack)
            }
        }
    }

    private fun handlePromptResult(currentState: ReaderUiState.Success, changeToReading: Boolean) {
        _internalState.value = currentState.copy(showStatusPrompt = false)
        flushProgressPersist()

        viewModelScope.launch {
            if (changeToReading) {
                updateBookUseCase.invoke(currentState.book.copy(status = BookStatus.READING))
            }
            _effect.send(UiEffect.NavigateBack)
        }
    }

    private fun saveCurrentReadingSession(currentState: ReaderUiState.Success) {
        if (sessionStartTimeMillis <= 0L) return

        val sessionEndTimeMillis = currentTimeProvider()
        val deltaTimeMillis = sessionEndTimeMillis - sessionStartTimeMillis
        if (deltaTimeMillis > 60_000) {
            val todayDateString = LocalDate.now().toString()
            val sessionMinutes = deltaTimeMillis / 60000.0
            val speedWpm = (currentState.accumulatedReadWords / sessionMinutes).toInt()
            val dailyReading = DailyReading(
                bookId = currentState.book.id,
                date = todayDateString,
                timeSpentMillis = deltaTimeMillis.toInt(),
                reachedPercentage = currentState.readingProgress.percentage.toFloat(),
                readingSpeed = speedWpm
            )
            viewModelScope.launch {
                addDailyReadingUseCase.invoke(dailyReading)
            }
        }
        _internalState.value = currentState.copy(accumulatedReadWords = 0)
        sessionStartTimeMillis = 0L
    }

    private fun scheduleProgressPersist(progress: ReadingProgress) {
        persistProgressJob?.cancel()
        persistProgressJob = viewModelScope.launch {
            delay(progressPersistDebounceMs)
            persistProgress(progress)
        }
    }

    private fun flushProgressPersist() {
        persistProgressJob?.cancel()
        persistProgressJob = null
        val state = _internalState.value as? ReaderUiState.Success ?: return
        viewModelScope.launch {
            persistProgress(state.readingProgress)
        }
    }

    private suspend fun persistProgress(progress: ReadingProgress) {
        if (lastPersistedProgress?.hasSamePersistedValues(progress) == true) return
        updateReadingProgressUseCase.invoke(progress)
        lastPersistedProgress = progress
    }

    private fun ReadingProgress.hasSamePersistedValues(other: ReadingProgress): Boolean =
        bookId == other.bookId &&
            totalChapters == other.totalChapters &&
            lastChapterIndex == other.lastChapterIndex &&
            scrollPosition == other.scrollPosition
}
