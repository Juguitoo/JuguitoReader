package com.juguito.juguitoreader.ui.reader.components

import android.webkit.JavascriptInterface
import com.juguito.juguitoreader.ui.reader.ReaderEvent

internal class AndroidBridge(
    private val onEvent: (ReaderEvent) -> Unit,
    private val onOverscroll: (Float) -> Unit
) {
    @JavascriptInterface
    fun reportScrollPosition(y: Float) {
        onEvent(ReaderEvent.OnScrollPositionChanged(y))
    }

    @JavascriptInterface
    fun reportTimeRemaining(minutes: Int) {
        onEvent(ReaderEvent.OnTimeRemainingChanged(minutes))
    }

    @JavascriptInterface
    fun goToNextChapter() {
        onEvent(ReaderEvent.OnNextChapter)
    }

    @JavascriptInterface
    fun goToPreviousChapter() {
        onEvent(ReaderEvent.OnPreviousChapter)
    }

    @JavascriptInterface
    fun updateOverscroll(delta: Float) {
        onOverscroll(delta)
    }

    @JavascriptInterface
    fun reportWordsRead(words: Int) {
        onEvent(ReaderEvent.OnReportWordsRead(words))
    }

    @JavascriptInterface
    fun reportInitialWordsRead(words: Int) {
        onEvent(ReaderEvent.OnChapterWordsBaseline(words))
    }
}
