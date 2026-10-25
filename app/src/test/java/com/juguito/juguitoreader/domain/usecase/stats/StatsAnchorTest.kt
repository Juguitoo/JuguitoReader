package com.juguito.juguitoreader.domain.usecase.stats

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.enums.StatsPeriod
import org.junit.Test
import java.time.LocalDate

class StatsAnchorTest {

    private val wednesday = LocalDate.of(2026, 10, 14)

    @Test
    fun `previous week lands on the sunday of that week`() {
        val anchor = previousStatsAnchor(StatsPeriod.WEEK, wednesday)

        assertThat(anchor).isEqualTo(LocalDate.of(2026, 10, 11))
        assertThat(periodRange(StatsPeriod.WEEK, anchor!!)).isEqualTo(
            LocalDate.of(2026, 10, 5)..LocalDate.of(2026, 10, 11)
        )
    }

    @Test
    fun `previous month lands on the last day of that month`() {
        assertThat(previousStatsAnchor(StatsPeriod.MONTH, wednesday))
            .isEqualTo(LocalDate.of(2026, 9, 30))
    }

    @Test
    fun `next from a past week returns today when the current week is open`() {
        val lastSunday = LocalDate.of(2026, 10, 11)

        assertThat(nextStatsAnchor(StatsPeriod.WEEK, lastSunday, wednesday)).isEqualTo(wednesday)
    }

    @Test
    fun `next stays put when the period already contains today`() {
        assertThat(nextStatsAnchor(StatsPeriod.WEEK, wednesday, wednesday)).isNull()
    }
}
