package com.juguito.juguitoreader.ui.library

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.book.ImportBookFromUriUseCase
import com.juguito.juguitoreader.domain.usecase.book.UpdateBookUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFoldersUseCase
import com.juguito.juguitoreader.domain.usecase.library.GetLibraryLayoutModeUseCase
import com.juguito.juguitoreader.domain.usecase.library.SaveLibraryLayoutModeUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.SavedStateHandle
import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.model.SortOption
import com.juguito.juguitoreader.domain.model.applyCriteria
import com.juguito.juguitoreader.domain.model.applyOrderBy
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
    private val importBookFromUriUseCase: ImportBookFromUriUseCase,
    private val getLibraryLayoutModeUseCase: GetLibraryLayoutModeUseCase,
    private val saveLibraryLayoutModeUseCase: SaveLibraryLayoutModeUseCase,
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    private val _criteria = MutableStateFlow(criteriaFrom(savedStateHandle))
    private val _internalState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _internalState.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _internalState.value = LibraryUiState.Loading

        viewModelScope.launch {
            combine(
                combine(
                    getFoldersUseCase(),
                    getBooksUseCase(),
                    _selectedFolder,
                    _criteria
                ) { folders, books, currentFolder, criteria ->
                    ShelfContent(folders, books, criteria.searchText ,currentFolder, criteria)
                },
                getLibraryLayoutModeUseCase()
            ) { content, layoutMode ->
                val folders = content.folders
                val books = content.books
                val query = content.query
                val currentFolder = content.currentFolder
                val criteria = content.criteria

                if (books.isEmpty()) {
                    return@combine LibraryUiState.Empty(
                        folders = folders,
                        selectedFolder = currentFolder,
                        criteria = criteria,
                        layoutMode = layoutMode
                    ) as LibraryUiState
                }

                val inFolder = if (currentFolder == null) {
                    books
                } else {
                    books.filter { book -> book.folders.any { it.id == currentFolder.id } }
                }
                val matched = inFolder.applyCriteria(criteria.copy(searchText = query))
                val visible = if (criteria.sortBy == SortOption.STARTED_AT_DESC) {
                    matched
                } else {
                    matched.applyOrderBy(criteria.sortBy)
                }

                LibraryUiState.Success(
                    folders = folders,
                    filteredBooks = visible,
                    allBooks = books,
                    selectedFolder = currentFolder,
                    criteria = criteria,
                    layoutMode = layoutMode
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
                if (_selectedFolder.value == event.selectedFolder) {
                    _selectedFolder.value = null
                } else {
                    _selectedFolder.value = event.selectedFolder
                }
            }
            is LibraryEvent.OnSearchTextChanged -> {
                _criteria.value = _criteria.value.copy(searchText = event.searchText?.trim() ?: "")
            }
            is LibraryEvent.OnStatusChanged -> {
                updateBookStatus(event.bookId, event.newStatus)
            }
            is LibraryEvent.OnLayoutModeChanged -> {
                viewModelScope.launch {
                    saveLibraryLayoutModeUseCase(event.layoutMode)
                }
            }
            is LibraryEvent.OnCriteriaChanged -> {
                updateCriteria(event.criteria)
            }
            LibraryEvent.OnClearFilters -> {
                updateCriteria(BookCriteria())
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

    private fun updateCriteria(criteria: BookCriteria) {
        _criteria.value = criteria
    }

    fun importBook(uri: Uri) {
        if (_isImporting.value) return
        _isImporting.value = true
        viewModelScope.launch {
            try {
                importBookFromUriUseCase(context, uri)
                    .onFailure { exception ->
                        exception.printStackTrace()
                        _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
                    }
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun dismissError() {
        loadData()
    }
}

private fun criteriaFrom(savedStateHandle: SavedStateHandle): BookCriteria {
    val status = savedStateHandle.get<String>("status")?.let { name ->
        runCatching { BookStatus.valueOf(name) }.getOrNull()
    }
    return BookCriteria(
        favouritesOnly = savedStateHandle.get<Boolean>("favourites") ?: false,
        statuses = status?.let { setOf(it) } ?: emptySet()
    )
}

private data class ShelfContent(
    val folders: List<Folder>,
    val books: List<Book>,
    val query: String,
    val currentFolder: Folder?,
    val criteria: BookCriteria
)