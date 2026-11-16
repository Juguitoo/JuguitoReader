package com.juguito.juguitoreader.ui.reader.guide

sealed interface ReaderGuideUiState {
    data object Loading : ReaderGuideUiState
    data object Visible : ReaderGuideUiState
    data object Hidden : ReaderGuideUiState
}
