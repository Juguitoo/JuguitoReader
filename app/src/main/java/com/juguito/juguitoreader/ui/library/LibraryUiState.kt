package com.juguito.juguitoreader.ui.library

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder

sealed interface LibraryUiState {
    data object Loading: LibraryUiState
    data class Error(val error: String): LibraryUiState
    data class Success(
        val allBooks: List<Book> = emptyList(),
        val filteredBooks: List<Book>,
        val folders: List<Folder> = emptyList(),
        val selectedFolder: Folder? = null,
        val searchText: String = ""
    ): LibraryUiState
}
