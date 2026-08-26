package com.juguito.juguitoreader.ui.common

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.exception.JuguitoException

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }
}

fun Throwable.asUiText(): UiText {
    return when (this) {
        is JuguitoException -> UiText.StringResource(resId, *args)
        else -> this.localizedMessage?.let { UiText.DynamicString(it) }
            ?: UiText.StringResource(R.string.something_went_wrong)
    }
}
