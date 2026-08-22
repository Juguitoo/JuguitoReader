package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.model.copy
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import javax.inject.Inject

class AddDailyReadingUseCase @Inject constructor(
    private val repository: DailyReadingRepository
) {

    suspend operator fun invoke(dailyReading: DailyReading): Result<Unit> {
        val todayDailyReading = repository.getDailyReadingByIdAndDate(dailyReading.bookId, dailyReading.date)

        val dailyReadingToSave = if (todayDailyReading != null) dailyReading.copy(timeSpentMillis = dailyReading.timeSpentMillis + todayDailyReading.timeSpentMillis) else dailyReading

        return try {
            repository.saveDailyReading(dailyReadingToSave)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al guardar la sesión de lectura."))
        }
    }
}