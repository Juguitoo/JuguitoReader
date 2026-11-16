package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.model.hasSamePersistedValues
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.bookmark.AddBookmarkUseCase
import com.juguito.juguitoreader.domain.usecase.bookmark.DeleteBookmarkUseCase
import com.juguito.juguitoreader.domain.usecase.bookmark.GetBookBookmarksUseCase
import com.juguito.juguitoreader.domain.usecase.bookmark.NewBookmark
import com.juguito.juguitoreader.domain.usecase.bookmark.UpdateBookmarkUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.AddDailyReadingUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.DeleteBookDailyReadingUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetBookDailyReadingsUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.asUiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect.NavigateBack
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect.ShowSnackbar
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val PROGRESS_PERSIST_DEBOUNCE_MS = 2_000L
private const val MAX_RENDERER_RECOVERIES = 2
private const val MAX_CHAPTER_WORDS = 200_000

private const val MAX_BASE_CREDIT_WORDS = 1_000L

data class MenuAnchor(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class TextSelection(
    val quote: String,
    val prefix: String,
    val suffix: String,
    val scrollPosition: Float,
    val anchor: MenuAnchor = MenuAnchor(0f, 0f, 0f, 0f)
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getReadingProgressByIdUseCase: GetReadingProgressByIdUseCase,
    private val getBookDailyReadingsUseCase: GetBookDailyReadingsUseCase,
    private val getBookBookmarksUseCase: GetBookBookmarksUseCase,
    private val addReadingProgressUseCase: AddReadingProgressUseCase,
    private val addDailyReadingUseCase: AddDailyReadingUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val deleteBookDailyReadingUseCase: DeleteBookDailyReadingUseCase,
    private val deleteBookmarkUseCase: DeleteBookmarkUseCase,
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
    internal var currentTimeProvider: () -> Long = { System.currentTimeMillis() }
    private var ignoreControlsUntil: Long = 0L
    private val sessionClock = ReadingSessionClock(now = { currentTimeProvider() })
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
                    ?: throw JuguitoException(R.string.error_book_not_found)
                val localPath = book.localFilePath
                    ?: throw JuguitoException(R.string.error_epub_not_found)

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
                            bookSessions = emptyList(),
                            bookmarks = emptyList()
                        )

                        startSessionTimerIfPossible()

                        viewModelScope.launch {
                            getBookDailyReadingsUseCase.invoke(bookId).collect { sessions ->
                                val currentState = _internalState.value
                                if (currentState is ReaderUiState.Success) _internalState.value = currentState.copy(bookSessions = sessions)
                            }
                        }

                        viewModelScope.launch {
                            getBookBookmarksUseCase.invoke(bookId).collect { bookmarks ->
                                val currentState = _internalState.value
                                if (currentState is ReaderUiState.Success) _internalState.value = currentState.copy(bookmarks = bookmarks)
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
        if (event is ReaderEvent.OnStartReading) return onReaderResumed()
        if (event is ReaderEvent.OnFinishReading) return onReaderPaused()

        val currentState = _internalState.value as? ReaderUiState.Success ?: return

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
                if (currentTimeProvider() < ignoreControlsUntil) return
                if (
                    currentState.showBookmarkList ||
                    currentState.showNewNote ||
                    currentState.selectedBookmark != null ||
                    currentState.showSessionsDialog ||
                    currentState.showStatusPrompt
                ) return
                if (currentState.markMenuId != null) {
                    updateSuccessState { it.copy(markMenuId = null, markMenuAnchor = null) }
                    return
                }
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
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                val updatedProgress = currentState.readingProgress.copy(scrollPosition = event.scrollPosition, lastReadAt = currentTimeProvider())
                _internalState.value = currentState.copy(readingProgress = updatedProgress)
                scheduleProgressPersist(updatedProgress)
            }
            is ReaderEvent.OnTimeRemainingChanged -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                _internalState.value = currentState.copy(timeRemaining = event.minutes)
            }
            is ReaderEvent.OnBackRequested -> {
                handleBackRequest(currentState)
            }
            is ReaderEvent.OnStatusPromptResult -> {
                handlePromptResult(currentState, event.changeToReading)
            }
            is ReaderEvent.OnToggleSessionsDialog -> {
                if (currentState.showSessionsDialog) {
                    updateSuccessState { it.copy(showSessionsDialog = false) }
                    startSessionTimerIfPossible()
                } else {
                    sessionClock.freeze()
                    saveCurrentReadingSession(currentState)
                    updateSuccessState { it.copy(showSessionsDialog = true) }
                }
            }
            is ReaderEvent.OnReportWordsRead -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                creditWordsRead(currentState, event.words)
            }
            is ReaderEvent.OnChapterWordsBaseline -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                _internalState.value = currentState.copy(lastReportedChapterWords = event.words)
            }
            is ReaderEvent.OnRenderProcessGone -> {
                handleRenderProcessGone(currentState)
            }
            is ReaderEvent.OnDeleteDailyReading -> {
                viewModelScope.launch {
                    deleteBookDailyReadingUseCase(currentState.book.id, event.date)
                        .onFailure { _effect.send(ShowSnackbar(it.asUiText())) }
                }
            }
            is ReaderEvent.OnAddBookmark -> addBookmark(currentState, event.bookmark)
            is ReaderEvent.OnCloseNote -> {
                updateSuccessState { it.copy(selectedBookmark = null) }
            }
            is ReaderEvent.OnDeleteBookmark -> {
                viewModelScope.launch {
                    deleteBookmarkUseCase(event.id)
                        .onFailure { _effect.send(ShowSnackbar(it.asUiText())) }
                        .onSuccess {
                            updateSuccessState { state ->
                                state.copy(
                                    markMenuId = state.markMenuId.takeUnless { it == event.id },
                                    markMenuAnchor = state.markMenuAnchor.takeUnless { state.markMenuId == event.id },
                                    selectedBookmark = state.selectedBookmark?.takeIf { it.id != event.id },
                                    openImageId = state.openImageId.takeUnless { it == event.id }
                                )
                            }
                        }
                }
            }
            is ReaderEvent.OnEditBookmark -> {
                viewModelScope.launch {
                    val bookmark = currentState.bookmarks.firstOrNull { it.id == event.id }?.copy(color = event.color, noteText = event.noteText)
                    if (bookmark == null) return@launch
                    updateBookmarkUseCase(bookmark)
                        .onFailure { _effect.send(ShowSnackbar(it.asUiText())) }
                        .onSuccess {
                            updateSuccessState { it.copy(selectedBookmark = null) }
                        }
                }
            }
            is ReaderEvent.OnOpenNote -> {
                val bookmark = currentState.bookmarks.firstOrNull { it.id == event.id }
                if (bookmark == null || bookmark.kind == BookmarkKind.POSITION) return
                suppressControls()
                updateSuccessState { it.copy(selectedBookmark = bookmark, markMenuId = null, markMenuAnchor = null, showNewNote = false) }
            }
            is ReaderEvent.OnMoveToBookmark -> {
                val newIndex = currentState.epubContent.spine.indexOf(event.bookmark.chapterHref)
                if (newIndex == -1) return
                if (newIndex != currentState.currentChapterIndex) updateChapter(currentState, newIndex)
                updateSuccessState { it.copy(showBookmarkList = false, pendingJump = event.bookmark) }
                startSessionTimerIfPossible()
            }
            is ReaderEvent.OnToggleBookmarksList -> {
                if (currentState.showBookmarkList && currentState.libraryTab == LibraryTab.BOOKMARKS) {
                    updateSuccessState { it.copy(showBookmarkList = false, openImageId = null) }
                    startSessionTimerIfPossible()
                } else {
                    sessionClock.freeze()
                    updateSuccessState { it.copy(showBookmarkList = true, libraryTab = LibraryTab.BOOKMARKS) }
                }
            }
            is ReaderEvent.OnToggleImageList -> {
                if (currentState.showBookmarkList && currentState.libraryTab == LibraryTab.IMAGES) {
                    updateSuccessState { it.copy(showBookmarkList = false, openImageId = null) }
                    startSessionTimerIfPossible()
                } else {
                    sessionClock.freeze()
                    updateSuccessState { it.copy(showBookmarkList = true, libraryTab = LibraryTab.IMAGES) }
                }
            }
            is ReaderEvent.OnSelectLibraryTab -> {
                updateSuccessState { it.copy(libraryTab = event.tab, openImageId = null) }
            }
            is ReaderEvent.OnCloseLibrarySheet -> {
                if (!currentState.showBookmarkList) return
                updateSuccessState { it.copy(showBookmarkList = false, openImageId = null) }
                startSessionTimerIfPossible()
            }
            is ReaderEvent.OnBookmarkJumpFinished -> {
                updateSuccessState { it.copy(pendingJump = null) }
            }
            is ReaderEvent.OnTextSelected -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                if (event.overlapsMark) {
                    suppressControls()
                    updateSuccessState { it.copy(pendingSelection = null) }
                    viewModelScope.launch {
                        _effect.send(ShowSnackbar(UiText.StringResource(R.string.error_bookmark_overlap)))
                    }
                    return
                }
                val quote = event.quote.trim()
                if (quote.isEmpty()) {
                    if (currentState.pendingSelection != null) suppressControls()
                    updateSuccessState { it.copy(pendingSelection = null) }
                } else {
                    updateSuccessState {
                        it.copy(
                            pendingSelection = TextSelection(
                                quote = quote,
                                prefix = event.prefix,
                                suffix = event.suffix,
                                scrollPosition = event.scrollPosition,
                                anchor = MenuAnchor(event.anchorLeft, event.anchorTop, event.anchorRight, event.anchorBottom)
                            ),
                            markMenuId = null,
                            markMenuAnchor = null
                        )
                    }
                }
            }
            is ReaderEvent.OnRequestBookmarkHere -> {
                updateSuccessState { it.copy(captureBookmarkRequest = it.captureBookmarkRequest + 1) }
            }
            is ReaderEvent.OnBookmarkCaptured -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                val imageSrc = event.imageSrc.trim().ifBlank { null }
                val quote = event.quote.trim()
                if (quote.isEmpty() && imageSrc == null) {
                    viewModelScope.launch {
                        _effect.send(ShowSnackbar(UiText.StringResource(R.string.error_bookmark_empty_text)))
                    }
                    return
                }
                addBookmark(
                    currentState,
                    NewBookmark.Position(
                        bookId = bookId,
                        chapterHref = "",
                        quote = quote,
                        prefix = event.prefix,
                        suffix = event.suffix,
                        scrollPosition = event.scrollPosition,
                        imageSrc = imageSrc
                    )
                )
            }
            is ReaderEvent.OnOpenNewNote -> {
                if (currentState.pendingSelection == null) return
                suppressControls()
                updateSuccessState { it.copy(showNewNote = true, markMenuId = null, markMenuAnchor = null) }
            }
            is ReaderEvent.OnCloseNewNote -> {
                updateSuccessState { it.copy(showNewNote = false) }
            }
            is ReaderEvent.OnPinImage -> {
                if (isStaleJsChapter(event.chapterIndex, currentState.currentChapterIndex)) return
                val src = event.imageSrc.trim()
                if (src.isEmpty()) return
                suppressControls()
                addBookmark(
                    currentState,
                    NewBookmark.Image(
                        bookId = bookId,
                        chapterHref = "",
                        quote = event.alt.trim(),
                        prefix = "",
                        suffix = "",
                        scrollPosition = event.scrollPosition,
                        imageSrc = src
                    )
                )
            }
            is ReaderEvent.OnOpenPinnedImage -> {
                val mark = currentState.bookmarks.firstOrNull { it.id == event.id } ?: return
                if (mark.kind != BookmarkKind.IMAGE) return
                updateSuccessState { it.copy(openImageId = mark.id) }
            }
            is ReaderEvent.OnClosePinnedImage -> {
                updateSuccessState { it.copy(openImageId = null) }
            }
            is ReaderEvent.OnMarkTapped -> {
                val mark = currentState.bookmarks.firstOrNull { it.id == event.id } ?: return
                if (mark.kind == BookmarkKind.POSITION || mark.kind == BookmarkKind.IMAGE) return
                if (currentState.markMenuId == mark.id) {
                    updateSuccessState { it.copy(markMenuId = null, markMenuAnchor = null) }
                    return
                }
                suppressControls()
                updateSuccessState {
                    it.copy(
                        markMenuId = mark.id,
                        markMenuAnchor = MenuAnchor(event.anchorLeft, event.anchorTop, event.anchorRight, event.anchorBottom),
                        pendingSelection = null
                    )
                }
            }
            is ReaderEvent.OnCloseMarkMenu -> {
                updateSuccessState { it.copy(markMenuId = null, markMenuAnchor = null) }
            }
        }
    }

    private fun onReaderResumed() {
        sessionClock.onResumed()
        startSessionTimerIfPossible()
    }

    private fun onReaderPaused() {
        flushProgressPersist()
        sessionClock.onPaused()
        val currentState = _internalState.value as? ReaderUiState.Success ?: return
        saveCurrentReadingSession(currentState)
    }

    private fun startSessionTimerIfPossible() {
        if (_internalState.value !is ReaderUiState.Success) return
        sessionClock.tryStart()
    }

    private fun resetSession() {
        updateSuccessState { it.copy(accumulatedReadWords = 0) }
        sessionClock.reset()
    }

    private fun handleRenderProcessGone(currentState: ReaderUiState.Success) {
        flushProgressPersist()

        if (currentState.webViewInstanceKey >= MAX_RENDERER_RECOVERIES) {
            _internalState.value = ReaderUiState.Error(UiText.StringResource(R.string.error_reader_render_process_gone))
            return
        }
        updateSuccessState { it.copy(webViewInstanceKey = it.webViewInstanceKey + 1) }
    }

    private fun handleBackRequest(currentState: ReaderUiState.Success) {
        flushProgressPersist()
        saveCurrentReadingSession(currentState)
        resetSession()
        val currentPercentage = currentState.readingProgress.percentage
        val sessionDelta = currentPercentage - initialPercentage

        viewModelScope.launch {
            val shouldPrompt = settingsRepository.promptStatusChangeFlow.first()
            val autoStart = settingsRepository.autoStartReadingFlow.first()
            if (currentState.book.status == BookStatus.PENDING && sessionDelta >= 15 && !autoStart && shouldPrompt) {
                updateSuccessState { it.copy(showStatusPrompt = true) }
            } else {
                _effect.send(NavigateBack)
            }
        }
    }

    private fun handlePromptResult(currentState: ReaderUiState.Success, changeToReading: Boolean) {
        updateSuccessState { it.copy(showStatusPrompt = false) }
        flushProgressPersist()

        viewModelScope.launch {
            if (changeToReading) {
                updateBookUseCase.invoke(currentState.book.copy(status = BookStatus.READING))
            }
            _effect.send(UiEffect.NavigateBack)
        }
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

    private fun saveCurrentReadingSession(currentState: ReaderUiState.Success) {
        val sessionDuration = sessionClock.durationMillis()
        if (sessionDuration <= 0L || !sessionClock.shouldPersist()) return

        val sessionMinutes = sessionDuration / 60000.0
        val dailyReading = DailyReading(
            bookId = currentState.book.id,
            date = LocalDate.now().toString(),
            timeSpentMillis = sessionDuration.toInt(),
            reachedPercentage = currentState.readingProgress.percentage.toFloat(),
            readingSpeed = (currentState.accumulatedReadWords / sessionMinutes).toInt()
        )
        resetSession()
        viewModelScope.launch {
            addDailyReadingUseCase.invoke(dailyReading)
        }
    }

    private fun suppressControls() {
        ignoreControlsUntil = currentTimeProvider() + 500L
    }

    private fun addBookmark(currentState: ReaderUiState.Success, incoming: NewBookmark) {
        val href = currentState.epubContent.spine[currentState.currentChapterIndex]
        val draft = when (incoming) {
            is NewBookmark.Highlight -> incoming.copy(bookId = bookId, chapterHref = href)
            is NewBookmark.Note -> incoming.copy(bookId = bookId, chapterHref = href)
            is NewBookmark.Position -> incoming.copy(bookId = bookId, chapterHref = href)
            is NewBookmark.Image -> incoming.copy(bookId = bookId, chapterHref = href)
        }
        viewModelScope.launch {
            addBookmarkUseCase(draft)
                .onFailure { _effect.send(ShowSnackbar(it.asUiText())) }
                .onSuccess {
                    if (draft is NewBookmark.Image) {
                        _effect.send(ShowSnackbar(UiText.StringResource(R.string.bookmark_image_saved)))
                    } else if (draft !is NewBookmark.Position) {
                        updateSuccessState {
                            it.copy(
                                pendingSelection = null,
                                showNewNote = false,
                                clearSelectionRequest = it.clearSelectionRequest + 1
                            )
                        }
                    }
                }
        }
    }

    private fun isStaleJsChapter(chapterIndex: Int, currentChapterIndex: Int): Boolean =
        chapterIndex >= 0 && chapterIndex != currentChapterIndex

    private fun updateSuccessState(transform: (ReaderUiState.Success) -> ReaderUiState.Success) {
        _internalState.update { state -> if (state is ReaderUiState.Success) transform(state) else state }
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
            lastReportedChapterWords = 0,
            timeRemaining = null,
            pendingSelection = null,
            markMenuId = null,
            markMenuAnchor = null
        )

        viewModelScope.launch {
            persistProgress(updatedProgress)
        }
    }

    private fun creditWordsRead(currentState: ReaderUiState.Success, words: Int) {
        val clamped = words.coerceIn(0, MAX_CHAPTER_WORDS)
        val delta = (clamped - currentState.lastReportedChapterWords).coerceAtLeast(0)
        val maxDelta = MAX_BASE_CREDIT_WORDS * 100 / currentState.textZoom.coerceAtLeast(1)
        val credited = if (delta <= maxDelta) delta else 0
        _internalState.value = currentState.copy(
            lastReportedChapterWords = clamped,
            accumulatedReadWords = currentState.accumulatedReadWords + credited
        )
    }
}
