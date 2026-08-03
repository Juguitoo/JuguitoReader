package com.juguito.juguitoreader.ui.folder.add

sealed interface AddFolderEvent {
    data class OnNameChanged(val name: String): AddFolderEvent
    data class OnColorChanged(val colorHex: String): AddFolderEvent
    data class OnDescriptionChanged(val description: String): AddFolderEvent
    data object OnSaveClick: AddFolderEvent
}