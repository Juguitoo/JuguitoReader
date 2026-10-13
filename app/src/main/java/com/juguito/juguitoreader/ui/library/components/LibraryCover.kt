package com.juguito.juguitoreader.ui.library.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp

internal fun Modifier.libraryCoverShadow(): Modifier = dropShadow(
    shape = RectangleShape,
    shadow = Shadow(
        radius = 8.dp,
        color = Color.Black,
        spread = 1.dp,
        alpha = 0.4f,
    ),
)
