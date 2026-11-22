package com.example.opportunitymanagement.ui.opportunities

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.FiltroOportunidades
import com.example.opportunitymanagement.viewmodel.FiltrosOportunidad
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.RangoFecha

/** Buscador por nombre. Solo avisa el texto al ViewModel; no filtra nada por sí mismo. */
@Composable
fun BuscadorOportunidades(texto: String, onBuscar: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onBuscar,
        label = { Text("Buscar por nombre") },
        placeholder = { Text("Oportunidad o evento") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        trailingIcon = {
            if (texto.isNotEmpty()) TextButton(onClick = { onBuscar("") }) { Text("Borrar") }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Fila de filtros que se desliza hacia el lado. Cada filtro arma una copia de [filtros]
 * con el valor elegido y se la pasa al ViewModel.
 */
@Composable
fun PanelFiltros(
    filtros: FiltrosOportunidad,
    opcionesOrganizacion: List<String>,
    opcionesComuna: List<String>,
    opcionesRegion: List<String>,
    opcionesResponsable: List<String>,
    onFiltrosChange: (FiltrosOportunidad) -> Unit
) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroDesplegable("Estado", filtros.estado, Opciones.estados) {
            onFiltrosChange(filtros.copy(estado = it))
        }
        FiltroDesplegable("Tipo", filtros.tipo, Opciones.tipos) {
            onFiltrosChange(filtros.copy(tipo = it))
        }
        FiltroDesplegable("Prioridad", filtros.prioridad, Opciones.prioridades) {
            onFiltrosChange(filtros.copy(prioridad = it))
        }
        FiltroDesplegable("Fecha", filtros.fecha.takeIf { it != RangoFecha.TODAS }?.etiqueta,
            RangoFecha.entries.filter { it != RangoFecha.TODAS }.map { it.etiqueta }) { etiqueta ->
            val rango = RangoFecha.entries.find { it.etiqueta == etiqueta } ?: RangoFecha.TODAS
            onFiltrosChange(filtros.copy(fecha = rango))
        }
        FiltroDesplegable("Organización", filtros.organizacion, opcionesOrganizacion) {
            onFiltrosChange(filtros.copy(organizacion = it))
        }
        FiltroDesplegable("Comuna", filtros.comuna, opcionesComuna) {
            onFiltrosChange(filtros.copy(comuna = it))
        }
        FiltroDesplegable("Región", filtros.region, opcionesRegion) {
            onFiltrosChange(filtros.copy(region = it))
        }
        FiltroDesplegable("Responsable", filtros.responsable, opcionesResponsable) {
            onFiltrosChange(filtros.copy(responsable = it))
        }
    }
}

/**
 * Un filtro: muestra "Etiqueta" o "Etiqueta: valor". "Todos" quita el filtro (null).
 * Si no hay opciones (por ejemplo, aún no hay eventos), el filtro se desactiva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroDesplegable(
    etiqueta: String,
    valor: String?,
    opciones: List<String>,
    onSeleccionar: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = valor != null,
            onClick = { abierto = true },
            enabled = opciones.isNotEmpty() || valor != null,
            label = { Text(if (valor == null) "$etiqueta ▾" else "$etiqueta: $valor ▾") }
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            DropdownMenuItem(text = { Text("Todos") }, onClick = { onSeleccionar(null); abierto = false })
            opciones.forEach { opcion ->
                DropdownMenuItem(text = { Text(opcion) }, onClick = { onSeleccionar(opcion); abierto = false })
            }
        }
    }
}

/** "3 de 12 oportunidades" y, si hay filtros activos, el botón para quitarlos. */
@Composable
fun ResumenFiltros(mostradas: Int, total: Int, filtros: FiltrosOportunidad, onLimpiar: () -> Unit) {
    val hayFiltros = FiltroOportunidades.hayFiltros(filtros)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (hayFiltros) "$mostradas de $total oportunidades" else "$total oportunidades registradas",
            style = MaterialTheme.typography.titleMedium
        )
        if (hayFiltros) TextButton(onClick = onLimpiar) { Text("Limpiar filtros") }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuscadorPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { BuscadorOportunidades("feria") {} }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun PanelFiltrosPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        Column {
            PanelFiltros(
                filtros = FiltrosOportunidad(prioridad = "Alta", fecha = RangoFecha.PROXIMOS_30),
                opcionesOrganizacion = listOf("Municipalidad Ficticia"),
                opcionesComuna = listOf("Puente Alto"),
                opcionesRegion = listOf("Metropolitana"),
                opcionesResponsable = listOf("Ana Pérez"),
                onFiltrosChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltroDesplegablePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        FiltroDesplegable("Estado", "Propuesta enviada", Opciones.estados) {}
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun ResumenFiltrosPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        ResumenFiltros(3, 12, FiltrosOportunidad(estado = "Propuesta enviada")) {}
    }
}
