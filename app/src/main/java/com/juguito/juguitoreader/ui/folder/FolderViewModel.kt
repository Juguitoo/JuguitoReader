package com.juguito.juguitoreader.ui.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.AddFolderUseCase
import com.juguito.juguitoreader.domain.usecase.folder.GetFolderByIdUseCase
import com.juguito.juguitoreader.domain.usecase.folder.UpdateFolderUseCase
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
class FolderViewModel @Inject constructor(
    private val addFolderUseCase: AddFolderUseCase,
    private val updateFolderUseCase: UpdateFolderUseCase,
    private val getFolderByIdUseCase: GetFolderByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val folderId: Int? = savedStateHandle.get<Int>("folderId")?.takeIf { it != -1 }

    private val _uiState = MutableStateFlow(FolderUiState(isEditing = folderId != null))
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        folderId?.let { id ->
            loadFolder(id)
        }
    }

    private fun loadFolder(id: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val folder = getFolderByIdUseCase.invoke(id)
            if (folder != null) {
                _uiState.value = _uiState.value.copy(
                    name = folder.name,
                    description = folder.description ?: "",
                    colorHex = folder.colorHex,
                    isLoading = false
                )
            } else {
                _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.something_went_wrong)))
            }
        }
    }

    fun onEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.OnColorChanged -> { _uiState.value = _uiState.value.copy(colorHex = event.colorHex) }
            is FolderEvent.OnDescriptionChanged -> { _uiState.value = _uiState.value.copy(description = event.description) }
            is FolderEvent.OnNameChanged -> { _uiState.value = _uiState.value.copy(name = event.name, nameError = null) }
            FolderEvent.OnSaveClick -> saveFolder()
        }
    }

    private fun saveFolder() {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        if (currentState.name.isBlank()) {
            viewModelScope.launch {
                _effect.send(UiEffect.ShowSnackbar(UiText.StringResource(R.string.name_empty_error)))
            }
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val folder = Folder(
                id = folderId ?: 0,
                name = currentState.name.trim(),
                description = currentState.description.trim().ifEmpty { null },
                colorHex = currentState.colorHex
            )

            val result = if (currentState.isEditing) {
                updateFolderUseCase(folder)
            } else {
                addFolderUseCase(folder)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.NavigateBack)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.ShowSnackbar(exception.asUiText()))
                }
            )
        }
    }
}