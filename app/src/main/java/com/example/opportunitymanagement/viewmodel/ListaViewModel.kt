package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.di.appContainer
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.utils.FormatoFecha
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ListaViewModel(
    private val repository: OportunidadRepository,
    // Fecha de hoy para el filtro de fechas; se reemplaza en las pruebas.
    private val hoyUtc: () -> Long = FormatoFecha::hoyUtc
) : ViewModel() {

    // Estado de la acción de eliminar; los items siguen viniendo de Room.
    private val eliminandoId = MutableStateFlow<Long?>(null)
    private val mensaje = MutableStateFlow<String?>(null)
    private val filtros = MutableStateFlow(FiltrosOportunidad())

    private val items = repository.oportunidades
        .map { ListaUiState(items = it, cargando = false) }
        .catch {
            emit(ListaUiState(cargando = false, error = "No se pudieron cargar las oportunidades"))
        }

    // Se recalcula sola cuando cambian los datos de Room o los filtros elegidos.
    val uiState: StateFlow<ListaUiState> = combine(items, eliminandoId, mensaje, filtros) { lista, id, m, f ->
        val todas = lista.items
        lista.copy(
            items = FiltroOportunidades.aplicar(todas, f, hoyUtc()),
            total = todas.size,
            filtros = f,
            opcionesOrganizacion = FiltroOportunidades.organizaciones(todas),
            opcionesComuna = FiltroOportunidades.comunas(todas),
            opcionesRegion = FiltroOportunidades.regiones(todas),
            opcionesResponsable = FiltroOportunidades.responsables(todas),
            eliminandoId = id,
            mensaje = m
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListaUiState())

    /** Texto del buscador (nombre de la oportunidad o de su evento). */
    fun onBuscar(texto: String) = filtros.update { it.copy(texto = texto) }

    /** Filtros elegidos en la pantalla (estado, tipo, prioridad, organización, ubicación, fecha...). */
    fun onFiltrosChange(nuevos: FiltrosOportunidad) {
        // El texto lo maneja onBuscar: se conserva el último escrito aunque la copia
        // que armó la pantalla venga de un estado anterior.
        filtros.update { actuales -> nuevos.copy(texto = actuales.texto) }
    }

    fun limpiarFiltros() {
        filtros.value = FiltrosOportunidad()
    }

    fun eliminar(id: Long) {
        // Bloquea un segundo toque antes de lanzar la corrutina.
        if (eliminandoId.value != null) return
        eliminandoId.value = id
        mensaje.value = null

        viewModelScope.launch {
            try {
                repository.eliminar(id)
                // No hace falta refrescar: Room vuelve a emitir la lista sin el registro.
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalStateException) {
                mensaje.value = "La oportunidad ya no existe"
            } catch (exception: Exception) {
                mensaje.value = "No se pudo eliminar. Inténtalo nuevamente."
            } finally {
                eliminandoId.update { if (it == id) null else it }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ListaViewModel(appContainer().oportunidadRepository) }
        }

    }
}
