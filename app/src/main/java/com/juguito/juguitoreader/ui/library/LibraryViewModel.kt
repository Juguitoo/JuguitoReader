package com.juguito.juguitoreader.ui.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getBooksUseCase: GetBooksUseCase,
    private val getFoldersUseCase: GetFoldersUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val importBookFromUriUseCase: ImportBookFromUriUseCase
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    private val _internalState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _internalState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _internalState.value = LibraryUiState.Loading

        viewModelScope.launch {
            combine(
                getFoldersUseCase(),
                getBooksUseCase(),
                _searchText,
                _selectedFolder
            ) { folders, books, query, currentFolder ->

                if (books.isEmpty()) {
                    return@combine LibraryUiState.Empty(
                        folders = folders,
                        selectedFolder = currentFolder,
                        searchText = query
                    ) as LibraryUiState
                }

                val filteredBooks = books.filter { book ->
                    val matchesText = query.isBlank() ||
                            book.title.contains(query, ignoreCase = true) ||
                            book.author.contains(query, ignoreCase = true) ||
                            (book.series?.contains(query, ignoreCase = true) ?: false)

                    val matchesFolder = currentFolder == null || book.folders.any {it.id == currentFolder.id}

                    matchesText && matchesFolder
                }

                LibraryUiState.Success(
                    folders = folders,
                    filteredBooks = filteredBooks,
                    allBooks = books,
                    selectedFolder = currentFolder,
                    searchText = query
                ) as LibraryUiState
            }.catch { error ->
                error.printStackTrace()
                emit (
                    LibraryUiState.Error(
                        message = UiText.StringResource(R.string.something_went_wrong).asString(context)
                    )
                )
            }.collect { newState ->
                _internalState.value = newState
            }
        }
    }

    fun onEvent(event: LibraryEvent) {
        when (event) {
            is LibraryEvent.OnSelectedFolderChanged -> {
                _selectedFolder.value = event.selectedFolder
            }
            is LibraryEvent.OnSearchTextChanged -> {
                _searchText.value = event.searchText?.trim() ?: ""
            }
            is LibraryEvent.OnStatusChanged -> {
                updateBookStatus(event.bookId, event.newStatus)
            }
        }
    }

    private fun updateBookStatus(bookId: Int, newStatus: BookStatus) {
        val currentState = _internalState.value
        if (currentState !is LibraryUiState.Success) return

        val book = currentState.allBooks.find { it.id == bookId } ?: return
        viewModelScope.launch {
            updateBookUseCase(book.copy(status = newStatus))
        }
    }

    fun importBook(uri: Uri) {
        _internalState.value = LibraryUiState.Loading
        viewModelScope.launch {
            val result = importBookFromUriUseCase(context, uri)

            result
                .onSuccess {
                }.onFailure { exception ->
                    exception.printStackTrace()
                    _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                }
        }
    }

    fun dismissError(){
        loadData()
    }
}