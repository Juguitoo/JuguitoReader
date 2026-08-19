package com.juguito.juguitoreader.ui.settings

import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.theme.AppTheme

sealed interface SettingsEvent {
    data class OnAppThemeChanged(val theme: AppTheme): SettingsEvent
    data class OnReaderThemeChanged(val theme: ReaderTheme): SettingsEvent
    data class OnTextZoomChanged(val zoom: Int): SettingsEvent
    data class OnLanguageChanged(val language: Language): SettingsEvent
    data class OnAutoPendingToReadingChanged(val enable: Boolean): SettingsEvent
    data class OnAutoFinishChanged(val enabled: Boolean): SettingsEvent
}