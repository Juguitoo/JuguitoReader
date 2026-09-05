package com.juguito.juguitoreader.ui.common

import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.enums.Language
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.theme.AppTheme

fun BookStatus.toUiText(): UiText {
    return when (this) {
        BookStatus.PENDING -> UiText.StringResource(R.string.status_pending)
        BookStatus.READING -> UiText.StringResource(R.string.status_reading)
        BookStatus.FINISHED -> UiText.StringResource(R.string.status_finished)
        BookStatus.DROPPED -> UiText.StringResource(R.string.status_dropped)
    }
}

fun Language.toUiText(): UiText {
    return when (this) {
        Language.SYSTEM -> UiText.StringResource(R.string.lang_system)
        Language.SPANISH -> UiText.StringResource(R.string.lang_spanish)
        Language.ENGLISH -> UiText.StringResource(R.string.lang_english)
    }
}

fun AppTheme.toUiText(): UiText {
    return when (this) {
        AppTheme.JUGUITO -> UiText.StringResource(R.string.theme_juguito)
        AppTheme.PASTEL -> UiText.StringResource(R.string.theme_pastel)
        AppTheme.DARK -> UiText.StringResource(R.string.theme_dark)
        AppTheme.HIGH_CONTRAST -> UiText.StringResource(R.string.theme_high_contrast)
    }
}

fun ReaderTheme.toUiText(): UiText {
    return when (this) {
        ReaderTheme.DAY -> UiText.StringResource(R.string.reader_day)
        ReaderTheme.SEPIA -> UiText.StringResource(R.string.reader_sepia)
        ReaderTheme.NIGHT -> UiText.StringResource(R.string.reader_night)
    }
}
