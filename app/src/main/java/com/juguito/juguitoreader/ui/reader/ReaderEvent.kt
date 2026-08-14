package com.juguito.juguitoreader.ui.reader

sealed interface ReaderEvent {
    data object OnNextChapter: ReaderEvent
    data object OnPreviousChapter: ReaderEvent
    data object OnToggleControls: ReaderEvent
    data class OnChapterSelected(val index: Int): ReaderEvent
    data class OnTextZoomChanged(val zoom: Int): ReaderEvent
    data class OnThemeChanged(val theme: ReaderTheme): ReaderEvent
    data class OnScrollPositionChanged(val scrollPosition: Float): ReaderEvent
    data class OnTimeRemainingChanged(val minutes: Int) : ReaderEvent
}