package com.juguito.juguitoreader.ui.reader.components

import android.webkit.JavascriptInterface
import com.juguito.juguitoreader.ui.reader.ReaderEvent

internal class AndroidBridge(
    private val onEvent: (ReaderEvent) -> Unit,
    private val onOverscroll: (Float) -> Unit
) {
    @JavascriptInterface
    fun reportScrollPosition(y: Float, chapterIndex: Int) {
        onEvent(ReaderEvent.OnScrollPositionChanged(y, chapterIndex))
    }

    @JavascriptInterface
    fun reportTimeRemaining(minutes: Int, chapterIndex: Int) {
        onEvent(ReaderEvent.OnTimeRemainingChanged(minutes, chapterIndex))
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
    fun reportWordsRead(words: Int, chapterIndex: Int) {
        onEvent(ReaderEvent.OnReportWordsRead(words, chapterIndex))
    }

    @JavascriptInterface
    fun reportInitialWordsRead(words: Int, chapterIndex: Int) {
        onEvent(ReaderEvent.OnChapterWordsBaseline(words, chapterIndex))
    }
}
