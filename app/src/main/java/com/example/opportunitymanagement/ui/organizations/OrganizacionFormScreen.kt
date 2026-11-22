package com.example.opportunitymanagement.ui.organizations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.opportunitymanagement.ui.auth.AuthMessage
import com.example.opportunitymanagement.ui.auth.AuthPage
import com.example.opportunitymanagement.ui.auth.AuthSubmitButton
import com.example.opportunitymanagement.ui.auth.AuthTextField
import com.example.opportunitymanagement.ui.opportunities.CampoSeleccion
import com.example.opportunitymanagement.ui.opportunities.CampoTexto
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.OrganizacionUiState


@Composable
fun OrganizacionFormScreen(
    state: OrganizacionUiState,
    onNombreChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onComunaChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onSitioWebChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    onAgregarContacto: () -> Unit,
    onAbrirContacto: (Long) -> Unit,
    modifier: Modifier = Modifier

){
    val enabled = !state.cargaFallida && !state.cargando && !state.guardando

    // Si la organización ya está guardada pero no tiene contactos, se avisa antes de salir.
    val faltanContactos = state.id != 0L && !state.cargaFallida &&
            state.contactosCargados && state.contactos.isEmpty()
    var avisarSinContactos by remember { mutableStateOf(false) }
    val salir: () -> Unit = {
        if (faltanContactos) {
            avisarSinContactos = true
        } else {
            onVolver()
        }
    }
    BackHandler(enabled = faltanContactos && !state.guardando) { avisarSinContactos = true }

    if (avisarSinContactos) {
        DialogoSinContactos(
            onAgregarContacto = { avisarSinContactos = false; onAgregarContacto() },
            onMasTarde = { avisarSinContactos = false; onVolver() },
            onCerrar = { avisarSinContactos = false }
        )
    }
    AuthPage(
        title = if (state.id==0L)"Nueva Organizacion" else "Editar Organizacion",
        description = "Registra quien organiza los eventos  y actividades",
        modifier = modifier
    ) {
        if (state.cargando){
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        state.mensaje?.let { AuthMessage(it, isError = true) }
        state.aviso?.let { AuthMessage(it, isError = false) }

        SeccionFormulario("DATOS DE LA ORGANIZACIÓN") {
            AuthTextField("Nombre *", state.nombre, onNombreChange, state.nombreError, enabled)
            CampoSeleccion(
                "Tipo de organizacion *",
                state.tipo,
                Opciones.tiposOrganizacion,
                state.tipoError,
                enabled,
                onTipoChange
            )
                AuthTextField("Rubro *",state.rubro,onRubroChange,state.rubroError,enabled)
        }

        SeccionFormulario("Ubicacion") {
            AuthTextField("Region *",state.region, onRegionChange,state.regionError,enabled)
            AuthTextField("Comuna *",state.comuna,onComunaChange,state.comunaError,enabled)
        }
        SeccionFormulario("Informacion Adicional") {
            CampoSitioWeb(state.sitioWeb,onSitioWebChange,state.sitioWebError,enabled)
            CampoTexto("Observaciones",state.observaciones,null,enabled, multilinea = true,onObservacionesChange)
        }
        AuthSubmitButton(
            text = "Guardar organización",
            loadingText = "Guardando…",
            isLoading = state.guardando,
            enabled = enabled,
            onClick = onGuardar
        )
        // Los contactos necesitan el id de la organización: solo aparecen cuando ya está guardada.
        if (state.id != 0L && !state.cargando && !state.cargaFallida) {
            SeccionContactos(
                contactos = state.contactos,
                enabled = !state.guardando,
                onAgregarContacto = onAgregarContacto,
                onAbrirContacto = onAbrirContacto
            )
        }
        TextButton(
            onClick = salir,
            enabled = !state.guardando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Cancelar y volver") }
    }

}


@Composable
fun SeccionFormulario(titulo: String, contenido: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}

/** Contactos de la organización: tocar uno lo abre para editar; el botón crea uno nuevo. */
@Composable
fun SeccionContactos(
    contactos: List<ContactoEntity>,
    enabled: Boolean,
    onAgregarContacto: () -> Unit,
    onAbrirContacto: (Long) -> Unit
) {
    SeccionFormulario("CONTACTOS (${contactos.size})") {
        if (contactos.isEmpty()) {
            Text(
                "Aún no hay contactos registrados para esta organización.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        contactos.forEach { contacto ->
            ContactoItem(contacto, enabled) { onAbrirContacto(contacto.id) }
        }
        // Sin contactos el botón se destaca, para que el usuario sepa que es el siguiente paso.
        if (contactos.isEmpty()) {
            Button(
                onClick = onAgregarContacto,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text("+ Agregar contacto") }
        } else {
            OutlinedButton(
                onClick = onAgregarContacto,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text("+ Agregar contacto") }
        }
    }
}

/** Recuerda agregar un contacto al salir de una organización que no tiene ninguno. */
@Composable
fun DialogoSinContactos(
    onAgregarContacto: () -> Unit,
    onMasTarde: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Esta organización aún no tiene contactos") },
        text = {
            Text(
                "Agrega al menos una persona de contacto para poder gestionar sus eventos " +
                    "y oportunidades. Puedes hacerlo ahora o más tarde desde esta organización."
            )
        },
        confirmButton = { TextButton(onClick = onAgregarContacto) { Text("Agregar contacto") } },
        dismissButton = { TextButton(onClick = onMasTarde) { Text("Más tarde") } }
    )
}

@Composable
fun ContactoItem(contacto: ContactoEntity, enabled: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(contacto.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                "${contacto.cargo} · ${contacto.area}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                contacto.estado,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Sitio web: teclado de URL, sin mayúscula automática ni autocorrector. */
@Composable
fun CampoSitioWeb(
    valor: String,
    cambiar: (String) -> Unit,
    error: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = cambiar,
        label = { Text("Sitio web") },
        enabled = enabled,
        singleLine = true,
        isError = error != null,
        supportingText = { Text(error ?: "Opcional, por ejemplo www.ejemplo.cl") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

val ejemplo = OrganizacionUiState(
    id = 3,
    nombre = "Municipalidad Ficticia",
    tipo = "Municipalidad",
    rubro = "Servicios públicos",
    comuna = "Puente Alto",
    region = "Metropolitana",
    sitioWeb = "www.ejemplo.cl"
)

@Preview(name = "Nueva organización", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
fun OrganizacionNuevaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(OrganizacionUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Con errores", showBackground = true, widthDp = 390, heightDp = 1300)
@Composable
fun OrganizacionErroresPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(
            OrganizacionUiState(
                sitioWeb = "sin punto",
                nombreError = "Ingresa el nombre de la organización",
                tipoError = "Selecciona un tipo de organización",
                rubroError = "Indica el rubro",
                comunaError = "Indica la comuna",
                regionError = "Indica la región",
                sitioWebError = "Ingresa un sitio web válido, por ejemplo www.ejemplo.cl"
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Editando y guardando", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun OrganizacionGuardandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(ejemplo.copy(guardando = true), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "No encontrada", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun OrganizacionNoEncontradaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(
            OrganizacionUiState(id = 99, cargaFallida = true, mensaje = "No existe una organización con ese código"),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Oscuro", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
fun OrganizacionOscuroPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        OrganizacionFormScreen(ejemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Sección", showBackground = true)
@Composable
fun SeccionFormularioPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeccionFormulario("UBICACIÓN") { Text("Contenido de la sección") }
    }
}

@Preview(name = "Sitio web con error", showBackground = true)
@Composable
fun CampoSitioWebPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        CampoSitioWeb("ejemplo", {}, "Ingresa un sitio web válido, por ejemplo www.ejemplo.cl", true)
    }
}

private val contactosEjemplo = listOf(
    ContactoEntity(
        id = 10, nombre = "Laura Soto", organizacionId = 3, cargo = "Encargada",
        area = "Bienestar", correo = "laura.soto@ejemplo.cl", medioPreferente = "Correo",
        estado = "Contactado"
    ),
    ContactoEntity(
        id = 11, nombre = "Pedro Rojas", organizacionId = 3, cargo = "Jefe de área",
        area = "Recursos Humanos", medioPreferente = "Teléfono", estado = "Por contactar"
    )
)

@Preview(name = "Recién creada, con contactos", showBackground = true, widthDp = 390, heightDp = 1600)
@Composable
private fun OrganizacionConContactosPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(
            ejemplo.copy(
                contactos = contactosEjemplo,
                contactosCargados = true,
                aviso = "Organización guardada. Ahora agrega al menos un contacto: la persona con quien gestionarás sus eventos."
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Sección de contactos vacía", showBackground = true)
@Composable
private fun SeccionContactosVaciaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        SeccionContactos(emptyList(), enabled = true, onAgregarContacto = {}, onAbrirContacto = {})
    }
}

@Preview(name = "Contacto", showBackground = true)
@Composable
private fun ContactoItemPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        ContactoItem(contactosEjemplo.first(), enabled = true) {}
    }
}

@Preview(name = "Recién creada, sin contactos", showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
private fun OrganizacionSinContactosPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(
            ejemplo.copy(
                contactosCargados = true,
                aviso = "Organización guardada. Ahora agrega al menos un contacto: la persona con quien gestionarás sus eventos."
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Diálogo sin contactos", showBackground = true)
@Composable
private fun DialogoSinContactosPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        DialogoSinContactos(onAgregarContacto = {}, onMasTarde = {}, onCerrar = {})
    }
}
