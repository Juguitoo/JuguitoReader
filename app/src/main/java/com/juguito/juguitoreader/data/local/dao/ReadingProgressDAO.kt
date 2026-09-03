package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgress(readingProgressEntity: ReadingProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingProgresses(readingProgressEntities: List<ReadingProgressEntity>)

    @Query("SELECT * FROM reading_progress")
    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    suspend fun getReadingProgressById(bookId: Int): ReadingProgressEntity?

    @Query("DELETE FROM reading_progress WHERE book_id = :bookId")
    suspend fun  deleteReadingProgressById(bookId: Int)
}