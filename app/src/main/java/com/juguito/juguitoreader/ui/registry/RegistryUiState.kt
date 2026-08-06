package com.juguito.juguitoreader.ui.registry

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.BookCriteria

data class RegistryUiState(
    val books: List<Book> = emptyList(),
    val filteredBooks: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val criteria: BookCriteria = BookCriteria(),
    val isSearchExpanded: Boolean = false,
    val showFilterSheet: Boolean = false
) {
    val availableSeries: List<String> = books.mapNotNull { it.series }.filter { it.isNotBlank() }.distinct().sorted()
}
