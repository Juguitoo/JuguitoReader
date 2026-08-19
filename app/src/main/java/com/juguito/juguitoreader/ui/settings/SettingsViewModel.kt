package com.juguito.juguitoreader.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.appThemeFlow,
        settingsRepository.readerThemeFlow,
        settingsRepository.textZoomFlow,
        settingsRepository.languageFlow
    ) { appThemeStr, readerThemeStr, textZoom, languageStr ->
        SettingsUiState(
            appTheme = runCatching { AppTheme.valueOf(appThemeStr) }.getOrDefault(AppTheme.JUGUITO),
            readerTheme = runCatching { ReaderTheme.valueOf(readerThemeStr) }.getOrDefault(ReaderTheme.SEPIA),
            textZoom = textZoom,
            language = runCatching { Language.valueOf(languageStr) }.getOrDefault(Language.SPANISH),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.OnAppThemeChanged -> {
                    settingsRepository.saveAppTheme(event.theme.name)
                }
                is SettingsEvent.OnReaderThemeChanged -> {
                    settingsRepository.saveReaderTheme(event.theme.name)
                }
                is SettingsEvent.OnTextZoomChanged -> {
                    settingsRepository.saveTextZoom(event.zoom)
                }
                is SettingsEvent.OnLanguageChanged -> {
                    settingsRepository.saveLanguage(event.language.name)
                }
                is SettingsEvent.OnAutoPendingToReadingChanged -> {
                    // Pendiente de añadir a DataStore en el futuro
                }
                is SettingsEvent.OnAutoFinishChanged -> {
                    // Pendiente de añadir a DataStore en el futuro
                }
            }
        }
    }
}