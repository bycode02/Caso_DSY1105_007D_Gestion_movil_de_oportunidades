package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.SeguimientoRepository
import com.example.opportunitymanagement.di.appContainer
import com.example.opportunitymanagement.domain.usecase.ObservarSesionUseCase
import com.example.opportunitymanagement.utils.FormatoFecha
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Registra una acción de seguimiento en una oportunidad (y, si se acepta, su cambio de estado). */
class SeguimientoViewModel(
    private val repository: SeguimientoRepository,
    private val oportunidadRepository: OportunidadRepository,
    private val eventoRepository: EventoRepository,
    private val contactoRepository: ContactoRepository,
    // Usuario de la sesión: responsable de la acción.
    private val observarSesion: ObservarSesionUseCase,
    private val validator: SeguimientoValidator,
    // Fecha y hora actuales; se reemplazan en las pruebas.
    private val ahoraReloj: () -> Long = FormatoFecha::ahoraReloj
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeguimientoUiState())
    val uiState: StateFlow<SeguimientoUiState> = _uiState.asStateFlow()

    // La pantalla vuelve a llamar a cargar() al recomponerse; solo se carga una vez.
    private var oportunidadCargada: Long? = null

    private val puedeEditar: Boolean
        get() = _uiState.value.let { !it.guardando && !it.cargando && !it.cargaFallida }

    fun cargar(oportunidadId: Long) {
        if (oportunidadCargada == oportunidadId) return
        oportunidadCargada = oportunidadId

        // Por defecto, la acción se registra con la fecha y hora de este momento.
        val ahora = ahoraReloj()
        _uiState.value = SeguimientoUiState(
            oportunidadId = oportunidadId,
            fecha = ahora - ahora % DIA_MS,
            hora = ((ahora % DIA_MS) / 60_000L).toInt(),
            cargando = true
        )

        viewModelScope.launch {
            try {
                val oportunidad = oportunidadRepository.buscar(oportunidadId)
                if (oportunidad == null) {
                    _uiState.update {
                        it.copy(cargando = false, cargaFallida = true, mensaje = "No existe la oportunidad")
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        oportunidadNombre = oportunidad.nombre,
                        estadoOportunidad = oportunidad.estado,
                        cargando = false
                    )
                }
                // Contactos de la organización que organiza el evento de la oportunidad.
                val organizacionId = oportunidad.eventoId
                    ?.let { eventoRepository.buscar(it) }
                    ?.organizacionId
                    ?: return@launch
                contactoRepository.contactosDeOrganizacion(organizacionId)
                    .catch { /* Sin contactos: el campo es opcional. */ }
                    .collect { lista -> _uiState.update { it.copy(contactos = lista) } }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(cargando = false, cargaFallida = true, mensaje = "No se pudo abrir la oportunidad")
                }
            }
        }
    }

    fun onTipoAccionChange(value: String) {
        if (!puedeEditar) return
        _uiState.update {
            // Propone el estado que suele seguir a esta acción, si es distinto del actual.
            val sugerido = Opciones.estadoSugerido(value)?.takeIf { estado -> estado != it.estadoOportunidad }
            it.copy(
                tipoAccion = value,
                tipoAccionError = null,
                estadoSugerido = sugerido,
                cambiarEstado = sugerido != null,
                mensaje = null
            )
        }
    }

    fun onCambiarEstadoChange(cambiar: Boolean) {
        if (!puedeEditar) return
        _uiState.update { it.copy(cambiarEstado = cambiar && it.estadoSugerido != null) }
    }

    fun onFechaChange(fecha: Long) {
        if (!puedeEditar) return
        _uiState.update { it.copy(fecha = fecha, fechaError = null, fechaProximoError = null, mensaje = null) }
    }

    fun onHoraChange(minutos: Int) {
        if (!puedeEditar) return
        _uiState.update { it.copy(hora = minutos, horaError = null, mensaje = null) }
    }

    /** null = "Sin contacto". */
    fun onContactoChange(contactoId: Long?) {
        if (!puedeEditar) return
        _uiState.update { it.copy(contactoId = contactoId, mensaje = null) }
    }

    fun onResultadoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(resultado = value, resultadoError = null, mensaje = null) }
    }

    /** null = "Sin próxima acción". */
    fun onProximaAccionChange(value: String?) {
        if (!puedeEditar) return
        _uiState.update {
            it.copy(
                proximaAccion = value.orEmpty(),
                // Sin próxima acción tampoco hay fecha de próximo seguimiento.
                fechaProximo = if (value == null) null else it.fechaProximo,
                proximaAccionError = null,
                fechaProximoError = null,
                mensaje = null
            )
        }
    }

    fun onFechaProximoChange(fecha: Long?) {
        if (!puedeEditar) return
        _uiState.update { it.copy(fechaProximo = fecha, fechaProximoError = null, proximaAccionError = null, mensaje = null) }
    }

    fun onObservacionesChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(observaciones = value, mensaje = null) }
    }

    private fun validateForm(): Boolean {
        val s = _uiState.value
        val tipoAccionError = validator.tipoAccionError(s.tipoAccion)
        val fechaError = validator.fechaError(s.fecha)
        val horaError = validator.horaError(s.hora)
        val resultadoError = validator.resultadoError(s.resultado)
        val proximaAccionError = validator.proximaAccionError(s.proximaAccion, s.fechaProximo)
        val fechaProximoError = validator.fechaProximoError(s.proximaAccion, s.fechaProximo, s.fecha)

        _uiState.update {
            it.copy(
                tipoAccionError = tipoAccionError,
                fechaError = fechaError,
                horaError = horaError,
                resultadoError = resultadoError,
                proximaAccionError = proximaAccionError,
                fechaProximoError = fechaProximoError
            )
        }

        return tipoAccionError == null &&
                fechaError == null &&
                horaError == null &&
                resultadoError == null &&
                proximaAccionError == null &&
                fechaProximoError == null
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (!puedeEditar || estadoActual.guardadoExitosamente) return

        if (!validateForm()) return
        // validateForm ya comprobó que no son nulos.
        val fecha = estadoActual.fecha ?: return
        val hora = estadoActual.hora ?: return

        // Bloquea un segundo toque antes de lanzar la corrutina.
        _uiState.update { it.copy(guardando = true, mensaje = null) }

        viewModelScope.launch {
            try {
                val usuarioId = observarSesion().first()?.id
                if (usuarioId == null) {
                    _uiState.update {
                        it.copy(guardando = false, mensaje = "Tu sesión terminó. Vuelve a iniciar sesión.")
                    }
                    return@launch
                }
                val fechaHora = fecha + hora * 60_000L
                repository.registrar(
                    SeguimientoEntity(
                        oportunidadId = estadoActual.oportunidadId,
                        usuarioId = usuarioId,
                        tipoAccion = estadoActual.tipoAccion,
                        fechaHora = fechaHora,
                        resultado = estadoActual.resultado.trim(),
                        contactoId = estadoActual.contactoId,
                        proximaAccion = estadoActual.proximaAccion,
                        fechaProximoSeguimiento = estadoActual.fechaProximo,
                        observaciones = estadoActual.observaciones.trim()
                    )
                )
                val nuevoEstado = estadoActual.estadoSugerido
                if (estadoActual.cambiarEstado && nuevoEstado != null) {
                    cambiarEstado(estadoActual.oportunidadId, usuarioId, nuevoEstado, fechaHora)
                }
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar. Inténtalo nuevamente.")
                }
            }
        }
    }

    /** Actualiza el estado de la oportunidad y deja el cambio registrado en su historial. */
    private suspend fun cambiarEstado(oportunidadId: Long, usuarioId: Long, nuevoEstado: String, fechaHora: Long) {
        val oportunidad = oportunidadRepository.buscar(oportunidadId) ?: return
        if (oportunidad.estado == nuevoEstado) return
        oportunidadRepository.guardar(
            oportunidad.copy(estado = nuevoEstado, ultimaActualizacion = System.currentTimeMillis())
        )
        repository.registrarCambioEstado(oportunidadId, usuarioId, oportunidad.estado, nuevoEstado, fechaHora)
    }

    companion object {
        private const val DIA_MS = 24L * 60 * 60 * 1000

        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                SeguimientoViewModel(
                    container.seguimientoRepository,
                    container.oportunidadRepository,
                    container.eventoRepository,
                    container.contactoRepository,
                    container.observarSesionUseCase,
                    SeguimientoValidator()
                )
            }
        }
    }
}
