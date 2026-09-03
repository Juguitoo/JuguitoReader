package com.juguito.juguitoreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDAO {

    @Insert
    suspend fun insertReadingProgress(readingProgressEntity: ReadingProgressEntity): Long

    @Insert
    suspend fun insertReadingProgresses(readingProgressEntities: List<ReadingProgressEntity>)

    @Update
    suspend fun updateReadingProgress(readingProgressEntity: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress")
    fun getAllReadingProgress(): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    suspend fun getReadingProgressById(bookId: Int): ReadingProgressEntity?

    @Query("DELETE FROM reading_progress WHERE book_id = :bookId")
    suspend fun  deleteReadingProgressById(bookId: Int)
}