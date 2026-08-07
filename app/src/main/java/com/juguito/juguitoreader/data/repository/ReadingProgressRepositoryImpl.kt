package com.juguito.juguitoreader.data.repository

import com.juguito.juguitoreader.data.local.dao.ReadingProgressDAO
import com.juguito.juguitoreader.data.mapper.toDomain
import com.juguito.juguitoreader.data.mapper.toEntity
import com.juguito.juguitoreader.domain.model.ReadingProgress
import com.juguito.juguitoreader.domain.repository.ReadingProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReadingProgressRepositoryImpl @Inject constructor(
    private val readingProgressDAO: ReadingProgressDAO
) : ReadingProgressRepository {
    override fun getAllReadingProgress(): Flow<List<ReadingProgress>> {
        return readingProgressDAO.getAllReadingProgress().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getReadingProgressById(bookId: Int): ReadingProgress? {
        return readingProgressDAO.getReadingProgressById(bookId)?.toDomain()
    }

    override suspend fun saveReadingProgress(readingProgress: ReadingProgress): Long {
        return readingProgressDAO.insertReadingProgress(readingProgress.toEntity())
    }

    override suspend fun saveReadingProgresses(readingProgresses: List<ReadingProgress>) {
        readingProgressDAO.insertReadingProgresses(readingProgresses.map { it.toEntity()})
    }

    override suspend fun deleteProgress(bookId: Int) {
        readingProgressDAO.deleteReadingProgressById(bookId)
    }

}