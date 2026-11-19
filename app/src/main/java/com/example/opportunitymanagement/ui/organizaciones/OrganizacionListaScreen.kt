package com.example.opportunitymanagement.ui.organizaciones

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
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.OrganizacionListaUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizacionListaScreen(
    s: OrganizacionListaUiState,
    agregar: () -> Unit,
    editar: (Long) -> Unit,
    eliminar: (Long) -> Unit,
    volver: () -> Unit
) {
    // Organización que espera confirmación; es estado visual, por eso vive en la pantalla.
    var porEliminar by remember { mutableStateOf<OrganizacionEntity?>(null) }

    porEliminar?.let { o ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar organización") },
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
                title = { Text("Organizaciones") },
                navigationIcon = { TextButton(onClick = volver) { Text("Menú") } })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = agregar) { Text("+ Nueva organización") }
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
                    "QUIÉN ORGANIZA",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Text(
                    "${s.items.size} organizaciones registradas",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (s.cargando) item { CircularProgressIndicator() }
            if (s.error != null) item { Text(s.error, color = MaterialTheme.colorScheme.error) }
            if (s.mensaje != null) item { Text(s.mensaje, color = MaterialTheme.colorScheme.error) }
            if (!s.cargando && s.error == null && s.items.isEmpty()) item {
                Text(
                    "Aún no tienes organizaciones. Registra la primera para comenzar.",
                    Modifier.padding(vertical = 36.dp)
                )
            }
            items(s.items, key = { it.id }) { o ->
                OrganizacionCard(
                    organizacion = o,
                    abrir = { editar(o.id) },
                    eliminar = { porEliminar = o }
                )
            }
        }
    }
}

@Composable
fun OrganizacionCard(
    organizacion: OrganizacionEntity,
    abrir: () -> Unit,
    eliminar: () -> Unit
) {
    Card(onClick = abrir, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                organizacion.tipo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(organizacion.nombre, style = MaterialTheme.typography.titleLarge)
            Text(organizacion.rubro, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${organizacion.comuna}, ${organizacion.region}",
                style = MaterialTheme.typography.bodyMedium
            )
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

private val organizacionDePrueba = OrganizacionEntity(
    id = 1,
    nombre = "Municipalidad Ficticia",
    tipo = "Municipalidad",
    rubro = "Servicios públicos",
    comuna = "Puente Alto",
    region = "Metropolitana",
    sitioWeb = "www.ejemplo.cl"
)

@Preview(showBackground = true)
@Composable
private fun OrganizacionListaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OrganizacionListaScreen(
            OrganizacionListaUiState(items = listOf(organizacionDePrueba), cargando = false),
            {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrganizacionListaVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OrganizacionListaScreen(OrganizacionListaUiState(cargando = false), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OrganizacionListaConMensajePreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OrganizacionListaScreen(
            OrganizacionListaUiState(
                items = listOf(organizacionDePrueba),
                cargando = false,
                mensaje = "No se puede eliminar: la organización tiene contactos. Elimínalos primero."
            ),
            {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrganizacionCardPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme {
        OrganizacionCard(organizacionDePrueba, abrir = {}, eliminar = {})
    }
}
