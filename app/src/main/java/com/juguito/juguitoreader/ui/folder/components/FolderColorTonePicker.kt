package com.juguito.juguitoreader.ui.folder.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

enum class ColorTone(val label: String) {
    PASTEL("Pastel"),
    STANDARD("Estándar"),
    DEEP("Intenso")
}

@Composable
fun FolderColorTonePicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Definición de paletas alineadas por índice cromático:
    // [Rojo, Rosa, Morado, Azul, Turquesa, Verde, Naranja, Marrón/Neutro]
    val pastelPalette = remember {
        listOf("#EF9A9A", "#F48FB1", "#CE93D8", "#90CAF9", "#80CBC4", "#A5D6A7", "#FFE082", "#BCAAA4")
    }
    val standardPalette = remember {
        listOf("#E53935", "#EC407A", "#8E24AA", "#1E88E5", "#00897B", "#43A047", "#FB8C00", "#6D4C41")
    }
    val deepPalette = remember {
        listOf("#B71C1C", "#AD1457", "#4A148C", "#1565C0", "#004D40", "#1B5E20", "#E65100", "#3E2723")
    }

    val currentTone = remember(selectedColorHex) {
        when {
            pastelPalette.any { it.equals(selectedColorHex, ignoreCase = true) } -> ColorTone.PASTEL
            deepPalette.any { it.equals(selectedColorHex, ignoreCase = true) } -> ColorTone.DEEP
            else -> ColorTone.STANDARD
        }
    }

    val activePalette = when (currentTone) {
        ColorTone.PASTEL -> pastelPalette
        ColorTone.STANDARD -> standardPalette
        ColorTone.DEEP -> deepPalette
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Color distintivo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ColorTone.entries.forEach { tone ->
                        val isToneSelected = tone == currentTone

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isToneSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (!isToneSelected) {
                                        val currentIndex = activePalette.indexOfFirst { it.equals(selectedColorHex, ignoreCase = true) }
                                        val targetPalette = when (tone) {
                                            ColorTone.PASTEL -> pastelPalette
                                            ColorTone.STANDARD -> standardPalette
                                            ColorTone.DEEP -> deepPalette
                                        }
                                        val newColor = if (currentIndex != -1) targetPalette[currentIndex] else targetPalette.first()
                                        onColorSelected(newColor)
                                    }
                                }
                        ) {
                            Text(
                                text = tone.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isToneSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isToneSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            activePalette.forEach { colorHex ->
                val isSelected = selectedColorHex.equals(colorHex, ignoreCase = true)
                val targetColor = Color(colorHex.toColorInt())

                val animatedColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(durationMillis = 250),
                    label = "ColorChangeAnimation"
                )

                val checkColor = if (animatedColor.luminance() > 0.5f) Color.Black else Color.White

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "ScaleSelectedColor"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .clip(CircleShape)
                        .background(animatedColor)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = checkColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}