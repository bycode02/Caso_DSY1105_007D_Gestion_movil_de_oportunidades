package com.example.opportunitymanagement.viewmodel

import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import com.example.opportunitymanagement.di.appContainer
import kotlinx.coroutines.CancellationException

class OrganizacioneViewModel(
    private val repository: OrganizacionRepository,
    private val validator: OrganizacionValidator
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizacionUiState())
    val uiState: StateFlow<OrganizacionUiState> = _uiState.asStateFlow()

    private val puedeEditar : Boolean
        get() = _uiState.value.let { !it.guardando && !it.cargando && !it.cargaFallida }


    fun onNombreChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(nombre = value, nombreError = null, mensaje = null) }
    }

    fun onTipoChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(tipo = value, tipoError = null, mensaje = null) }
    }

    fun onRubroChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(rubro = value, rubroError = null, mensaje = null) }
    }

    fun onComunaChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(comuna = value, comunaError = null, mensaje = null) }
    }

    fun onRegionChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(region = value, regionError = null, mensaje = null) }
    }

    fun onSitioWebChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(sitioWeb = value, sitioWebError = null, mensaje = null) }
    }

    fun onObservacionesChange(value: String) {
        if (!puedeEditar) return
        _uiState.update { it.copy(observaciones = value, mensaje = null) }
    }

    fun cargar(id:Long){

        _uiState.value = OrganizacionUiState(id = id , cargando = id != 0L)

        if(id == 0L)return

        viewModelScope.launch{
            try {
                val organizacion = repository.buscar(id)
                if(organizacion == null){
                    _uiState.value = OrganizacionUiState(
                        id = id,
                        cargaFallida = true,
                        mensaje = "No existe una ortganizacion con el id ${id}"
                    )
                    return@launch
                }
               _uiState.value = OrganizacionUiState(
                   id = organizacion.id,
                   nombre = organizacion.nombre,
                   tipo = organizacion.tipo,
                   rubro = organizacion.rubro,
                   comuna = organizacion.comuna,
                   region = organizacion.region,
                   sitioWeb = organizacion.sitioWeb,
                   observaciones = organizacion.observaciones
               )

            }catch(exeption: CancellationException){
                throw  exeption
            }catch (exeption : Exception){
                _uiState.update {  it.copy(
                    id = id,
                    cargaFallida = true,
                    mensaje = "No se pudo abrir la organizacion"
                )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value

        val nombreError = validator.nombreError(state.nombre)
        val tipoError = validator.tipoError(state.tipo)
        val rubroError = validator.rubroError(state.rubro)
        val comunaError = validator.comunaError(state.comuna)
        val regionError = validator.regionError(state.region)
        val sitioWebError = validator.sitioWebError(state.sitioWeb)

        _uiState.update {
            it.copy(
                nombreError = nombreError,
                tipoError = tipoError,
                rubroError = rubroError,
                comunaError = comunaError,
                regionError = regionError,
                sitioWebError = sitioWebError
            )
        }

        return nombreError == null &&
                tipoError == null &&
                rubroError == null &&
                comunaError == null &&
                regionError == null &&
                sitioWebError == null
    }

    fun guardar() {
        val estadoActual = _uiState.value
        if (!puedeEditar || estadoActual.guardadoExitosamente) return

        if (!validateForm()) return

        // Bloquea un segundo toque antes de lanzar la corrutina.
        _uiState.update { it.copy(guardando = true, mensaje = null) }

        viewModelScope.launch {
            try {
                repository.guardar(
                    OrganizacionEntity(
                        id = estadoActual.id,
                        nombre = estadoActual.nombre.trim(),
                        tipo = estadoActual.tipo,
                        rubro = estadoActual.rubro.trim(),
                        comuna = estadoActual.comuna.trim(),
                        region = estadoActual.region.trim(),
                        sitioWeb = estadoActual.sitioWeb.trim(),
                        observaciones = estadoActual.observaciones.trim()
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoExitosamente = true) }
            } catch (exception: CancellationException) {
                _uiState.update { it.copy(guardando = false) }
                throw exception
            } catch (exception: IllegalStateException) {
                // Lanzada por el repositorio si la organización se borró mientras se editaba.
                _uiState.update {
                    it.copy(guardando = false, mensaje = exception.message ?: "La organización ya no existe")
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(guardando = false, mensaje = "No se pudo guardar. Inténtalo nuevamente.")
                }
            }
        }
    }

    companion object{
        val Factory = viewModelFactory {
            initializer {
                OrganizacioneViewModel(
                    appContainer().organizacionRepository,
                    OrganizacionValidator()
                )
            }
        }
    }


}