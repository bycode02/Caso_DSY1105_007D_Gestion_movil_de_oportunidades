package com.example.opportunitymanagement.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OportunidadViewModel(private val repository: OportunidadRepository) : ViewModel() {
 val lista = repository.oportunidades.map { ListaUiState(it, false) }
  .catch { emit(ListaUiState(cargando = false, error = "No se pudieron cargar las oportunidades")) }
  .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListaUiState())
 private val _estado = MutableStateFlow(OportunidadUiState())
 val estado = _estado.asStateFlow()
 private var original: OportunidadEntity? = null

 fun cargar(id: Long) {
  _estado.value = OportunidadUiState(id = id, cargando = id != 0L)
  original = null
  if (id == 0L) return
  viewModelScope.launch {
   try {
    val o = repository.buscar(id)
    original = o
    _estado.value = if (o == null) OportunidadUiState(id = id, cargaFallida = true, mensaje = "No existe una oportunidad con ese código")
    else OportunidadUiState(o.id, o.nombre, o.tipo, o.descripcion, o.fuente, o.prioridad, o.potencial, o.estado, o.observaciones, o.referenciaGeografica)
   } catch (e: CancellationException) { throw e }
   catch (e: Exception) { _estado.update { it.copy(cargando = false, cargaFallida = true, mensaje = "No se pudo abrir el registro") } }
  }
 }

 fun cambiar(campo: String, valor: String) {
  if (_estado.value.guardando) return
  _estado.update { s ->
   val n = when(campo) {
    "nombre" -> s.copy(nombre = valor); "tipo" -> s.copy(tipo = valor)
    "descripcion" -> s.copy(descripcion = valor); "fuente" -> s.copy(fuente = valor)
    "prioridad" -> s.copy(prioridad = valor); "potencial" -> s.copy(potencial = valor)
    "estado" -> s.copy(estado = valor); "observaciones" -> s.copy(observaciones = valor)
    "referenciaGeografica" -> s.copy(referenciaGeografica = valor); else -> s
   }
   n.copy(errores = s.errores - campo, mensaje = null)
  }
 }

 fun guardar() {
  val s = _estado.value
  if (s.guardando || s.cargando || s.cargaFallida || s.guardadoExitosamente) return
  val errores = validar(s)
  _estado.update { it.copy(errores = errores) }
  if (errores.isNotEmpty()) return
  _estado.update { it.copy(guardando = true, mensaje = null) }
  viewModelScope.launch {
   try {
    repository.guardar(OportunidadEntity(s.id, s.nombre.trim(), s.tipo, s.descripcion.trim(), s.fuente.trim(), s.prioridad, s.potencial, s.estado, s.observaciones.trim(), fechaDeteccion = original?.fechaDeteccion ?: System.currentTimeMillis(), referenciaGeografica = s.referenciaGeografica))
    _estado.update { it.copy(guardando = false, guardadoExitosamente = true) }
   } catch (e: CancellationException) { throw e }
   catch (e: Exception) { _estado.update { it.copy(guardando = false, mensaje = "No se pudo guardar. Inténtalo nuevamente.") } }
  }
 }

}
