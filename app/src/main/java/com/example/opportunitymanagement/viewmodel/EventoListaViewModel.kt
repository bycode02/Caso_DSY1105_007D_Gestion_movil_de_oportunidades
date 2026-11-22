package com.example.opportunitymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.repository.EventoConOportunidadesException
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.di.appContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventoListaViewModel(
    private val repository: EventoRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)

    // Evita borrar dos veces con un doble toque.
    private var eliminando = false

    // Cada item trae el nombre de su organización (JOIN en EventoDao).
    private val items = repository.eventos
        .map { EventoListaUiState(items = it, cargando = false) }
        .catch { emit(EventoListaUiState(cargando = false, error = "No se pudieron cargar los eventos")) }

    val uiState: StateFlow<EventoListaUiState> = combine(items, mensaje) { lista, m ->
        lista.copy(mensaje = m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventoListaUiState())

    fun eliminar(id: Long) {
        if (eliminando) return
        eliminando = true
        mensaje.value = null

        viewModelScope.launch {
            try {
                repository.eliminar(id)
                // Room vuelve a emitir la lista sin el evento.
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: EventoConOportunidadesException) {
                // Debe ir antes que IllegalStateException, porque hereda de ella.
                mensaje.value = "No se puede eliminar: el evento tiene oportunidades asociadas."
            } catch (exception: IllegalStateException) {
                mensaje.value = "El evento ya no existe"
            } catch (exception: Exception) {
                mensaje.value = "No se pudo eliminar. Inténtalo nuevamente."
            } finally {
                eliminando = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { EventoListaViewModel(appContainer().eventoRepository) }
        }
    }
}
