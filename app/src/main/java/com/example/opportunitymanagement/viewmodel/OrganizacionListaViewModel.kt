package com.example.opportunitymanagement.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.repository.OrganizacionConContactosException
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
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

class OrganizacionListaViewModel(
    private val repository: OrganizacionRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)

    // Evita borrar dos veces con un doble toque.
    private var eliminando = false

    private val items = repository.organizaciones
        .map { OrganizacionListaUiState(items = it, cargando = false) }
        .catch {
            emit(OrganizacionListaUiState(cargando = false, error = "No se pudieron cargar las organizaciones"))
        }

    val uiState: StateFlow<OrganizacionListaUiState> = combine(items, mensaje) { lista, m ->
        lista.copy(mensaje = m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrganizacionListaUiState())

    fun eliminar(id: Long) {
        if (eliminando) return
        eliminando = true
        mensaje.value = null

        viewModelScope.launch {
            try {
                repository.eliminar(id)
                // Room vuelve a emitir la lista sin la organización.
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: OrganizacionConContactosException) {
                // Debe ir antes que IllegalStateException, porque hereda de ella.
                mensaje.value = "No se puede eliminar: la organización tiene contactos. Elimínalos primero."
            } catch (exception: IllegalStateException) {
                mensaje.value = "La organización ya no existe"
            } catch (exception: Exception) {
                mensaje.value = "No se pudo eliminar. Inténtalo nuevamente."
            } finally {
                eliminando = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { OrganizacionListaViewModel(appContainer().organizacionRepository) }
        }
    }
}