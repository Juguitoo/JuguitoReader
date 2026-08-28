package com.juguito.juguitoreader.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

/**
 * Waits for [StateFlow] updates that complete on [Dispatchers.IO].
 * [runTest] virtual time does not advance real IO work.
 */
suspend fun <T> StateFlow<T>.awaitValue(
    timeoutMs: Long = 5_000,
    predicate: (T) -> Boolean,
) {
    withContext(Dispatchers.Default) {
        withTimeout(timeoutMs) {
            while (!predicate(value)) {
                yield()
            }
        }
    }
}
