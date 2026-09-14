package com.juguito.juguitoreader.ui.settings.backup

sealed interface BackupEvent {
    data object OnRestoreClick : BackupEvent
    data object OnConfirmRestore : BackupEvent
    data object OnCancelRestore : BackupEvent
    data class OnExportPicked(val uri: String) : BackupEvent
    data class OnImportPicked(val uri: String) : BackupEvent
}
