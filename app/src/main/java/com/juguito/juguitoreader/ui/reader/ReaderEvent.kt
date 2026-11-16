package com.juguito.juguitoreader.ui.reader

import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.domain.usecase.bookmark.NewBookmark

sealed interface ReaderEvent {
    data object OnNextChapter: ReaderEvent
    data object OnPreviousChapter: ReaderEvent
    data object OnToggleControls: ReaderEvent
    data object OnBackRequested: ReaderEvent
    data class OnChapterSelected(val index: Int): ReaderEvent
    data class OnTextZoomChanged(val zoom: Int): ReaderEvent
    data class OnThemeChanged(val theme: ReaderTheme): ReaderEvent
    data class OnScrollPositionChanged(val scrollPosition: Float, val chapterIndex: Int = -1): ReaderEvent
    data class OnTimeRemainingChanged(val minutes: Int, val chapterIndex: Int = -1): ReaderEvent
    data class OnBrightnessChanged(val brightness: Float): ReaderEvent
    data class OnStatusPromptResult(val changeToReading: Boolean): ReaderEvent
    data object OnStartReading: ReaderEvent
    data object OnFinishReading: ReaderEvent
    data object OnToggleSessionsDialog: ReaderEvent
    data object OnRenderProcessGone: ReaderEvent
    data class OnReportWordsRead(val words: Int, val chapterIndex: Int = -1): ReaderEvent
    data class OnChapterWordsBaseline(val words: Int, val chapterIndex: Int = -1): ReaderEvent
    data class OnDeleteDailyReading(val bookId: Int, val date: String): ReaderEvent

    data class OnOpenNote(val id: Int): ReaderEvent
    data object OnCloseNote: ReaderEvent
    data object OnToggleBookmarksList: ReaderEvent
    data class OnDeleteBookmark(val id: Int): ReaderEvent
    data class OnEditBookmark(val id: Int, val color: BookmarkColor, val noteText: String): ReaderEvent
    data class OnMoveToBookmark(val bookmark: Bookmark): ReaderEvent
    data class OnAddBookmark(val bookmark: NewBookmark): ReaderEvent
    data object OnBookmarkJumpFinished: ReaderEvent
    data class OnTextSelected(
        val quote: String,
        val prefix: String,
        val suffix: String,
        val scrollPosition: Float,
        val chapterIndex: Int = -1,
        val overlapsMark: Boolean = false,
        val anchorLeft: Float = 0f,
        val anchorTop: Float = 0f,
        val anchorRight: Float = 0f,
        val anchorBottom: Float = 0f
    ): ReaderEvent
    data object OnRequestBookmarkHere: ReaderEvent
    data class OnBookmarkCaptured(
        val quote: String,
        val prefix: String,
        val suffix: String,
        val imageSrc: String,
        val scrollPosition: Float,
        val chapterIndex: Int = -1
    ): ReaderEvent
    data object OnOpenNewNote: ReaderEvent
    data object OnCloseNewNote: ReaderEvent
    data class OnMarkTapped(
        val id: Int,
        val anchorLeft: Float = 0f,
        val anchorTop: Float = 0f,
        val anchorRight: Float = 0f,
        val anchorBottom: Float = 0f
    ): ReaderEvent
    data class OnPinImage(
        val imageSrc: String,
        val alt: String,
        val scrollPosition: Float,
        val chapterIndex: Int = -1
    ): ReaderEvent
    data class OnOpenPinnedImage(val id: Int): ReaderEvent
    data object OnToggleImageList: ReaderEvent
    data class OnSelectLibraryTab(val tab: LibraryTab): ReaderEvent
    data object OnClosePinnedImage: ReaderEvent
    data object OnCloseLibrarySheet: ReaderEvent
    data object OnCloseMarkMenu: ReaderEvent
}