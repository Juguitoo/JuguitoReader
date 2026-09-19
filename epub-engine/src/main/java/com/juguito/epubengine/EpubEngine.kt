package com.juguito.epubengine

/**
 * Native EPUB renderer scaffold (TAR-30).
 *
 * Lives in `:epub-engine` so work can land without replacing the app WebView reader.
 * Do not add `implementation(project(":epub-engine"))` in `:app` until this module can
 * stand in for `EpubWebView` at parity with shipped reader features.
 *
 * This module must not import `com.juguito.juguitoreader.*`. EPUB parsing stays in
 * `:app` (`EpubParser`) through the v1.4 EPUB3 work.
 */
object EpubEngine
