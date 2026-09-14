package com.juguito.juguitoreader.ui.reader

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReadingSessionClockTest {

    @Test
    fun `pause freezes elapsed so background time is excluded`() {
        var now = 1_000L
        val clock = ReadingSessionClock(now = { now })

        clock.tryStart()
        now = 11_000L
        clock.onPaused()

        now = 121_000L
        assertThat(clock.durationMillis()).isEqualTo(10_000L)

        clock.onResumed()
        clock.tryStart()
        now = 141_000L
        assertThat(clock.durationMillis()).isEqualTo(30_000L)
        assertThat(clock.shouldPersist()).isTrue()
    }

    @Test
    fun `short session is not persistable`() {
        var now = 0L
        val clock = ReadingSessionClock(now = { now })

        clock.tryStart()
        now = MIN_SESSION_DURATION_MS
        assertThat(clock.shouldPersist()).isFalse()

        now = MIN_SESSION_DURATION_MS + 1
        assertThat(clock.shouldPersist()).isTrue()
    }

    @Test
    fun `tryStart while paused is a no-op`() {
        var now = 1_000L
        val clock = ReadingSessionClock(now = { now })

        clock.onPaused()
        clock.tryStart()
        now = 20_000L

        assertThat(clock.durationMillis()).isEqualTo(0L)
        assertThat(clock.shouldPersist()).isFalse()
    }

    @Test
    fun `tryStart keeps the original start`() {
        var now = 1_000L
        val clock = ReadingSessionClock(now = { now })

        clock.tryStart()
        now = 61_000L
        clock.tryStart()
        now = 121_000L

        assertThat(clock.durationMillis()).isEqualTo(120_000L)
    }

    @Test
    fun `reset clears elapsed and start but keeps resumed`() {
        var now = 1_000L
        val clock = ReadingSessionClock(now = { now })

        clock.tryStart()
        now = 21_000L
        clock.reset()

        assertThat(clock.durationMillis()).isEqualTo(0L)
        assertThat(clock.isResumed).isTrue()

        clock.tryStart()
        now = 31_000L
        assertThat(clock.durationMillis()).isEqualTo(10_000L)
    }

    @Test
    fun `freeze without pause still allows tryStart`() {
        var now = 1_000L
        val clock = ReadingSessionClock(now = { now })

        clock.tryStart()
        now = 11_000L
        clock.freeze()
        clock.tryStart()
        now = 16_000L

        assertThat(clock.isResumed).isTrue()
        assertThat(clock.durationMillis()).isEqualTo(15_000L)
    }
}
