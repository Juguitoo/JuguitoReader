package com.juguito.juguitoreader.ui.book.add

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre

data class AddBookUiState(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val isPhysical: Boolean = false,
    val coverUrl: String? = null,
    val localFilePath: String? = null,
    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList(),

    val availableFolders: List<Folder> = emptyList(),
    val availableGenres: List<Genre> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)