package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.domain.model.Genre

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreHybridSelector(
    availableGenres: List<Genre>,
    selectedGenres: List<Genre>,
    onGenresChanged: (List<Genre>) -> Unit,
    modifier: Modifier = Modifier
) {
    var genreInput by remember { mutableStateOf("") }

    val suggestedGenres = availableGenres.filter { available ->
        val notSelected = selectedGenres.none { selected -> 
            selected.name.equals(available.name, ignoreCase = true) 
        }
        val matchesInput = available.name.contains(genreInput, ignoreCase = true)
        
        notSelected && (genreInput.isEmpty() || matchesInput)
    }

    val suggestionsScrollState = rememberScrollState()
    val selectedScrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Géneros", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = genreInput,
            onValueChange = { genreInput = it },
            label = { Text("Escribe un género nuevo...") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (genreInput.isNotBlank()) {
                            if (selectedGenres.none { it.name.equals(genreInput.trim(), ignoreCase = true) }) {
                                val newGenres = selectedGenres + Genre(name = genreInput.trim())
                                onGenresChanged(newGenres)
                            }
                            genreInput = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        )

        if (selectedGenres.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).horizontalScroll(selectedScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedGenres.forEach { genre ->
                    InputChip(
                        selected = true,
                        onClick = {
                            val newGenres = selectedGenres.filter { it.name != genre.name }
                            onGenresChanged(newGenres)
                        },
                        label = { Text(genre.name) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Eliminar", modifier = Modifier.size(16.dp)) },
                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer)
                    )
                }
            }
        }

        if (suggestedGenres.isNotEmpty()) {
            Text(
                text = if (genreInput.isEmpty()) "Géneros existentes" else "Sugerencias",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(suggestionsScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedGenres.forEach { genre ->
                    SuggestionChip(
                        onClick = {
                            val newGenres = selectedGenres + genre
                            onGenresChanged(newGenres)
                            genreInput = "" 
                        },
                        label = { Text(genre.name) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
    }
}