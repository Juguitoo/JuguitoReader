package com.juguito.juguitoreader.ui.folder.editor

data class FolderEditorUiState(
    val name: String = "",
    val colorHex: String = "#EF5350",
    val description: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isEditing: Boolean = false
)
