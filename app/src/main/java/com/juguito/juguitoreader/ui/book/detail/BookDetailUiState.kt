package com.juguito.juguitoreader.ui.book.detail

import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.book.state.BookDraftState

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState
    data class Error(val message: String) : BookDetailUiState
    data class Success(
        val book: Book,
        val bookDraft: BookDraftState = BookDraftState(),
        val status: BookStatus = BookStatus.PENDING,
        val rating: Float? = 0.0f,
        val comment: String? = null,
        val startDate: Long? = null,
        val endDate: Long? = null,
        val isEditMode: Boolean = false,
        val isActionLoading: Boolean = false,
        val availableFolders: List<Folder> = emptyList(),
        val availableGenres: List<Genre> = emptyList()
    ) : BookDetailUiState
}
