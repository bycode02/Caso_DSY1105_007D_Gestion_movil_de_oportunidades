package com.example.opportunitymanagement.ui.organizaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier

){
    val enabled = !state.cargaFallida && !state.cargando && !state.guardando
    AuthPage(
        title = if (state.id==0L)"Nueva Organizacion" else "Editar Organizacion",
        description = "Registra quien organiza los eventos  y actividades",
        modifier = modifier
    ) {
        if (state.cargando){
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        state.mensaje?.let { AuthMessage(it, isError = true) }

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
        TextButton(
            onClick = onVolver,
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
        OrganizacionFormScreen(OrganizacionUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {})
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
            {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Editando y guardando", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun OrganizacionGuardandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(ejemplo.copy(guardando = true), {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "No encontrada", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun OrganizacionNoEncontradaPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        OrganizacionFormScreen(
            OrganizacionUiState(id = 99, cargaFallida = true, mensaje = "No existe una organización con ese código"),
            {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Oscuro", showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
fun OrganizacionOscuroPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        OrganizacionFormScreen(ejemplo, {}, {}, {}, {}, {}, {}, {}, {}, {})
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