package com.juguito.juguitoreader.ui.reader.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.reader.BottomBarMode
import com.juguito.juguitoreader.ui.reader.ReaderEvent
import com.juguito.juguitoreader.ui.reader.ReaderTheme
import com.juguito.juguitoreader.ui.reader.ReaderUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderControls(
    state: ReaderUiState.Success,
    onEvent: (ReaderEvent) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var bottomBarMode by remember { mutableStateOf(BottomBarMode.DEFAULT) }
    var brightness by remember(state.brightness) { mutableFloatStateOf(state.brightness) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = state.isControlsVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it })
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = state.book.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ReaderEvent.OnBackRequested) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) }
                },
                actions = {
                    IconButton(onClick = { onEvent(ReaderEvent.OnToggleSessionsDialog) }) { Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.stats), tint = Color.White) }
                    IconButton(onClick = onOpenDrawer) { Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.index)) }
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
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
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
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
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
                                            contentDescription = stringResource(R.string.previous),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    IconButton(onClick = { bottomBarMode = BottomBarMode.FONT_SIZE }) {
                                        Icon(Icons.Default.FormatSize, contentDescription = stringResource(R.string.font_size))
                                    }

                                    IconButton(onClick = { bottomBarMode = BottomBarMode.BRIGHTNESS }) {
                                        Icon(Icons.Default.LightMode, contentDescription = stringResource(R.string.brightness))
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
                                        Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.change_theme))
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
                                            contentDescription = stringResource(R.string.next),
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
    }
}