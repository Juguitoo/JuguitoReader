package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.reader.ParseEpubUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.AddReadingProgressUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.GetReadingProgressByIdUseCase
import com.juguito.juguitoreader.domain.usecase.readingProgress.UpdateReadingProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getReadingProgressByIdUseCase: GetReadingProgressByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val addReadingProgressUseCase: AddReadingProgressUseCase,
    private val parseEpubUseCase: ParseEpubUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = ReaderUiState.Loading

        viewModelScope.launch {
            try {
                val book = getBookByIdUseCase.invoke(bookId)
                    ?: throw Exception("El libro que se está intentando leer no existe.")

                val localPath = book.localFilePath
                    ?: throw Exception("El libro no tiene un archivo físico asociado. Añade un fichero EPUB.")

                var progress = getReadingProgressByIdUseCase.invoke(bookId)
                if (progress == null) {
                    val newProgress = ReadingProgress(bookId, 0, 0f, System.currentTimeMillis())
                    addReadingProgressUseCase.invoke(newProgress)
                    progress = newProgress
                }

                val parseResult = parseEpubUseCase.invoke(bookId, localPath)
                parseResult.fold(
                    onSuccess = { content ->
                        _uiState.value = ReaderUiState.Success(
                            book = book,
                            epubContent = content,
                            readingProgress = progress,
                            currentChapterIndex = progress.lastChapterIndex,
                            isControlsVisible = true
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = ReaderUiState.Error(exception.localizedMessage ?: "Error desconocido.")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error(e.localizedMessage ?: "Error al cargar los datos.")
            }
        }
    }

    fun onEvent(event: ReaderEvent) {
        val currentState = _uiState.value
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
                _uiState.value = currentState.copy(isControlsVisible = !currentState.isControlsVisible)
            }
            is ReaderEvent.OnTextZoomChanged -> {
                _uiState.value = currentState.copy(textZoom = event.zoom)
            }
            is ReaderEvent.OnThemeChanged -> {
                _uiState.value = currentState.copy(theme = event.theme)
            }
            is ReaderEvent.OnScrollPositionChanged -> {
                val updatedProgress = currentState.readingProgress.copy(scrollPosition = event.scrollPosition, lastReadAt = System.currentTimeMillis())
                _uiState.value = currentState.copy(
                    readingProgress = updatedProgress
                )

                viewModelScope.launch {
                    updateReadingProgressUseCase.invoke(updatedProgress)
                }
            }
            is ReaderEvent.OnTimeRemainingChanged -> {
                _uiState.value = currentState.copy(timeRemaining = event.minutes)
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

        _uiState.value = currentState.copy(
            currentChapterIndex = newIndex,
            readingProgress = updatedProgress
        )

        viewModelScope.launch {
            updateReadingProgressUseCase.invoke(updatedProgress)
        }
    }
}
