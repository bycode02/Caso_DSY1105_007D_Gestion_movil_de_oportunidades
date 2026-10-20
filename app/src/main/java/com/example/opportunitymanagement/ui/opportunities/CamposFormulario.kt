package com.example.opportunitymanagement.ui.opportunities

import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.Opciones
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.OportunidadEntity

@Composable
fun CampoTexto(
    label: String,
    valor: String,
    error: String?,
    enabled: Boolean = true,
    multilinea: Boolean = false,
    cambiar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = cambiar,
        label = { Text(label) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        enabled = enabled,
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CampoSeleccion(
    label: String,
    valor: String,
    opciones: List<String>,
    error: String?,
    enabled: Boolean = true,
    cambiar: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text(valor.ifBlank { "Seleccionar" } + " ▾") }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = { cambiar(opcion); abierto = false })
                }
            }
        }
        if (error != null) Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { CampoTexto("Nombre *", "", "Campo obligatorio", cambiar = {}) }
}

@Preview(showBackground = true)
@Composable
private fun SeleccionPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { CampoSeleccion("Prioridad", "Alta", Opciones.prioridades, null, cambiar = {}) }
}