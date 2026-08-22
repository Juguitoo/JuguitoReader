package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetBookDailyReadingsUseCaseTest {
    private lateinit var useCase: GetBookDailyReadingsUseCase
    private val repository = mockk<DailyReadingRepository>()

    @Before
    fun setup() {
        useCase = GetBookDailyReadingsUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getAllBookDailyReadings(1) } returns flowOf(emptyList())
        val result = useCase(1)
        assertThat(result.first()).isEmpty()
        assertThat(result).isInstanceOf(Flow::class.java)
    }
}