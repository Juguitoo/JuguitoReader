package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.DailyReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReadingDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyReading: DailyReadingEntity): Long

    @Query("SELECT * FROM daily_reading WHERE book_id = :bookId")
    fun getAllBookDailyReadings(bookId: Int): Flow<List<DailyReadingEntity>>

    @Query("SELECT * FROM daily_reading")
    fun getAllDailyReadings(): Flow<List<DailyReadingEntity>>

    @Query("SELECT * FROM daily_reading WHERE book_id = :bookId AND date = :date")
    suspend fun getDailyReadingByIdAndDate(bookId: Int, date: String): DailyReadingEntity?

    @Query("DELETE FROM daily_reading WHERE book_id = :bookId")
    suspend fun deleteBookDailyReadings(bookId: Int)
}