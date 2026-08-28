package com.juguito.juguitoreader.ui.reader.components

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
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
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderUiState

@Composable
fun EpubWebView(
    state: ReaderUiState.Success,
    currentScrollY: Float,
    onEvent: (ReaderEvent) -> Unit,
    onOverscroll: (Float) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(vertical = 24.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(state.theme.bgColor.toColorInt())

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
                }, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        val viewportScript = """
                                    var meta = document.createElement('meta');
                                    meta.name = 'viewport';
                                    meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
                                    document.getElementsByTagName('head')[0].appendChild(meta);
                                """.trimIndent()
                        val css = """
                                    html, body {
                                        background-color: ${state.theme.bgColor} !important;
                                        color: ${state.theme.textColor} !important;
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
                        val styleScript = "var style = document.createElement('style'); style.innerHTML = '$css'; document.head.appendChild(style);"
                        val scrollScript = """
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
                                        AndroidBridge.reportWordsRead(wordsRead);
                                    }
                
                                    var scrollableHeight = document.body.scrollHeight - window.innerHeight;
                                    var targetPos = ${currentScrollY};
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
                        view?.evaluateJavascript(viewportScript + styleScript + scrollScript, null)
                    }
                }

                val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
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
                    allowFileAccess = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    textZoom = 100
                }
            }
        },
        update = { webView ->
            if (webView.url != state.currentChapterUrl) {
                webView.loadUrl(state.currentChapterUrl)
            }
            if (webView.settings.textZoom != state.textZoom) {
                webView.settings.textZoom = state.textZoom

                val restoreScrollScript = """
                            setTimeout(function() {
                                var scrollableHeight = document.body.scrollHeight - window.innerHeight;
                                var targetPos = ${state.readingProgress.scrollPosition};
                                if (targetPos > 1.1) {
                                    window.scrollTo(0, targetPos);
                                } else if (scrollableHeight > 0) {
                                    window.scrollTo(0, scrollableHeight * targetPos);
                                }
                            }, 100);
                        """.trimIndent()
                webView.evaluateJavascript(restoreScrollScript, null)
            }
            val themeScript = """
                        document.documentElement.style.setProperty('background-color', '${state.theme.bgColor}', 'important');
                        document.body.style.setProperty('background-color', '${state.theme.bgColor}', 'important');
                        document.documentElement.style.setProperty('color', '${state.theme.textColor}', 'important');
                        document.body.style.setProperty('color', '${state.theme.textColor}', 'important');
                    """.trimIndent()
            webView.evaluateJavascript(themeScript, null)
        }
    )
}