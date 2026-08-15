package com.juguito.juguitoreader.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val appTheme: StateFlow<AppTheme> = settingsRepository.appThemeFlow
        .map { themeString ->
            runCatching { AppTheme.valueOf(themeString) }.getOrDefault(AppTheme.JUGUITO)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.JUGUITO
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.saveAppTheme(theme.name)
        }
    }
}
