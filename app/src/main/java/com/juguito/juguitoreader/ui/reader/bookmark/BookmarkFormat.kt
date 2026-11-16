package com.juguito.juguitoreader.ui.reader.bookmark

import androidx.compose.ui.graphics.Color
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.enums.BookmarkColor
import com.juguito.juguitoreader.domain.enums.BookmarkKind
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.ui.theme.JuguitoSecondary
import com.juguito.juguitoreader.ui.theme.NeonPrimary
import com.juguito.juguitoreader.ui.theme.PastelPink
import com.juguito.juguitoreader.ui.theme.PastelPurple
import com.juguito.juguitoreader.ui.theme.SuccessGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun Bookmark.matchesQuery(needle: String): Boolean {
    if (needle.isEmpty()) return true
    return searchText().forSearch().contains(needle)
}

internal fun Bookmark.searchText(): String = buildString {
    append(quote)
    if (!noteText.isNullOrBlank()) append(' ').append(noteText)
    val fileName = imageSrc?.substringAfterLast('/')?.substringBefore('?')
    if (!fileName.isNullOrBlank()) append(' ').append(fileName)
}

internal fun String.forSearch(): String = lowercase(Locale.getDefault())
    .replace('á', 'a')
    .replace('é', 'e')
    .replace('í', 'i')
    .replace('ó', 'o')
    .replace('ú', 'u')
    .replace('ü', 'u')
    .replace('ñ', 'n')

internal fun BookmarkKind.labelRes(): Int = when (this) {
    BookmarkKind.POSITION -> R.string.bookmark_kind_position
    BookmarkKind.HIGHLIGHT -> R.string.bookmark_kind_highlight
    BookmarkKind.NOTE -> R.string.bookmark_kind_note
    BookmarkKind.IMAGE -> R.string.bookmark_kind_image
}

internal fun formatBookmarkTime(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.getDefault())
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
}

internal fun Bookmark.displayQuote(): String {
    if (kind == BookmarkKind.IMAGE) return listTitle()
    if (kind == BookmarkKind.POSITION && quote.isBlank()) return listTitle()
    val raw = listTitle().trim()
    val startsCut = prefix.isNotEmpty() && !prefix.last().isWhitespace() &&
        raw.isNotEmpty() && !raw.first().isWhitespace()
    val endsCut = suffix.isNotEmpty() && !suffix.first().isWhitespace() &&
        raw.isNotEmpty() && !raw.last().isWhitespace() && raw.last() !in ".!?…\"”"
    var text = raw
    if (startsCut) {
        val space = text.indexOf(' ')
        text = if (space >= 0) text.substring(space + 1) else ""
    }
    if (endsCut) {
        val space = text.lastIndexOf(' ')
        text = if (space > 0) text.substring(0, space) else text
    }
    text = text.trim().ifBlank { raw }
    val lead = prefix.isNotBlank()
    val trail = suffix.isNotBlank()
    return if (kind == BookmarkKind.POSITION) {
        buildString {
            if (lead) append('…')
            append(text)
            if (trail) append('…')
        }
    } else {
        buildString {
            append('"')
            if (lead) append('…')
            append(text)
            if (trail) append('…')
            append('"')
        }
    }
}

internal fun Bookmark.listTitle(): String {
    if (quote.isNotBlank()) return quote
    val fileName = imageSrc?.substringAfterLast('/')?.substringBefore('?')
    if (!fileName.isNullOrBlank()) return fileName
    return "—"
}

internal fun BookmarkColor.labelRes(): Int = when (this) {
    BookmarkColor.AMBER -> R.string.bookmark_color_amber
    BookmarkColor.GREEN -> R.string.bookmark_color_green
    BookmarkColor.BLUE -> R.string.bookmark_color_blue
    BookmarkColor.PINK -> R.string.bookmark_color_pink
    BookmarkColor.LILAC -> R.string.bookmark_color_lilac
}

internal fun BookmarkColor.swatch(): Color = when (this) {
    BookmarkColor.AMBER -> JuguitoSecondary
    BookmarkColor.GREEN -> SuccessGreen
    BookmarkColor.BLUE -> NeonPrimary
    BookmarkColor.PINK -> PastelPink
    BookmarkColor.LILAC -> PastelPurple
}
