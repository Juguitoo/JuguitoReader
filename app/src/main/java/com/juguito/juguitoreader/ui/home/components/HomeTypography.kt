package com.juguito.juguitoreader.ui.home.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
internal fun homeSerif(
    size: TextUnit,
    lineHeight: TextUnit = TextUnit.Unspecified,
): TextStyle = MaterialTheme.typography.titleLarge.copy(
    fontSize = size,
    lineHeight = lineHeight,
    fontWeight = FontWeight.Bold,
    letterSpacing = (-0.2).sp
)

@Composable
internal fun homeSans(
    size: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified,
): TextStyle = MaterialTheme.typography.bodyLarge.copy(
    fontSize = size,
    lineHeight = lineHeight,
    fontWeight = fontWeight,
    letterSpacing = letterSpacing
)
