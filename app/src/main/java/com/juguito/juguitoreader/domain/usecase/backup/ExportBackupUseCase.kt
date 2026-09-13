package com.juguito.juguitoreader.domain.usecase.backup

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BackupRepository
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(destinationUri: String): Result<Unit> {
        if (destinationUri.isBlank()) return Result.failure(JuguitoException(R.string.error_backup_destination))
        return runCatching {
            backupRepository.exportTo(destinationUri)
        }
    }
}