package com.juguito.juguitoreader.ui.reader.components

import android.content.Context
import android.graphics.Rect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SystemMenuAction(val id: Int, val title: String)

/**
 * The WebView clears the text selection when its action mode ends, so this
 * view keeps that mode alive and hides the system toolbar. The captured
 * actions are drawn by the reader in its own two-row menu.
 */
internal class ReaderWebView(context: Context) : WebView(context) {
    var onSystemActionsChanged: (List<SystemMenuAction>) -> Unit = {}
    private var selectionCallback: HiddenSelectionCallback? = null

    override fun startActionMode(callback: ActionMode.Callback): ActionMode? {
        return startActionMode(callback, ActionMode.TYPE_PRIMARY)
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? {
        val wrapped = HiddenSelectionCallback(context, callback, onSystemActionsChanged)
        selectionCallback = wrapped
        return super.startActionMode(wrapped, type)
    }

    fun performSystemAction(id: Int) {
        selectionCallback?.perform(id)
    }
}

class SystemMenuState {
    var actions by mutableStateOf<List<SystemMenuAction>>(emptyList())
    var perform: (Int) -> Unit = {}
}

private class HiddenSelectionCallback(
    private val context: Context,
    private val original: ActionMode.Callback,
    private val onActionsChanged: (List<SystemMenuAction>) -> Unit
) : ActionMode.Callback2() {
    private var mode: ActionMode? = null
    private val items = linkedMapOf<Int, MenuItem>()
    private var published: List<SystemMenuAction> = emptyList()

    fun perform(id: Int) {
        val current = mode ?: return
        val item = items[id] ?: return
        original.onActionItemClicked(current, item)
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        val created = original.onCreateActionMode(mode, menu)
        hideSystemToolbar(mode, menu)
        return created
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        original.onPrepareActionMode(mode, menu)
        hideSystemToolbar(mode, menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = false

    override fun onDestroyActionMode(mode: ActionMode) {
        items.clear()
        publish(emptyList())
        this.mode = null
        original.onDestroyActionMode(mode)
    }

    override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
        if (original is ActionMode.Callback2) {
            original.onGetContentRect(mode, view, outRect)
        } else {
            super.onGetContentRect(mode, view, outRect)
        }
    }

    private fun hideSystemToolbar(mode: ActionMode, menu: Menu) {
        val next = linkedMapOf<Int, MenuItem>()
        val actions = mutableListOf<SystemMenuAction>()
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            val title = item.title?.toString()?.takeIf { it.isNotBlank() } ?: continue
            if (!item.isVisible) continue
            next[item.itemId] = item
            actions += SystemMenuAction(item.itemId, title)
        }
        items.clear()
        items.putAll(next)
        publish(actions)
        menu.clear()
        if (mode.customView == null) {
            mode.customView = View(context).apply { visibility = View.GONE }
        }
    }

    private fun publish(actions: List<SystemMenuAction>) {
        if (actions == published) return
        published = actions
        onActionsChanged(actions)
    }
}
