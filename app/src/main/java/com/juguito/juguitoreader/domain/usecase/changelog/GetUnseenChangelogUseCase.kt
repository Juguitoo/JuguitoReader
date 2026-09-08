package com.juguito.juguitoreader.domain.usecase.changelog

import com.juguito.juguitoreader.domain.repository.SettingsRepository
import com.juguito.juguitoreader.utils.unseenChangelogVersions
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetUnseenChangelogUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(currentVersion: String, catalogNewestFirst: List<String>): List<String> {
        val lastSeen = settingsRepository.lastSeenChangelogVersion.first()
        return unseenChangelogVersions(currentVersion, lastSeen, catalogNewestFirst)
    }
}