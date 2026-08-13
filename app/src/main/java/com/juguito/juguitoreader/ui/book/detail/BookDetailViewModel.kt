package com.juguito.juguitoreader.ui.book.detail

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.usecase.book.GetBookByIdUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import com.juguito.juguitoreader.ui.book.state.BookDraftState
import com.juguito.juguitoreader.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.net.toUri
import com.juguito.juguitoreader.domain.usecase.book.DeleteBookUseCase

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

    private val _uiState = MutableStateFlow(BookDetailUiState(book = null))
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    private val bookId: Int = checkNotNull(savedStateHandle["bookId"])

    init {
        loadBook()
        loadAvailableData()
    }

    private fun loadAvailableData() {
        viewModelScope.launch {
            getFoldersUseCase().collect { folders ->
                _uiState.value = _uiState.value.copy(availableFolders = folders)
            }
        }
        viewModelScope.launch {
            getGenresUseCase().collect { genres ->
                _uiState.value = _uiState.value.copy(availableGenres = genres)
            }
        }
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val book = getBookByIdUseCase(bookId)
            if (book != null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
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
                    endDate = book.endDate
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudo encontrar el libro"
                )
            }
        }
    }

    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.OnTitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(title = event.title)
                )
            }
            is BookDetailEvent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(author = event.author)
                )
            }
            is BookDetailEvent.OnPublisherChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(publisher = event.publisher)
                )
            }
            is BookDetailEvent.OnSeriesChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(series = event.series)
                )
            }
            is BookDetailEvent.OnSeriesOrderChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(seriesOrder = event.seriesOrder)
                )
            }
            is BookDetailEvent.OnIsPhysicalChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(isPhysical = event.isPhysical)
                )
            }
            is BookDetailEvent.OnCoverUrlChanged -> {
                viewModelScope.launch {
                    val permanentPath = withContext(Dispatchers.IO) {
                        FileUtils.saveImageToInternalStorage(application, event.coverUrl.toUri())
                    }
                    if (permanentPath != null) {
                        _uiState.value = _uiState.value.copy(
                            bookDraft = _uiState.value.bookDraft.copy(coverUrl = permanentPath)
                        )
                    }
                }
            }
            is BookDetailEvent.OnLocalFilePathChanged -> {
                if (event.localFilePath == null) {
                    _uiState.value = _uiState.value.copy(
                        bookDraft = _uiState.value.bookDraft.copy(localFilePath = null)
                    )
                } else {
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)

                        val bookMetadata = withContext(Dispatchers.IO) {
                            getBookFromEpubUseCase(application, event.localFilePath.toUri())
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            bookDraft = _uiState.value.bookDraft.copy(
                                localFilePath = event.localFilePath,
                                coverUrl = bookMetadata.coverUrl ?: _uiState.value.bookDraft.coverUrl
                            )
                        )
                    }
                }
            }
            is BookDetailEvent.OnFoldersChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(folders = event.folders)
                )
            }
            is BookDetailEvent.OnGenresChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(genres = event.genres)
                )
            }
            is BookDetailEvent.OnImportEpub -> {
                importEpubData(event.uri)
            }
            is BookDetailEvent.OnStartDateChanged -> {
                _uiState.value = _uiState.value.copy(startDate = event.startDate)
            }
            is BookDetailEvent.OnEndDateChanged -> {
                _uiState.value = _uiState.value.copy(
                    endDate = event.endDate,
                    status = if (event.endDate != null) BookStatus.FINISHED else _uiState.value.status
                )
            }
            is BookDetailEvent.OnRatingChanged -> {
                _uiState.value = _uiState.value.copy(rating = event.rating)
            }
            is BookDetailEvent.OnCommentChanged -> {
                _uiState.value = _uiState.value.copy(comment = event.comment)
            }
            is BookDetailEvent.OnStatusChanged -> {
                _uiState.value = _uiState.value.copy(status = event.status)
            }
            is BookDetailEvent.OnEditModeChanged -> {
                if (!event.mode) {
                    val book = _uiState.value.book
                    if (book != null) {
                        _uiState.value = _uiState.value.copy(
                            isEditMode = false,
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
                            endDate = book.endDate
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isEditMode = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isEditMode = true)
                }
            }
            is BookDetailEvent.OnTabChanged -> {
                _uiState.value = _uiState.value.copy(selectedTab = event.tab)
            }
            is BookDetailEvent.OnSaveClick -> {
                saveChanges()
            }

            is BookDetailEvent.OnDeleteClick -> {
                val currentBook = _uiState.value.book
                if (currentBook != null) {
                    viewModelScope.launch {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        deleteBookUseCase(currentBook.id)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    private fun importEpubData(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val book = withContext(Dispatchers.IO) {
                getBookFromEpubUseCase(application, uri)
            }
            
            val currentDraft = _uiState.value.bookDraft
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                bookDraft = currentDraft.copy(
                    title = book.title.ifBlank { currentDraft.title },
                    author = book.author.ifBlank { currentDraft.author },
                    publisher = book.publisher ?: currentDraft.publisher,
                    series = book.series ?: currentDraft.series,
                    seriesOrder = book.seriesOrder?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: currentDraft.seriesOrder,
                    coverUrl = book.coverUrl ?: currentDraft.coverUrl,
                    localFilePath = book.localFilePath,
                    genres = (book.genres + currentDraft.genres).distinctBy { it.name.lowercase() }
                )
            )
        }
    }

    private fun saveChanges() {
        val state = _uiState.value
        val draft = state.bookDraft
        
        if (state.book == null) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val updatedBook = Book(
                id = state.book.id,
                title = draft.title,
                author = draft.author,
                publisher = draft.publisher,
                series = draft.series,
                seriesOrder = draft.seriesOrder.toDoubleOrNull(),
                isPhysical = draft.isPhysical,
                status = state.status,
                rating = state.rating ?: 0f,
                comment = state.comment,
                startDate = state.startDate,
                endDate = state.endDate,
                coverUrl = draft.coverUrl,
                localFilePath = draft.localFilePath,
                folders = draft.folders,
                genres = draft.genres,
                createdAt = state.book.createdAt
            )

            val result = updateBookUseCase(updatedBook)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = false,
                        book = updatedBook
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage
                    )
                }
            )
        }
    }
}
