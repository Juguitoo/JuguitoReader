package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import javax.inject.Inject

class DeleteBookDailyReadingsUseCase @Inject constructor(
    private val repository: DailyReadingRepository
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> {
        return try {
            repository.deleteBookDailyReadings(bookId)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_delete_reading_sessions))
        }
    }
}
