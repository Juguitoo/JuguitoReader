package com.juguito.juguitoreader.ui.book.add

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.usecase.book.AddBookUseCase
import com.juguito.juguitoreader.domain.usecase.book.GetBookFromEpubUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.genre.GetGenresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
                _uiState.value = _uiState.value.copy(title = event.title)
            }
            is AddBookEvent.OnAuthorChanged -> {
                _uiState.value = _uiState.value.copy(author = event.author)
            }
            is AddBookEvent.OnPublisherChanged -> {
                _uiState.value = _uiState.value.copy(publisher = event.publisher)
            }
            is AddBookEvent.OnIsPhysicalChanged -> {
                _uiState.value = _uiState.value.copy(isPhysical = event.isPhysical)
            }
            is AddBookEvent.OnCoverUrlChanged -> {
                _uiState.value = _uiState.value.copy(coverUrl = event.coverUrl)
            }
            is AddBookEvent.OnLocalFilePathChanged -> {
                _uiState.value = _uiState.value.copy(localFilePath = event.localFilePath)
            }
            is AddBookEvent.OnFoldersChanged -> {
                _uiState.value = _uiState.value.copy(folders = event.folders)
            }
            is AddBookEvent.OnGenresChanged -> {
                _uiState.value = _uiState.value.copy(genres = event.genres)
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

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val newBook = Book(
                title = currentState.title,
                author = currentState.author,
                publisher = currentState.publisher,
                isPhysical = currentState.isPhysical,
                coverUrl = currentState.coverUrl,
                localFilePath = currentState.localFilePath,
                folders = currentState.folders,
                genres = currentState.genres,
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
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                title = book.title.ifBlank { _uiState.value.title },
                author = book.author.ifBlank { _uiState.value.author },
                publisher = book.publisher ?: _uiState.value.publisher,
                coverUrl = book.coverUrl ?: _uiState.value.coverUrl,
                localFilePath = book.localFilePath,
                genres = (book.genres + _uiState.value.genres).distinctBy { it.name.lowercase() }
            )
        }
    }
}
