package com.juguito.juguitoreader.ui.folder

data class FolderUiState (
    val name: String = "",
    val description: String = "",
    val colorHex: String = "#EF5350",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null
)