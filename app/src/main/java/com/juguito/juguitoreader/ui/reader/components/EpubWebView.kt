package com.juguito.juguitoreader.ui.reader.components

import android.content.Context
import android.graphics.Bitmap
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.webkit.WebViewAssetLoader
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.Bookmark
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.reader.ReaderUiState
import com.juguito.juguitoreader.utils.resolveWpm
import java.io.File

private const val JS_BRIDGE_NAME = "AndroidBridge"
private const val STYLE_REVEAL_TIMEOUT_MS = 2_000L

private class WebViewHolder(
    var loader: WebViewAssetLoader,
    var baseDir: String,
    var theme: ReaderTheme,
    var scrollPosition: Float,
    var wpm: Int,
    var chapterIndex: Int,
    var pendingJump: Bookmark? = null,
    var appliedJumpId: Int? = null,
    var loadedUrl: String? = null,
    var chapterBookmarks: List<Bookmark> = emptyList(),
    var paintKey: String = "",
    var runtimeReady: Boolean = false,
    var captureBookmarkRequest: Int = 0,
    var appliedCaptureRequest: Int = 0,
    var clearSelectionRequest: Int = 0,
    var appliedClearRequest: Int = 0,
    var pendingActiveMarkId: Int? = null,
    var pendingActiveColor: String = "#FF7F50",
    var appliedActiveKey: String = ""
) {
    var isDestroyed: Boolean = false
    var pendingReveal: Runnable? = null
    var consumeTap: Boolean = false
    var pendingToggle: Runnable? = null
}

@Composable
fun EpubWebView(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit,
    onOverscroll: (Float) -> Unit,
    systemMenu: SystemMenuState
) {
    val primaryHex = "#%06X".format(0xFFFFFF and MaterialTheme.colorScheme.primary.toArgb())
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
                scrollPosition = state.readingProgress.scrollPosition,
                wpm = resolveWpm(state.bookSessions),
                chapterIndex = state.currentChapterIndex
            )

            ReaderWebView(ContextThemeWrapper(context, R.style.Theme_JuguitoReader_WebView)).apply {
                tag = holder
                onSystemActionsChanged = { systemMenu.actions = it }
                systemMenu.perform = { id -> performSystemAction(id) }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(state.theme.bgColor.toColorInt())
                visibility = View.INVISIBLE

                addJavascriptInterface(
                    AndroidBridge(
                        onEvent = onEvent,
                        onOverscroll = onOverscroll,
                        onConsumeTap = { post { holder.consumeTap = true } }
                    ),
                    JS_BRIDGE_NAME
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                        return holder.loader.shouldInterceptRequest(request.url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        return request.url.host != WebViewAssetLoader.DEFAULT_DOMAIN
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        holder.runtimeReady = false
                        holder.appliedActiveKey = ""
                        view?.let { hideUntilStyled(it, holder) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        val loadedView = view ?: return
                        val payload = bookmarkPayload(holder.chapterBookmarks)
                        val script = listOf(
                            viewportScript(),
                            styleScript(holder.theme),
                            behaviorScript(holder.scrollPosition, holder.wpm, holder.chapterIndex),
                            bookmarkEngineScript(),
                            "paintBookmarks(" + payload + ");"
                        ).joinToString(separator = "\n")
                        loadedView.evaluateJavascript(script) {
                            holder.runtimeReady = true
                            val latest = bookmarkPayload(holder.chapterBookmarks)
                            holder.paintKey = latest
                            if (latest != payload) {
                                loadedView.evaluateJavascript("paintBookmarks(" + latest + ");", null)
                            }
                            if (holder.appliedCaptureRequest != holder.captureBookmarkRequest) {
                                holder.appliedCaptureRequest = holder.captureBookmarkRequest
                                loadedView.evaluateJavascript("captureBookmarkHere();", null)
                            }
                            revealStyledContent(loadedView, holder)
                            holder.appliedActiveKey = ""
                            applyActiveMark(loadedView, holder)
                        }
                        val bookmark = holder.pendingJump
                        if (bookmark != null && holder.appliedJumpId != bookmark.id) {
                            holder.appliedJumpId = bookmark.id
                            loadedView.evaluateJavascript(jumpToBookmarkScript(bookmark), null)
                        }
                        holder.loadedUrl = loadedView.url
                    }

                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        val deadView = view ?: return true
                        releaseWebView(deadView)
                        onEvent(ReaderEvent.OnRenderProcessGone)
                        return true
                    }
                }

                val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        holder.pendingToggle?.let { removeCallbacks(it) }
                        val toggle = Runnable {
                            if (!holder.consumeTap) onEvent(ReaderEvent.OnToggleControls)
                            holder.consumeTap = false
                            holder.pendingToggle = null
                        }
                        holder.pendingToggle = toggle
                        postDelayed(toggle, 100)
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

            if (!holder.isDestroyed) {
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

                val newWpm = resolveWpm(state.bookSessions)
                val wpmChanged = holder.wpm != newWpm
                holder.wpm = newWpm

                holder.chapterIndex = state.currentChapterIndex

                holder.pendingJump = state.pendingJump
                if (state.pendingJump == null) holder.appliedJumpId = null

                val chapterHref = state.epubContent.spine.getOrNull(state.currentChapterIndex)
                holder.chapterBookmarks = state.bookmarks.filter { it.chapterHref == chapterHref }
                holder.captureBookmarkRequest = state.captureBookmarkRequest
                holder.clearSelectionRequest = state.clearSelectionRequest
                holder.pendingActiveMarkId = state.markMenuId
                holder.pendingActiveColor = primaryHex

                if (webView.url != state.currentChapterUrl) {
                    webView.loadUrl(state.currentChapterUrl)
                } else {
                    if (wpmChanged) {
                        webView.evaluateJavascript(
                            "wpm = $newWpm; if (typeof updateReadingTime === 'function') updateReadingTime();",
                            null
                        )
                    }
                    if (holder.loadedUrl == webView.url && holder.runtimeReady) {
                        val bookmark = holder.pendingJump
                        if (bookmark != null && holder.appliedJumpId != bookmark.id) {
                            holder.appliedJumpId = bookmark.id
                            webView.evaluateJavascript(jumpToBookmarkScript(bookmark), null)
                        }
                        if (holder.appliedCaptureRequest != holder.captureBookmarkRequest) {
                            holder.appliedCaptureRequest = holder.captureBookmarkRequest
                            webView.evaluateJavascript("captureBookmarkHere();", null)
                        }
                        val paintPayload = bookmarkPayload(holder.chapterBookmarks)
                        if (paintPayload != holder.paintKey) {
                            holder.paintKey = paintPayload
                            webView.evaluateJavascript("paintBookmarks(" + paintPayload + ");", null)
                        }
                        if (holder.appliedClearRequest != holder.clearSelectionRequest) {
                            holder.appliedClearRequest = holder.clearSelectionRequest
                            webView.evaluateJavascript(
                                "var sel = window.getSelection(); if (sel) sel.removeAllRanges();",
                                null
                            )
                        }
                        applyActiveMark(webView, holder)
                    }
                }

                if (webView.settings.textZoom != state.textZoom) {
                    webView.settings.textZoom = state.textZoom
                    webView.evaluateJavascript(restoreScrollScript(state.readingProgress.scrollPosition), null)
                }
            }
        },
        onRelease = { webView ->
            releaseWebView(webView)
        }
    )
}

