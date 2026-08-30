package com.juguito.juguitoreader.ui.reader

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val HOST = "https://appassets.androidplatform.net"
private const val PREFIX = "/epub/"

fun epubChapterUrl(spineItem: String): String {
    val encodedPath = spineItem.split("/")
        .filter { it.isNotEmpty() }
        .joinToString(separator = "/") { encodePathSegment(it) }

    return HOST + PREFIX + encodedPath
}

private fun encodePathSegment(segment: String): String =
    URLEncoder.encode(segment, StandardCharsets.UTF_8.name()).replace("+", "%20")
