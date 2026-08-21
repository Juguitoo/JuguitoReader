package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getReadingProgressByIdUseCase: GetReadingProgressByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val addReadingProgressUseCase: AddReadingProgressUseCase,
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

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])

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
                    ?: throw Exception("El libro que se está intentando leer no existe.")

                val localPath = book.localFilePath
                    ?: throw Exception("El libro no tiene un archivo físico asociado. Añade un fichero EPUB.")

                val parseResult = parseEpubUseCase.invoke(bookId, localPath)
                parseResult.fold(
                    onSuccess = { content ->
                        var progress = getReadingProgressByIdUseCase.invoke(bookId)
                        if (progress == null) {
                            val newProgress = ReadingProgress(bookId, content.spine.size ,0, 0f, System.currentTimeMillis())
                            addReadingProgressUseCase.invoke(newProgress)
                            progress = newProgress
                        }

                        _internalState.value = ReaderUiState.Success(
                            book = book,
                            epubContent = content,
                            readingProgress = progress,
                            currentChapterIndex = progress.lastChapterIndex,
                            isControlsVisible = true,
                            textZoom = zoom,
                            theme = initialTheme,
                            brightness = brightness
                        )
                    },
                    onFailure = { exception ->
                        _internalState.value = ReaderUiState.Error(exception.localizedMessage ?: "Error desconocido.")
                    }
                )



            } catch (e: Exception) {
                _internalState.value = ReaderUiState.Error(e.localizedMessage ?: "Error al cargar los datos.")
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
                val updatedProgress = currentState.readingProgress.copy(scrollPosition = event.scrollPosition, lastReadAt = System.currentTimeMillis())
                _internalState.value = currentState.copy(readingProgress = updatedProgress)
                viewModelScope.launch {
                    updateReadingProgressUseCase.invoke(updatedProgress)
                }
            }
            is ReaderEvent.OnTimeRemainingChanged -> {
                _internalState.value = currentState.copy(timeRemaining = event.minutes)
            }
        }
    }

    private fun updateChapter(currentState: ReaderUiState.Success, newIndex: Int) {
        val spineSize = currentState.epubContent.spine.size

        if (newIndex !in 0..<spineSize) return

        val updatedProgress = currentState.readingProgress.copy(
            lastChapterIndex = newIndex,
            scrollPosition = 0f,
            lastReadAt = System.currentTimeMillis()
        )

        _internalState.value = currentState.copy(
            currentChapterIndex = newIndex,
            readingProgress = updatedProgress
        )

        viewModelScope.launch {
            updateReadingProgressUseCase.invoke(updatedProgress)
        }
    }
}
