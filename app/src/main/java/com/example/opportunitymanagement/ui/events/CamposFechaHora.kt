package com.example.opportunitymanagement.ui.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.utils.FormatoFecha

/** Botón que abre el calendario. [valor] y lo elegido son medianoche UTC en milisegundos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    label: String,
    valor: Long?,
    error: String?,
    enabled: Boolean,
    onSeleccionar: (Long) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    if (abierto) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = valor)
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.selectedDateMillis?.let(onSeleccionar)
                        abierto = false
                    },
                    enabled = estado.selectedDateMillis != null
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) { DatePicker(state = estado) }
    }
    CampoBoton(label, valor?.let(FormatoFecha::fecha) ?: "Seleccionar fecha", error, enabled) {
        abierto = true
    }
}

/** Botón que abre el reloj. [valor] y lo elegido son minutos desde la medianoche. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoHora(
    label: String,
    valor: Int?,
    error: String?,
    enabled: Boolean,
    onSeleccionar: (Int) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    if (abierto) {
        val estado = rememberTimePickerState(
            initialHour = (valor ?: 9 * 60) / 60,
            initialMinute = (valor ?: 0) % 60,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { abierto = false },
            title = { Text(label.removeSuffix(" *")) },
            text = { TimePicker(state = estado) },
            confirmButton = {
                TextButton(onClick = {
                    onSeleccionar(estado.hour * 60 + estado.minute)
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        )
    }
    CampoBoton(label, valor?.let(FormatoFecha::hora) ?: "Seleccionar hora", error, enabled) {
        abierto = true
    }
}

/** Mismo aspecto que CampoSeleccion: título, botón con el valor y error debajo. */
@Composable
private fun CampoBoton(
    label: String,
    texto: String,
    error: String?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text(texto)
        }
        if (error != null) Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(name = "Fecha elegida", showBackground = true)
@Composable
private fun CampoFechaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        // 15/03/2027 a medianoche UTC.
        CampoFecha("Fecha *", 1_805_068_800_000, null, true) {}
    }
}

@Preview(name = "Hora con error", showBackground = true)
@Composable
private fun CampoHoraPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CampoHora("Hora de término *", null, "Selecciona la hora de término", true) {}
    }
}
