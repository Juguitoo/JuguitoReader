package com.juguito.juguitoreader.ui.changelog

sealed interface WhatsNewEvent {
    data object OnDismiss : WhatsNewEvent
}
