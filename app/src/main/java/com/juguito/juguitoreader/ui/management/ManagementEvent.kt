package com.juguito.juguitoreader.ui.management

import com.juguito.juguitoreader.domain.model.Genre

sealed interface ManagementEvent {
    data class OnTabSelected(val index: Int) : ManagementEvent
    data class OnDeleteFolder(val folderId: Int) : ManagementEvent
    data class OnDeleteGenre(val genreId: Int) : ManagementEvent
    data class OnEditGenreClick(val genre: Genre) : ManagementEvent
    data class OnGenreNameChanged(val name: String) : ManagementEvent
    data class OnSearchQueryChanged(val query: String) : ManagementEvent
    data object OnToggleSearch : ManagementEvent
    data object OnUpdateGenreConfirm : ManagementEvent
    data object OnCancelEditGenre : ManagementEvent
}
