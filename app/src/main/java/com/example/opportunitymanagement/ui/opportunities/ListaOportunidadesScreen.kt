package com.example.opportunitymanagement.ui.opportunities

import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.FiltroOportunidades
import com.example.opportunitymanagement.viewmodel.FiltroSeguimiento
import com.example.opportunitymanagement.viewmodel.FiltrosOportunidad
import com.example.opportunitymanagement.utils.FormatoFecha
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
    volver: () -> Unit,
    onBuscar: (String) -> Unit,
    onFiltrosChange: (FiltrosOportunidad) -> Unit,
    onLimpiarFiltros: () -> Unit
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
            // Búsqueda y filtros: la pantalla solo avisa lo elegido; el ViewModel filtra.
            item { BuscadorOportunidades(s.filtros.texto, onBuscar) }
            item {
                PanelFiltros(
                    filtros = s.filtros,
                    opcionesOrganizacion = s.opcionesOrganizacion,
                    opcionesComuna = s.opcionesComuna,
                    opcionesRegion = s.opcionesRegion,
                    opcionesResponsable = s.opcionesResponsable,
                    onFiltrosChange = onFiltrosChange
                )
            }
            item { ResumenFiltros(s.items.size, s.total, s.filtros, onLimpiarFiltros) }
            if (s.cargando) item { CircularProgressIndicator() }
            if (s.error != null) item { Text(s.error, color = MaterialTheme.colorScheme.error) }
            if (s.mensaje != null) item { Text(s.mensaje, color = MaterialTheme.colorScheme.error) }
            if (!s.cargando && s.error == null && s.total == 0) item {
                Text(
                    "Aún no tienes oportunidades. Registra la primera para comenzar.",
                    Modifier.padding(vertical = 36.dp)
                )
            }
            if (!s.cargando && s.error == null && s.total > 0 && s.items.isEmpty()) item {
                Text(
                    "Ninguna oportunidad coincide con la búsqueda o los filtros.",
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
                    habilitado = s.eliminandoId == null,
                    hoyUtc = s.hoyUtc
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
    habilitado: Boolean = true,
    hoyUtc: Long = 0L
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
            ProximoSeguimiento(item, hoyUtc)
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
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(items = listOf(ejemplo), total = 1, cargando = false), {}, {}, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ListaVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(cargando = false), {}, {}, {}, {}, {}, {}, {}) }
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ListaSinCoincidenciasPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        ListaOportunidadesScreen(
            ListaUiState(
                total = 4,
                cargando = false,
                filtros = FiltrosOportunidad(texto = "congreso", prioridad = "Alta")
            ),
            {}, {}, {}, {}, {}, {}, {}
        )
    }
}

/** "Próxima acción: Llamada · 22/11/2026" o, si ya pasó, "Seguimiento atrasado desde ..." en rojo. */
@Composable
fun ProximoSeguimiento(item: OportunidadConResponsable, hoyUtc: Long) {
    val fecha = item.proximoSeguimiento ?: return
    val atrasado = FiltroOportunidades.seguimiento(item, hoyUtc) == FiltroSeguimiento.ATRASADOS
    Text(
        if (atrasado) "Seguimiento atrasado desde ${FormatoFecha.fecha(fecha)}"
        else "Próxima acción: ${item.proximaAccion.orEmpty()} · ${FormatoFecha.fecha(fecha)}",
        style = MaterialTheme.typography.labelLarge,
        color = if (atrasado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
private fun ProximoSeguimientoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        Column {
            ProximoSeguimiento(ejemplo.copy(proximaAccion = "Llamada", proximoSeguimiento = 1_805_068_800_000), hoyUtc = 0L)
            ProximoSeguimiento(ejemplo.copy(proximaAccion = "Llamada", proximoSeguimiento = 1_805_068_800_000), hoyUtc = 1_806_000_000_000)
        }
    }
}
