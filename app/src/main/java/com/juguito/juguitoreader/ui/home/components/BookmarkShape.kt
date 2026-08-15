package com.juguito.juguitoreader.ui.home.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class BookmarkShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - 40f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(size.width - 40f, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}