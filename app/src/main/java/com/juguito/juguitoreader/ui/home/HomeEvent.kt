package com.juguito.juguitoreader.ui.home

import android.net.Uri
import com.juguito.juguitoreader.domain.model.Book

sealed interface HomeEvent {
    data class OnDeleteBookClick(val book: Book): HomeEvent
    data object OnUndoDeleteClick: HomeEvent
    data object OnDeleteConfirmed: HomeEvent
    data class OnImportBook(val uri: Uri) : HomeEvent
    data object OnDismissError : HomeEvent
}