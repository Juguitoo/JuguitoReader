package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.DailyReading
import kotlinx.coroutines.flow.Flow

interface DailyReadingRepository {

    fun getAllBookDailyReadings(bookId: Int): Flow<List<DailyReading>>
    fun getAllDailyReadings(): Flow<List<DailyReading>>
    suspend fun getDailyReadingByIdAndDate(bookId: Int, date: String): DailyReading?
    suspend fun saveDailyReading(dailyReading: DailyReading): Long
    suspend fun deleteBookDailyReadings(bookId: Int)
}