package com.juguito.juguitoreader.domain.usecase.dailyReading

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.repository.DailyReadingRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteBookDailyReadingsUseCaseTest {
    private lateinit var useCase: DeleteBookDailyReadingsUseCase
    private val repository = mockk<DailyReadingRepository>()

    @Before
    fun setup() {
        useCase = DeleteBookDailyReadingsUseCase(repository)
    }

    @Test
    fun `invoke calls delete on repository`() = runTest {
        coEvery { repository.deleteBookDailyReadings(1) } returns Unit
        val result = useCase(1)
        assertThat(result.isSuccess).isTrue()
    }
}