package com.juguito.juguitoreader.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.ui.home.StatsUiState

@Composable
fun StatsSection(
    statsUiState: StatsUiState
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Libros en biblioteca",
            value = statsUiState.totalBooksCount.toString(),
            icon = Icons.AutoMirrored.Filled.LibraryBooks,
            color = MaterialTheme.colorScheme.primaryContainer
        )
        StatCard(
            title = "Libros leyendo",
            value = statsUiState.readingBooksCount.toString(),
            icon = Icons.Default.AutoStories,
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        StatCard(
            title = "Libros finalizados",
            value = statsUiState.finishedBooksCount.toString(),
            icon = Icons.Default.DoneAll,
            color = MaterialTheme.colorScheme.tertiaryContainer
        )
        /* TODO: Calculador de velocidad de lectura
        StatCard(
            title = "Velocidad media",
            value = statsUiState.readingVelocity.toString(),
            icon = Icons.Default.BarChart,
            color = MaterialTheme.colorScheme.errorContainer
        ) */
    }
}