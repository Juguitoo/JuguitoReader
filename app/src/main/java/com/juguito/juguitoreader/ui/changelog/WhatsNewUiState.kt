package com.juguito.juguitoreader.ui.changelog

sealed interface WhatsNewUiState {
    data object Idle : WhatsNewUiState
    data class Visible(val currentVersion: String, val versions: List<String>) : WhatsNewUiState
}
