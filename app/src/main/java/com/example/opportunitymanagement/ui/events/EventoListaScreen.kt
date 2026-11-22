package com.example.opportunitymanagement.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.EventoEntity
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.utils.FormatoFecha
import com.example.opportunitymanagement.viewmodel.EventoListaUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoListaScreen(
    s: EventoListaUiState,
    agregar: () -> Unit,
    editar: (Long) -> Unit,
    eliminar: (Long) -> Unit,
    volver: () -> Unit
) {
    // Evento que espera confirmación; es estado visual, por eso vive en la pantalla.
    var porEliminar by remember { mutableStateOf<EventoEntity?>(null) }

    porEliminar?.let { e ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar evento") },
            text = { Text("¿Seguro que quieres eliminar \"${e.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { eliminar(e.id); porEliminar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos") },
                navigationIcon = { TextButton(onClick = volver) { Text("Menú") } })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = agregar) { Text("+ Nuevo evento") }
        }
    ) { padding ->
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
                    "ACTIVIDADES Y FERIAS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Text("${s.items.size} eventos registrados", style = MaterialTheme.typography.titleMedium)
            }
            if (s.cargando) item { CircularProgressIndicator() }
            if (s.error != null) item { Text(s.error, color = MaterialTheme.colorScheme.error) }
            if (s.mensaje != null) item { Text(s.mensaje, color = MaterialTheme.colorScheme.error) }
            if (!s.cargando && s.error == null && s.items.isEmpty()) item {
                Text(
                    "Aún no tienes eventos. Registra el primero para comenzar.",
                    Modifier.padding(vertical = 36.dp)
                )
            }
            items(s.items, key = { it.evento.id }) { item ->
                EventoCard(
                    item = item,
                    abrir = { editar(item.evento.id) },
                    eliminar = { porEliminar = item.evento }
                )
            }
        }
    }
}

@Composable
fun EventoCard(
    item: EventoConOrganizacion,
    abrir: () -> Unit,
    eliminar: () -> Unit
) {
    val evento = item.evento
    Card(onClick = abrir, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(evento.tipo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(evento.nombre, style = MaterialTheme.typography.titleLarge)
            Text(
                "${FormatoFecha.fecha(evento.fechaInicio)} · ${FormatoFecha.horario(evento.horaInicio, evento.horaTermino)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("${evento.comuna}, ${evento.region}", style = MaterialTheme.typography.bodyMedium)
            Text("Organiza: ${item.organizacionNombre}", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Abajo a la izquierda, separado de la acción principal para evitar toques por error.
                TextButton(onClick = eliminar, contentPadding = PaddingValues(0.dp)) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
                Text("Consultar / editar →", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private val eventoDePrueba = EventoConOrganizacion(
    EventoEntity(
        id = 7,
        nombre = "Feria de beneficios 2027",
        tipo = "Feria institucional",
        fechaInicio = 1_805_068_800_000,
        horaInicio = 9 * 60,
        horaTermino = 18 * 60,
        comuna = "Puente Alto",
        region = "Metropolitana",
        organizacionId = 3,
        publicoEsperado = "Trabajadores municipales y familias"
    ),
    organizacionNombre = "Municipalidad Ficticia"
)

@Preview(showBackground = true)
@Composable
private fun EventoListaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        EventoListaScreen(EventoListaUiState(items = listOf(eventoDePrueba), cargando = false), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EventoListaVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        EventoListaScreen(EventoListaUiState(cargando = false), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun EventoListaConMensajePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        EventoListaScreen(
            EventoListaUiState(
                items = listOf(eventoDePrueba),
                cargando = false,
                mensaje = "No se puede eliminar: el evento tiene oportunidades asociadas."
            ),
            {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EventoCardPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        EventoCard(eventoDePrueba, abrir = {}, eliminar = {})
    }
}
