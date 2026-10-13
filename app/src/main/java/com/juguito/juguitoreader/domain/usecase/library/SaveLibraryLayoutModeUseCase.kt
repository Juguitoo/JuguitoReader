package com.juguito.juguitoreader.domain.usecase.library

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import javax.inject.Inject

class SaveLibraryLayoutModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(layoutMode: Int) {
        settingsRepository.saveLibraryLayoutMode(layoutMode)
    }
}
