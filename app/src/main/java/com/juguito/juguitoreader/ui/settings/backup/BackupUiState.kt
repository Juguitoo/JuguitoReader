package com.juguito.juguitoreader.ui.settings.backup

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Exporting : BackupUiState
    data object Importing : BackupUiState
    data object ConfirmImport : BackupUiState
}
