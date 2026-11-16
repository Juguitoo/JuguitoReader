package com.juguito.juguitoreader.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.net.URLDecoder

fun resolveEpubImage(baseDir: String, chapterHref: String, src: String): File? {
    val cleaned = src.substringBefore('#').substringBefore('?').trim()
    if (cleaned.isEmpty() || cleaned.startsWith("data:", ignoreCase = true) || cleaned.contains("://")) {
        return null
    }
    val decoded = runCatching { URLDecoder.decode(cleaned, Charsets.UTF_8.name()) }.getOrDefault(cleaned)
    val root = File(baseDir).canonicalFile
    val parent = if (decoded.startsWith("/")) {
        root
    } else {
        File(root, chapterHref).parentFile ?: root
    }
    val resolved = File(parent, decoded.removePrefix("/")).canonicalFile
    val rootPath = root.path
    val resolvedPath = resolved.path
    val inside = resolvedPath.equals(rootPath, ignoreCase = true) ||
        resolvedPath.startsWith(rootPath + File.separator, ignoreCase = true)
    if (!inside || !resolved.isFile) return null
    return resolved
}

fun isPreviewableEpubImage(file: File): Boolean {
    return file.extension.lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif")
}

data class EpubImagePreview(
    val bitmap: Bitmap,
    val widthPx: Int,
    val heightPx: Int
)

fun loadEpubImagePreview(file: File, maxSide: Int): EpubImagePreview? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val bitmap = decodeEpubImage(file, maxSide) ?: return null
    return EpubImagePreview(bitmap, bounds.outWidth, bounds.outHeight)
}

fun decodeEpubImage(file: File, maxSide: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.path, options)
}
