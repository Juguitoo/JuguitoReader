package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import javax.inject.Inject

class DeleteBookDailyReadingUseCase @Inject constructor(
    private val dailyReadingRepository: DailyReadingRepository
) {
    suspend operator fun invoke(bookId: Int, date: String): Result<Unit> {
        return try {
            dailyReadingRepository.deleteBookDailyReading(bookId, date)
            Result.success(Unit)
        } catch (_ : Exception) {
            Result.failure(JuguitoException(R.string.error_delete_reading_session))
        }
    }
}