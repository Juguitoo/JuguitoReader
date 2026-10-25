package com.juguito.juguitoreader.ui.stats

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import com.juguito.juguitoreader.domain.model.ReadingStats
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.domain.model.ReadingTimeComparison
import com.juguito.juguitoreader.domain.usecase.stats.ObserveReadingStatsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val observeReadingStatsUseCase = mockk<ObserveReadingStatsUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `heatmap follows the month of the selected period`() = runTest {
        val today = LocalDate.now()
        every { observeReadingStatsUseCase(any(), any()) } answers {
            val anchor = invocation.args[1] as LocalDate
            flowOf(stats(buckets = listOf(ReadingStatsBucket(anchor, anchor.dayOfMonth)), velocity = 200))
        }
        every { observeReadingStatsUseCase(StatsPeriod.WEEK, any()) } answers {
            flowOf(stats(buckets = listOf(ReadingStatsBucket(today, 15)), velocity = 285))
        }

        val viewModel = StatsViewModel(observeReadingStatsUseCase)

        viewModel.uiState.test {
            val month = awaitItem() as StatsUiState.Success
            assertThat(month.heatmapMonth).isEqualTo(YearMonth.from(today))
            assertThat(month.heatmapDays.single().start).isEqualTo(today.withDayOfMonth(1))

            viewModel.onEvent(StatsEvent.OnPreviousPeriod)

            val previous = awaitItem() as StatsUiState.Success
            val previousMonth = YearMonth.from(today).minusMonths(1)
            assertThat(previous.heatmapMonth).isEqualTo(previousMonth)
            assertThat(previous.heatmapDays.single().start).isEqualTo(previousMonth.atDay(1))

            viewModel.onEvent(StatsEvent.OnSelectPeriod(StatsPeriod.WEEK))

            val week = awaitItem() as StatsUiState.Success
            assertThat(week.selectedPeriod).isEqualTo(StatsPeriod.WEEK)
            assertThat(week.stats.readingAverageVelocity).isEqualTo(285)
            assertThat(week.heatmapMonth).isEqualTo(heatmapMonth(StatsPeriod.WEEK, today, today))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `heatmap uses the last month of a past year and the current month of this year`() {
        val today = LocalDate.of(2026, 10, 25)
        assertThat(heatmapMonth(StatsPeriod.YEAR, LocalDate.of(2025, 6, 1), today))
            .isEqualTo(YearMonth.of(2025, 12))
        assertThat(heatmapMonth(StatsPeriod.YEAR, today, today))
            .isEqualTo(YearMonth.of(2026, 10))
        assertThat(heatmapMonth(StatsPeriod.WEEK, LocalDate.of(2026, 1, 4), today))
            .isEqualTo(YearMonth.of(2025, 12))
    }

    @Test
    fun `a failing stats flow surfaces an error`() = runTest {
        every { observeReadingStatsUseCase(any(), any()) } returns flow {
            throw IllegalStateException("stats failed")
        }

        val viewModel = StatsViewModel(observeReadingStatsUseCase)

        viewModel.uiState.test {
            assertThat(awaitItem()).isInstanceOf(StatsUiState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun stats(buckets: List<ReadingStatsBucket>, velocity: Int): ReadingStats {
        return ReadingStats(
            totalFinished = 0,
            finishedInPeriod = emptyList(),
            readingStreak = 1,
            bestReadingStreak = 3,
            readingMinutes = buckets,
            readingTimeComparison = ReadingTimeComparison(buckets.sumOf { it.minutes }, 0),
            daysReadInPeriod = buckets.count { it.minutes > 0 },
            readingAverageVelocity = velocity,
            averageFinishedRating = null,
            mostAllReadGenre = null,
            mostReadGenreInPeriod = null,
        )
    }
}
