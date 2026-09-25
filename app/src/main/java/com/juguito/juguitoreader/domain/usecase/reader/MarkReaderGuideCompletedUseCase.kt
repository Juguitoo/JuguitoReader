package com.juguito.juguitoreader.domain.usecase.reader

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import javax.inject.Inject

class MarkReaderGuideCompletedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.saveReaderGuideCompleted(true)
    }
}
