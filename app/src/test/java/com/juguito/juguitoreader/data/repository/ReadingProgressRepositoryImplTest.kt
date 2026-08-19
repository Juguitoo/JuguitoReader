package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.dao.ReadingProgressDAO
import com.juguito.juguitoreader.data.local.entity.ReadingProgressEntity
import com.juguito.juguitoreader.domain.model.ReadingProgress
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ReadingProgressRepositoryImplTest {

    private lateinit var repository: ReadingProgressRepositoryImpl
    private val dao = mockk<ReadingProgressDAO>()

    @Before
    fun setup() {
        repository = ReadingProgressRepositoryImpl(dao)
    }

    @Test
    fun `getAllReadingProgress maps entities to domain`() = runTest {
        val entity = ReadingProgressEntity(bookId = 1, lastChapterIndex = 0, scrollPosition = 0f, lastReadAt = 0L)
        every { dao.getAllReadingProgress() } returns flowOf(listOf(entity))
        
        val result = repository.getAllReadingProgress().first()
        
        assertThat(result).hasSize(1)
        assertThat(result[0].bookId).isEqualTo(1)
    }

    @Test
    fun `getReadingProgressById returns domain progress`() = runTest {
        val entity = ReadingProgressEntity(bookId = 1, lastChapterIndex = 2, scrollPosition = 0.5f, lastReadAt = 100L)
        coEvery { dao.getReadingProgressById(1) } returns entity
        
        val result = repository.getReadingProgressById(1)
        
        assertThat(result).isNotNull()
        assertThat(result?.lastChapterIndex).isEqualTo(2)
    }

    @Test
    fun `saveReadingProgress calls insert on DAO`() = runTest {
        val progress = ReadingProgress(bookId = 1, lastChapterIndex = 5, scrollPosition = 0.5f, lastReadAt = 100L)
        coEvery { dao.insertReadingProgress(any()) } returns 1L
        
        repository.saveReadingProgress(progress)
        
        coVerify { dao.insertReadingProgress(any()) }
    }

    @Test
    fun `saveReadingProgresses calls insert on DAO`() = runTest {
        val list = listOf(ReadingProgress(bookId = 1, lastChapterIndex = 1, scrollPosition = 0f, lastReadAt = 0L))
        coEvery { dao.insertReadingProgresses(any()) } returns Unit
        
        repository.saveReadingProgresses(list)
        
        coVerify { dao.insertReadingProgresses(any()) }
    }

    @Test
    fun `deleteProgress calls deleteReadingProgressById on DAO`() = runTest {
        coEvery { dao.deleteReadingProgressById(1) } returns Unit
        
        repository.deleteProgress(1)
        
        coVerify { dao.deleteReadingProgressById(1) }
    }
}
