package com.juguito.juguitoreader.data.backup

fun relocateInternalPaths(path: String?, filesDir: String) : String? {
    if (path.isNullOrBlank()) return path
    val basename = path.substringAfterLast("/")
    if (basename.isBlank()) return null
    return "$filesDir/$basename"
}