package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
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
        val d = DailyReading(1, "0000-00-00", 0, 0f, readingSpeed = 0)
        coEvery { repository.saveDailyReading(any()) } returns 1L
        coEvery { repository.getDailyReadingByIdAndDate(any(), any()) } returns null
        val result = useCase(d)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `invoke calls saveDailyReading with existing daily session`() = runTest {
        val d1 = DailyReading(1, "0000-00-00", 300, 0f, readingSpeed = 0)
        val d2 = DailyReading(1, "0000-00-00", 200, 0f, readingSpeed = 0)
        coEvery { repository.saveDailyReading(any()) } returns 1L
        coEvery { repository.getDailyReadingByIdAndDate(any(), any()) } returns d1
        val result = useCase(d2)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `invoke with existing session calculates weighted average speed`() = runTest {
        val existingSession = DailyReading(
            bookId = 1,
            date = "2026-08-27",
            timeSpentMillis = 600_000,
            reachedPercentage = 20f,
            readingSpeed = 200
        )
        val newSession = DailyReading(
            bookId = 1,
            date = "2026-08-27",
            timeSpentMillis = 600_000,
            reachedPercentage = 30f,
            readingSpeed = 400
        )

        coEvery { repository.getDailyReadingByIdAndDate(1, "2026-08-27") } returns existingSession

        val slot = slot<DailyReading>()
        coEvery { repository.saveDailyReading(capture(slot)) } returns 1L

        // 3. Ejecutamos
        val result = useCase(newSession)

        // 4. Comprobamos la magia
        assertThat(result.isSuccess).isTrue()
        assertThat(slot.captured.timeSpentMillis).isEqualTo(1_200_000)
        assertThat(slot.captured.readingSpeed).isEqualTo(300)
    }
}