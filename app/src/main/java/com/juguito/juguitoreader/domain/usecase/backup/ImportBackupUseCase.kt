package com.juguito.juguitoreader.domain.usecase.backup

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.BackupRepository
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {

    suspend operator fun invoke(sourceUri: String): Result<Unit> {
        if (sourceUri.isBlank()) return Result.failure(JuguitoException(R.string.error_backup_source))
        return runCatching {
            backupRepository.importFrom(sourceUri)
        }
    }
}