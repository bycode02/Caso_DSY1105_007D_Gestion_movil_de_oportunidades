package com.example.opportunitymanagement.ui.opportunities

import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.OportunidadUiState
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.OportunidadValidator
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
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.utils.FormatoFecha
import com.example.opportunitymanagement.ui.followups.SeccionHistorial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OportunidadFormScreen(
    s: OportunidadUiState,
    onNombreChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFuenteChange: (String) -> Unit,
    onPrioridadChange: (String) -> Unit,
    onPotencialChange: (String) -> Unit,
    onEstadoChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onReferenciaGeograficaChange: (String) -> Unit,
    onEventoChange: (Long?) -> Unit,
    onRegistrarAccion: () -> Unit,
    guardar: () -> Unit,
    volver: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (s.id == 0L) "Nueva oportunidad" else "Editar oportunidad") },
            navigationIcon = {
                TextButton(
                    onClick = volver,
                    enabled = !s.guardando
                ) { Text("Volver") }
            })
    }) { padding ->
        Column(
            Modifier
             .fillMaxSize()
             .padding(padding)
             .verticalScroll(rememberScrollState())
             .imePadding()
             .padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "DATOS DE LA OPORTUNIDAD",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Completa los campos obligatorios (*) para guardar en este dispositivo.")
            if (s.cargando) CircularProgressIndicator()
            s.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            val activo = !s.guardando && !s.cargando && !s.cargaFallida
            CampoTexto("Nombre *", s.nombre, s.nombreError, activo) { onNombreChange(it) }
            CampoSeleccion(
                "Tipo *",
                s.tipo,
                Opciones.tipos,
                s.tipoError,
                activo
            ) { onTipoChange(it) }
            CampoTexto(
                "Descripción *",
                s.descripcion,
                s.descripcionError,
                activo,
                true
            ) { onDescripcionChange(it) }
            CampoTexto(
                "Fuente de información *",
                s.fuente,
                s.fuenteError,
                activo
            ) { onFuenteChange(it) }
            CampoSeleccion(
                "Prioridad *",
                s.prioridad,
                Opciones.prioridades,
                s.prioridadError,
                activo
            ) { onPrioridadChange(it) }
            CampoSeleccion(
                "Potencial comercial *",
                s.potencial,
                Opciones.potenciales,
                s.potencialError,
                activo
            ) { onPotencialChange(it) }
            CampoSeleccion(
                "Estado de gestión *",
                s.estado,
                Opciones.estados,
                s.estadoError,
                activo
            ) { onEstadoChange(it) }
            CampoEvento(s.eventos, s.eventoId, s.eventoError, activo, onEventoChange)
            CampoTexto(
                "Observaciones",
                s.observaciones,
                null,
                activo,
                true
            ) { onObservacionesChange(it) }
            Text(
                "Referencia geográfica de prueba (opcional)",
                style = MaterialTheme.typography.titleSmall
            )
            Text(s.referenciaGeografica.ifBlank { "Sin referencia asociada" })
            OutlinedButton(onClick = {
                onReferenciaGeograficaChange("Puente Alto · Región Metropolitana (referencia ficticia)")
            }, enabled = activo) { Text("Usar ubicación de prueba") }
            if (s.referenciaGeografica.isNotBlank()) TextButton(onClick = {
                onReferenciaGeograficaChange("")
            }, enabled = activo) { Text("Quitar referencia") }
            if (OportunidadValidator().tieneErrores(s)) Text(
                "Revisa los campos marcados antes de continuar.",
                color = MaterialTheme.colorScheme.error
            )
            Button(onClick = guardar, enabled = activo, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (s.guardando) "Guardando…" else "Guardar oportunidad"
                )
            }
            if (s.id != 0L) Text(
                "Código QR de este registro: OPORTUNIDAD:${s.id}",
                style = MaterialTheme.typography.bodySmall
            )
            // El historial necesita una oportunidad ya guardada.
            if (s.id != 0L && !s.cargando && !s.cargaFallida) {
                SeccionHistorial(s.historial, enabled = !s.guardando, onRegistrarAccion = onRegistrarAccion)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun FormPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { FormularioPreview(OportunidadUiState()) }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
private fun ErroresPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { FormularioPreview(OportunidadValidator().validar(OportunidadUiState())) }
}

@Composable
private fun FormularioPreview(s: OportunidadUiState) {
    OportunidadFormScreen(
        s = s,
        onNombreChange = { },
        onTipoChange = {},
        onDescripcionChange = {},
        onFuenteChange = {},
        onPrioridadChange = {},
        onPotencialChange = {},
        onEstadoChange = {},
        onObservacionesChange = {},
        onReferenciaGeograficaChange = {},
        onEventoChange = {},
        onRegistrarAccion = {},
        guardar = {},
        volver = {}
    )
}

/**
 * Evento al que se asocia la oportunidad (obligatorio): Oportunidad → Evento → Organización.
 * Trabaja con el id del evento, no con el nombre.
 */
@Composable
fun CampoEvento(
    eventos: List<EventoConOrganizacion>,
    seleccionadoId: Long?,
    error: String?,
    enabled: Boolean,
    onSeleccionar: (Long?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val seleccionado = eventos.find { it.evento.id == seleccionadoId }
    Column(Modifier.fillMaxWidth()) {
        Text("Evento asociado *", style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text((seleccionado?.let { etiquetaEvento(it) } ?: "Seleccionar") + " ▾") }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                eventos.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(etiquetaEvento(item)) },
                        onClick = { onSeleccionar(item.evento.id); abierto = false }
                    )
                }
            }
        }
        if (error != null) Text(
            error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
        if (eventos.isEmpty()) Text(
            "Aún no hay eventos registrados. Créalos primero desde el menú Eventos.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun etiquetaEvento(item: EventoConOrganizacion): String =
    "${item.evento.nombre} · ${FormatoFecha.fecha(item.evento.fechaInicio)} · ${item.organizacionNombre}"

@Preview(showBackground = true)
@Composable
private fun CampoEventoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        CampoEvento(emptyList(), null, "Selecciona el evento de la oportunidad", true) {}
    }
}
