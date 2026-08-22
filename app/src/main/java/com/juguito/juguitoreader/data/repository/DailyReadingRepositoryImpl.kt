package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.DailyReadingDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DailyReadingRepositoryImpl @Inject constructor(
    private val dailyReadingDAO: DailyReadingDAO
) : DailyReadingRepository {
    override fun getAllBookDailyReadings(bookId: Int): Flow<List<DailyReading>> {
        return dailyReadingDAO.getAllBookDailyReadings(bookId).map { list -> list.map { it.toDomain() } }
    }

    override fun getAllDailyReadings(): Flow<List<DailyReading>> {
        return dailyReadingDAO.getAllDailyReadings().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDailyReadingByIdAndDate(
        bookId: Int,
        date: String
    ): DailyReading? {
        return dailyReadingDAO.getDailyReadingByIdAndDate(bookId, date)?.toDomain()
    }

    override suspend fun saveDailyReading(dailyReading: DailyReading): Long {
        return dailyReadingDAO.insert(dailyReading.toEntity())
    }

    override suspend fun deleteBookDailyReadings(bookId: Int) {
        dailyReadingDAO.deleteBookDailyReadings(bookId)
    }
}