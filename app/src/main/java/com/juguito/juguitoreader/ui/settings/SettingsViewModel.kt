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
    private data class ReadingPrefs(
        val language: String,
        val autoStart: Boolean,
        val autoFinish: Boolean,
        val prompt: Boolean
    )
    private val themeAndZoomFlow = combine(
        settingsRepository.appThemeFlow,
        settingsRepository.readerThemeFlow,
        settingsRepository.textZoomFlow
    ) { appTheme, readerTheme, zoom ->
        Triple(appTheme, readerTheme, zoom)
    }

    private val readingPrefsFlow = combine(
        settingsRepository.languageFlow,
        settingsRepository.autoStartReadingFlow,
        settingsRepository.autoFinishReadingFlow,
        settingsRepository.promptStatusChangeFlow
    ) { language, autoStart, autoFinish, promptStatusChangeFlow->
        ReadingPrefs(language, autoStart, autoFinish, promptStatusChangeFlow)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        themeAndZoomFlow,
        readingPrefsFlow
    ) { (appThemeStr, readerThemeStr, textZoom), prefs ->
        SettingsUiState(
            appTheme = runCatching { AppTheme.valueOf(appThemeStr) }.getOrDefault(AppTheme.JUGUITO),
            readerTheme = runCatching { ReaderTheme.valueOf(readerThemeStr) }.getOrDefault(ReaderTheme.SEPIA),
            textZoom = textZoom,
            language = runCatching { Language.valueOf(prefs.language) }.getOrDefault(Language.SPANISH),
            autoStart = prefs.autoStart,
            autoFinish = prefs.autoFinish,
            promptStatusChange = prefs.prompt,
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
                    settingsRepository.saveAutoStartReading(event.enable)

                    if (event.enable) {
                        settingsRepository.savePromptStatusChange(false)
                    }
                }
                is SettingsEvent.OnAutoFinishChanged -> {
                    settingsRepository.saveAutoFinishReading(event.enable)
                }
                is SettingsEvent.OnPromptStatusChangeChanged -> {
                    settingsRepository.savePromptStatusChange(event.enable)
                }
            }
        }
    }
}