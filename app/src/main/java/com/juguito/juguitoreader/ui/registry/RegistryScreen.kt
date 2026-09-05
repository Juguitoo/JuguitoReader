package com.juguito.juguitoreader.ui.registry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.juguito.juguitoreader.R
import coil.compose.AsyncImage
import com.juguito.juguitoreader.ui.common.toUiText
import com.juguito.juguitoreader.domain.model.Book
import com.juguito.juguitoreader.domain.enums.BookStatus
import com.juguito.juguitoreader.domain.model.SortOption
import com.juguito.juguitoreader.ui.components.ErrorView
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.text.SimpleDateFormat
import java.util.*

private val ColLibroWidth = 280.dp
private val ColEstadoWidth = 160.dp
private val ColNotaWidth = 80.dp
private val ColFechaWidth = 140.dp
private val ColComentarioWidth = 350.dp
private val TotalTableWidth = ColLibroWidth + ColEstadoWidth + ColNotaWidth + (ColFechaWidth * 2) + ColComentarioWidth + 10.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistryScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit,
    viewModel: RegistryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    RegistryContent(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenDrawer = onOpenDrawer,
        onNavigateToAddBook = onNavigateToAddBook,
        onNavigateToBookDetail = onNavigateToBookDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistryContent(
    state: RegistryUiState,
    onEvent: (RegistryEvent) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToAddBook: () -> Unit,
    onNavigateToBookDetail: (Int) -> Unit
) {
    if (state.showFilterSheet) {
        RegistryFilterSheet(
            currentCriteria = state.criteria,
            availableSeries = state.availableSeries,
            onCriteriaChanged = { onEvent(RegistryEvent.OnCriteriaChanged(it)) },
            onDismiss = { onEvent(RegistryEvent.OnShowFilterSheet(false)) },
            onClearFilters = { onEvent(RegistryEvent.OnClearFilters) }
        )
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    if (state.isSearchExpanded) {
                        TextField(
                            value = state.criteria.searchText,
                            onValueChange = { onEvent(RegistryEvent.OnSearchTextChanged(it)) },
                            placeholder = { Text(stringResource(R.string.search_registry_hint), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            stringResource(R.string.registry_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = LoraFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (state.isSearchExpanded) {
                        IconButton(onClick = { onEvent(RegistryEvent.OnToggleSearch(false)) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.close_search))
                        }
                    } else {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                        }
                    }
                },
                actions = {
                    if (!state.isSearchExpanded) {
                        IconButton(onClick = { onEvent(RegistryEvent.OnToggleSearch(true)) }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.search_hint))
                        }
                    }
                    IconButton(onClick = { onEvent(RegistryEvent.OnShowFilterSheet(true)) }) {
                        val isFiltered = state.criteria.statuses.isNotEmpty() || state.criteria.series != null
                        BadgedBox(badge = { if (isFiltered) Badge() }) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = stringResource(R.string.filters))
                        }
                    }
                    IconButton(onClick = onNavigateToAddBook) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_book))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.errorMessage != null) {
                ErrorView(
                    message = state.errorMessage,
                    onRetry = { onEvent(RegistryEvent.OnDismissError) }
                )
            } else {
                val horizontalScrollState = rememberScrollState()

                Box(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                    Column(modifier = Modifier.width(TotalTableWidth)) {
                        // Header Row con Ordenación
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            TableHeaderItem(
                                text = stringResource(R.string.book_col),
                                width = ColLibroWidth,
                                sortOption = SortOption.TITLE_ASC,
                                currentSort = state.criteria.sortBy,
                                onSortClick = { onEvent(RegistryEvent.OnCriteriaChanged(state.criteria.copy(sortBy = it))) }
                            )
                            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 0.5.dp)
                            TableHeaderItem(text = stringResource(R.string.status_col), width = ColEstadoWidth)
                            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 0.5.dp)
                            TableHeaderItem(
                                text = stringResource(R.string.rating_col),
                                width = ColNotaWidth,
                                sortOption = SortOption.RATING_DESC,
                                currentSort = state.criteria.sortBy,
                                onSortClick = { onEvent(RegistryEvent.OnCriteriaChanged(state.criteria.copy(sortBy = it))) }
                            )
                            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 0.5.dp)
                            TableHeaderItem(
                                text = stringResource(R.string.start_col),
                                width = ColFechaWidth,
                                sortOption = SortOption.CREATED_AT_DESC,
                                currentSort = state.criteria.sortBy,
                                onSortClick = { onEvent(RegistryEvent.OnCriteriaChanged(state.criteria.copy(sortBy = it))) }
                            )
                            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 0.5.dp)
                            TableHeaderItem(text = stringResource(R.string.end_col), width = ColFechaWidth)
                            VerticalDivider(modifier = Modifier.height(48.dp), thickness = 0.5.dp)
                            TableHeaderItem(text = stringResource(R.string.comment_col), width = ColComentarioWidth)
                        }
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        if (state.filteredBooks.isEmpty()) {
                            EmptyRegistryState(TotalTableWidth)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.filteredBooks) { book ->
                                    RegistryRow(
                                        book = book,
                                        onEvent = onEvent,
                                        onEditClick = { onNavigateToBookDetail(book.id) }
                                    )
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                }

                                if (!state.isSearchExpanded && state.criteria.statuses.isEmpty() && state.criteria.series == null) {
                                    item {
                                        GhostRow(onClick = onNavigateToAddBook)
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

@Composable
fun EmptyRegistryState(width: Dp) {
    Column(
        modifier = Modifier.width(width).padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_books_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TableHeaderItem(
    text: String,
    width: Dp,
    sortOption: SortOption? = null,
    currentSort: SortOption? = null,
    onSortClick: (SortOption) -> Unit = {}
) {
    val isSorting = when (sortOption) {
        SortOption.TITLE_ASC -> currentSort == SortOption.TITLE_ASC || currentSort == SortOption.TITLE_DESC
        SortOption.RATING_DESC -> currentSort == SortOption.RATING_DESC || currentSort == SortOption.RATING_ASC
        SortOption.CREATED_AT_DESC -> currentSort == SortOption.CREATED_AT_DESC || currentSort == SortOption.CREATED_AT_ASC
        else -> false
    }

    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .then(if (sortOption != null) Modifier.clickable {
                val nextSort = when (sortOption) {
                    SortOption.TITLE_ASC -> when (currentSort) {
                        SortOption.TITLE_ASC -> SortOption.TITLE_DESC
                        SortOption.TITLE_DESC -> SortOption.CREATED_AT_DESC
                        else -> SortOption.TITLE_ASC
                    }
                    SortOption.RATING_DESC -> when (currentSort) {
                        SortOption.RATING_DESC -> SortOption.RATING_ASC
                        SortOption.RATING_ASC -> SortOption.CREATED_AT_DESC
                        else -> SortOption.RATING_DESC
                    }
                    SortOption.CREATED_AT_DESC -> when (currentSort) {
                        SortOption.CREATED_AT_DESC -> SortOption.CREATED_AT_ASC
                        else -> SortOption.CREATED_AT_DESC
                    }
                    else -> sortOption
                }
                onSortClick(nextSort)
            } else Modifier)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSorting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                fontSize = 13.sp
            )
            if (sortOption != null) {
                val icon = when {
                    currentSort == SortOption.TITLE_ASC && sortOption == SortOption.TITLE_ASC -> Icons.Default.ArrowUpward
                    currentSort == SortOption.TITLE_DESC && sortOption == SortOption.TITLE_ASC -> Icons.Default.ArrowDownward
                    currentSort == SortOption.RATING_DESC && sortOption == SortOption.RATING_DESC -> Icons.Default.ArrowDownward
                    currentSort == SortOption.RATING_ASC && sortOption == SortOption.RATING_DESC -> Icons.Default.ArrowUpward
                    currentSort == SortOption.CREATED_AT_DESC && sortOption == SortOption.CREATED_AT_DESC -> Icons.Default.ArrowDownward
                    currentSort == SortOption.CREATED_AT_ASC && sortOption == SortOption.CREATED_AT_DESC -> Icons.Default.ArrowUpward
                    else -> null
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp).padding(start = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun RegistryRow(
    book: Book,
    onEvent: (RegistryEvent) -> Unit,
    onEditClick: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var localComment by remember { mutableStateOf(book.comment ?: "") }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(book.comment) {
        if (!isFocused) {
            localComment = book.comment ?: ""
        }
    }

    if (showStartDatePicker) {
        RegistryDatePickerDialog(
            initialDate = book.startDate,
            onDateSelected = { onEvent(RegistryEvent.OnStartDateChanged(book.id, it)) },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        RegistryDatePickerDialog(
            initialDate = book.endDate,
            onDateSelected = { onEvent(RegistryEvent.OnEndDateChanged(book.id, it)) },
            onDismiss = { showEndDatePicker = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(ColLibroWidth).fillMaxHeight().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(45.dp, 65.dp)) {
                if (book.coverUrl != null) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
                
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(book.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!book.series.isNullOrBlank()) {
                    Text(
                        text = "${book.series} #${book.seriesOrder?.let { if (it % 1.0 == 0.0) it.toInt() else it } ?: "?"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.width(ColEstadoWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
            RegistryStatusPicker(book.status) { onEvent(RegistryEvent.OnStatusChanged(book.id, it)) }
        }
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier.width(ColNotaWidth).fillMaxHeight(), contentAlignment = Alignment.Center) {
            RegistryRatingInput(book.rating) { onEvent(RegistryEvent.OnRatingChanged(book.id, it)) }
        }
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(
            modifier = Modifier.width(ColFechaWidth).fillMaxHeight().clickable { showStartDatePicker = true }.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(formatDate(book.startDate), style = MaterialTheme.typography.bodyMedium)
        }
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(
            modifier = Modifier.width(ColFechaWidth).fillMaxHeight().clickable { showEndDatePicker = true }.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatDate(book.endDate),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        VerticalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

        Box(modifier = Modifier
            .width(ColComentarioWidth)
            .fillMaxHeight()
            .clickable { focusRequester.requestFocus() }
            .padding(12.dp)) {
            BasicTextField(
                value = localComment,
                onValueChange = { localComment = it },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (!focusState.isFocused && localComment != (book.comment ?: "")) {
                            onEvent(RegistryEvent.OnCommentChanged(book.id, localComment))
                        }
                    }
                    .verticalScroll(rememberScrollState()),
                decorationBox = { innerTextField ->
                    if (localComment.isEmpty()) {
                        Text(
                            stringResource(R.string.write_comment_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun GhostRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.add_new_book_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RegistryStatusPicker(status: BookStatus, onStatusSelected: (BookStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(status.toUiText().asString(), style = MaterialTheme.typography.bodyMedium)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            BookStatus.entries.forEach { statusOption ->
                DropdownMenuItem(
                    text = { Text(statusOption.toUiText().asString(), style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onStatusSelected(statusOption)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RegistryRatingInput(rating: Float, onRatingChanged: (Float) -> Unit) {
    var textValue by remember(rating) { mutableStateOf(if (rating == 0f) "" else rating.toString()) }

    val color = when {
        rating == 0f -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        rating < 5f -> Color(0xFFE57373) // Rojo
        rating < 7f -> Color(0xFFFFB74D) // Naranja
        rating < 9f -> Color(0xFF81C784) // Verde
        else -> Color(0xFF1DA1F2) // Azul
    }

    BasicTextField(
        value = textValue,
        onValueChange = { newValue ->
            val clean = newValue.replace(',', '.')
            if (clean.isEmpty() || (clean.toFloatOrNull() != null && clean.toFloat() in 0f..10f) || clean.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                textValue = clean
                clean.toFloatOrNull()?.let { onRatingChanged(it) }
            }
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.width(45.dp),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                if (textValue.isEmpty()) {
                    Text("-", style = MaterialTheme.typography.bodyMedium, color = color)
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistryDatePickerDialog(
    initialDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 12.dp, bottom = 8.dp)
            ) {
                Text(stringResource(R.string.accept))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = Color.White,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

private fun formatDate(millis: Long?): String {
    if (millis == null) return "-"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}
