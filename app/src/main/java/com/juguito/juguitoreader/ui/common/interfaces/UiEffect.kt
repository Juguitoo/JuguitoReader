package com.juguito.juguitoreader.ui.common.interfaces

import com.juguito.juguitoreader.ui.common.UiText

sealed interface UiEffect {
    data class ShowSnackbar(
        val message: UiText,
        val actionLabel: UiText? = null,
        val actionPayload: String? = null
    ) : UiEffect

    data object NavigateBack : UiEffect
    data class Navigate(val route: String) : UiEffect
}