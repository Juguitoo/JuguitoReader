package com.juguito.juguitoreader.ui.library

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder

data class LibraryUiState(
    val allBooks: List<Book> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val selectedFolder: Folder? = null,
    val searchText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredBooks: List<Book> get() =
        allBooks.filter { book ->
            val matchesText = searchText.isBlank() ||
                    book.title.contains(searchText, ignoreCase = true) ||
                    book.author.contains(searchText, ignoreCase = true) ||
                    (book.series?.contains(searchText, ignoreCase = true) ?: false)

            val matchesFolder = selectedFolder == null || book.folders.any {it.id == selectedFolder.id}

            matchesText && matchesFolder
        }
}
