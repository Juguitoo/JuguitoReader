package com.juguito.juguitoreader.ui.reader

import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.juguito.juguitoreader.domain.model.EpubNavElement
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

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
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error al abrir el libro") },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Aceptar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderContent(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
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
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.WHITE)

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
                                        background-color: #EEE7D7 !important;
                                        color: #1A1A1A !important;
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
                                view?.evaluateJavascript(viewportScript + styleScript, null)
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
                }
            )

            AnimatedVisibility(
                visible = state.isControlsVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = { Text(text = state.book.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    },
                    actions = {
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
                    Row(
                        modifier = Modifier.padding(6.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onEvent(ReaderEvent.OnPreviousChapter) },
                            enabled = state.currentChapterIndex > 0,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.4f))
                        ) { Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(32.dp)) }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Capítulo ${state.currentChapterIndex + 1} de ${state.epubContent.spine.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { (state.currentChapterIndex + 1).toFloat() / state.epubContent.spine.size },
                                modifier = Modifier.width(120.dp).padding(top = 6.dp).clip(CircleShape),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = { onEvent(ReaderEvent.OnNextChapter) },
                            enabled = state.currentChapterIndex < state.epubContent.spine.size - 1,
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = 0.4f))
                        ) { Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(32.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CollapsibleIndexItem(element: EpubNavElement, level: Int, activeChapterFileName: String, onSelect: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(true) }
    val hasChildren = element.children.isNotEmpty()
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f, label = "rotation")
    val isSelected = remember(element.href, activeChapterFileName) {
        element.href.substringAfterLast("/") == activeChapterFileName
    }

    Column {
        NavigationDrawerItem(
            label = { 
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (hasChildren) {
                        IconButton(onClick = { isExpanded = !isExpanded }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.rotate(rotation))
                        }
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Spacer(Modifier.width(if (level > 0) (level * 16).dp else 0.dp))
                    }
                    Text(
                        text = element.title.ifBlank { "Sin título" } + if (isSelected) " ✓" else "",
                        style = if(level == 0) MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            selected = false,
            onClick = { onSelect(element.href) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        if (hasChildren && isExpanded) {
            element.children.forEach { child ->
                CollapsibleIndexItem(element = child, level = level + 1, activeChapterFileName = activeChapterFileName, onSelect = onSelect)
            }
        }
    }
}
