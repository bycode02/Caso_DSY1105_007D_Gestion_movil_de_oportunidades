package com.example.opportunitymanagement.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.opportunitymanagement.data.repository.ContactoRepository
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

class ContactoListaViewModel(
    private val repository: ContactoRepository
) : ViewModel() {

    private val mensaje = MutableStateFlow<String?>(null)

    // Evita borrar dos veces con un doble toque.
    private var eliminando = false

    // Cada item trae el nombre de su organización (JOIN en ContactoDao).
    private val items = repository.contactos
        .map { ContactoListaUiState(items = it, cargando = false) }
        .catch {
            emit(ContactoListaUiState(cargando = false, error = "No se pudieron cargar los contactos"))
        }

    val uiState: StateFlow<ContactoListaUiState> = combine(items, mensaje) { lista, m ->
        lista.copy(mensaje = m)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactoListaUiState())

    fun eliminar(id: Long) {
        if (eliminando) return
        eliminando = true
        mensaje.value = null

        viewModelScope.launch {
            try {
                repository.eliminar(id)
                // Room vuelve a emitir la lista sin el contacto.
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalStateException) {
                mensaje.value = "El contacto ya no existe"
            } catch (exception: Exception) {
                mensaje.value = "No se pudo eliminar. Inténtalo nuevamente."
            } finally {
                eliminando = false
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ContactoListaViewModel(appContainer().contactoRepository) }
        }
    }
}