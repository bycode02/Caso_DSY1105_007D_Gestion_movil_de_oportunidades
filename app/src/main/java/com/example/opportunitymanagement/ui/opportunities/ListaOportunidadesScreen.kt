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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.OportunidadEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaOportunidadesScreen(
    s: ListaUiState,
    agregar: () -> Unit,
    editar: (Long) -> Unit,
    volver: () -> Unit
) {
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
            if (!s.cargando && s.error == null && s.items.isEmpty()) item {
                Text(
                    "Aún no tienes oportunidades. Registra la primera para comenzar.",
                    Modifier.padding(vertical = 36.dp)
                )
            }
            items(s.items, key = { it.id }) { o -> OportunidadCard(o) { editar(o.id) } }
        }
    }
}

@Composable
fun OportunidadCard(o: OportunidadEntity, abrir: () -> Unit) {
    Card(onClick = abrir, modifier = Modifier.fillMaxWidth()) {
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
            Text("Consultar / editar →", color = MaterialTheme.colorScheme.primary)
        }
    }
}

private val ejemplo = OportunidadEntity(
    1,
    "Jornada de bienestar corporativo",
    "Jornada de beneficios",
    "Actividad de demostración",
    "Sitio público de prueba",
    "Alta",
    "Alto"
)

@Preview(showBackground = true)
@Composable
private fun ListaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(listOf(ejemplo), false), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ListaVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { ListaOportunidadesScreen(ListaUiState(cargando = false), {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun CardPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme { OportunidadCard(ejemplo, {}) }
}