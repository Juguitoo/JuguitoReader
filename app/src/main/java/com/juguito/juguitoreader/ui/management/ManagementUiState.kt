package com.juguito.juguitoreader.ui.management

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre

data class ManagementUiState(
    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val genreToEdit: Genre? = null,
    val newGenreName: String = "",
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
)
