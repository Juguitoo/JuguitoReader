package com.juguito.juguitoreader.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

/** MockK timeout for verifications on work dispatched to [Dispatchers.IO]. */
const val IO_DISPATCHER_TIMEOUT_MS = 5_000L

/**
 * Gives coroutines on [Dispatchers.IO] time to finish before tearing down mocks.
 * [Dispatchers.setMain] does not control IO work started from ViewModels.
 */
suspend fun drainIoDispatcher(timeoutMs: Long = IO_DISPATCHER_TIMEOUT_MS) {
    withContext(Dispatchers.Default) {
        withTimeout(timeoutMs) {
            repeat(200) { yield() }
        }
    }
}
