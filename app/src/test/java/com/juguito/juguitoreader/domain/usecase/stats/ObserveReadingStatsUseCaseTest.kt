package com.juguito.juguitoreader.domain.usecase.stats

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.model.DailyReading
import com.juguito.juguitoreader.domain.usecase.book.GetBooksUseCase
import com.juguito.juguitoreader.domain.usecase.dailyReading.GetDailyReadingsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class ObserveReadingStatsUseCaseTest {

    private val getBooksUseCase = mockk<GetBooksUseCase>()
    private val getDailyReadingsUseCase = mockk<GetDailyReadingsUseCase>()
    private val useCase = ObserveReadingStatsUseCase(getBooksUseCase, getDailyReadingsUseCase)
    private val today = LocalDate.of(2026, 10, 14)

    @Test
    fun `invoke aggregates the current books and readings`() = runTest {
        every { getBooksUseCase() } returns flowOf(
            listOf(
                Book(
                    id = 1,
                    title = "Finished",
                    author = "Author",
                    isPhysical = false,
                    status = BookStatus.FINISHED,
                )
            )
        )
        every { getDailyReadingsUseCase() } returns flowOf(
            listOf(
                DailyReading(
                    bookId = 1,
                    date = "2026-10-14",
                    timeSpentMillis = 60_000,
                    reachedPercentage = 10f,
                    readingSpeed = 200,
                )
            )
        )

        useCase(StatsPeriod.MONTH, today).test {
            val stats = awaitItem()
            assertThat(stats.totalFinished).isEqualTo(1)
            assertThat(stats.readingTimeComparison.currentMinutes).isEqualTo(1)
            assertThat(stats.readingAverageVelocity).isEqualTo(200)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits again when the books change`() = runTest {
        val books = MutableSharedFlow<List<Book>>(replay = 1)
        every { getBooksUseCase() } returns books
        every { getDailyReadingsUseCase() } returns flowOf(emptyList())
        books.emit(emptyList())

        useCase(StatsPeriod.MONTH, today).test {
            assertThat(awaitItem().totalFinished).isEqualTo(0)
            books.emit(
                listOf(
                    Book(
                        id = 2,
                        title = "Later",
                        author = "Author",
                        isPhysical = true,
                        status = BookStatus.FINISHED,
                    )
                )
            )
            assertThat(awaitItem().totalFinished).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
