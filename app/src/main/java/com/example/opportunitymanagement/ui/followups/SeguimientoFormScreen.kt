package com.example.opportunitymanagement.ui.followups

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.ui.auth.AuthMessage
import com.example.opportunitymanagement.ui.auth.AuthPage
import com.example.opportunitymanagement.ui.auth.AuthSubmitButton
import com.example.opportunitymanagement.ui.events.CampoFecha
import com.example.opportunitymanagement.ui.events.CampoHora
import com.example.opportunitymanagement.ui.opportunities.CampoSeleccion
import com.example.opportunitymanagement.ui.opportunities.CampoTexto
import com.example.opportunitymanagement.ui.organizations.SeccionFormulario
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.SeguimientoUiState

@Composable
fun SeguimientoFormScreen(
    state: SeguimientoUiState,
    onTipoAccionChange: (String) -> Unit,
    onFechaChange: (Long) -> Unit,
    onHoraChange: (Int) -> Unit,
    onContactoChange: (Long?) -> Unit,
    onResultadoChange: (String) -> Unit,
    onProximaAccionChange: (String?) -> Unit,
    onFechaProximoChange: (Long?) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onCambiarEstadoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !state.guardando && !state.cargando && !state.cargaFallida
    AuthPage(
        title = "Registrar acción",
        description = if (state.oportunidadNombre.isBlank()) "Seguimiento de la oportunidad."
        else "Oportunidad: ${state.oportunidadNombre} · Estado: ${state.estadoOportunidad}",
        modifier = modifier
    ) {
        if (state.cargando) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        state.mensaje?.let { AuthMessage(it, isError = true) }

        SeccionFormulario("ACCIÓN") {
            CampoSeleccion("Tipo de acción *", state.tipoAccion, Opciones.tiposAccion, state.tipoAccionError, enabled, onTipoAccionChange)
            CampoFecha("Fecha *", state.fecha, state.fechaError, enabled, onFechaChange)
            CampoHora("Hora *", state.hora, state.horaError, enabled, onHoraChange)
            CampoContacto(state.contactos, state.contactoId, enabled, onContactoChange)
        }

        // Propuesta de cambio de estado según el tipo de acción (se puede desmarcar).
        state.estadoSugerido?.let { sugerido ->
            SeccionFormulario("ESTADO DE LA OPORTUNIDAD") {
                CambioDeEstado(state.estadoOportunidad, sugerido, state.cambiarEstado, enabled, onCambiarEstadoChange)
            }
        }

        SeccionFormulario("RESULTADO") {
            CampoTexto("Resultado *", state.resultado, state.resultadoError, enabled, multilinea = true, cambiar = onResultadoChange)
            CampoTexto("Observaciones", state.observaciones, null, enabled, multilinea = true, cambiar = onObservacionesChange)
        }

        SeccionFormulario("PRÓXIMO PASO") {
            CampoOpcional(
                label = "Próxima acción",
                valor = state.proximaAccion.ifBlank { null },
                opciones = Opciones.tiposAccion,
                textoVacio = "Sin próxima acción",
                error = state.proximaAccionError,
                enabled = enabled,
                onSeleccionar = onProximaAccionChange
            )
            CampoFecha("Fecha del próximo seguimiento", state.fechaProximo, state.fechaProximoError, enabled) {
                onFechaProximoChange(it)
            }
            if (state.fechaProximo != null) {
                TextButton(onClick = { onFechaProximoChange(null) }, enabled = enabled) { Text("Quitar fecha") }
            }
        }

        AuthSubmitButton(
            text = "Guardar acción",
            loadingText = "Guardando…",
            isLoading = state.guardando,
            enabled = enabled,
            onClick = onGuardar
        )
        TextButton(
            onClick = onVolver,
            enabled = !state.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Cancelar y volver") }
    }
}

/** "Cambiar el estado a Propuesta enviada", marcado por defecto. */
@Composable
fun CambioDeEstado(
    estadoActual: String,
    estadoSugerido: String,
    marcado: Boolean,
    enabled: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = marcado, onCheckedChange = onCambiar, enabled = enabled)
        Column {
            Text("Cambiar el estado a \"$estadoSugerido\"", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Estado actual: $estadoActual. El cambio quedará en el historial.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Contacto de la organización del evento (opcional). Trabaja con el id del contacto. */
@Composable
fun CampoContacto(
    contactos: List<ContactoEntity>,
    seleccionadoId: Long?,
    enabled: Boolean,
    onSeleccionar: (Long?) -> Unit
) {
    val seleccionado = contactos.find { it.id == seleccionadoId }
    CampoOpcional(
        label = "Contacto",
        valor = seleccionado?.let { "${it.nombre} · ${it.area}" },
        opciones = contactos.map { "${it.nombre} · ${it.area}" },
        textoVacio = if (contactos.isEmpty()) "La organización no tiene contactos" else "Sin contacto",
        error = null,
        enabled = enabled && contactos.isNotEmpty(),
        onSeleccionar = { etiqueta ->
            onSeleccionar(contactos.find { "${it.nombre} · ${it.area}" == etiqueta }?.id)
        }
    )
}

/** Desplegable con una primera opción "vacía" ([textoVacio]) que devuelve null. */
@Composable
fun CampoOpcional(
    label: String,
    valor: String?,
    opciones: List<String>,
    textoVacio: String,
    error: String?,
    enabled: Boolean,
    onSeleccionar: (String?) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { abierto = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text((valor ?: textoVacio) + " ▾")
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(text = { Text(textoVacio) }, onClick = { onSeleccionar(null); abierto = false })
                opciones.forEach { opcion ->
                    DropdownMenuItem(text = { Text(opcion) }, onClick = { onSeleccionar(opcion); abierto = false })
                }
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

private val contactosEjemplo = listOf(
    ContactoEntity(
        id = 5, nombre = "Laura Soto", organizacionId = 3, cargo = "Encargada",
        area = "Bienestar", medioPreferente = "Correo", estado = "Contactado"
    )
)

private val estadoEjemplo = SeguimientoUiState(
    oportunidadId = 1,
    oportunidadNombre = "Stand dental en feria de beneficios",
    estadoOportunidad = "Contacto realizado",
    tipoAccion = "Envío de propuesta",
    fecha = 1_805_068_800_000,
    hora = 630,
    contactoId = 5,
    contactos = contactosEjemplo,
    resultado = "Se envió la propuesta por correo.",
    proximaAccion = "Seguimiento",
    fechaProximo = 1_805_673_600_000,
    estadoSugerido = "Propuesta enviada",
    cambiarEstado = true
)

@Preview(name = "Con cambio de estado sugerido", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun SeguimientoFormPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeguimientoFormScreen(estadoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Con errores", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun SeguimientoErroresPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeguimientoFormScreen(
            SeguimientoUiState(
                oportunidadNombre = "Stand dental en feria de beneficios",
                estadoOportunidad = "Evento identificado",
                fecha = 1_805_068_800_000,
                hora = 630,
                proximaAccion = "Llamada",
                tipoAccionError = "Selecciona el tipo de acción",
                resultadoError = "Describe el resultado de la acción",
                fechaProximoError = "Indica la fecha del próximo seguimiento"
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Oscuro", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun SeguimientoOscuroPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        SeguimientoFormScreen(estadoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Cambio de estado", showBackground = true)
@Composable
private fun CambioDeEstadoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CambioDeEstado("Contacto realizado", "Propuesta enviada", true, true) {}
    }
}

@Preview(name = "Contacto", showBackground = true)
@Composable
private fun CampoContactoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CampoContacto(contactosEjemplo, 5, true) {}
    }
}

@Preview(name = "Campo opcional", showBackground = true)
@Composable
private fun CampoOpcionalPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CampoOpcional("Próxima acción", null, Opciones.tiposAccion, "Sin próxima acción", null, true) {}
    }
}
