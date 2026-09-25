package com.juguito.juguitoreader.ui.reader

sealed interface ReaderGuideEvent {
    data object OnDismiss : ReaderGuideEvent
}
