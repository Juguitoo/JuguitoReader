package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDailyReadingsUseCase @Inject constructor(
    private val repository: DailyReadingRepository
) {
    operator fun invoke(): Flow<List<DailyReading>>{
        return repository.getAllDailyReadings()
    }
}