package com.juguito.juguitoreader.domain.usecase.reader

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetReaderGuideCompletedUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean = settingsRepository.readerGuideCompletedFlow.first()
}
