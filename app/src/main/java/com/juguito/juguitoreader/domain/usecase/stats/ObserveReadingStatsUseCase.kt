package com.juguito.juguitoreader.domain.usecase.stats

import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetDailyReadingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

class ObserveReadingStatsUseCase @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val getDailyReadingsUseCase: GetDailyReadingsUseCase,
) {
    operator fun invoke(
        period: StatsPeriod,
        today: LocalDate = LocalDate.now(),
    ): Flow<ReadingStats> {
        return combine(
            getBooksUseCase(),
            getDailyReadingsUseCase(),
        ) { books, readings ->
            aggregateReadingStats(
                books = books,
                dailyReadings = readings,
                period = period,
                today = today,
                streakAsOf = LocalDate.now(),
            )
        }
    }
}
