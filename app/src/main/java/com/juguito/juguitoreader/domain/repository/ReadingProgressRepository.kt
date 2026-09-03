package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReadingProgressRepository {

    fun getAllReadingProgress(): Flow<List<ReadingProgress>>

    suspend fun getReadingProgressById(bookId: Int): ReadingProgress?

    suspend fun insertReadingProgress(readingProgress: ReadingProgress): Long

    suspend fun insertReadingProgresses(readingProgresses: List<ReadingProgress>)

    suspend fun updateReadingProgress(readingProgress: ReadingProgress)

    suspend fun deleteProgress(bookId: Int)
}