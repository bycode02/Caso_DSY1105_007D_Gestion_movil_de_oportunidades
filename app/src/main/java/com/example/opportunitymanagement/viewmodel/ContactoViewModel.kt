package com.example.opportunitymanagement.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.di.appContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactoViewModel(
    private val repository: ContactoRepository,
    organizacionRepository: OrganizacionRepository,
    private val validator: ContactoValidator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactoUiState())
    val uiState: StateFlow<ContactoUiState> = _uiState.asStateFlow()

    // Última carga pedida (id del contacto y organización preseleccionada).
    private var cargaSolicitada: Pair<Long, Long?>? = null

    init {
        // Llena el desplegable y lo mantiene al día si se crea o borra una organización.
        viewModelScope.launch {
            organizacionRepository.organizaciones
                .catch { _uiState.update { it.copy(mensaje = "No se pudieron cargar las organizaciones") } }
                .collect { lista -> _uiState.update { it.copy(organizaciones = lista) } }
        }
    }

    // No se permite escribir mientras se guarda, se carga o si la carga falló.
    private val puedeEditar: Boolean
        get() = _uiState.value.let { !it.guardando && !it.cargando && !it.cargaFallida }

    fun onNombreChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(nombre = value, nombreError = null, mensaje = null) }
    }

    // Recibe el id de la organización elegida en el desplegable, no su nombre.
    fun onOrganizacionChange(organizacionId: Long) {
        if (!puedeEditar) return
        _uiState.update { it.copy(organizacionId = organizacionId, organizacionError = null, mensaje = null) }
    }

    fun onCargoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(cargo = value, cargoError = null, mensaje = null) }
    }

    fun onAreaChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(area = value, areaError = null, mensaje = null) }
    }

    fun onCorreoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(correo = value, correoError = null, mensaje = null) }
    }

    fun onTelefonoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(telefono = value, telefonoError = null, mensaje = null) }
    }

    fun onMedioPreferenteChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(medioPreferente = value, medioPreferenteError = null, mensaje = null) }
    }

    fun onEstadoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(estado = value, estadoError = null, mensaje = null) }
    }

    fun onObservacionesChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(observaciones = value, mensaje = null) }
    }

    /**
     * @param organizacionId organización ya elegida al crear un contacto desde su organización.
     * Al editar (id != 0) se ignora: manda la organización guardada en el contacto.
     */
    fun cargar(id: Long, organizacionId: Long? = null) {
        // La pantalla vuelve a llamar a cargar() al recomponerse; solo se carga una vez.
        if (cargaSolicitada == id to organizacionId) return
        cargaSolicitada = id to organizacionId

        // Se usa update y no un ContactoUiState nuevo, para no perder la lista de organizaciones.
        _uiState.update {
            ContactoUiState(
                id = id,
                cargando = id != 0L,
                organizacionId = if (id == 0L) organizacionId?.takeIf { org -> org > 0L } else null,
                organizaciones = it.organizaciones
            )
        }
        if (id == 0L) return

        viewModelScope.launch {
            try {
                val contacto = repository.buscar(id)

                if (contacto == null) {
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            cargaFallida = true,
                            mensaje = "No existe un contacto con ese código"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        id = contacto.id,
                        nombre = contacto.nombre,
                        organizacionId = contacto.organizacionId,
                        cargo = contacto.cargo,
                        area = contacto.area,
                        correo = contacto.correo,
                        telefono = contacto.telefono,
                        medioPreferente = contacto.medioPreferente,
                        estado = contacto.estado,
                        fechaUltimoContacto = contacto.fechaUltimoContacto,
                        observaciones = contacto.observaciones,
                        cargando = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        cargaFallida = true,
                        mensaje = "No se pudo abrir el contacto"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val nombreError = validator.nombreError(state.nombre)
        val organizacionError = validator.organizacionError(state.organizacionId)
        val cargoError = validator.cargoError(state.cargo)
        val areaError = validator.areaError(state.area)
        val correoError = validator.correoError(state.correo)
        val telefonoError = validator.telefonoError(state.telefono)
        val medioPreferenteError = validator.medioPreferenteError(state.medioPreferente)
        val estadoError = validator.estadoError(state.estado)

        _uiState.update {
            it.copy(
                nombreError = nombreError,
                organizacionError = organizacionError,
                cargoError = cargoError,
                areaError = areaError,
                correoError = correoError,
                telefonoError = telefonoError,
                medioPreferenteError = medioPreferenteError,
                estadoError = estadoError
            )
        }

        return nombreError == null &&
                organizacionError == null &&
                cargoError == null &&
                areaError == null &&
                correoError == null &&
                telefonoError == null &&
                medioPreferenteError == null &&
                estadoError == null
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (!puedeEditar || estadoActual.guardadoExitosamente) return

        if (!validateForm()) return
        // validateForm ya comprobó que hay una organización elegida.
        val organizacionId = estadoActual.organizacionId ?: return

        // Bloquea un segundo toque antes de lanzar la corrutina.
        _uiState.update { it.copy(guardando = true, mensaje = null) }

        viewModelScope.launch {
            try {
                repository.guardar(
                    ContactoEntity(
                        id = estadoActual.id,
                        nombre = estadoActual.nombre.trim(),
                        organizacionId = organizacionId,
                        cargo = estadoActual.cargo.trim(),
                        area = estadoActual.area,
                        correo = estadoActual.correo.trim(),
                        telefono = estadoActual.telefono.trim(),
                        medioPreferente = estadoActual.medioPreferente,
                        estado = estadoActual.estado,
                        fechaUltimoContacto = estadoActual.fechaUltimoContacto,
                        observaciones = estadoActual.observaciones.trim()
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw exception
            } catch (exception: IllegalStateException) {
                // Lanzada por el repositorio si el contacto se borró mientras se editaba.
                _uiState.update {
                    it.copy(guardando = false, mensaje = exception.message ?: "El contacto ya no existe")
                }
            } catch (exception: Exception) {
                // Por ejemplo, si la organización elegida se eliminó mientras tanto.
                _uiState.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar. Inténtalo nuevamente.")
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ContactoViewModel(
                    container.contactoRepository,
                    container.organizacionRepository,
                    ContactoValidator()
                )
            }
        }
    }
}