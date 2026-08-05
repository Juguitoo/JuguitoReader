package com.juguito.juguitoreader.ui.book.add

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.book.state.BookDraftState

data class AddBookUiState(
    val bookDraft: BookDraftState = BookDraftState(),

    val availableFolders: List<Folder> = emptyList(),
    val availableGenres: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)