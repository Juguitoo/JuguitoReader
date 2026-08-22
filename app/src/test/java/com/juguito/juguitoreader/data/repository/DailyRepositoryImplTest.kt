package com.juguito.juguitoreader.data.repository

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.data.local.dao.DailyReadingDAO
import com.juguito.juguitoreader.data.local.entity.DailyReadingEntity
import com.juguito.juguitoreader.domain.model.DailyReading
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DailyRepositoryImplTest {
    private lateinit var repository: DailyReadingRepositoryImpl
    private val dao = mockk<DailyReadingDAO>()

    @Before
    fun setup() {
        repository = DailyReadingRepositoryImpl(dao)
    }

    @Test
    fun `getAllBookDailyReadings maps entities to domain`() = runTest {
        val entity = DailyReadingEntity(bookId = 1, date = "0000-00-00", timeSpentMillis = 0, reachedPercentage = 0f)
        every { dao.getAllBookDailyReadings(1) } returns flowOf(listOf(entity))

        val result = repository.getAllBookDailyReadings(1).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].bookId).isEqualTo(1)
    }

    @Test
    fun `getAllDailyReadings maps entities to domain`() = runTest {
        val entity = DailyReadingEntity(bookId = 1, date = "0000-00-00", timeSpentMillis = 0, reachedPercentage = 0f)
        every { dao.getAllDailyReadings() } returns flowOf(listOf(entity))

        val result = repository.getAllDailyReadings().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].bookId).isEqualTo(1)
    }

    @Test
    fun `getDailyReadingByIdAndDate successfully returns`() = runTest {
        val entity = DailyReadingEntity(bookId = 1, date = "0000-00-00", timeSpentMillis = 20, reachedPercentage = 0f)
        coEvery { dao.getDailyReadingByIdAndDate(1,"0000-00-00" ) } returns entity

        val result = repository.getDailyReadingByIdAndDate(1, "0000-00-00")

        assertThat(result).isNotNull()
        assertThat(result?.timeSpentMillis).isEqualTo(20)
    }

    @Test
    fun `saveDailyReading calls insert on DAO`() = runTest {
        val dailyReading = DailyReading(
            bookId = 1,
            date = "0000-00-00",
            timeSpentMillis = 20,
            reachedPercentage = 0f
        )
        coEvery { dao.insert(any()) } returns 1L

        val result = repository.saveDailyReading(dailyReading)

        assertThat(result).isEqualTo(1L)
        coVerify { dao.insert(any()) }
    }

    @Test
    fun `deleteBookDailyReadings calls deleteBookDailyReadings on DAO`() = runTest {
        coEvery { dao.deleteBookDailyReadings(1) } returns Unit

        repository.deleteBookDailyReadings(1)

        coVerify { dao.deleteBookDailyReadings(1) }
    }
}