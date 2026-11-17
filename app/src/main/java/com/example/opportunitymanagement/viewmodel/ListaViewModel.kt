package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.di.appContainer
import com.example.opportunitymanagement.data.repository.OportunidadRepository
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
    private val repository: OportunidadRepository
) : ViewModel() {

    // Estado de la acción de eliminar; los items siguen viniendo de Room.
    private val eliminandoId = MutableStateFlow<Long?>(null)
    private val mensaje = MutableStateFlow<String?>(null)

    private val items = repository.oportunidades
        .map { ListaUiState(items = it, cargando = false) }
        .catch {
            emit(ListaUiState(cargando = false, error = "No se pudieron cargar las oportunidades"))
        }

    val uiState: StateFlow<ListaUiState> = combine(items, eliminandoId, mensaje) { lista, id, m ->
        lista.copy(eliminandoId = id, mensaje = m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListaUiState())

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
