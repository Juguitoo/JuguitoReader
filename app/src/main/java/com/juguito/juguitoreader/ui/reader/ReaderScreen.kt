package com.juguito.juguitoreader.ui.reader

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.common.ObserveAsEvents
import com.juguito.juguitoreader.ui.common.UiText
import com.juguito.juguitoreader.ui.common.interfaces.UiEffect
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.reader.components.ChangeStatusPromptDialog
import com.juguito.juguitoreader.ui.reader.components.CollapsibleIndexItem
import com.juguito.juguitoreader.ui.reader.components.EpubWebView
import com.juguito.juguitoreader.ui.reader.components.OverscrollIndicators
import com.juguito.juguitoreader.ui.reader.components.ReaderControls
import com.juguito.juguitoreader.ui.reader.components.ReaderFooter
import com.juguito.juguitoreader.ui.reader.components.ReadingSessionsDialog
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
    val lifecycleOwner = LocalLifecycleOwner.current

    ObserveAsEvents(viewModel.effect) { uiEffect ->
        when (uiEffect) {
            is UiEffect.NavigateBack -> onNavigateBack()
            else -> Unit
        }
    }

    DisposableEffect(activity) {
        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.let(::restoreReaderWindow)
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

internal fun restoreReaderWindow(window: Window) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    window.attributes = window.attributes.apply {
        screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }
    WindowCompat.getInsetsController(window, window.decorView)
        .show(WindowInsetsCompat.Type.systemBars())
}

@Composable
fun ReaderLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ReaderError(message: UiText, onDismiss: () -> Unit) {
    JuguitoDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.error_opening_book),
        message = message.asString(),
        confirmButtonText = stringResource(R.string.accept),
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
    var overscrollDelta by remember { mutableFloatStateOf(0f) }

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
                        stringResource(R.string.index),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
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
                                val index = state.epubContent.spine.indexOfFirst {
                                    it.endsWith(
                                        href.substringAfterLast("/")
                                    )
                                }
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
        val currentState = rememberUpdatedState(state)
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(state.theme.bgColor.toColorInt())))
        {
            key(state.webViewInstanceKey) {
                EpubWebView(
                    state = currentState.value,
                    onEvent = onEvent,
                    onOverscroll = { overscrollDelta = it }
                )
            }

            OverscrollIndicators(
                overscrollDelta = overscrollDelta,
                canGoPrevious = currentState.value.currentChapterIndex > 0,
                canGoNext = currentState.value.currentChapterIndex < currentState.value.epubContent.spine.size - 1
            )

            ReaderControls(
                state = currentState.value,
                onEvent = onEvent,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )

            ReaderFooter(
                state = currentState.value,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (state.showStatusPrompt) ChangeStatusPromptDialog(onEvent)
            if (state.showSessionsDialog) ReadingSessionsDialog(state = currentState.value, onEvent = onEvent)
        }
    }
}