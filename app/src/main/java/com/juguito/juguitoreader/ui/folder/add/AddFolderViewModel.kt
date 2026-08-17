package com.juguito.juguitoreader.ui.folder.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.domain.usecase.folder.AddFolderUseCase
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
class AddFolderViewModel @Inject constructor(
    private val addFolderUseCase: AddFolderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFolderUiState())
    val uiState: StateFlow<AddFolderUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: AddFolderEvent) {
        when (event) {
            is AddFolderEvent.OnNameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name)
            }
            is AddFolderEvent.OnColorChanged -> {
                _uiState.value = _uiState.value.copy(colorHex = event.colorHex)
            }
            is AddFolderEvent.OnDescriptionChanged -> {
                _uiState.value = _uiState.value.copy(description = event.description)
            }
            AddFolderEvent.OnSaveClick -> {
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
                id = 0,
                name = currentState.name,
                description = currentState.description,
                colorHex =  currentState.colorHex
            )

            val result = addFolderUseCase(folder)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.NavigateBack)
                },
                onFailure = { exception ->
                    exception.printStackTrace()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _effect.send(UiEffect.ShowSnackbar(exception.message ?: "Error al crear la carpeta."))
                }
            )
        }
    }
}
