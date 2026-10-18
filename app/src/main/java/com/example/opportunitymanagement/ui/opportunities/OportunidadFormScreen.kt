package com.example.opportunitymanagement.ui.opportunities
import com.example.opportunitymanagement.viewmodel.OportunidadUiState
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.validar
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
fun OportunidadFormScreen(s: OportunidadUiState, cambiar: (String,String) -> Unit, guardar: () -> Unit, volver: () -> Unit) {
 Scaffold(topBar = { TopAppBar(title = { Text(if(s.id == 0L) "Nueva oportunidad" else "Editar oportunidad") }, navigationIcon = { TextButton(onClick = volver, enabled = !s.guardando) { Text("Volver") } }) }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
   Text("DATOS DE LA OPORTUNIDAD", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
   Text("Completa los campos obligatorios (*) para guardar en este dispositivo.")
   if(s.cargando) CircularProgressIndicator()
   s.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
   val activo = !s.guardando && !s.cargando && !s.cargaFallida
   CampoTexto("Nombre *", s.nombre, s.errores["nombre"], activo) { cambiar("nombre", it) }
   CampoSeleccion("Tipo *", s.tipo, Opciones.tipos, s.errores["tipo"], activo) { cambiar("tipo", it) }
   CampoTexto("Descripción *", s.descripcion, s.errores["descripcion"], activo, true) { cambiar("descripcion", it) }
   CampoTexto("Fuente de información *", s.fuente, s.errores["fuente"], activo) { cambiar("fuente", it) }
   CampoSeleccion("Prioridad *", s.prioridad, Opciones.prioridades, s.errores["prioridad"], activo) { cambiar("prioridad", it) }
   CampoSeleccion("Potencial comercial *", s.potencial, Opciones.potenciales, s.errores["potencial"], activo) { cambiar("potencial", it) }
   CampoSeleccion("Estado de gestión *", s.estado, Opciones.estados, s.errores["estado"], activo) { cambiar("estado", it) }
   CampoTexto("Observaciones", s.observaciones, null, activo, true) { cambiar("observaciones", it) }
   Text("Referencia geográfica de prueba (opcional)", style = MaterialTheme.typography.titleSmall)
   Text(s.referenciaGeografica.ifBlank { "Sin referencia asociada" })
   OutlinedButton(onClick = { cambiar("referenciaGeografica", "Puente Alto · Región Metropolitana (referencia ficticia)") }, enabled = activo) { Text("Usar ubicación de prueba") }
   if(s.referenciaGeografica.isNotBlank()) TextButton(onClick = { cambiar("referenciaGeografica", "") }, enabled = activo) { Text("Quitar referencia") }
   if(s.errores.isNotEmpty()) Text("Revisa los campos marcados antes de continuar.", color = MaterialTheme.colorScheme.error)
   Button(onClick = guardar, enabled = activo, modifier = Modifier.fillMaxWidth()) { Text(if(s.guardando) "Guardando…" else "Guardar oportunidad") }
   if(s.id != 0L) Text("Código QR de este registro: OPORTUNIDAD:${s.id}", style = MaterialTheme.typography.bodySmall)
  }
 }
}

@Preview(showBackground = true, heightDp = 1800) @Composable private fun FormPreview() { MaterialTheme { OportunidadFormScreen(OportunidadUiState(), {_,_->}, {}, {}) } }
@Preview(showBackground = true, heightDp = 1800) @Composable private fun ErroresPreview() { MaterialTheme { OportunidadFormScreen(OportunidadUiState(errores = validar(OportunidadUiState())), {_,_->}, {}, {}) } }