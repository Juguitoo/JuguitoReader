package com.juguito.juguitoreader.ui.book.detail

import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.BookStatus
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.ui.book.state.BookDraftState

data class BookDetailUiState(
    // Book instance
    val book: Book?,

    // Static book attributes
    val bookDraft: BookDraftState = BookDraftState(),

    // Personal reading attributes
    val status: BookStatus = BookStatus.PENDING,
    val rating: Float? = 0.0f,
    val comment: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,

    // Screen control
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val isEditMode: Boolean = false,

    val availableFolders: List<Folder> = emptyList(),
    val availableGenres: List<Genre> = emptyList()
)
