package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import javax.inject.Inject

class AddDailyReadingUseCase @Inject constructor(
    private val repository: DailyReadingRepository
) {
    suspend operator fun invoke(dailyReading: DailyReading): Result<Unit> {
        val todayDailyReading = repository.getDailyReadingByIdAndDate(dailyReading.bookId, dailyReading.date)

        val dailyReadingToSave = if (todayDailyReading != null) {
            val newVelocity =
                (((todayDailyReading.readingSpeed.toLong() * todayDailyReading.timeSpentMillis.toLong()) + (dailyReading.readingSpeed.toLong() * dailyReading.timeSpentMillis.toLong())) / (todayDailyReading.timeSpentMillis.toLong() + dailyReading.timeSpentMillis.toLong())).toInt()
            dailyReading.copy(
                timeSpentMillis = dailyReading.timeSpentMillis + todayDailyReading.timeSpentMillis,
                readingSpeed = newVelocity
            )
        } else dailyReading

        return try {
            repository.saveDailyReading(dailyReadingToSave)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(JuguitoException(R.string.error_save_reading_session))
        }
    }
}
