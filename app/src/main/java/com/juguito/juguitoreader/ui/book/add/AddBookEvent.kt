package com.juguito.juguitoreader.ui.book.add

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre

sealed interface AddBookEvent {
    data class OnTitleChanged(val title: String): AddBookEvent
    data class OnAuthorChanged(val author: String): AddBookEvent
    data class OnPublisherChanged(val publisher: String): AddBookEvent
    data class OnIsPhysicalChanged(val isPhysical: Boolean): AddBookEvent
    data class OnCoverUrlChanged(val coverUrl: String): AddBookEvent
    data class OnLocalFilePathChanged(val localFilePath: String): AddBookEvent
    data class OnFoldersChanged(val folders: List<Folder>): AddBookEvent
    data class OnGenresChanged(val genres: List<Genre>): AddBookEvent
    data class OnImportEpub(val uri: android.net.Uri): AddBookEvent

    data object OnSaveClick: AddBookEvent
}