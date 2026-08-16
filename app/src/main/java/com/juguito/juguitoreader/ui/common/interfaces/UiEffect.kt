package com.juguito.juguitoreader.ui.common.interfaces

sealed interface UiEffect {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null
    ) : UiEffect

    data object NavigateBack : UiEffect
    data class Navigate(val route: String) : UiEffect
}