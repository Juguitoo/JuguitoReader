package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.juguito.juguitoreader.R
import com.juguito.juguitoreader.ui.components.JuguitoDialog
import com.juguito.juguitoreader.ui.theme.LoraFontFamily
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RatingNumberInput(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val locale = Locale.getDefault()
    val valueText = if (rating == 0f) {
        stringResource(R.string.rating_out_of, stringResource(R.string.em_dash))
    } else {
        stringResource(R.string.rating_out_of, formatRating(rating, locale))
    }
    val rowDescription = stringResource(R.string.rating_short) + ", " + valueText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button) { showPicker = true }
            .semantics { contentDescription = rowDescription },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.rating_short),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = valueText,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.Medium,
            color = if (rating == 0f) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.End
        )
    }

    if (showPicker) {
        RatingInputDialog(
            initialRating = rating,
            locale = locale,
            onDismiss = { showPicker = false },
            onConfirm = { value ->
                onRatingChanged(value)
                showPicker = false
            }
        )
    }
}

@Composable
private fun RatingInputDialog(
    initialRating: Float,
    locale: Locale,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var textValue by remember {
        mutableStateOf(if (initialRating == 0f) "" else formatRating(initialRating, locale))
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun submit() {
        parseRatingInput(textValue)?.let(onConfirm)
    }

    JuguitoDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.rating_short),
        message = stringResource(R.string.rating_picker_hint),
        confirmButtonText = stringResource(R.string.save),
        onConfirm = { submit() },
        dismissButtonText = stringResource(R.string.cancel),
        content = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (isRatingDraft(newValue)) {
                        textValue = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(stringResource(R.string.rating_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = RoundedCornerShape(12.dp)
            )
        }
    )
}

internal fun parseRatingInput(raw: String): Float? {
    val clean = raw.replace(',', '.').trim()
    if (clean.isEmpty() || clean == ".") return 0f
    val parsed = clean.toFloatOrNull() ?: return null
    return parsed.takeIf { it in 0f..10f }
}

internal fun isRatingDraft(raw: String): Boolean {
    val clean = raw.replace(',', '.')
    if (clean.isEmpty()) return true
    if (!clean.matches(Regex("^\\d{0,2}(\\.\\d{0,2})?$"))) return false
    val parsed = clean.toFloatOrNull()
    return parsed == null || parsed in 0f..10f
}

internal fun formatRating(value: Float, locale: Locale): String {
    val format = NumberFormat.getNumberInstance(locale)
    format.minimumFractionDigits = if (value == value.toInt().toFloat()) 0 else 1
    format.maximumFractionDigits = 1
    return format.format(value)
}
