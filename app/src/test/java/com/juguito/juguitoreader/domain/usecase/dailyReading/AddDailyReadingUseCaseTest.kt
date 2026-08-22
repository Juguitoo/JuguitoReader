package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddDailyReadingUseCaseTest {
    private lateinit var useCase: AddDailyReadingUseCase
    private val repository = mockk<DailyReadingRepository>()

    @Before
    fun setup() {
        useCase = AddDailyReadingUseCase(repository)
    }

    @Test
    fun `invoke calls saveDailyReading with new session`() = runTest {
        val d = DailyReading(1, "0000-00-00", 0, 0f)
        coEvery { repository.saveDailyReading(any()) } returns 1L
        coEvery { repository.getDailyReadingByIdAndDate(any(), any()) } returns null
        val result = useCase(d)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `invoke calls saveDailyReading with existing daily session`() = runTest {
        val d1 = DailyReading(1, "0000-00-00", 300, 0f)
        val d2 = DailyReading(1, "0000-00-00", 200, 0f)
        coEvery { repository.saveDailyReading(any()) } returns 1L
        coEvery { repository.getDailyReadingByIdAndDate(any(), any()) } returns d1
        val result = useCase(d2)
        assertThat(result.isSuccess).isTrue()
    }
}