package com.juguito.juguitoreader.ui.home

import android.net.Uri
import com.juguito.juguitoreader.domain.model.Book

sealed interface HomeEvent {
    data class OnDeleteBookClick(val book: Book): HomeEvent
    data class OnUndoDeleteClick(val bookId: Int): HomeEvent
    data class OnDeleteConfirmed(val bookId: Int): HomeEvent
    data class OnImportBook(val uri: Uri) : HomeEvent
    data object OnDismissError : HomeEvent
}