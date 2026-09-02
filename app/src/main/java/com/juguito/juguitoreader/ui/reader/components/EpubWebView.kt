package com.juguito.juguitoreader.ui.reader.components

import android.content.Context
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.webkit.WebViewAssetLoader
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import java.io.File

private const val JS_BRIDGE_NAME = "AndroidBridge"
private const val STYLE_REVEAL_TIMEOUT_MS = 2_000L

private class WebViewHolder(
    var loader: WebViewAssetLoader,
    var baseDir: String,
    var theme: ReaderTheme,
    var scrollPosition: Float
) {
    var pendingReveal: Runnable? = null
}

@Composable
fun EpubWebView(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit,
    onOverscroll: (Float) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(vertical = 24.dp),
        factory = { context ->
            val holder = WebViewHolder(
                loader = createAssetLoader(context, state.epubContent.baseDir),
                baseDir = state.epubContent.baseDir,
                theme = state.theme,
                scrollPosition = state.readingProgress.scrollPosition
            )

            WebView(context).apply {
                tag = holder
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(state.theme.bgColor.toColorInt())
                visibility = View.INVISIBLE

                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun reportScrollPosition(y: Float) {
                        onEvent(ReaderEvent.OnScrollPositionChanged(y))
                    }
                    @JavascriptInterface
                    fun reportTimeRemaining(minutes: Int) {
                        onEvent(ReaderEvent.OnTimeRemainingChanged(minutes))
                    }
                    @JavascriptInterface
                    fun goToNextChapter() {
                        onEvent(ReaderEvent.OnNextChapter)
                    }
                    @JavascriptInterface
                    fun goToPreviousChapter() {
                        onEvent(ReaderEvent.OnPreviousChapter)
                    }
                    @JavascriptInterface
                    fun updateOverscroll(delta: Float) {
                        onOverscroll(delta)
                    }
                    @JavascriptInterface
                    fun reportWordsRead(words: Int) {
                        onEvent(ReaderEvent.OnReportWordsRead(words))
                    }
                }, JS_BRIDGE_NAME)

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        return holder.loader.shouldInterceptRequest(request.url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        return request.url.host != WebViewAssetLoader.DEFAULT_DOMAIN
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        view?.let { hideUntilStyled(it, holder) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        val loadedView = view ?: return
                        val script = listOf(
                            viewportScript(),
                            styleScript(holder.theme),
                            behaviorScript(holder.scrollPosition)
                        ).joinToString(separator = "\n")
                        loadedView.evaluateJavascript(script) { revealStyledContent(loadedView, holder) }
                    }
                }

                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        onEvent(ReaderEvent.OnToggleControls)
                        return true
                    }
                })

                setOnTouchListener { _, event ->
                    gestureDetector.onTouchEvent(event)
                    false
                }

                settings.apply {
                    javaScriptEnabled = true
                    allowFileAccess = false
                    allowContentAccess = false
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    textZoom = 100
                }
            }
        },
        update = { webView ->
            val holder = webView.tag as WebViewHolder

            if (holder.baseDir != state.epubContent.baseDir) {
                holder.baseDir = state.epubContent.baseDir
                holder.loader = createAssetLoader(webView.context, state.epubContent.baseDir)
            }
            holder.scrollPosition = state.readingProgress.scrollPosition

            if (holder.theme != state.theme) {
                holder.theme = state.theme
                webView.setBackgroundColor(state.theme.bgColor.toColorInt())
                webView.evaluateJavascript(themeScript(state.theme), null)
            }

            if (webView.url != state.currentChapterUrl) {
                webView.loadUrl(state.currentChapterUrl)
            }

            if (webView.settings.textZoom != state.textZoom) {
                webView.settings.textZoom = state.textZoom
                webView.evaluateJavascript(restoreScrollScript(state.readingProgress.scrollPosition), null)
            }
        },
        onRelease = { webView ->
            val holder = webView.tag as? WebViewHolder
            holder?.pendingReveal?.let(webView::removeCallbacks)
            holder?.pendingReveal = null

            webView.stopLoading()
            webView.removeJavascriptInterface(JS_BRIDGE_NAME)
            webView.destroy()
        }
    )
}

private fun createAssetLoader(context: Context, baseDir: String): WebViewAssetLoader =
    WebViewAssetLoader.Builder()
        .setDomain(WebViewAssetLoader.DEFAULT_DOMAIN)
        .addPathHandler("/epub/", WebViewAssetLoader.InternalStoragePathHandler(context, File(baseDir)))
        .build()

private fun hideUntilStyled(webView: WebView, holder: WebViewHolder) {
    holder.pendingReveal?.let(webView::removeCallbacks)
    webView.setBackgroundColor(holder.theme.bgColor.toColorInt())
    webView.visibility = View.INVISIBLE

    val reveal = Runnable { webView.visibility = View.VISIBLE }
    holder.pendingReveal = reveal
    webView.postDelayed(reveal, STYLE_REVEAL_TIMEOUT_MS)
}

private fun revealStyledContent(webView: WebView, holder: WebViewHolder) {
    holder.pendingReveal?.let(webView::removeCallbacks)
    holder.pendingReveal = null
    webView.visibility = View.VISIBLE
}

private fun viewportScript(): String = """
    var meta = document.createElement('meta');
    meta.name = 'viewport';
    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
    document.getElementsByTagName('head')[0].appendChild(meta);
""".trimIndent()

