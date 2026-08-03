package com.juguito.juguitoreader.ui.home

import com.juguito.juguitoreader.domain.model.Book

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentBooks: List<Book> = emptyList(),
    val errorMessage: String? = null
)
