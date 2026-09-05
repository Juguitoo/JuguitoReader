package com.juguito.juguitoreader.utils

import com.google.common.truth.Truth.assertThat
import com.juguito.juguitoreader.domain.model.DailyReading
import org.junit.Test

class ResolveWpmTest {

    @Test
    fun `empty sessions returns fallback`() {
        assertThat(resolveWpm(emptyList())).isEqualTo(250)
    }

    @Test
    fun `history under five minutes returns fallback`() {
        val sessions = listOf(session(timeSpentMillis = 4 * 60_000, readingSpeed = 400))

        assertThat(resolveWpm(sessions)).isEqualTo(250)
    }

    @Test
    fun `history of five minutes uses weighted average`() {
        val sessions = listOf(
            session(timeSpentMillis = 10 * 60_000, readingSpeed = 200),
            session(timeSpentMillis = 5 * 60_000, readingSpeed = 400),
        )

        assertThat(resolveWpm(sessions)).isEqualTo(266)
    }

    private fun session(timeSpentMillis: Int, readingSpeed: Int) = DailyReading(
        bookId = 1,
        date = "2026-01-01",
        timeSpentMillis = timeSpentMillis,
        reachedPercentage = 0f,
        readingSpeed = readingSpeed,
    )
}
