package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import javax.inject.Inject

class DeleteReadingProgressUseCase @Inject constructor(
    private val repository: ReadingProgressRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            repository.deleteProgress(bookId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_progress))
        }
    }
}
