package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.remote.mensajeParaUsuario
import com.example.opportunitymanagement.data.local.EventoEntity
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.di.appContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventoViewModel(
    private val repository: EventoRepository,
    organizacionRepository: OrganizacionRepository,
    private val validator: EventoValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventoUiState())
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    // Última carga pedida (id del evento y organización preseleccionada).
    private var cargaSolicitada: Pair<Long, Long?>? = null

    init {
        // Llena el desplegable de organizaciones y lo mantiene al día.
        viewModelScope.launch {
            organizacionRepository.organizaciones
                .catch { _uiState.update { it.copy(mensaje = "No se pudieron cargar las organizaciones") } }
                .collect { lista ->
                    _uiState.update { it.copy(organizaciones = lista) }
                    // Si la organización llegó preseleccionada, propone su ubicación.
                    _uiState.value.organizacionId?.let { completarUbicacion(it) }
                }
        }
    }

    private val puedeEditar: Boolean
        get() = _uiState.value.let { !it.guardando && !it.cargando && !it.cargaFallida }

    fun onNombreChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(nombre = value, nombreError = null, mensaje = null) }
    }

    fun onTipoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(tipo = value, tipoError = null, mensaje = null) }
    }

    fun onDescripcionChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(descripcion = value, mensaje = null) }
    }

    /** Fecha elegida en el calendario (medianoche UTC en milisegundos). */
    fun onFechaChange(fechaInicio: Long) {
        if (!puedeEditar) return
        _uiState.update { it.copy(fechaInicio = fechaInicio, fechaError = null, mensaje = null) }
    }

    /** Hora en minutos desde la medianoche (9:30 = 570). */
    fun onHoraInicioChange(minutos: Int) {
        if (!puedeEditar) return
        _uiState.update {
            it.copy(horaInicio = minutos, horaInicioError = null, horaTerminoError = null, mensaje = null)
        }
    }

    fun onHoraTerminoChange(minutos: Int) {
        if (!puedeEditar) return
        _uiState.update { it.copy(horaTermino = minutos, horaTerminoError = null, mensaje = null) }
    }

    fun onComunaChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(comuna = value, comunaError = null, mensaje = null) }
    }

    fun onRegionChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(region = value, regionError = null, mensaje = null) }
    }

    fun onOrganizacionChange(organizacionId: Long) {
        if (!puedeEditar) return
        _uiState.update { it.copy(organizacionId = organizacionId, organizacionError = null, mensaje = null) }
        completarUbicacion(organizacionId)
    }

    fun onPublicoEsperadoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(publicoEsperado = value, publicoEsperadoError = null, mensaje = null) }
    }

    fun onCondicionesChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(condiciones = value, mensaje = null) }
    }

    /**
     * Muchos eventos ocurren donde está la organización: si la comuna o la región están vacías,
     * se proponen las de la organización. Lo que el usuario ya escribió no se pisa.
     */
    private fun completarUbicacion(organizacionId: Long) {
        val organizacion = _uiState.value.organizaciones.find { it.id == organizacionId } ?: return
        _uiState.update {
            it.copy(
                comuna = it.comuna.ifBlank { organizacion.comuna },
                region = it.region.ifBlank { organizacion.region },
                comunaError = if (it.comuna.isBlank()) null else it.comunaError,
                regionError = if (it.region.isBlank()) null else it.regionError
            )
        }
    }

    /**
     * @param organizacionId organización ya elegida al crear un evento desde su organización.
     * Al editar (id != 0) se ignora: manda la organización guardada en el evento.
     */
    fun cargar(id: Long, organizacionId: Long? = null) {
        // La pantalla vuelve a llamar a cargar() al recomponerse; solo se carga una vez.
        if (cargaSolicitada == id to organizacionId) return
        cargaSolicitada = id to organizacionId

        val preseleccionada = if (id == 0L) organizacionId?.takeIf { it > 0L } else null
        _uiState.update {
            EventoUiState(
                id = id,
                cargando = id != 0L,
                organizacionId = preseleccionada,
                organizaciones = it.organizaciones
            )
        }
        preseleccionada?.let { completarUbicacion(it) }
        if (id == 0L) return

        viewModelScope.launch {
            try {
                val evento = repository.buscar(id)
                if (evento == null) {
                    _uiState.update {
                        it.copy(cargando = false, cargaFallida = true, mensaje = "No existe un evento con ese código")
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        id = evento.id,
                        nombre = evento.nombre,
                        tipo = evento.tipo,
                        descripcion = evento.descripcion,
                        fechaInicio = evento.fechaInicio,
                        horaInicio = evento.horaInicio,
                        horaTermino = evento.horaTermino,
                        comuna = evento.comuna,
                        region = evento.region,
                        organizacionId = evento.organizacionId,
                        publicoEsperado = evento.publicoEsperado,
                        condiciones = evento.condiciones,
                        cargando = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(cargando = false, cargaFallida = true, mensaje = "No se pudo abrir el evento")
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val nombreError = validator.nombreError(state.nombre)
        val tipoError = validator.tipoError(state.tipo)
        val fechaError = validator.fechaError(state.fechaInicio)
        val horaInicioError = validator.horaInicioError(state.horaInicio)
        val horaTerminoError = validator.horaTerminoError(state.horaInicio, state.horaTermino)
        val comunaError = validator.comunaError(state.comuna)
        val regionError = validator.regionError(state.region)
        val organizacionError = validator.organizacionError(state.organizacionId)
        val publicoEsperadoError = validator.publicoEsperadoError(state.publicoEsperado)

        _uiState.update {
            it.copy(
                nombreError = nombreError,
                tipoError = tipoError,
                fechaError = fechaError,
                horaInicioError = horaInicioError,
                horaTerminoError = horaTerminoError,
                comunaError = comunaError,
                regionError = regionError,
                organizacionError = organizacionError,
                publicoEsperadoError = publicoEsperadoError
            )
        }

        return nombreError == null &&
                tipoError == null &&
                fechaError == null &&
                horaInicioError == null &&
                horaTerminoError == null &&
                comunaError == null &&
                regionError == null &&
                organizacionError == null &&
                publicoEsperadoError == null
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (!puedeEditar || estadoActual.guardadoExitosamente) return

        if (!validateForm()) return
        // validateForm ya comprobó que estos datos no son nulos.
        val fechaInicio = estadoActual.fechaInicio ?: return
        val horaInicio = estadoActual.horaInicio ?: return
        val horaTermino = estadoActual.horaTermino ?: return
        val organizacionId = estadoActual.organizacionId ?: return

        // Bloquea un segundo toque antes de lanzar la corrutina.
        _uiState.update { it.copy(guardando = true, mensaje = null) }

        viewModelScope.launch {
            try {
                repository.guardar(
                    EventoEntity(
                        id = estadoActual.id,
                        nombre = estadoActual.nombre.trim(),
                        tipo = estadoActual.tipo,
                        descripcion = estadoActual.descripcion.trim(),
                        fechaInicio = fechaInicio,
                        horaInicio = horaInicio,
                        horaTermino = horaTermino,
                        comuna = estadoActual.comuna.trim(),
                        region = estadoActual.region.trim(),
                        organizacionId = organizacionId,
                        publicoEsperado = estadoActual.publicoEsperado.trim(),
                        condiciones = estadoActual.condiciones.trim()
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw exception
            } catch (exception: IllegalStateException) {
                // Lanzada por el repositorio si el evento se borró mientras se editaba.
                _uiState.update {
                    it.copy(guardando = false, mensaje = exception.message ?: "El evento ya no existe")
                }
            } catch (exception: Exception) {
                // Por ejemplo, si la organización elegida se eliminó mientras tanto.
                _uiState.update {
                    it.copy(guardando = false, mensaje = exception.mensajeParaUsuario("No se pudo guardar. Inténtalo nuevamente."))
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                EventoViewModel(
                    container.eventoRepository,
                    container.organizacionRepository,
                    EventoValidator()
                )
            }
        }
    }
}
