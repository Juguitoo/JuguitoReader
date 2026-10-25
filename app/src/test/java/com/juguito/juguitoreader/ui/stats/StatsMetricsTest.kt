package com.juguito.juguitoreader.ui.stats

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.ReadingStatsBucket
import com.juguito.juguitoreader.ui.stats.components.chartAxisMax
import com.juguito.juguitoreader.ui.stats.components.heatIntensity
import com.juguito.juguitoreader.ui.stats.components.minutesPerStandardPage
import com.juguito.juguitoreader.ui.stats.components.pagesPerHour
import com.juguito.juguitoreader.ui.stats.components.peakBucketIndex
import org.junit.Test
import java.time.LocalDate

class StatsMetricsTest {

    @Test
    fun `heat intensity stays soft for a short day and saturates on the peak`() {
        assertThat(heatIntensity(minutes = 0, peakMinutes = 40)).isEqualTo(0f)
        assertThat(heatIntensity(minutes = 40, peakMinutes = 40)).isEqualTo(1f)
        assertThat(heatIntensity(minutes = 20, peakMinutes = 40)).isWithin(0.001f).of(0.65f)
    }

    @Test
    fun `chart scale rounds up to a readable step`() {
        assertThat(chartAxisMax(0)).isEqualTo(2)
        assertThat(chartAxisMax(8)).isEqualTo(8)
        assertThat(chartAxisMax(45)).isEqualTo(60)
        assertThat(chartAxisMax(105)).isEqualTo(120)
    }

    @Test
    fun `peak index ignores a period where every bucket is empty`() {
        val day = LocalDate.of(2026, 10, 5)
        assertThat(peakBucketIndex(listOf(ReadingStatsBucket(day, 0)))).isNull()
        assertThat(
            peakBucketIndex(
                listOf(
                    ReadingStatsBucket(day, 5),
                    ReadingStatsBucket(day.plusDays(1), 9),
                )
            )
        ).isEqualTo(1)
    }

    @Test
    fun `pages per hour assumes a 250 word page`() {
        assertThat(pagesPerHour(250)).isEqualTo(60)
        assertThat(pagesPerHour(0)).isEqualTo(0)
        assertThat(minutesPerStandardPage(250)).isEqualTo(1f)
    }
}
