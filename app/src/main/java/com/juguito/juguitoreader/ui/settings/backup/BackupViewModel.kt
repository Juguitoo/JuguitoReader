package com.juguito.juguitoreader.ui.settings.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.usecase.backup.ExportBackupUseCase
import com.juguito.juguitoreader.domain.usecase.backup.ImportBackupUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.asUiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: BackupEvent) {
        when (event) {
            BackupEvent.OnRestoreClick -> {
                if (_uiState.value != BackupUiState.Idle) return
                _uiState.value = BackupUiState.ConfirmImport
            }
            BackupEvent.OnConfirmRestore -> {
                if (_uiState.value != BackupUiState.ConfirmImport) return
                _uiState.value = BackupUiState.Idle
            }
            BackupEvent.OnCancelRestore -> {
                if (_uiState.value != BackupUiState.ConfirmImport) return
                _uiState.value = BackupUiState.Idle
            }
            is BackupEvent.OnExportPicked -> export(event.uri)
            is BackupEvent.OnImportPicked -> import(event.uri)
        }
    }

    private fun isWorking(): Boolean {
        return _uiState.value is BackupUiState.Exporting ||
            _uiState.value is BackupUiState.Importing
    }

    private fun export(uri: String) {
        if (isWorking()) return
        _uiState.value = BackupUiState.Exporting
        viewModelScope.launch {
            exportBackupUseCase(uri).fold(
                onSuccess = {
                    _uiState.value = BackupUiState.Idle
                    _effect.send(
                        UiEffect.ShowSnackbar(UiText.StringResource(R.string.backup_export_success))
                    )
                },
                onFailure = { exception ->
                    _uiState.value = BackupUiState.Idle
                    _effect.send(UiEffect.ShowSnackbar(exception.asUiText()))
                }
            )
        }
    }

    private fun import(uri: String) {
        if (isWorking()) return
        _uiState.value = BackupUiState.Importing
        viewModelScope.launch {
            importBackupUseCase(uri).fold(
                onSuccess = {
                    _effect.send(UiEffect.RestartApp)
                },
                onFailure = { exception ->
                    _uiState.value = BackupUiState.Idle
                    _effect.send(UiEffect.ShowSnackbar(exception.asUiText()))
                }
            )
        }
    }
}
