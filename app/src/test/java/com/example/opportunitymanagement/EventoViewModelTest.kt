package com.example.opportunitymanagement

import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.utils.FormatoFecha
import com.example.opportunitymanagement.viewmodel.EventoValidator
import com.example.opportunitymanagement.viewmodel.EventoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var organizaciones: FakeOrganizacionDao
    private lateinit var eventos: FakeEventoDao
    private lateinit var vm: EventoViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        organizaciones = FakeOrganizacionDao()
        organizaciones.items.value = listOf(organizacion(3), organizacion(4, "Empresa Demo SpA"))
        eventos = FakeEventoDao(organizaciones)
        vm = EventoViewModel(repositorio(eventos), repositorio(organizaciones), EventoValidator())
    }

    @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    private fun completar() {
        vm.onNombreChange("  Feria de beneficios  ")
        vm.onTipoChange("Feria institucional")
        vm.onFechaChange(FECHA_EVENTO)
        vm.onHoraInicioChange(9 * 60)
        vm.onHoraTerminoChange(18 * 60)
        vm.onPublicoEsperadoChange("Trabajadores y familias")
    }

    @Test fun nuevoDesdeLaOrganizacionLlegaConEllaYSuUbicacion() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        val estado = vm.uiState.value
        assertEquals(3L, estado.organizacionId)
        assertEquals("Puente Alto", estado.comuna)
        assertEquals("Metropolitana", estado.region)
    }

    @Test fun guardaConLaOrganizacionPreseleccionada() = runTest(dispatcher) {
        advanceUntilIdle()
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        completar(); vm.guardar(); advanceUntilIdle()
        val guardado = eventos.items.value.single()
        assertEquals(3L, guardado.organizacionId)
        assertEquals("Feria de beneficios", guardado.nombre)
        assertEquals(FECHA_EVENTO, guardado.fechaInicio)
        assertTrue(vm.uiState.value.guardadoExitosamente)
    }

    @Test fun elegirOrganizacionNoPisaLaUbicacionEscrita() = runTest(dispatcher) {
        vm.cargar(0); advanceUntilIdle()
        vm.onComunaChange("Santiago")
        vm.onOrganizacionChange(3)
        assertEquals("Santiago", vm.uiState.value.comuna)
        assertEquals("Metropolitana", vm.uiState.value.region)
    }

    @Test fun incompletoNoSeGuardaYMarcaLosErrores() = runTest(dispatcher) {
        vm.cargar(0); advanceUntilIdle()
        vm.guardar(); advanceUntilIdle()
        val estado = vm.uiState.value
        assertEquals(0, eventos.guardados)
        assertNotNull(estado.nombreError)
        assertNotNull(estado.fechaError)
        assertNotNull(estado.horaInicioError)
        assertNotNull(estado.organizacionError)
        assertNotNull(estado.publicoEsperadoError)
    }

    @Test fun terminoAntesDelInicioNoSeGuarda() = runTest(dispatcher) {
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        completar(); vm.onHoraTerminoChange(8 * 60); vm.guardar(); advanceUntilIdle()
        assertEquals(0, eventos.guardados)
        assertNotNull(vm.uiState.value.horaTerminoError)
    }

    @Test fun editarConservaElRegistroYSuOrganizacion() = runTest(dispatcher) {
        eventos.items.value = listOf(evento(7, organizacionId = 4))
        vm.cargar(7, organizacionId = 3); advanceUntilIdle()
        assertEquals(4L, vm.uiState.value.organizacionId)
        vm.onNombreChange("Feria renombrada"); vm.guardar(); advanceUntilIdle()
        val guardado = eventos.items.value.single()
        assertEquals(7L, guardado.id)
        assertEquals("Feria renombrada", guardado.nombre)
    }

    @Test fun volverNoDescartaCambiosSinGuardar() = runTest(dispatcher) {
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        vm.onNombreChange("Feria")
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        assertEquals("Feria", vm.uiState.value.nombre)
    }

    @Test fun eventoInexistenteMarcaCargaFallida() = runTest(dispatcher) {
        vm.cargar(99); advanceUntilIdle()
        assertTrue(vm.uiState.value.cargaFallida)
    }

    @Test fun validadorDeHorario() {
        val validator = EventoValidator()
        assertNotNull(validator.horaTerminoError(9 * 60, null))
        assertNotNull(validator.horaTerminoError(9 * 60, 9 * 60))
        assertNull(validator.horaTerminoError(9 * 60, 18 * 60))
        assertNotNull(validator.tipoError("Inventado"))
    }

    @Test fun formatoDeFechaYHoraNoSeCorreUnDia() {
        assertEquals("15/03/2027", FormatoFecha.fecha(FECHA_EVENTO))
        assertEquals("09:30", FormatoFecha.hora(570))
        assertEquals("09:00 – 18:00", FormatoFecha.horario(540, 1080))
    }
}
