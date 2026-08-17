package com.juguito.juguitoreader.common

class ActionUndoManager<T> {
    private var tempItem: T? = null
    private var confirmAction: (suspend (T) -> Unit)? = null
    private var undoAction: (suspend (T) -> Unit)? = null

    suspend fun executeAction(
        item: T,
        immediateAction: suspend (T) -> Boolean,
        onConfirm: suspend (T) -> Unit,
        onUndo: suspend (T) -> Unit
    ) {
        tempItem = item
        confirmAction = onConfirm
        undoAction = onUndo

        val success = immediateAction(item)

        if (!success) clear()
    }

    suspend fun undoPending() {
        tempItem?.let {item ->
            undoAction?.invoke(item)
        }
        clear()
    }

    suspend fun confirmPending() {
        tempItem?.let {item ->
            confirmAction?.invoke(item)
        }
        clear()
    }

    private fun clear() {
        tempItem = null
        confirmAction = null
        undoAction = null
    }
}