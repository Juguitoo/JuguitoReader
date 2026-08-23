package com.juguito.juguitoreader.ui.reader

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.juguito.juguitoreader.domain.model.EpubNavElement
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun ReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    ObserveAsEvents(viewModel.effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.NavigateBack -> onNavigateBack()
            else -> Unit
        }
    }

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onEvent(ReaderEvent.OnStartReading)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onEvent(ReaderEvent.OnFinishReading)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state) {
        if (state is ReaderUiState.Success && activity != null) {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val isVisible = (state as ReaderUiState.Success).isControlsVisible
            if (isVisible) {
                controller.show(WindowInsetsCompat.Type.systemBars())
            } else {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    when (val currentState = state) {
        is ReaderUiState.Loading -> ReaderLoading()
        is ReaderUiState.Error -> ReaderError(currentState.message, onNavigateBack)
        is ReaderUiState.Success -> ReaderContent(
            state = currentState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun ReaderLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ReaderError(message: String, onDismiss: () -> Unit) {
    JuguitoDialog(
        onDismissRequest = onDismiss,
        title = "Error al abrir el libro",
        message = message,
        confirmButtonText = "Aceptar",
        onConfirm = onDismiss,
        isDestructive = true
    )
}

enum class BottomBarMode { DEFAULT, FONT_SIZE, BRIGHTNESS }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderContent(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context as? Activity

    var bottomBarMode by remember { mutableStateOf(BottomBarMode.DEFAULT) }
    var brightness by remember(state.brightness) { mutableFloatStateOf(state.brightness) }
    var overscrollDelta by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(brightness) {
        activity?.window?.let { window ->
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
        }
    }

    LaunchedEffect(state.isControlsVisible) {
        if (!state.isControlsVisible) bottomBarMode = BottomBarMode.DEFAULT
    }

    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (state.isControlsVisible) {
            onEvent(ReaderEvent.OnToggleControls)
        } else {
            onEvent(ReaderEvent.OnBackRequested)
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Índice",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                val activeChapterFileName = remember(state.currentChapterIndex, state.epubContent.spine) {
                    state.epubContent.spine.getOrNull(state.currentChapterIndex)?.substringAfterLast("/") ?: ""
                }
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)) {
                    items(state.epubContent.chaptersTree) { chapter ->
                        CollapsibleIndexItem(
                            element = chapter,
                            level = 0,
                            activeChapterFileName = activeChapterFileName,
                            onSelect = { href ->
                                val index = state.epubContent.spine.indexOfFirst { it.endsWith(href.substringAfterLast("/")) }
                                if (index != -1) {
                                    onEvent(ReaderEvent.OnChapterSelected(index))
                                    scope.launch { drawerState.close() }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        val currentScrollY = rememberUpdatedState(state.readingProgress.scrollPosition)
        val currentState = rememberUpdatedState(state)
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(state.theme.bgColor.toColorInt()))) {
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
                                overscrollDelta = delta
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
                                        background-color: ${currentState.value.theme.bgColor} !important;
                                        color: ${currentState.value.theme.textColor} !important;
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
                                                    scrollPercent = windows.scrollY / scrollableHeight;
                                                }
                                            }
                                        if (scrollPercent > 1) scrollPercent = 1;
                                        if (scrollPercent < 0) scrollPercent = 0;
                                        
                                        var wordsLeft = totalWords * (1 - scrollPercent);
                                        var minutesLeft = Math.ceil(wordsLeft / 250);
                                        
                                        AndroidBridge.reportTimeRemaining(minutesLeft);
                                    }
                
                                    var scrollableHeight = document.body.scrollHeight - window.innerHeight;
                                    var targetPos = ${currentScrollY.value};
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
                                                    scrollPercent = windows.scrollY / scrollableHeight;
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

            AnimatedVisibility(
                visible = overscrollDelta < -20f,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            ) {
                val progress = (-overscrollDelta / 80f).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 6.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (progress >= 1f) 180f else 0f),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (progress >= 1f) "Suelta para volver" else "Capítulo anterior",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = overscrollDelta > 20f,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                val progress = (overscrollDelta / 80f).coerceIn(0f, 1f)

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shadowElevation = 6.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (progress >= 1f) 180f else 0f),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (progress >= 1f) "Suelta para avanzar" else "Siguiente capítulo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isControlsVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = { Text(text = state.book.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { onEvent(ReaderEvent.OnBackRequested) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    },
                    actions = {
                        IconButton(onClick = { onEvent(ReaderEvent.OnToggleSessionsDialog) }) { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas", tint = Color.White) }
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Índice") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }

            AnimatedVisibility(
                visible = state.isControlsVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .navigationBarsPadding()
                    ) {
                        when (bottomBarMode) {
                            BottomBarMode.FONT_SIZE -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { bottomBarMode = BottomBarMode.DEFAULT }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                                    }
                                    Icon(
                                        Icons.Default.FormatSize,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Slider(
                                        value = state.textZoom.toFloat(),
                                        onValueChange = { onEvent(ReaderEvent.OnTextZoomChanged(it.toInt())) },
                                        valueRange = 50f..200f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        thumb = {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                        },
                                        track = { positions ->
                                            SliderDefaults.Track(
                                                colors = SliderDefaults.colors(
                                                    activeTrackColor = Color.White,
                                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                                ),
                                                sliderState = positions,
                                                modifier = Modifier.height(2.dp)
                                            )
                                        }
                                    )
                                    Icon(
                                        Icons.Default.FormatSize,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            BottomBarMode.BRIGHTNESS -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { bottomBarMode = BottomBarMode.DEFAULT }) {
                                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                                    }
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Slider(
                                        value = brightness,
                                        onValueChange = {
                                            brightness = it
                                            onEvent(ReaderEvent.OnBrightnessChanged(it))
                                        },
                                        valueRange = 0.05f..1.0f,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 12.dp),
                                        thumb = {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                        },
                                        track = { positions ->
                                            SliderDefaults.Track(
                                                colors = SliderDefaults.colors(
                                                    activeTrackColor = Color.White,
                                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                                ),
                                                sliderState = positions,
                                                modifier = Modifier.height(2.dp)
                                            )
                                        }
                                    )
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            else -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEvent(ReaderEvent.OnPreviousChapter) },
                                            enabled = state.currentChapterIndex > 0,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = Color.White,
                                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronLeft,
                                                contentDescription = "Anterior",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }

                                        IconButton(onClick = { bottomBarMode = BottomBarMode.FONT_SIZE }) {
                                            Icon(Icons.Default.FormatSize, contentDescription = "Tamaño de letra")
                                        }

                                        IconButton(onClick = { bottomBarMode = BottomBarMode.BRIGHTNESS }) {
                                            Icon(Icons.Default.LightMode, contentDescription = "Brillo")
                                        }
                                    }

                                    // Bloque Central: Progreso
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${state.currentChapterIndex + 1} / ${state.epubContent.spine.size}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        LinearProgressIndicator(
                                            progress = { (state.currentChapterIndex + 1).toFloat() / state.epubContent.spine.size },
                                            modifier = Modifier
                                                .width(120.dp)
                                                .padding(top = 4.dp)
                                                .clip(CircleShape)
                                                .height(4.dp),
                                            color = Color.White,
                                            trackColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            val nextThemeIndex = (state.theme.ordinal + 1) % ReaderTheme.entries.size
                                            onEvent(ReaderEvent.OnThemeChanged(ReaderTheme.entries[nextThemeIndex]))
                                        }) {
                                            Icon(Icons.Default.Palette, contentDescription = "Cambiar tema")
                                        }

                                        IconButton(
                                            onClick = { onEvent(ReaderEvent.OnNextChapter) },
                                            enabled = state.currentChapterIndex < state.epubContent.spine.size - 1,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = Color.White,
                                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = "Siguiente",
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !state.isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val footerColor = Color(state.theme.textColor.toColorInt()).copy(alpha = 0.4f)

                    Text(
                        text = "${state.currentChapterIndex + 1} / ${state.epubContent.spine.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = footerColor
                    )

                    if (state.timeRemaining != null) {
                        val timeText = if (state.timeRemaining == 0) "< 1 min" else "${state.timeRemaining} min"
                        Text(
                            text = "Faltan $timeText",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = footerColor
                        )
                    }
                }
            }

            if (state.showStatusPrompt) {
                JuguitoDialog(
                    onDismissRequest = { onEvent(ReaderEvent.OnStatusPromptResult(false)) },
                    icon = Icons.Default.AutoStories,
                    title = "¿Quieres cambiar el estado?",
                    message = "Has avanzado bastante en esta lectura. El libro actual tiene el estado 'Pendiente'.\n¿Quieres mover el libro a tu lista de 'Leyendo'?",
                    confirmButtonText = "Sí, cambiar",
                    dismissButtonText = "Dejar igual",
                    onConfirm = { onEvent(ReaderEvent.OnStatusPromptResult(true)) }
                )
            }
        }
    }

    if (state.showSessionsDialog) {
        JuguitoDialog(
            onDismissRequest = { onEvent(ReaderEvent.OnToggleSessionsDialog) },
            icon = Icons.Default.BarChart,
            title = "Tus sesiones de lectura",
            message = "Progreso diario registrado en este libro.",
            dismissButtonText = "Cerrar",
            content = {
                if (state.bookSessions.isEmpty()) {
                    Text(
                        text = "Aún no hay sesiones guardadas. Vuelve a visitarlo cuando termines de leer hoy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.bookSessions.sortedByDescending { it.date }) { session ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Fecha
                                    Text(
                                        text = session.date,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    val minutes = ceil(session.timeSpentMillis / 60000.0).toInt()

                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text(
                                            text = "$minutes min",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${session.reachedPercentage}%",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CollapsibleIndexItem(
    element: EpubNavElement,
    level: Int,
    activeChapterFileName: String,
    onSelect: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val hasChildren = element.children.isNotEmpty()
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "rotation")

    val isSelected = remember(element.href, activeChapterFileName) {
        element.href.substringAfterLast("/") == activeChapterFileName
    }

    Column {
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSelect(element.href) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                if (level > 0) {
                    Spacer(Modifier.width((level * 16).dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Text(
                    text = element.title.ifBlank { "Sin título" },
                    style = if (level == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected || level == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (hasChildren) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.rotate(rotation),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = hasChildren && isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (level == 0) 8.dp else 0.dp)
            ) {
                element.children.forEach { child ->
                    CollapsibleIndexItem(
                        element = child,
                        level = level + 1,
                        activeChapterFileName = activeChapterFileName,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}
