package com.juguito.juguitoreader.utils

fun compareVersionNames(a: String, b: String): Int {
    val splitA = a.split(".").map { it.toIntOrNull() ?: 0 }
    val splitB = b.split(".").map { it.toIntOrNull() ?: 0 }

    val max = maxOf(splitA.size, splitB.size)
    for (i in 0 until max) {
        val cmp = splitA.getOrElse(i) { 0 }.compareTo(splitB.getOrElse(i) { 0 })
        if (cmp != 0) return cmp
    }
    return 0
}

fun unseenChangelogVersions(
    current: String,
    lastSeen: String?,
    catalogNewestFirst: List<String>
): List<String> {
    if (lastSeen == null) {
        return if (current in catalogNewestFirst) listOf(current) else emptyList()
    }

    if (compareVersionNames(lastSeen, current) >= 0) return emptyList()

    return catalogNewestFirst.filter { version ->
        compareVersionNames(lastSeen, version) < 0 &&
                compareVersionNames(current, version) >= 0
    }

}

