package com.juguito.juguitoreader.ui.registry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.domain.model.BookCriteria
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.SortOption
import com.juguito.juguitoreader.ui.common.toUiText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegistryFilterSheet(
    currentCriteria: BookCriteria,
    availableSeries: List<String>,
    onCriteriaChanged: (BookCriteria) -> Unit,
    onDismiss: () -> Unit,
    onClearFilters: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.filter_books), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = onClearFilters) { Text(stringResource(R.string.clear_filters)) }
            }

            // Estado de lectura
            Text(stringResource(R.string.reading_status), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookStatus.entries.forEach { status ->
                    val isChecked = currentCriteria.statuses.contains(status)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val newStatuses = if (isChecked) {
                                currentCriteria.statuses - status
                            } else {
                                currentCriteria.statuses + status
                            }
                            onCriteriaChanged(currentCriteria.copy(statuses = newStatuses))
                        }
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = null
                        )
                        Text(
                            text = status.toUiText().asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Saga
            if (availableSeries.isNotEmpty()) {
                Text(stringResource(R.string.saga_series), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = currentCriteria.series ?: stringResource(R.string.all_sagas),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.all_sagas)) },
                            onClick = {
                                onCriteriaChanged(currentCriteria.copy(series = null))
                                expanded = false
                            }
                        )
                        availableSeries.forEach { series ->
                            DropdownMenuItem(
                                text = { Text(series) },
                                onClick = {
                                    onCriteriaChanged(currentCriteria.copy(series = series))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Ordenación específica
            Text(stringResource(R.string.other_filters), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            FilterChip(
                selected = currentCriteria.sortBy == SortOption.SERIES_ORDER_ASC,
                onClick = { onCriteriaChanged(currentCriteria.copy(sortBy = SortOption.SERIES_ORDER_ASC)) },
                label = { Text(stringResource(R.string.series_order)) }
            )
        }
    }
}
