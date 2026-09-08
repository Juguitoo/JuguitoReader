package com.juguito.juguitoreader.ui.changelog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.usecase.changelog.GetUnseenChangelogUseCase
import com.juguito.juguitoreader.domain.usecase.changelog.MarkChangelogSeenUseCase
import com.juguito.juguitoreader.utils.appVersionName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsNewViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val getUnseenChangelogUseCase: GetUnseenChangelogUseCase,
    private val markChangelogSeenUseCase: MarkChangelogSeenUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<WhatsNewUiState>(WhatsNewUiState.Idle)
    val uiState: StateFlow<WhatsNewUiState> = _uiState.asStateFlow()

    private val currentVersion = context.appVersionName()

    init {
        viewModelScope.launch {
            val versionNames = runCatching {
                getUnseenChangelogUseCase(
                    currentVersion,
                    ChangelogUiCatalog.newestFirst.map { it.versionName }
                )
            }.getOrDefault(emptyList())

            if (versionNames.isEmpty()) return@launch
            if (_uiState.value !is WhatsNewUiState.Idle) return@launch
            _uiState.value = WhatsNewUiState.Visible(currentVersion, versionNames)
        }
    }

    fun onEvent(event: WhatsNewEvent) {
        when (event) {
            is WhatsNewEvent.OnDismiss -> {
                if (_uiState.value !is WhatsNewUiState.Visible) return
                _uiState.value = WhatsNewUiState.Idle
                viewModelScope.launch {
                    runCatching { markChangelogSeenUseCase(currentVersion) }
                }
            }
        }
    }
}
