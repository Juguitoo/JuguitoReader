package com.juguito.juguitoreader.ui.folder.add

data class AddFolderUiState(
    val name: String = "",
    val colorHex: String = "#FFFFFF",
    val description: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)