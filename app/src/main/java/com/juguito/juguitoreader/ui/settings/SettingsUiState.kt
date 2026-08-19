package com.juguito.juguitoreader.ui.settings

import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.theme.AppTheme

data class SettingsUiState(
    val appTheme: AppTheme = AppTheme.JUGUITO,
    val readerTheme: ReaderTheme = ReaderTheme.SEPIA,
    val textZoom: Int = 14,
    val language: Language = Language.SPANISH,
    val autoPendingToReading: Boolean = false,
    val autoFinish: Boolean = false,
    val isLoading: Boolean = false
)