private fun styleScript(theme: ReaderTheme): String {
    val css = """
        html, body {
            background-color: ${theme.bgColor} !important;
            color: ${theme.textColor} !important;
            margin: 0 !important;
            padding: 0 !important;
            max-width: 100% !important;
            overflow-x: hidden !important;
            -webkit-text-size-adjust: 100% !important;
        }
        body {
            display: block !important;
            padding-top: 12vh !important;
            padding-left: 24px !important;
            padding-right: 24px !important;
            padding-bottom: 6vh !important;
            font-family: Georgia, serif !important;
            line-height: 1.6 !important;
            font-size: 14px !important;
            text-align: justify !important;
        }
        * {
            max-width: 100% !important;
            box-sizing: border-box !important;
            overflow-wrap: break-word !important;
            word-wrap: break-word !important;
        }
        img, svg, image {
            display: block !important;
            max-width: 100% !important;
            height: auto !important;
            margin: 1.5em auto !important;
            object-fit: contain !important;
        }
        h1, h2, h3 {
            text-align: center !important;
            margin-bottom: 2em !important;
            page-break-after: avoid !important;
        }
    """.trimIndent().replace("\n", " ")

    return "var style = document.createElement('style'); style.innerHTML = '$css'; document.head.appendChild(style);"
}

private fun themeScript(theme: ReaderTheme): String = """
    document.documentElement.style.setProperty('background-color', '${theme.bgColor}', 'important');
    document.body.style.setProperty('background-color', '${theme.bgColor}', 'important');
    document.documentElement.style.setProperty('color', '${theme.textColor}', 'important');
    document.body.style.setProperty('color', '${theme.textColor}', 'important');
""".trimIndent()

private fun restoreScrollScript(scrollPosition: Float): String = """
    setTimeout(function() {
        var scrollableHeight = document.body.scrollHeight - window.innerHeight;
        var targetPos = $scrollPosition;
        if (targetPos > 1.1) {
            window.scrollTo(0, targetPos);
        } else if (scrollableHeight > 0) {
            window.scrollTo(0, scrollableHeight * targetPos);
        }
    }, 100);
""".trimIndent()

private fun behaviorScript(scrollPosition: Float): String = """
    function updateReadingTime() {
        var text = document.body.innerText;
        var totalWords = text.split(/\s+/).length;

        var scrollableHeight = document.body.scrollHeight - window.innerHeight;
        var scrollPercent = 1.0;
        if (scrollableHeight > 0) {
            if (window.scrollY >= scrollableHeight - 5) {
                scrollPercent = 1.0;
            } else {
                scrollPercent = window.scrollY / scrollableHeight;
            }
        }
        if (scrollPercent > 1) scrollPercent = 1;
        if (scrollPercent < 0) scrollPercent = 0;

        var wordsRead = totalWords * scrollPercent;
        var wordsLeft = totalWords * (1 - scrollPercent);
        var minutesLeft = Math.ceil(wordsLeft / 250);

        AndroidBridge.reportTimeRemaining(minutesLeft);
        AndroidBridge.reportWordsRead(Math.round(wordsRead));
    }

    var scrollableHeight = document.body.scrollHeight - window.innerHeight;
    var targetPos = $scrollPosition;
    if (targetPos > 1.1) {
        window.scrollTo(0, targetPos);
    } else if (scrollableHeight > 0) {
        window.scrollTo(0, scrollableHeight * targetPos);
    }

    var scrollTimeout;
    window.onscroll = function() {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(function() {
            var scrollableHeight = document.body.scrollHeight - window.innerHeight;
            var scrollPercent = 1.0;
            if (scrollableHeight > 0) {
                if (window.scrollY >= scrollableHeight - 5) {
                    scrollPercent = 1.0;
                } else {
                    scrollPercent = window.scrollY / scrollableHeight;
                }
            }
            scrollPercent = Math.max(0, Math.min(1, scrollPercent));
            AndroidBridge.reportScrollPosition(scrollPercent);
            updateReadingTime();
        }, 500);
    }

    var startY = 0;
    var isAtTop = false;
    var isAtBottom = false;

    window.addEventListener('touchstart', function(e) {
        startY = e.touches[0].clientY;
        isAtTop = (window.scrollY <= 5);

        var scrollableHeight = document.body.scrollHeight - window.innerHeight;
        isAtBottom = (window.scrollY >= scrollableHeight - 5);
    }, {passive: true});

    window.addEventListener('touchmove', function(e) {
        if (!isAtTop && !isAtBottom) return;
        var currentY = e.touches[0].clientY;
        var deltaY = startY - currentY;

        if ((isAtTop && deltaY < 0) || (isAtBottom && deltaY > 0)) {
            AndroidBridge.updateOverscroll(deltaY);
        }
    }, {passive: true});

    window.addEventListener('touchend', function(e) {
        AndroidBridge.updateOverscroll(0);

        var endY = e.changedTouches[0].clientY;
        var deltaY = startY - endY;

        if (isAtBottom && deltaY > 80) {
            AndroidBridge.goToNextChapter();
        }
        else if (isAtTop && deltaY < -80) {
            AndroidBridge.goToPreviousChapter();
        }
    }, {passive: true});
""".trimIndent()
