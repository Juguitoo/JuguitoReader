package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class AddReadingProgressUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(readingProgress: ReadingProgress): Result<Unit> {
        return try {
            repository.saveReadingProgress(readingProgress)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(JuguitoException(R.string.error_save_progress))
        }
    }
}
