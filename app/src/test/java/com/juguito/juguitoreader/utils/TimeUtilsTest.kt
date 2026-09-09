package com.juguito.juguitoreader.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class TimeUtilsTest {

    @Test
    fun `utcMidnightMillis maps a local calendar date to midnight UTC`() {
        val millis = utcMidnightMillis(LocalDate.of(2026, 9, 10))

        assertThat(millis).isEqualTo(Instant.parse("2026-09-10T00:00:00Z").toEpochMilli())
    }

    @Test
    fun `utcMidnightMillis with null falls back to local today UTC midnight`() {
        assertThat(utcMidnightMillis(null)).isEqualTo(localTodayUtcMidnightMillis())
        assertThat(localTodayUtcMidnightMillis())
            .isEqualTo(utcMidnightMillis(LocalDate.now()))
    }
}