private fun applyActiveMark(webView: WebView, holder: WebViewHolder) {
    if (!holder.runtimeReady || holder.loadedUrl != webView.url) return
    val key = "${holder.pendingActiveMarkId ?: "none"}:${holder.pendingActiveColor}"
    if (holder.appliedActiveKey == key) return
    holder.appliedActiveKey = key
    val id = holder.pendingActiveMarkId?.toString() ?: "null"
    webView.evaluateJavascript("setActiveMark($id, '${holder.pendingActiveColor}');", null)
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
    if (!holder.isDestroyed) webView.visibility = View.VISIBLE
}

private fun releaseWebView(webView: WebView) {
    val holder = webView.tag as? WebViewHolder
    if (holder?.isDestroyed == true) return
    holder?.isDestroyed = true
    holder?.pendingReveal?.let(webView::removeCallbacks)
    holder?.pendingReveal = null

    (webView.parent as? ViewGroup)?.removeView(webView)
    webView.stopLoading()
    webView.removeJavascriptInterface(JS_BRIDGE_NAME)
    webView.destroy()
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
            --jr-bg: ${theme.bgColor};
            --jr-text: ${theme.textColor};
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
        ::selection {
            background-color: rgba(255,127,80,0.35) !important;
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
        AndroidBridge.reportScrollPosition(currentScrollPercent(), chapterIndex);
        if (typeof updateReadingTime === 'function') updateReadingTime();
    }, 100);
