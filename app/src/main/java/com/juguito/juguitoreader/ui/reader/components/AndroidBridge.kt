package com.juguito.juguitoreader.ui.reader.components

import android.webkit.JavascriptInterface
import com.juguito.juguitoreader.ui.reader.ReaderEvent

internal class AndroidBridge(
    private val onEvent: (ReaderEvent) -> Unit,
    private val onOverscroll: (Float) -> Unit,
    private val onConsumeTap: () -> Unit = {}
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

    @JavascriptInterface
    fun reportBookmarkJumpFinished() {
        onEvent(ReaderEvent.OnBookmarkJumpFinished)
    }

    @JavascriptInterface
    fun reportTextSelected(
        quote: String,
        prefix: String,
        suffix: String,
        scrollPosition: Float,
        chapterIndex: Int,
        overlapsMark: Boolean,
        anchorLeft: Float,
        anchorTop: Float,
        anchorRight: Float,
        anchorBottom: Float
    ) {
        onEvent(
            ReaderEvent.OnTextSelected(
                quote = quote,
                prefix = prefix,
                suffix = suffix,
                scrollPosition = scrollPosition,
                chapterIndex = chapterIndex,
                overlapsMark = overlapsMark,
                anchorLeft = anchorLeft,
                anchorTop = anchorTop,
                anchorRight = anchorRight,
                anchorBottom = anchorBottom
            )
        )
    }

    @JavascriptInterface
    fun reportBookmarkCapture(
        quote: String,
        prefix: String,
        suffix: String,
        imageSrc: String,
        scrollPosition: Float,
        chapterIndex: Int
    ) {
        onEvent(ReaderEvent.OnBookmarkCaptured(quote, prefix, suffix, imageSrc, scrollPosition, chapterIndex))
    }

    @JavascriptInterface
    fun consumeReaderTap() {
        onConsumeTap()
    }

    @JavascriptInterface
    fun reportPinnedImageTapped(id: Int) {
        onEvent(ReaderEvent.OnOpenPinnedImage(id))
    }

    @JavascriptInterface
    fun reportImagePinned(imageSrc: String, alt: String, scrollPosition: Float, chapterIndex: Int) {
        onEvent(ReaderEvent.OnPinImage(imageSrc, alt, scrollPosition, chapterIndex))
    }

    @JavascriptInterface
    fun reportMarkTapped(id: Int, anchorLeft: Float, anchorTop: Float, anchorRight: Float, anchorBottom: Float) {
        onEvent(ReaderEvent.OnMarkTapped(id, anchorLeft, anchorTop, anchorRight, anchorBottom))
    }
}
