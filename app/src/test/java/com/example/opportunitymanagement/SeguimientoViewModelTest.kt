package com.example.opportunitymanagement

import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.SeguimientoRepository
import com.example.opportunitymanagement.data.repository.SessionRepository
import com.example.opportunitymanagement.domain.usecase.ObservarSesionUseCase
import com.example.opportunitymanagement.utils.FormatoFecha
import com.example.opportunitymanagement.viewmodel.Opciones
import com.example.opportunitymanagement.viewmodel.SeguimientoValidator
import com.example.opportunitymanagement.viewmodel.SeguimientoViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeguimientoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val dia = 24L * 60 * 60 * 1000
    // 15/03/2027 a las 10:30 (hora de reloj).
    private val ahora = FECHA_EVENTO + 630 * 60_000L

    private lateinit var oportunidades: FakeOportunidadDao
    private lateinit var seguimientos: FakeSeguimientoDao
    private lateinit var sesion: FakeSesion
    private lateinit var servidor: FakeSeguimientoApi
    private var sincronizaciones = 0
    private lateinit var vm: SeguimientoViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        val organizaciones = FakeOrganizacionDao().apply {
            items.value = listOf(organizacion(3), organizacion(4, "Empresa Demo SpA"))
        }
        val contactos = FakeContactoDao(organizaciones).apply {
            items.value = listOf(contacto(5, organizacionId = 3), contacto(6, organizacionId = 4, nombre = "Pedro Rojas"))
        }
        val eventos = FakeEventoDao(organizaciones).apply { items.value = listOf(evento(7, organizacionId = 3)) }
        oportunidades = FakeOportunidadDao().apply { items.value = listOf(oportunidadDePrueba(1)) }
        seguimientos = FakeSeguimientoDao()
        sesion = FakeSesion(USUARIO)
        servidor = FakeSeguimientoApi()
        sincronizaciones = 0
        vm = SeguimientoViewModel(
            repositorio(seguimientos, servidor),
            repositorio(oportunidades),
            repositorio(eventos),
            repositorio(contactos),
            ObservarSesionUseCase(SessionRepository(sesion)),
            SeguimientoValidator(),
            ahoraReloj = { ahora },
            sincronizar = { sincronizaciones++ }
        )
    }

    @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    private fun completar(tipo: String = "Envío de propuesta") {
        vm.onTipoAccionChange(tipo)
        vm.onResultadoChange("  Se envió la propuesta por correo.  ")
    }

    @Test fun cargaLaOportunidadLaFechaDeHoyYLosContactosDeSuOrganizacion() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        val s = vm.uiState.value
        assertEquals("Stand dental", s.oportunidadNombre)
        assertEquals("Contacto realizado", s.estadoOportunidad)
        assertEquals(FECHA_EVENTO, s.fecha)
        assertEquals(630, s.hora)
        // Solo los contactos de la organización del evento (la 3), no los de otra.
        assertEquals(listOf("Laura Soto"), s.contactos.map { it.nombre })
    }

    @Test fun elTipoDeAccionSugiereElEstado() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        vm.onTipoAccionChange("Envío de propuesta")
        assertEquals("Propuesta enviada", vm.uiState.value.estadoSugerido)
        assertTrue(vm.uiState.value.cambiarEstado)
        // Una llamada lleva a "Contacto realizado", que ya es el estado actual: no se sugiere nada.
        vm.onTipoAccionChange("Llamada")
        assertNull(vm.uiState.value.estadoSugerido)
        assertFalse(vm.uiState.value.cambiarEstado)
    }

    @Test fun guardaLaAccionYPideAlServidorElCambioDeEstado() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        completar(); vm.onContactoChange(5); vm.guardar(); advanceUntilIdle()

        assertTrue(vm.uiState.value.guardadoExitosamente)
        // Lo que recibió el servidor: la acción y el estado al que pasa la oportunidad.
        val (oportunidadId, enviado) = servidor.recibidos.single()
        assertEquals(1L, oportunidadId)
        assertEquals("Propuesta enviada", enviado.nuevoEstado)
        assertNull(enviado.id)
        // La respuesta del servidor quedó en Room.
        val accion = seguimientos.items.value.single()
        assertEquals("Envío de propuesta", accion.tipoAccion)
        assertEquals("Se envió la propuesta por correo.", accion.resultado)
        assertEquals(5L, accion.contactoId)
        assertEquals(USUARIO, accion.usuarioId)
        assertEquals(ahora, accion.fechaHora)
        // El nuevo estado y el "Cambio de estado" se traen con la sincronización.
        assertEquals(1, sincronizaciones)
    }

    @Test fun siSeDesmarcaElCambioNoSePideCambiarElEstado() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        completar(); vm.onCambiarEstadoChange(false); vm.guardar(); advanceUntilIdle()
        assertNull(servidor.recibidos.single().second.nuevoEstado)
        assertEquals(1, seguimientos.items.value.size)
        assertEquals(0, sincronizaciones)
    }

    @Test fun sinResultadoNoSeGuarda() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        vm.onTipoAccionChange("Llamada"); vm.guardar(); advanceUntilIdle()
        assertNotNull(vm.uiState.value.resultadoError)
        assertTrue(seguimientos.items.value.isEmpty())
    }

    @Test fun proximaAccionNecesitaFechaYNoPuedeSerAnterior() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        completar(); vm.onProximaAccionChange("Seguimiento"); vm.guardar(); advanceUntilIdle()
        assertNotNull(vm.uiState.value.fechaProximoError)

        vm.onFechaProximoChange(FECHA_EVENTO - dia); vm.guardar(); advanceUntilIdle()
        assertNotNull(vm.uiState.value.fechaProximoError)
        assertTrue(seguimientos.items.value.isEmpty())

        vm.onFechaProximoChange(FECHA_EVENTO + 7 * dia); vm.guardar(); advanceUntilIdle()
        val accion = seguimientos.items.value.first()
        assertEquals("Seguimiento", accion.proximaAccion)
        assertEquals(FECHA_EVENTO + 7 * dia, accion.fechaProximoSeguimiento)
    }

    @Test fun quitarLaProximaAccionQuitaSuFecha() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        vm.onProximaAccionChange("Llamada"); vm.onFechaProximoChange(FECHA_EVENTO + dia)
        vm.onProximaAccionChange(null)
        assertEquals("", vm.uiState.value.proximaAccion)
        assertNull(vm.uiState.value.fechaProximo)
    }

    @Test fun sinSesionNoGuarda() = runTest(dispatcher) {
        vm.cargar(1); advanceUntilIdle()
        sesion.cerrar()
        completar(); vm.guardar(); advanceUntilIdle()
        assertNotNull(vm.uiState.value.mensaje)
        assertTrue(seguimientos.items.value.isEmpty())
    }

    @Test fun oportunidadInexistenteMarcaCargaFallida() = runTest(dispatcher) {
        vm.cargar(99); advanceUntilIdle()
        assertTrue(vm.uiState.value.cargaFallida)
    }

    @Test fun opcionesYFormato() {
        assertEquals(12, Opciones.tiposAccion.size)
        assertNull(Opciones.estadoSugerido("Recepción de respuesta"))
        assertEquals("15/03/2027 10:30", FormatoFecha.fechaHora(ahora))
    }

    private companion object { const val USUARIO = 42L }
}
