package com.juguito.juguitoreader.common

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ActionUndoManagerTest {

    @Test
    fun `executeAction confirms previous pending before starting a new one`() = runTest {
        val manager = ActionUndoManager<String>()
        val confirmed = mutableListOf<String>()

        manager.executeAction(
            item = "A",
            immediateAction = { true },
            onConfirm = { confirmed += it },
            onUndo = {}
        )
        manager.executeAction(
            item = "B",
            immediateAction = { true },
            onConfirm = { confirmed += it },
            onUndo = {}
        )

        assertThat(confirmed).containsExactly("A")

        manager.confirmPending()
        assertThat(confirmed).containsExactly("A", "B")
    }

    @Test
    fun `confirmPendingIf ignores stale id`() = runTest {
        val manager = ActionUndoManager<String>()
        val confirmed = mutableListOf<String>()

        manager.executeAction(
            item = "B",
            immediateAction = { true },
            onConfirm = { confirmed += it },
            onUndo = {}
        )
        manager.confirmPendingIf { it == "A" }

        assertThat(confirmed).isEmpty()

        manager.confirmPendingIf { it == "B" }
        assertThat(confirmed).containsExactly("B")
    }

    @Test
    fun `undoPendingIf ignores stale id`() = runTest {
        val manager = ActionUndoManager<String>()
        val undone = mutableListOf<String>()

        manager.executeAction(
            item = "B",
            immediateAction = { true },
            onConfirm = {},
            onUndo = { undone += it }
        )
        manager.undoPendingIf { it == "A" }

        assertThat(undone).isEmpty()

        manager.undoPendingIf { it == "B" }
        assertThat(undone).containsExactly("B")
    }

    @Test
    fun `failed immediateAction clears pending without confirm`() = runTest {
        val manager = ActionUndoManager<String>()
        val confirmed = mutableListOf<String>()

        manager.executeAction(
            item = "A",
            immediateAction = { false },
            onConfirm = { confirmed += it },
            onUndo = {}
        )
        manager.confirmPending()

        assertThat(confirmed).isEmpty()
    }
}
