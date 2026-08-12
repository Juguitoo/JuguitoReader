package com.juguito.juguitoreader.ui.folder.editor

sealed interface FolderEditorEvent {
    data class OnNameChanged(val name: String): FolderEditorEvent
    data class OnColorChanged(val colorHex: String): FolderEditorEvent
    data class OnDescriptionChanged(val description: String): FolderEditorEvent
    data object OnSaveClick: FolderEditorEvent
}
