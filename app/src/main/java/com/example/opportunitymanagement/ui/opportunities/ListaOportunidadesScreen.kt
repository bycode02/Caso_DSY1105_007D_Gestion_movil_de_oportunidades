package com.example.opportunitymanagement.ui.opportunities

import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.ListaUiState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.OportunidadConResponsable
import com.example.opportunitymanagement.data.local.OportunidadEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaOportunidadesScreen(
    s: ListaUiState,
    agregar: () -> Unit,
    editar: (Long) -> Unit,
    eliminar: (Long) -> Unit,
    volver: () -> Unit
) {
    // Oportunidad que espera confirmación; es estado visual, por eso vive en la pantalla.
    var porEliminar by remember { mutableStateOf<OportunidadEntity?>(null) }

    porEliminar?.let { o ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar oportunidad") },
            text = { Text("¿Seguro que quieres eliminar \"${o.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { eliminar(o.id); porEliminar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oportunidades") },
                navigationIcon = { TextButton(onClick = volver) { Text("Menú") } })
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = agregar) { Text("+ Nueva oportunidad") } }) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Text(
                    "TU GESTIÓN COMERCIAL",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Text(
                    "${s.items.size} oportunidades registradas",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (s.cargando) item { CircularProgressIndicator() }
            if (s.error != null) item { Text(s.error, color = MaterialTheme.colorScheme.error) }
            if (s.mensaje != null) item { Text(s.mensaje, color = MaterialTheme.colorScheme.error) }
            if (!s.cargando && s.error == null && s.items.isEmpty()) item {
                Text(
                    "Aún no tienes oportunidades. Registra la primera para comenzar.",
                    Modifier.padding(vertical = 36.dp)
                )
            }
            items(s.items, key = { it.oportunidad.id }) { item ->
                val o = item.oportunidad
                OportunidadCard(
                    item = item,
                    abrir = { editar(o.id) },
                    eliminar = { porEliminar = o },
                    eliminando = s.eliminandoId == o.id,
                    habilitado = s.eliminandoId == null
                )
            }
        }
    }
}

@Composable
fun OportunidadCard(
    item: OportunidadConResponsable,
    abrir: () -> Unit,
    eliminar: () -> Unit,
    eliminando: Boolean = false,
    habilitado: Boolean = true
) {
    val o = item.oportunidad
    Card(onClick = abrir, enabled = habilitado, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                o.tipo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(o.nombre, style = MaterialTheme.typography.titleLarge)
            Text(
                "Prioridad ${o.prioridad} · Potencial ${o.potencial}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(o.estado, style = MaterialTheme.typography.labelLarge)
            Text(
                "Responsable: ${item.responsableNombre ?: "Sin asignar"}",
                style = MaterialTheme.typography.bodyMedium
            )
            item.eventoNombre?.let { evento ->
                Text(
                    "Evento: $evento · ${item.organizacionNombre.orEmpty()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Abajo a la izquierda, separado de la acción principal para evitar toques por error.
                TextButton(
                    onClick = eliminar,
                    enabled = habilitado,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (eliminando) "Eliminando…" else "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text("Consultar / editar →", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private val ejemploOportunidad = OportunidadEntity(
    1,
    "Jornada de bienestar corporativo",
    "Jornada de beneficios",
    "Actividad de demostración",
    "Sitio público de prueba",
    "Alta",
    "Alto",
    estado = "Evento identificado",
    observaciones = "ninguna",
    responsableId = 1,
    fechaDeteccion = 0L,
    ultimaActualizacion = 0L,
    referenciaGeografica = ""
)

private val ejemplo = OportunidadConResponsable(ejemploOportunidad, responsableNombre = "Ana Pérez")

@Preview(showBackground = true)
@Composable
private fun ListaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(listOf(ejemplo), false), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ListaVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(cargando = false), {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun CardPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { OportunidadCard(ejemplo, abrir = {}, eliminar = {}) }
}

@Preview(showBackground = true)
@Composable
private fun CardEliminandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OportunidadCard(ejemplo, abrir = {}, eliminar = {}, eliminando = true, habilitado = false)
    }
}


@Preview(showBackground = true)
@Composable
private fun CardSinResponsablePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OportunidadCard(
            OportunidadConResponsable(ejemploOportunidad.copy(responsableId = null), responsableNombre = null),
            abrir = {},
            eliminar = {}
        )
    }
}
