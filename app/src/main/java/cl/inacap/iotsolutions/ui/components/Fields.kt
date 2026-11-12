package cl.inacap.iotsolutions.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Campo de texto numérico reutilizable. */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    decimal: Boolean = true,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { text ->
            // Solo se permiten dígitos y, si corresponde, un separador decimal
            val allowed = text.filter { it.isDigit() || (decimal && (it == ',' || it == '.')) }
            onValueChange(allowed)
        },
        label = { Text(label) },
        prefix = if (prefix != null) { { Text(prefix) } } else null,
        suffix = if (suffix != null) { { Text(suffix) } } else null,
        singleLine = true,
        isError = errorText != null,
        supportingText = if (errorText != null) { { Text(errorText) } } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        modifier = modifier
    )
}

/** Tarjeta con un valor calculado (ej. "Precio de venta: $25.000"). */
@Composable
fun StatCard(
    title: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    highlight: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = highlight
            )
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
