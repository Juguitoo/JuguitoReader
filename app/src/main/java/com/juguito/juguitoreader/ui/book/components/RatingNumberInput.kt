package com.juguito.juguitoreader.ui.book.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RatingNumberInput(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Usamos un String interno temporal para permitir que el usuario escriba
    // cosas como "8." sin que el Compose se vuelva loco al intentar pasarlo a Float al instante.
    var textValue by remember(rating) {
        mutableStateOf(if (rating == 0f) "" else rating.toString())
    }

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            // Reemplazamos la coma por punto para evitar fallos de conversión según el teclado
            val cleanValue = newValue.replace(',', '.')

            // Si el campo se queda vacío, reseteamos a 0
            if (cleanValue.isEmpty()) {
                textValue = ""
                onRatingChanged(0f)
            } else {
                // Validamos que sea un número y esté en el rango de 0 a 10
                val parsed = cleanValue.toFloatOrNull()
                if (parsed != null && parsed in 0f..10f) {
                    textValue = cleanValue
                    onRatingChanged(parsed)
                } else if (cleanValue.matches(Regex("^[0-9]*\\.?[0-9]*$"))) {
                    // Si están a mitad de escribir un decimal (ej: "8."), lo guardamos visualmente
                    // pero no actualizamos el ViewModel aún para no enviar un valor nulo.
                    textValue = cleanValue
                }
            }
        },
        label = { Text("Nota (1 - 10)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}