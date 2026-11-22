package com.example.opportunitymanagement
import com.example.opportunitymanagement.viewmodel.OportunidadValidator
import com.example.opportunitymanagement.viewmodel.OportunidadViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.*
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.SessionRepository
import com.example.opportunitymanagement.domain.usecase.ObservarSesionUseCase
import com.example.opportunitymanagement.ui.opportunities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class OportunidadViewModelTest {
 private val dispatcher = StandardTestDispatcher()
 private lateinit var dao: FakeDao
 private lateinit var vm: OportunidadViewModel
 private lateinit var sesion: FakeSesionDao
 private lateinit var organizaciones: FakeOrganizacionDao
 private lateinit var eventos: FakeEventoDao
 @Before fun setup() { Dispatchers.setMain(dispatcher); dao = FakeDao(); sesion = FakeSesionDao(USUARIO_SESION); organizaciones = FakeOrganizacionDao(); organizaciones.items.value = listOf(organizacion(3)); eventos = FakeEventoDao(organizaciones); eventos.items.value = listOf(evento(7, organizacionId = 3)); vm = OportunidadViewModel(OportunidadRepository(dao), OportunidadValidator(), ObservarSesionUseCase(SessionRepository(sesion)), EventoRepository(eventos)) }
 @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }
 private fun completar() {
  vm.onNombreChange("  Feria  "); vm.onTipoChange("Feria empresarial")
  vm.onDescripcionChange("Prueba"); vm.onFuenteChange("Referencia")
  vm.onPrioridadChange("Alta"); vm.onPotencialChange("Alto")
  vm.onEventoChange(7)
 }
 @Test fun invalidoNoSeGuarda() = runTest(dispatcher) {
  vm.guardar(); advanceUntilIdle()
  assertEquals(0, dao.guardados); assertTrue(vm.uiState.value.nombreError != null)
 }
 @Test fun escribirLimpiaElErrorDelCampo() = runTest(dispatcher) {
  vm.guardar(); advanceUntilIdle(); vm.onNombreChange("Feria")
  assertNull(vm.uiState.value.nombreError); assertNotNull(vm.uiState.value.descripcionError)
 }
 @Test fun dobleClickGuardaUnaVez() = runTest(dispatcher) {
  completar(); vm.guardar(); vm.guardar(); advanceUntilIdle()
  assertEquals(1, dao.guardados); assertEquals("Feria", dao.items.value.single().nombre)
  assertTrue(vm.uiState.value.guardadoExitosamente)
 }
 @Test fun editarConservaFechaYRegistro() = runTest(dispatcher) {
  dao.items.value = listOf(OportunidadEntity(7, "Anterior", "Feria empresarial", "Desc", "Fuente", "Alta", "Alto", fechaDeteccion=123, eventoId = 7))
  vm.cargar(7); advanceUntilIdle(); vm.onNombreChange("Modificada"); vm.guardar(); advanceUntilIdle()
  assertEquals(1, dao.items.value.size); assertEquals(123L, dao.items.value.single().fechaDeteccion)
  assertEquals("Modificada", dao.items.value.single().nombre)
 }
 @Test fun nuevaOportunidadQuedaACargoDelUsuarioDeLaSesion() = runTest(dispatcher) {
  completar(); vm.guardar(); advanceUntilIdle()
  assertEquals(USUARIO_SESION, dao.items.value.single().responsableId)
 }
 @Test fun editarConservaElResponsableOriginal() = runTest(dispatcher) {
  dao.items.value = listOf(OportunidadEntity(7, "Anterior", "Feria empresarial", "Desc", "Fuente", "Alta", "Alto", responsableId = 5, eventoId = 7))
  vm.cargar(7); advanceUntilIdle(); vm.onNombreChange("Modificada"); vm.guardar(); advanceUntilIdle()
  assertEquals(5L, dao.items.value.single().responsableId)
 }
 @Test fun editarUnaSinResponsableLaAsignaAlUsuarioDeLaSesion() = runTest(dispatcher) {
  dao.items.value = listOf(OportunidadEntity(7, "Antigua", "Feria empresarial", "Desc", "Fuente", "Alta", "Alto", responsableId = null, eventoId = 7))
  vm.cargar(7); advanceUntilIdle(); vm.guardar(); advanceUntilIdle()
  assertEquals(USUARIO_SESION, dao.items.value.single().responsableId)
 }
 @Test fun sinSesionNoGuardaYPideIniciarSesion() = runTest(dispatcher) {
  sesion.usuario.value = null
  completar(); vm.guardar(); advanceUntilIdle()
  assertEquals(0, dao.guardados); assertFalse(vm.uiState.value.guardando)
  assertEquals("Tu sesión terminó. Vuelve a iniciar sesión.", vm.uiState.value.mensaje)
 }
 @Test fun errorPermiteReintentar() = runTest(dispatcher) {
  completar(); dao.fallar=true; vm.guardar(); advanceUntilIdle()
  assertFalse(vm.uiState.value.guardando); assertFalse(vm.uiState.value.guardadoExitosamente)
  assertNotNull(vm.uiState.value.mensaje)
  dao.fallar=false; vm.guardar(); advanceUntilIdle(); assertTrue(vm.uiState.value.guardadoExitosamente)
 }
 @Test fun identificadorInexistenteNoCreaRegistro() = runTest(dispatcher) {
  vm.cargar(99); advanceUntilIdle(); completar(); vm.guardar(); advanceUntilIdle()
  assertTrue(vm.uiState.value.cargaFallida); assertEquals(0, dao.guardados)
 }
 @Test fun losEventosLleganAlDesplegable() = runTest(dispatcher) {
  advanceUntilIdle()
  assertEquals(listOf("Feria de beneficios"), vm.uiState.value.eventos.map { it.evento.nombre })
 }
 @Test fun guardaElEventoElegido() = runTest(dispatcher) {
  completar(); vm.onEventoChange(7); vm.guardar(); advanceUntilIdle()
  assertEquals(7L, dao.items.value.single().eventoId)
 }
 @Test fun sinEventoNoSeGuardaYMarcaElError() = runTest(dispatcher) {
  completar(); vm.onEventoChange(null); vm.guardar(); advanceUntilIdle()
  assertEquals(0, dao.guardados); assertNotNull(vm.uiState.value.eventoError)
 }
 @Test fun elegirEventoLimpiaElError() = runTest(dispatcher) {
  completar(); vm.onEventoChange(null); vm.guardar(); advanceUntilIdle()
  vm.onEventoChange(7)
  assertNull(vm.uiState.value.eventoError)
 }
 @Test fun cargarConservaElEventoYLaListaDeEventos() = runTest(dispatcher) {
  dao.items.value = listOf(OportunidadEntity(7, "Anterior", "Feria empresarial", "Desc", "Fuente", "Alta", "Alto", eventoId = 7))
  advanceUntilIdle(); vm.cargar(7); advanceUntilIdle()
  assertEquals(7L, vm.uiState.value.eventoId)
  assertEquals(1, vm.uiState.value.eventos.size)
 }
 private class FakeDao : OportunidadDao {
  val items = MutableStateFlow<List<OportunidadEntity>>(emptyList())
  var guardados=0; var fallar=false
  override fun observarTodas() = items.map { lista -> lista.map { OportunidadConResponsable(it, responsableNombre = null) } }
  override suspend fun buscar(id: Long) = items.value.find { it.id == id }
  override suspend fun insertar(item: OportunidadEntity): Long { if(fallar) error("disco"); guardados++; items.value=items.value+item.copy(id=1); return 1 }
  override suspend fun actualizar(item: OportunidadEntity): Int { if(fallar) error("disco"); if(items.value.none { it.id==item.id }) return 0; guardados++; items.value=items.value.map { if(it.id==item.id) item else it }; return 1 }
  override suspend fun eliminar(id: Long): Int { val antes=items.value.size; items.value=items.value.filterNot { it.id==id }; return antes-items.value.size }
 }
 private class FakeSesionDao(id: Long) : SesionDao {
  val usuario = MutableStateFlow<UsuarioEntity?>(UsuarioEntity(id, "Vendedor", "v@ejemplo.cl", Role.VENDEDOR, true, 0, 0))
  override fun usuarioActual() = usuario
  override suspend fun guardar(sesion: SesionEntity) = error("no se usa")
  override suspend fun cerrar() { usuario.value = null }
 }
 private companion object { const val USUARIO_SESION = 42L }
}
