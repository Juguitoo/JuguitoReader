package com.juguito.juguitoreader.ui.reader.guide

sealed interface ReaderGuideEvent {
    data object OnDismiss : ReaderGuideEvent
}
