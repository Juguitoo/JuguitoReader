package com.juguito.juguitoreader.ui.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.model.Genre
import com.juguito.juguitoreader.domain.usecase.genre.AddGenreUseCase
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.asUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGenreViewModel @Inject constructor(
    private val addGenreUseCase: AddGenreUseCase
) : ViewModel() {
    private val _error = MutableStateFlow<UiText?>(null)
    val error: StateFlow<UiText?> = _error.asStateFlow()

    fun saveGenre(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _error.value = null

            val result = addGenreUseCase(Genre(name = name))

            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { exception ->
                    _error.value = exception.asUiText()
                }
            )
        }
    }

    fun clearError() {
        _error.value = null
    }
}