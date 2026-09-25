package com.juguito.juguitoreader.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juguito.juguitoreader.domain.usecase.onboarding.GetOnboardingCompletedUseCase
import com.juguito.juguitoreader.domain.usecase.onboarding.MarkOnboardingCompletedUseCase
import com.juguito.juguitoreader.utils.appVersionName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase,
    private val markOnboardingCompletedUseCase: MarkOnboardingCompletedUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Loading)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val currentVersion = context.appVersionName()

    init {
        viewModelScope.launch {
            val completed = runCatching { getOnboardingCompletedUseCase() }.getOrDefault(true)
            _uiState.value = if (completed) OnboardingUiState.Hidden else OnboardingUiState.Visible
        }
    }

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.OnFinished -> {
                if (_uiState.value !is OnboardingUiState.Visible) return
                _uiState.value = OnboardingUiState.Hidden
                viewModelScope.launch {
                    runCatching { markOnboardingCompletedUseCase(currentVersion) }
                }
            }
        }
    }
}
