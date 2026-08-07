package com.juguito.juguitoreader.domain.repository

import com.juguito.juguitoreader.domain.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReadingProgressRepository {

    fun getAllReadingProgress(): Flow<List<ReadingProgress>>

    suspend fun getReadingProgressById(bookId: Int): ReadingProgress?

    suspend fun saveReadingProgress(readingProgress: ReadingProgress): Long

    suspend fun saveReadingProgresses(readingProgresses: List<ReadingProgress>)

    suspend fun deleteProgress(bookId: Int)
}