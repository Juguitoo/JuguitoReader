package com.juguito.juguitoreader.ui.onboarding

sealed interface OnboardingUiState {
    data object Loading : OnboardingUiState
    data object Visible : OnboardingUiState
    data object Hidden : OnboardingUiState
}
