package com.juguito.juguitoreader.ui.book.state

import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.model.Genre

data class BookDraftState(
    val title: String = "",
    val author: String = "",
    val publisher: String = "",
    val isPhysical: Boolean = false,
    val coverUrl: String? = null,
    val localFilePath: String? = null,
    val folders: List<Folder> = emptyList(),
    val genres: List<Genre> = emptyList(),
)
