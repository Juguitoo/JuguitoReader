package com.juguito.juguitoreader.ui.onboarding

sealed interface OnboardingEvent {
    data object OnFinished : OnboardingEvent
}
