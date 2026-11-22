package com.example.opportunitymanagement.ui.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import com.example.opportunitymanagement.ui.auth.AuthMessage
import com.example.opportunitymanagement.ui.auth.AuthPage
import com.example.opportunitymanagement.ui.auth.AuthSubmitButton
import com.example.opportunitymanagement.ui.auth.AuthTextField
import com.example.opportunitymanagement.ui.opportunities.CampoSeleccion
import com.example.opportunitymanagement.ui.opportunities.CampoTexto
import com.example.opportunitymanagement.ui.organizations.SeccionFormulario
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.ContactoUiState
import com.example.opportunitymanagement.viewmodel.Opciones

@Composable
fun ContactoFormScreen(
    state: ContactoUiState,
    onNombreChange: (String) -> Unit,
    onOrganizacionChange: (Long) -> Unit,
    onCargoChange: (String) -> Unit,
    onAreaChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onMedioPreferenteChange: (String) -> Unit,
    onEstadoChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !state.guardando && !state.cargando && !state.cargaFallida
    AuthPage(
        title = if (state.id == 0L) "Nuevo contacto" else "Editar contacto",
        description = "Registra con quién hablar dentro de cada organización. Usa solo datos ficticios.",
        modifier = modifier
    ) {
        if (state.cargando) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        state.mensaje?.let { AuthMessage(it, isError = true) }

        SeccionFormulario("DATOS DEL CONTACTO") {
            AuthTextField("Nombre *", state.nombre, onNombreChange, state.nombreError, enabled)
            CampoOrganizacion(
                organizaciones = state.organizaciones,
                seleccionadaId = state.organizacionId,
                error = state.organizacionError,
                enabled = enabled,
                onSeleccionar = onOrganizacionChange
            )
            AuthTextField("Cargo o función *", state.cargo, onCargoChange, state.cargoError, enabled)
            CampoSeleccion(
                "Área *",
                state.area,
                Opciones.areasContacto,
                state.areaError,
                enabled,
                onAreaChange
            )
        }

        SeccionFormulario("MEDIOS DE CONTACTO") {
            AuthTextField(
                "Correo", state.correo, onCorreoChange, state.correoError,
                enabled, KeyboardType.Email
            )
            AuthTextField(
                "Teléfono", state.telefono, onTelefonoChange, state.telefonoError,
                enabled, KeyboardType.Phone
            )
            CampoSeleccion(
                "Medio preferente *",
                state.medioPreferente,
                Opciones.mediosContacto,
                state.medioPreferenteError,
                enabled,
                onMedioPreferenteChange
            )
        }

        SeccionFormulario("GESTIÓN") {
            CampoSeleccion(
                "Estado *",
                state.estado,
                Opciones.estadosContacto,
                state.estadoError,
                enabled,
                onEstadoChange
            )
            CampoTexto(
                "Observaciones",
                state.observaciones,
                error = null,
                enabled = enabled,
                multilinea = true,
                cambiar = onObservacionesChange
            )
        }

        AuthSubmitButton(
            text = "Guardar contacto",
            loadingText = "Guardando…",
            isLoading = state.guardando,
            enabled = enabled && state.organizaciones.isNotEmpty(),
            onClick = onGuardar
        )
        TextButton(
            onClick = onVolver,
            enabled = !state.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Cancelar y volver") }
    }
}

/**
 * Desplegable de organizaciones que trabaja con el id, no con el nombre:
 * así dos organizaciones con el mismo nombre no se confunden.
 */
@Composable
fun CampoOrganizacion(
    organizaciones: List<OrganizacionEntity>,
    seleccionadaId: Long?,
    error: String?,
    enabled: Boolean,
    onSeleccionar: (Long) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val seleccionada = organizaciones.find { it.id == seleccionadaId }
    Column(Modifier.fillMaxWidth()) {
        Text("Organización *", style = MaterialTheme.typography.labelLarge)
        if (organizaciones.isEmpty()) {
            Text(
                "Aún no hay organizaciones. Registra una desde el menú Organizaciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }
        Box {
            OutlinedButton(
                onClick = { abierto = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text((seleccionada?.nombre ?: "Seleccionar") + " ▾") }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                organizaciones.forEach { organizacion ->
                    DropdownMenuItem(
                        text = { Text("${organizacion.nombre} · ${organizacion.comuna}") },
                        onClick = { onSeleccionar(organizacion.id); abierto = false }
                    )
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

private val organizacionesEjemplo = listOf(
    OrganizacionEntity(
        id = 3, nombre = "Municipalidad Ficticia", tipo = "Municipalidad",
        rubro = "Servicios públicos", comuna = "Puente Alto", region = "Metropolitana"
    ),
    OrganizacionEntity(
        id = 4, nombre = "Empresa Demo SpA", tipo = "Empresa",
        rubro = "Retail", comuna = "Santiago", region = "Metropolitana"
    )
)

private val contactoEjemplo = ContactoUiState(
    id = 10,
    nombre = "Laura Soto",
    organizacionId = 3,
    cargo = "Encargada",
    area = "Bienestar",
    correo = "laura.soto@ejemplo.cl",
    telefono = "+56 9 1234 5678",
    medioPreferente = "Correo",
    estado = "Contactado",
    organizaciones = organizacionesEjemplo
)

@Preview(name = "Nuevo, desde su organización", showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun ContactoNuevoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ContactoFormScreen(
            ContactoUiState(organizacionId = 3, organizaciones = organizacionesEjemplo),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Con errores", showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
private fun ContactoErroresPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ContactoFormScreen(
            ContactoUiState(
                correo = "laura@",
                telefono = "12345",
                organizaciones = organizacionesEjemplo,
                nombreError = "Ingresa el nombre del contacto",
                organizacionError = "Selecciona una organización",
                cargoError = "Indica el cargo o función",
                areaError = "Selecciona un área",
                correoError = "Ingresa un correo válido",
                telefonoError = "El teléfono debe tener entre 8 y 12 dígitos",
                medioPreferenteError = "Selecciona un medio de contacto"
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Editando", showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun ContactoEditandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ContactoFormScreen(contactoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Sin organizaciones", showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun ContactoSinOrganizacionesPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ContactoFormScreen(ContactoUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Oscuro", showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun ContactoOscuroPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        ContactoFormScreen(contactoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Campo organización", showBackground = true)
@Composable
private fun CampoOrganizacionPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CampoOrganizacion(organizacionesEjemplo, 3, null, true) {}
    }
}
