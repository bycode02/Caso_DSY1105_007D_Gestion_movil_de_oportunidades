package com.example.opportunitymanagement
import com.example.opportunitymanagement.viewmodel.OportunidadViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.*
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.ui.opportunities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadViewModelTest {
 private val dispatcher = StandardTestDispatcher()
 private lateinit var dao: FakeDao
 private lateinit var vm: OportunidadViewModel
 @Before fun setup() { Dispatchers.setMain(dispatcher); dao = FakeDao(); vm = OportunidadViewModel(OportunidadRepository(dao)) }
 @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
 private fun completar() {
  vm.cambiar("nombre", "  Feria  "); vm.cambiar("tipo", "Feria empresarial")
  vm.cambiar("descripcion", "Prueba"); vm.cambiar("fuente", "Referencia")
  vm.cambiar("prioridad", "Alta"); vm.cambiar("potencial", "Alto")
 }
 @Test fun invalidoNoSeGuarda() = runTest(dispatcher) {
  vm.guardar(); advanceUntilIdle()
  assertEquals(0, dao.guardados); assertTrue(vm.estado.value.errores.containsKey("nombre"))
 }
 @Test fun dobleClickGuardaUnaVez() = runTest(dispatcher) {
  completar(); vm.guardar(); vm.guardar(); advanceUntilIdle()
  assertEquals(1, dao.guardados); assertEquals("Feria", dao.items.value.single().nombre)
  assertTrue(vm.estado.value.guardadoExitosamente)
 }
 @Test fun editarConservaFechaYRegistro() = runTest(dispatcher) {
  dao.items.value = listOf(OportunidadEntity(7, "Anterior", "Feria empresarial", "Desc", "Fuente", "Alta", "Alto", fechaDeteccion=123))
  vm.cargar(7); advanceUntilIdle(); vm.cambiar("nombre", "Modificada"); vm.guardar(); advanceUntilIdle()
  assertEquals(1, dao.items.value.size); assertEquals(123L, dao.items.value.single().fechaDeteccion)
  assertEquals("Modificada", dao.items.value.single().nombre)
 }
 @Test fun errorPermiteReintentar() = runTest(dispatcher) {
  completar(); dao.fallar=true; vm.guardar(); advanceUntilIdle()
  assertFalse(vm.estado.value.guardando); assertFalse(vm.estado.value.guardadoExitosamente)
  assertNotNull(vm.estado.value.mensaje)
  dao.fallar=false; vm.guardar(); advanceUntilIdle(); assertTrue(vm.estado.value.guardadoExitosamente)
 }
 @Test fun identificadorInexistenteNoCreaRegistro() = runTest(dispatcher) {
  vm.cargar(99); advanceUntilIdle(); completar(); vm.guardar(); advanceUntilIdle()
  assertTrue(vm.estado.value.cargaFallida); assertEquals(0, dao.guardados)
 }
 private class FakeDao : OportunidadDao {
  val items = MutableStateFlow<List<OportunidadEntity>>(emptyList())
  var guardados=0; var fallar=false
  override fun observarTodas() = items
  override suspend fun buscar(id: Long) = items.value.find { it.id == id }
  override suspend fun insertar(item: OportunidadEntity): Long { if(fallar) error("disco"); guardados++; items.value=items.value+item.copy(id=1); return 1 }
  override suspend fun actualizar(item: OportunidadEntity): Int { if(fallar) error("disco"); if(items.value.none { it.id==item.id }) return 0; guardados++; items.value=items.value.map { if(it.id==item.id) item else it }; return 1 }
 }
}
