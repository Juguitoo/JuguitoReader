package com.juguito.juguitoreader.domain.usecase.library

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLibraryLayoutModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Int> = settingsRepository.libraryLayoutModeFlow
}
