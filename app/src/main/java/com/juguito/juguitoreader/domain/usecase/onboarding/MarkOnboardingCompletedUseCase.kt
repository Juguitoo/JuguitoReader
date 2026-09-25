package com.juguito.juguitoreader.domain.usecase.onboarding

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MarkOnboardingCompletedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentVersion: String) {
        val lastSeen = settingsRepository.lastSeenChangelogVersionFlow.first()
        settingsRepository.saveOnboardingCompleted(true)
        if (lastSeen == null) {
            settingsRepository.saveLastSeenChangelogVersion(currentVersion)
        }
    }
}
