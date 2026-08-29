package com.juguito.juguitoreader.ui.book.detail

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.ui.book.state.BookDraftState
import com.juguito.juguitoreader.ui.common.UiText.StringResource
import com.juguito.juguitoreader.ui.common.asUiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect.ShowSnackbar
import com.juguito.juguitoreader.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val application: Application,
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val getBookFromEpubUseCase: GetBookFromEpubUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val deleteBookUseCase: DeleteBookUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])
    private var availableFolders: List<Folder> = emptyList()
    private var availableGenres: List<Genre> = emptyList()

    init {
        loadBook()
        loadAvailableData()
    }

    private fun loadAvailableData() {
        viewModelScope.launch {
            getFoldersUseCase()
                .catch { error -> _effect.send(ShowSnackbar(error.asUiText())) }
                .collect { folders ->
                    availableFolders = folders
                    updateSuccessState { it.copy(availableFolders = folders) }
                }
        }
        viewModelScope.launch {
            getGenresUseCase()
                .catch { error -> _effect.send(ShowSnackbar(error.asUiText())) }
                .collect { genres ->
                    availableGenres = genres
                    updateSuccessState { it.copy(availableGenres = genres) }
                }
        }
    }

    fun loadBook() {
        viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            runCatching {
                getBookByIdUseCase(bookId)
            }.onSuccess { book ->
                if (book != null) {
                    _uiState.value = BookDetailUiState.Success(
                        book = book,
                        bookDraft = BookDraftState(
                            title = book.title,
                            author = book.author,
                            publisher = book.publisher ?: "",
                            series = book.series ?: "",
                            seriesOrder = book.seriesOrder?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                            isPhysical = book.isPhysical,
                            coverUrl = book.coverUrl,
                            localFilePath = book.localFilePath,
                            folders = book.folders,
                            genres = book.genres
                        ),
                        status = book.status,
                        rating = book.rating,
                        comment = book.comment,
                        startDate = book.startDate,
                        endDate = book.endDate,
                        availableFolders = availableFolders,
                        availableGenres = availableGenres
                    )
                } else {
                    _uiState.value = BookDetailUiState.Error(StringResource(R.string.error_epub_not_found).asString(application))
                }
            }.onFailure { exception ->
                exception.printStackTrace()
                _uiState.value = BookDetailUiState.Error("Error al cargar el libro")
            }
        }
    }

    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.OnTitleChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(title = event.title)) }
            }
            is BookDetailEvent.OnAuthorChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(author = event.author)) }
            }
            is BookDetailEvent.OnPublisherChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(publisher = event.publisher)) }
            }
            is BookDetailEvent.OnSeriesChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(series = event.series)) }
            }
            is BookDetailEvent.OnSeriesOrderChanged -> {
                if (event.seriesOrder.length <= 4 && event.seriesOrder.all { it.isDigit() }) {
                    updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(seriesOrder = event.seriesOrder)) }
                }
            }
            is BookDetailEvent.OnIsPhysicalChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(isPhysical = event.isPhysical)) }
            }
            is BookDetailEvent.OnCoverUrlChanged -> {
                viewModelScope.launch {
                    val permanentPath = withContext(Dispatchers.IO) {
                        FileUtils.saveImageToInternalStorage(application, event.coverUrl.toUri())
                    }
                    if (permanentPath != null) {
                        updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(coverUrl = permanentPath)) }
                    }
                }
            }
            is BookDetailEvent.OnEpubFilePicked -> {
                viewModelScope.launch {
                    updateSuccessState { it.copy(isActionLoading = true) }
                    runCatching {
                        getBookFromEpubUseCase(application, event.uri)
                    }.onSuccess { bookMetadata ->
                        val success = _uiState.value as? BookDetailUiState.Success
                        val previousPath = success?.bookDraft?.localFilePath
                        val persistedPath = success?.book?.localFilePath
                        val newPath = bookMetadata.localFilePath
                        updateSuccessState {
                            it.copy(
                                isActionLoading = false,
                                bookDraft = it.bookDraft.copy(
                                    localFilePath = bookMetadata.localFilePath,
                                    coverUrl = bookMetadata.coverUrl ?: it.bookDraft.coverUrl
                                )
                            )
                        }
                        if (previousPath != null && previousPath != newPath && previousPath != persistedPath) {
                            withContext(Dispatchers.IO) {
                                FileUtils.deleteFileFromInternalStorage(application, previousPath)
                            }
                        }
                    }.onFailure {
                        updateSuccessState { it.copy(isActionLoading = false) }
                        _effect.send(ShowSnackbar(StringResource(R.string.error_copy_epub)))
                    }
                }
            }
            is BookDetailEvent.OnLocalFilePathChanged -> {
                val success = _uiState.value as? BookDetailUiState.Success
                val previousPath = success?.bookDraft?.localFilePath
                val persistedPath = success?.book?.localFilePath
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(localFilePath = null)) }
                if (previousPath != null && previousPath != persistedPath) {
                    viewModelScope.launch(Dispatchers.IO) {
                        FileUtils.deleteFileFromInternalStorage(application, previousPath)
                    }
                }
            }
            is BookDetailEvent.OnFoldersChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(folders = event.folders)) }
            }
            is BookDetailEvent.OnGenresChanged -> {
                updateSuccessState { it.copy(bookDraft = it.bookDraft.copy(genres = event.genres)) }
            }
            is BookDetailEvent.OnStartDateChanged -> {
                updateSuccessState { it.copy(startDate = event.startDate) }
            }
            is BookDetailEvent.OnEndDateChanged -> {
                updateSuccessState {
                    it.copy(
                        endDate = event.endDate,
                        status = if (event.endDate != null) BookStatus.FINISHED else it.status
                    )
                }
            }
            is BookDetailEvent.OnRatingChanged -> {
                updateSuccessState { it.copy(rating = event.rating) }
            }
            is BookDetailEvent.OnCommentChanged -> {
                updateSuccessState { it.copy(comment = event.comment) }
            }
            is BookDetailEvent.OnStatusChanged -> {
                updateSuccessState { it.copy(status = event.status) }
            }
            is BookDetailEvent.OnEditModeChanged -> {
                updateSuccessState { current ->
                    if (!event.mode) {
                        current.copy(
                            isEditMode = false,
                            bookDraft = BookDraftState(
                                title = current.book.title,
                                author = current.book.author,
                                publisher = current.book.publisher ?: "",
                                series = current.book.series ?: "",
                                seriesOrder = current.book.seriesOrder?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "",
                                isPhysical = current.book.isPhysical,
                                coverUrl = current.book.coverUrl,
                                localFilePath = current.book.localFilePath,
                                folders = current.book.folders,
                                genres = current.book.genres
                            ),
                            status = current.book.status,
                            rating = current.book.rating,
                            comment = current.book.comment,
                            startDate = current.book.startDate,
                            endDate = current.book.endDate
                        )
                    } else {
                        current.copy(isEditMode = true)
                    }
                }
            }
            is BookDetailEvent.OnTabChanged -> {
                updateSuccessState { it.copy(selectedTab = event.tab) }
            }
            is BookDetailEvent.OnSaveClick -> {
                saveChanges()
            }
            is BookDetailEvent.OnDeleteClick -> {
                val current = _uiState.value as? BookDetailUiState.Success ?: return
                viewModelScope.launch {
                    updateSuccessState { it.copy(isActionLoading = true) }
                    deleteBookUseCase(current.book.id)
                    _effect.send(UiEffect.NavigateBack)
                }
            }
        }
    }

    private fun saveChanges() {
        val current = _uiState.value as? BookDetailUiState.Success ?: return
        val draft = current.bookDraft

        if (draft.title.isBlank()) {
            viewModelScope.launch { _effect.send(ShowSnackbar(StringResource(R.string.error_title_empty))) }
            return
        }
        if (draft.author.isBlank()) {
            viewModelScope.launch { _effect.send(ShowSnackbar(StringResource(R.string.error_author_empty))) }
            return
        }

        updateSuccessState { it.copy(isActionLoading = true) }

        viewModelScope.launch {
            val updatedBook = Book(
                id = current.book.id,
                title = draft.title,
                author = draft.author,
                publisher = draft.publisher,
                series = draft.series,
                seriesOrder = draft.seriesOrder.toDoubleOrNull(),
                isPhysical = draft.isPhysical,
                status = current.status,
                rating = current.rating ?: 0f,
                comment = current.comment,
                startDate = current.startDate,
                endDate = current.endDate,
                coverUrl = draft.coverUrl,
                localFilePath = draft.localFilePath,
                folders = draft.folders,
                genres = draft.genres,
                createdAt = current.book.createdAt
            )

            val result = updateBookUseCase(updatedBook)

            result.fold(
                onSuccess = {
                    val oldPath = current.book.localFilePath
                    val newPath = updatedBook.localFilePath
                    if (oldPath != null && oldPath != newPath) {
                        withContext(Dispatchers.IO) {
                            FileUtils.deleteFileFromInternalStorage(application, oldPath)
                        }
                    }
                    updateSuccessState {
                        it.copy(
                            isActionLoading = false,
                            isEditMode = false,
                            book = updatedBook
                        )
                    }
                    _effect.send(ShowSnackbar(StringResource(R.string.save_success)))
                },
                onFailure = { exception ->
                    updateSuccessState { it.copy(isActionLoading = false) }
                    _effect.send(ShowSnackbar(exception.asUiText()))
                }
            )
        }
    }

    private inline fun updateSuccessState(crossinline block: (BookDetailUiState.Success) -> BookDetailUiState.Success) {
        val current = _uiState.value
        if (current is BookDetailUiState.Success) {
            _uiState.value = block(current)
        }
    }
}