package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.OportunidadEntity
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.di.appContainer
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.domain.usecase.ObservarSesionUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OportunidadViewModel(
    private val repository: OportunidadRepository,
    private val validator: OportunidadValidator,
    // Usuario con sesión iniciada: será el responsable de las oportunidades nuevas.
    private val observarSesion: ObservarSesionUseCase,
    // Eventos para el desplegable "Evento" del formulario.
    eventoRepository: EventoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OportunidadUiState())
    val uiState: StateFlow<OportunidadUiState> = _uiState.asStateFlow()

    init {
        // Mantiene al día el desplegable si se crea o modifica un evento.
        viewModelScope.launch {
            eventoRepository.eventos
                .catch { _uiState.update { it.copy(mensaje = "No se pudieron cargar los eventos") } }
                .collect { lista -> _uiState.update { it.copy(eventos = lista) } }
        }
    }

    /** Evento elegido en el desplegable (obligatorio para guardar). */
    fun onEventoChange(eventoId: Long?) {
        if (!puedeEditar) return
        _uiState.update { it.copy(eventoId = eventoId, eventoError = null, mensaje = null) }
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
        _uiState.update { it.copy(descripcion = value, descripcionError = null, mensaje = null) }
    }

    fun onFuenteChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(fuente = value, fuenteError = null, mensaje = null) }
    }

    fun onPrioridadChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(prioridad = value, prioridadError = null, mensaje = null) }
    }

    fun onPotencialChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(potencial = value, potencialError = null, mensaje = null) }
    }

    fun onEstadoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(estado = value, estadoError = null, mensaje = null) }
    }

    fun onObservacionesChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(observaciones = value, mensaje = null) }
    }

    fun onReferenciaGeograficaChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(referenciaGeografica = value, mensaje = null) }
    }

    fun cargar(id: Long) {
        _uiState.value = OportunidadUiState(eventos = _uiState.value.eventos, id = id, cargando = id != 0L)
        if (id == 0L) return

        viewModelScope.launch {
            try {
                val oportunidad = repository.buscar(id)

                if (oportunidad == null) {
                    _uiState.value = OportunidadUiState(eventos = _uiState.value.eventos,
                        id = id,
                        cargaFallida = true,
                        mensaje = "No existe una oportunidad con ese código"
                    )
                    return@launch
                }

                _uiState.value = OportunidadUiState(eventos = _uiState.value.eventos,
                    id = oportunidad.id,
                    nombre = oportunidad.nombre,
                    tipo = oportunidad.tipo,
                    descripcion = oportunidad.descripcion,
                    fuente = oportunidad.fuente,
                    prioridad = oportunidad.prioridad,
                    potencial = oportunidad.potencial,
                    estado = oportunidad.estado,
                    observaciones = oportunidad.observaciones,
                    referenciaGeografica = oportunidad.referenciaGeografica,
                    fechaDeteccion = oportunidad.fechaDeteccion,
                    responsableId = oportunidad.responsableId,
                    eventoId = oportunidad.eventoId
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        cargaFallida = true,
                        mensaje = "No se pudo abrir el registro"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val nombreError = validator.nombreError(state.nombre)
        val tipoError = validator.tipoError(state.tipo)
        val descripcionError = validator.descripcionError(state.descripcion)
        val fuenteError = validator.fuenteError(state.fuente)
        val prioridadError = validator.prioridadError(state.prioridad)
        val potencialError = validator.potencialError(state.potencial)
        val estadoError = validator.estadoError(state.estado)
        val eventoError = validator.eventoError(state.eventoId)

        _uiState.update {
            it.copy(
                nombreError = nombreError,
                tipoError = tipoError,
                descripcionError = descripcionError,
                fuenteError = fuenteError,
                prioridadError = prioridadError,
                potencialError = potencialError,
                estadoError = estadoError,
                eventoError = eventoError
            )
        }

        return nombreError == null &&
                tipoError == null &&
                descripcionError == null &&
                fuenteError == null &&
                prioridadError == null &&
                potencialError == null &&
                estadoError == null &&
                eventoError == null
    }


    fun guardar() {
        val estadoActual = _uiState.value
        if (!puedeEditar || estadoActual.guardadoExitosamente) return

        if (!validateForm()) return

        // Momento exacto del primer guardado; al editar se conserva la fecha cargada.
        val fechaDeteccion = estadoActual.fechaDeteccion ?: System.currentTimeMillis()

        // Bloquea un segundo toque antes de lanzar la corrutina.
        _uiState.update {
            it.copy(guardando = true, mensaje = null, fechaDeteccion = fechaDeteccion)
        }

        viewModelScope.launch {
            try {
                // Al editar se conserva el responsable; una oportunidad nueva
                // (o una antigua sin responsable) queda a cargo de quien la guarda.
                val responsableId = estadoActual.responsableId ?: observarSesion().first()?.id
                if (responsableId == null) {
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = "Tu sesión terminó. Vuelve a iniciar sesión."
                        )
                    }
                    return@launch
                }
                repository.guardar(
                    OportunidadEntity(
                        id = estadoActual.id,
                        nombre = estadoActual.nombre.trim(),
                        tipo = estadoActual.tipo,
                        descripcion = estadoActual.descripcion.trim(),
                        fuente = estadoActual.fuente.trim(),
                        prioridad = estadoActual.prioridad,
                        potencial = estadoActual.potencial,
                        estado = estadoActual.estado,
                        observaciones = estadoActual.observaciones.trim(),
                        fechaDeteccion = fechaDeteccion,
                        responsableId = responsableId,
                        referenciaGeografica = estadoActual.referenciaGeografica,
                        eventoId = estadoActual.eventoId
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        mensaje = "No se pudo guardar. Inténtalo nuevamente."
                    )
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                OportunidadViewModel(
                    container.oportunidadRepository,
                    OportunidadValidator(),
                    container.observarSesionUseCase,
                    container.eventoRepository
                )
            }
        }
    }
}
