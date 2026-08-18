package com.juguito.juguitoreader.ui.folder

sealed interface FolderEvent {
    data class OnNameChanged(val name: String) : FolderEvent
    data class OnDescriptionChanged(val description: String) : FolderEvent
    data class OnColorChanged(val colorHex: String) : FolderEvent
    data object OnSaveClick : FolderEvent
}