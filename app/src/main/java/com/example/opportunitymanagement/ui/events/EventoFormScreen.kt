package com.example.opportunitymanagement.ui.events

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import com.example.opportunitymanagement.ui.auth.AuthMessage
import com.example.opportunitymanagement.ui.auth.AuthPage
import com.example.opportunitymanagement.ui.auth.AuthSubmitButton
import com.example.opportunitymanagement.ui.auth.AuthTextField
import com.example.opportunitymanagement.ui.contacts.CampoOrganizacion
import com.example.opportunitymanagement.ui.opportunities.CampoSeleccion
import com.example.opportunitymanagement.ui.opportunities.CampoTexto
import com.example.opportunitymanagement.ui.organizations.SeccionFormulario
import com.example.opportunitymanagement.ui.theme.Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme
import com.example.opportunitymanagement.viewmodel.EventoUiState
import com.example.opportunitymanagement.viewmodel.Opciones

@Composable
fun EventoFormScreen(
    state: EventoUiState,
    onNombreChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (Long) -> Unit,
    onHoraInicioChange: (Int) -> Unit,
    onHoraTerminoChange: (Int) -> Unit,
    onComunaChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onOrganizacionChange: (Long) -> Unit,
    onPublicoEsperadoChange: (String) -> Unit,
    onCondicionesChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = !state.guardando && !state.cargando && !state.cargaFallida
    AuthPage(
        title = if (state.id == 0L) "Nuevo evento" else "Editar evento",
        description = "Registra la actividad, cuándo y dónde ocurre, y quién la organiza.",
        modifier = modifier
    ) {
        if (state.cargando) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        state.mensaje?.let { AuthMessage(it, isError = true) }

        SeccionFormulario("DATOS DEL EVENTO") {
            AuthTextField("Nombre *", state.nombre, onNombreChange, state.nombreError, enabled)
            CampoSeleccion("Tipo de evento *", state.tipo, Opciones.tipos, state.tipoError, enabled, onTipoChange)
            CampoTexto(
                "Descripción",
                state.descripcion,
                error = null,
                enabled = enabled,
                multilinea = true,
                cambiar = onDescripcionChange
            )
        }

        SeccionFormulario("ORGANIZACIÓN RESPONSABLE") {
            CampoOrganizacion(
                organizaciones = state.organizaciones,
                seleccionadaId = state.organizacionId,
                error = state.organizacionError,
                enabled = enabled,
                onSeleccionar = onOrganizacionChange
            )
        }

        SeccionFormulario("FECHA Y HORARIO") {
            CampoFecha("Fecha *", state.fechaInicio, state.fechaError, enabled, onFechaChange)
            CampoHora("Hora de inicio *", state.horaInicio, state.horaInicioError, enabled, onHoraInicioChange)
            CampoHora("Hora de término *", state.horaTermino, state.horaTerminoError, enabled, onHoraTerminoChange)
        }

        SeccionFormulario("UBICACIÓN") {
            AuthTextField("Comuna *", state.comuna, onComunaChange, state.comunaError, enabled)
            AuthTextField("Región *", state.region, onRegionChange, state.regionError, enabled)
        }

        SeccionFormulario("PÚBLICO Y CONDICIONES") {
            AuthTextField(
                "Público esperado *", state.publicoEsperado, onPublicoEsperadoChange,
                state.publicoEsperadoError, enabled
            )
            CampoTexto(
                "Condiciones de participación",
                state.condiciones,
                error = null,
                enabled = enabled,
                multilinea = true,
                cambiar = onCondicionesChange
            )
        }

        AuthSubmitButton(
            text = "Guardar evento",
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

private val organizacionesEjemplo = listOf(
    OrganizacionEntity(
        id = 3, nombre = "Municipalidad Ficticia", tipo = "Municipalidad",
        rubro = "Servicios públicos", comuna = "Puente Alto", region = "Metropolitana"
    )
)

private val eventoEjemplo = EventoUiState(
    id = 7,
    nombre = "Feria de beneficios 2027",
    tipo = "Feria institucional",
    descripcion = "Jornada abierta para trabajadores municipales y sus familias.",
    fechaInicio = 1_805_068_800_000,
    horaInicio = 9 * 60,
    horaTermino = 18 * 60,
    comuna = "Puente Alto",
    region = "Metropolitana",
    organizacionId = 3,
    publicoEsperado = "Trabajadores municipales y familias",
    condiciones = "Stand de 3x3 m, sin costo de participación.",
    organizaciones = organizacionesEjemplo
)

@Preview(name = "Nuevo, desde su organización", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun EventoNuevoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        EventoFormScreen(
            EventoUiState(
                organizacionId = 3, comuna = "Puente Alto", region = "Metropolitana",
                organizaciones = organizacionesEjemplo
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Con errores", showBackground = true, widthDp = 390, heightDp = 1800)
@Composable
private fun EventoErroresPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        EventoFormScreen(
            EventoUiState(
                horaInicio = 18 * 60,
                horaTermino = 9 * 60,
                organizaciones = organizacionesEjemplo,
                nombreError = "Ingresa el nombre del evento",
                tipoError = "Selecciona un tipo de evento",
                fechaError = "Selecciona la fecha del evento",
                horaTerminoError = "Debe ser posterior a la hora de inicio",
                comunaError = "Indica la comuna",
                regionError = "Indica la región",
                organizacionError = "Selecciona la organización responsable",
                publicoEsperadoError = "Indica el público esperado"
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(name = "Editando", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun EventoEditandoPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        EventoFormScreen(eventoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Sin organizaciones", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun EventoSinOrganizacionesPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(dynamicColor = false) {
        EventoFormScreen(EventoUiState(), {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(name = "Oscuro", showBackground = true, widthDp = 390, heightDp = 1700)
@Composable
private fun EventoOscuroPreview() {
    Caso_DSY1105_007D_Gestion_movil_de_oportunidadesTheme(darkTheme = true, dynamicColor = false) {
        EventoFormScreen(eventoEjemplo, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
