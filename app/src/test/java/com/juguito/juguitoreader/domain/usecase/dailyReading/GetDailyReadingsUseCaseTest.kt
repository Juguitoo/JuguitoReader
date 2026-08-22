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

class GetDailyReadingsUseCaseTest {
    private lateinit var useCase: GetDailyReadingsUseCase
    private val repository = mockk<DailyReadingRepository>()

    @Before
    fun setup() {
        useCase = GetDailyReadingsUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        every { repository.getAllDailyReadings() } returns flowOf(emptyList())
        val result = useCase()
        assertThat(result.first()).isEmpty()
        assertThat(result).isInstanceOf(Flow::class.java)
    }
}