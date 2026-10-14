package com.juguito.juguitoreader.ui.library.components

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.domain.model.Folder
import com.juguito.juguitoreader.utils.prefersDarkContent

@Composable
fun FolderBinder(
    folder: Folder,
    selected: Boolean,
    onClick: () -> Unit
) {
    val binderColor = runCatching { Color(folder.colorHex.toColorInt()) }.getOrElse {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }

    val reduceMotion = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f
    ) == 0f

    val shape = RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
    val edgeColor = lerp(binderColor, Color.Black, 0.35f)
    val labelColor = lerp(binderColor, Color(0xFFFFF8E7), 0.78f)

    val spineHeight = 88.dp
    val pullDistance = 8.dp
    val headroom = pullDistance + 8.dp
    val spec: AnimationSpec<Dp> = if (reduceMotion) snap() else tween(220, easing = FastOutSlowInEasing)
    val pull by animateDpAsState(
        targetValue = if (selected) pullDistance else 0.dp,
        animationSpec = spec,
        label = "binderPull"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 0.dp,
        animationSpec = spec,
        label = "binderElevation"
    )

    Box(
        modifier = Modifier
            .width(30.dp)
            .height(spineHeight + headroom)
            .zIndex(if (selected) 1f else 0f)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -pull)
                .width(30.dp)
                .height(spineHeight)
                .clickable(onClick = onClick),
            shadowElevation = elevation,
            shape = shape,
            color = binderColor
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(edgeColor)
                )
                Box(
                    Modifier
                        .padding(start = 8.dp, end = 3.dp, top = 8.dp, bottom = 8.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.dp))
                        .background(labelColor)
                ) {
                    Layout(
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            Text(
                                text = folder.name,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(folder.colorHex.toColorInt())
                            )
                        }
                    ) { measurables, constraints ->
                        val placeable = measurables.first().measure(
                            Constraints(maxWidth = constraints.maxHeight, maxHeight = constraints.maxWidth)
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.placeWithLayer(
                                x = constraints.maxWidth / 2 - placeable.width / 2,
                                y = constraints.maxHeight / 2 - placeable.height / 2
                            ) {
                                rotationZ = -90f
                            }
                        }
                    }
                }
            }
        }
    }
}