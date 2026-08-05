package com.juguito.juguitoreader.ui.book.add

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
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

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val application: Application,
    private val addBookUseCase: AddBookUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val getBookFromEpubUseCase: GetBookFromEpubUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddBookUiState())
    val uiState: StateFlow<AddBookUiState> = _uiState.asStateFlow()

    init {
        loadAvailableData()
    }

    private fun loadAvailableData(){
        viewModelScope.launch {
            getFoldersUseCase().collect { foldersFromDb ->
                _uiState.value = _uiState.value.copy(
                    availableFolders = foldersFromDb
                )
            }
        }
        viewModelScope.launch {
            getGenresUseCase().collect { genresFromDb ->
                _uiState.value = _uiState.value.copy(
                    availableGenres = genresFromDb
                )
            }
        }
    }

    fun onEvent(event: AddBookEvent) {
        when (event) {
            is AddBookEvent.OnTitleChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(title = event.title)
                )
            }
            is AddBookEvent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(author = event.author)
                )
            }
            is AddBookEvent.OnPublisherChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(publisher = event.publisher)
                )
            }
            is AddBookEvent.OnIsPhysicalChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(isPhysical = event.isPhysical)
                )
            }
            is AddBookEvent.OnCoverUrlChanged -> {
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
            is AddBookEvent.OnLocalFilePathChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(localFilePath = event.localFilePath)
                )
            }
            is AddBookEvent.OnFoldersChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(folders = event.folders)
                )
            }
            is AddBookEvent.OnGenresChanged -> {
                _uiState.value = _uiState.value.copy(
                    bookDraft = _uiState.value.bookDraft.copy(genres = event.genres)
                )
            }
            is AddBookEvent.OnImportEpub -> {
                importEpubData(event.uri)
            }
            AddBookEvent.OnSaveClick -> {
                saveBook()
            }
        }
    }

    private fun saveBook() {
        val currentState = _uiState.value
        val draft = currentState.bookDraft

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val newBook = Book(
                title = draft.title,
                author = draft.author,
                publisher = draft.publisher,
                isPhysical = draft.isPhysical,
                coverUrl = draft.coverUrl,
                localFilePath = draft.localFilePath,
                folders = draft.folders,
                genres = draft.genres,
            )

            val result = addBookUseCase(newBook)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true
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
                    coverUrl = book.coverUrl ?: currentDraft.coverUrl,
                    localFilePath = book.localFilePath,
                    genres = (book.genres + currentDraft.genres).distinctBy { it.name.lowercase() }
                )
            )
        }
    }
}
