package com.juguito.juguitoreader.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.juguito.juguitoreader.ui.reader.ReaderTheme

@Composable
fun ReaderPreviewBox(
    theme: ReaderTheme,
    textSizePercentage: Int,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(theme.bgColor.toColorInt())
    val textColor = Color(theme.textColor.toColorInt())

    val baseFontSize = 16f
    val scaleFactor = textSizePercentage / 100f

    val currentFontSize = (baseFontSize * scaleFactor).sp
    val currentLineHeight = (baseFontSize * 1.6f * scaleFactor).sp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "MUESTRA",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "«En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.»",
                color = textColor,
                fontSize = currentFontSize,
                lineHeight = currentLineHeight,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Justify,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}