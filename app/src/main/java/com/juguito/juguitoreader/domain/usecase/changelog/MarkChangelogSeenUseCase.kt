package com.juguito.juguitoreader.domain.usecase.changelog

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import javax.inject.Inject

class MarkChangelogSeenUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(current: String) {
        settingsRepository.saveLastSeenChangelogVersion(current)
    }
}