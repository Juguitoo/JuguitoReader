package com.juguito.juguitoreader.domain.usecase.readingProgress

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.BookRepository
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import com.juguito.juguitoreader.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateReadingProgressUseCase @Inject constructor(
    private val progressRepository: ReadingProgressRepository,
    private val bookRepository: BookRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(readingProgress: ReadingProgress): Result<Unit> {
        return try {
            progressRepository.saveReadingProgress(readingProgress)

            val book = bookRepository.getBookById(readingProgress.bookId) ?: return Result.success(Unit)

            val autoStart = settingsRepository.autoStartReadingFlow.first()
            val autoFinish = settingsRepository.autoFinishReadingFlow.first()

            var newStatus = book.status

            if (autoStart && book.status == BookStatus.PENDING) {
                newStatus = BookStatus.READING
            }

            if (autoFinish && readingProgress.percentage == 100) {
                newStatus = BookStatus.FINISHED
            }

            if (newStatus != book.status) {
                bookRepository.updateBook(book.copy(status = newStatus))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(JuguitoException(R.string.error_update_progress))
        }
    }
}
