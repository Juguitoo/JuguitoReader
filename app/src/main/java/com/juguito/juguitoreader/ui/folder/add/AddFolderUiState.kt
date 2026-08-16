package com.juguito.juguitoreader.ui.folder.add

data class AddFolderUiState(
    val name: String = "",
    val colorHex: String = "#EF5350",
    val description: String = "",
    val isLoading: Boolean = false
)
