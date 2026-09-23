package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteBookDailyReadingUseCaseTest {

    private lateinit var useCase: DeleteBookDailyReadingUseCase
    private val repository = mockk<DailyReadingRepository>()

    @Before
    fun setup() {
        useCase = DeleteBookDailyReadingUseCase(repository)
    }

    @Test
    fun `invoke deletes the row for that book and date`() = runTest {
        coEvery { repository.deleteBookDailyReading(1, "2026-09-23") } returns Unit

        val result = useCase(1, "2026-09-23")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { repository.deleteBookDailyReading(1, "2026-09-23") }
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        coEvery { repository.deleteBookDailyReading(1, "2026-09-23") } throws IllegalStateException("db")

        val result = useCase(1, "2026-09-23")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull() as JuguitoException
        assertThat(exception.resId).isEqualTo(R.string.error_delete_reading_session)
    }
}
