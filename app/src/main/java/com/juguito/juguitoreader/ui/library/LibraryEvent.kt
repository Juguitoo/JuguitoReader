package com.juguito.juguitoreader.ui.library

import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.Folder

sealed interface LibraryEvent {
    data class OnSelectedFolderChanged(val selectedFolder: Folder?): LibraryEvent
    data class OnSearchTextChanged(val searchText: String?): LibraryEvent
    data class OnStatusChanged(val bookId: Int, val newStatus: BookStatus): LibraryEvent
}