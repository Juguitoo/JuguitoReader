package com.juguito.juguitoreader.ui.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.usecase.reader.GetReaderGuideCompletedUseCase
import com.juguito.juguitoreader.domain.usecase.reader.MarkReaderGuideCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderGuideViewModel @Inject constructor(
    private val getReaderGuideCompletedUseCase: GetReaderGuideCompletedUseCase,
    private val markReaderGuideCompletedUseCase: MarkReaderGuideCompletedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderGuideUiState>(ReaderGuideUiState.Loading)
    val uiState: StateFlow<ReaderGuideUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = runCatching { getReaderGuideCompletedUseCase() }.getOrDefault(true)
            _uiState.value = if (completed) ReaderGuideUiState.Hidden else ReaderGuideUiState.Visible
        }
    }

    fun onEvent(event: ReaderGuideEvent) {
        when (event) {
            ReaderGuideEvent.OnDismiss -> {
                if (_uiState.value !is ReaderGuideUiState.Visible) return
                _uiState.value = ReaderGuideUiState.Hidden
                viewModelScope.launch {
                    runCatching { markReaderGuideCompletedUseCase() }
                }
            }
        }
    }
}
