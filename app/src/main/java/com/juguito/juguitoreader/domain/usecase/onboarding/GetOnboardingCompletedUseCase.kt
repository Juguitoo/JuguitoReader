package com.juguito.juguitoreader.domain.usecase.onboarding

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetOnboardingCompletedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = settingsRepository.onboardingCompletedFlow.first()
}
