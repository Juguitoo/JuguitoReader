package com.juguito.juguitoreader.ui.folder.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.GetFolderByIdUseCase
import com.juguito.juguitoreader.domain.usecase.folder.UpdateFolderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class FolderEditorViewModel @Inject constructor(
    private val updateFolderUseCase: UpdateFolderUseCase,
    private val getFolderByIdUseCase: GetFolderByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FolderEditorUiState())
    val uiState: StateFlow<FolderEditorUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    private val folderId: Int = checkNotNull(savedStateHandle["folderId"])

    init {
        loadFolder(folderId)
    }

    private fun loadFolder(id: Int) {
        viewModelScope.launch {
            val folder = getFolderByIdUseCase(id)
            if (folder != null) {
                _uiState.value = _uiState.value.copy(
                    name = folder.name,
                    description = folder.description ?: "",
                    colorHex = folder.colorHex
                )
            }
        }
    }

    fun onEvent(event: FolderEditorEvent) {
        when (event) {
            is FolderEditorEvent.OnNameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is FolderEditorEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.colorHex)
            }
            is FolderEditorEvent.OnDescriptionChanged -> {
                _uiState.value = _uiState.value.copy(description = event.description)
            }
            FolderEditorEvent.OnSaveClick -> {
                saveFolder()
            }
        }
    }

    private fun saveFolder() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            viewModelScope.launch {
                _effect.send(UiEffect.ShowSnackbar("El nombre de la carpeta no puede estar vacío."))
            }
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val folder = Folder(
                id = folderId,
                name = currentState.name,
                description = currentState.description,
                colorHex =  currentState.colorHex
            )

            val result = updateFolderUseCase(folder)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.NavigateBack)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.ShowSnackbar(exception.localizedMessage ?: "Error al guardar los cambios"))
                }
            )
        }
    }
}
