package com.juguito.juguitoreader.ui.reader

internal const val MIN_SESSION_DURATION_MS = 15_000L

internal class ReadingSessionClock(
    private val now: () -> Long,
    private val minDurationMs: Long = MIN_SESSION_DURATION_MS
) {
    private var elapsedMillis: Long = 0L
    private var startTimeMillis: Long? = null
    var isResumed: Boolean = true
        private set

    fun durationMillis(): Long {
        val running = startTimeMillis?.let { now() - it } ?: 0L
        return elapsedMillis + running
    }

    fun shouldPersist(): Boolean = durationMillis() > minDurationMs

    fun onResumed() {
        isResumed = true
    }

    fun onPaused() {
        isResumed = false
        freeze()
    }

    fun freeze() {
        val start = startTimeMillis ?: return
        elapsedMillis += now() - start
        startTimeMillis = null
    }

    fun tryStart() {
        if (!isResumed || startTimeMillis != null) return
        startTimeMillis = now()
    }

    fun reset() {
        elapsedMillis = 0L
        startTimeMillis = null
    }
}