""".trimIndent()

private fun behaviorScript(scrollPosition: Float, wpm: Int, currentChapterIndex: Int): String = """
    var wpm = $wpm;
    var chapterIndex = $currentChapterIndex;

    function currentScrollPercent() {
        var scrollableHeight = document.body.scrollHeight - window.innerHeight;
        if (scrollableHeight <= 0) return 1.0;
        if (window.scrollY >= scrollableHeight - 5) return 1.0;
        return Math.max(0, Math.min(1, window.scrollY / scrollableHeight));
    }

    function currentWordsRead() {
        var totalWords = document.body.innerText.split(/\s+/).length;
        return Math.round(totalWords * currentScrollPercent());
    }

    function updateReadingTime() {
        var totalWords = document.body.innerText.split(/\s+/).length;
        var scrollPercent = currentScrollPercent();
        var minutesLeft = Math.ceil(totalWords * (1 - scrollPercent) / wpm);

        AndroidBridge.reportTimeRemaining(minutesLeft, chapterIndex);
        AndroidBridge.reportWordsRead(Math.round(totalWords * scrollPercent), chapterIndex);
    }

    var targetPos = $scrollPosition;
    setTimeout(function() {
        var scrollableHeight = document.body.scrollHeight - window.innerHeight;
        if (targetPos > 1.1) {
            window.scrollTo(0, targetPos);
        } else if (scrollableHeight > 0) {
            window.scrollTo(0, scrollableHeight * targetPos);
        }
        AndroidBridge.reportScrollPosition(currentScrollPercent(), chapterIndex);
        AndroidBridge.reportInitialWordsRead(currentWordsRead(), chapterIndex);
        updateReadingTime();
    }, 100);

    var scrollTimeout;
    function scheduleProgressReport() {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(function () {
            AndroidBridge.reportScrollPosition(currentScrollPercent(), chapterIndex);
            updateReadingTime();
        }, 500);
    }
    
    window.onscroll = scheduleProgressReport;
    window.addEventListener('resize', scheduleProgressReport);
    if (typeof ResizeObserver !== 'undefined') {
        new ResizeObserver(scheduleProgressReport).observe(document.documentElement);
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

    var selectionTimer;
    document.addEventListener('selectionchange', function() {
        clearTimeout(selectionTimer);
        selectionTimer = setTimeout(reportSelection, 250);
    });

    function reportSelection() {
        var sel = window.getSelection();
        if (!sel || sel.isCollapsed || sel.rangeCount === 0) {
            AndroidBridge.reportTextSelected('', '', '', currentScrollPercent(), chapterIndex, false, 0, 0, 0, 0);
            return;
        }
        var range = sel.getRangeAt(0);
        if (selectionHitsMark(range)) {
            AndroidBridge.reportTextSelected('', '', '', currentScrollPercent(), chapterIndex, true, 0, 0, 0, 0);
            return;
        }
        var quote = sel.toString().replace(/\u00ad/g, '').replace(/\s+/g, ' ').trim();
        if (!quote) {
            AndroidBridge.reportTextSelected('', '', '', currentScrollPercent(), chapterIndex, false, 0, 0, 0, 0);
            return;
        }
        var prefix = contextAround(range.startContainer, range.startOffset, true);
        var suffix = contextAround(range.endContainer, range.endOffset, false);
        var rect = range.getBoundingClientRect();
        AndroidBridge.reportTextSelected(
            quote, prefix, suffix, currentScrollPercent(), chapterIndex, false,
            rect.left, rect.top, rect.right, rect.bottom
        );
    }

    function contextAround(container, offset, before) {
        if (!container || container.nodeType !== 3) return '';
        var nodes = textNodes();
        var index = nodes.indexOf(container);
        if (index < 0) return '';
        var out = before
            ? nodes[index].textContent.substring(0, offset)
            : nodes[index].textContent.substring(offset);
        if (before) {
            for (var i = index - 1; i >= 0 && out.length < 48; i--) out = nodes[i].textContent + out;
            out = out.replace(/\u00ad/g, '').replace(/\s+/g, ' ');
            return out.substring(Math.max(0, out.length - 48));
        }
        for (var j = index + 1; j < nodes.length && out.length < 48; j++) out = out + nodes[j].textContent;
        return out.replace(/\u00ad/g, '').replace(/\s+/g, ' ').substring(0, 48);
    }

    function textNodes() {
        var walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT, {
            acceptNode: function(node) {
                var parent = node.parentElement;
                if (!parent) return NodeFilter.FILTER_REJECT;
                var tag = parent.tagName;
                if (tag === 'SCRIPT' || tag === 'STYLE') return NodeFilter.FILTER_REJECT;
                return NodeFilter.FILTER_ACCEPT;
            }
        });
        var nodes = [];
        var node;
        while ((node = walker.nextNode())) nodes.push(node);
        return nodes;
    }

    function selectionHitsMark(range) {
        var marks = document.querySelectorAll('.jr-mark');
        for (var i = 0; i < marks.length; i++) {
            try {
                if (range.intersectsNode(marks[i])) return true;
            } catch (e) {}
        }
        return false;
    }
""".trimIndent()
