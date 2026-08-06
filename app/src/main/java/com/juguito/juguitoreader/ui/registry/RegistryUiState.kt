package com.juguito.juguitoreader.ui.registry

import com.juguito.juguitoreader.domain.model.Book

data class RegistryUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)