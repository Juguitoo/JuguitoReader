package com.juguito.juguitoreader.utils

import kotlin.math.pow

internal fun prefersDarkContent(colorHex: String): Boolean? {
    val rgb = parseRgb(colorHex) ?: return null
    val luminance = relativeLuminance(rgb)
    val contrastWithBlack = (luminance + 0.05) / 0.05
    val contrastWithWhite = 1.05 / (luminance + 0.05)
    return contrastWithBlack >= contrastWithWhite
}

private fun parseRgb(colorHex: String): Int? {
    val hex = colorHex.trim().removePrefix("#")
    if (hex.length != 6) return null
    return hex.toIntOrNull(16)
}

private fun relativeLuminance(rgb: Int): Double {
    val red = linearize((rgb shr 16) and 0xFF)
    val green = linearize((rgb shr 8) and 0xFF)
    val blue = linearize(rgb and 0xFF)
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue
}

private fun linearize(channel: Int): Double {
    val srgb = channel / 255.0
    return if (srgb <= 0.04045) srgb / 12.92
    else ((srgb + 0.055) / 1.055).pow(2.4)
}