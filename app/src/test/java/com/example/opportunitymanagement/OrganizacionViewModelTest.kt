package com.example.opportunitymanagement

import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.viewmodel.OrganizacionValidator
import com.example.opportunitymanagement.viewmodel.OrganizacionViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizacionViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var organizaciones: FakeOrganizacionDao
    private lateinit var contactos: FakeContactoDao
    private lateinit var vm: OrganizacionViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        organizaciones = FakeOrganizacionDao()
        contactos = FakeContactoDao(organizaciones)
        vm = OrganizacionViewModel(
            OrganizacionRepository(organizaciones),
            ContactoRepository(contactos),
            OrganizacionValidator()
        )
    }

    @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    private fun completar() {
        vm.onNombreChange("  Municipalidad Ficticia  ")
        vm.onTipoChange("Municipalidad")
        vm.onRubroChange("Servicios públicos")
        vm.onComunaChange("Puente Alto")
        vm.onRegionChange("Metropolitana")
    }

    @Test fun invalidaNoSeGuarda() = runTest(dispatcher) {
        vm.cargar(0); vm.guardar(); advanceUntilIdle()
        assertEquals(0, organizaciones.guardados)
        assertNotNull(vm.uiState.value.nombreError)
    }

    @Test fun nuevaQuedaAbiertaConSuIdParaAgregarContactos() = runTest(dispatcher) {
        vm.cargar(0); completar(); vm.guardar(); advanceUntilIdle()
        val estado = vm.uiState.value
        val guardada = organizaciones.items.value.single()
        assertEquals(guardada.id, estado.id)
        assertEquals("Municipalidad Ficticia", guardada.nombre)
        assertFalse("No debe volver a la lista", estado.guardadoExitosamente)
        assertNotNull(estado.aviso)
    }

    @Test fun losContactosDeLaOrganizacionAparecenSolos() = runTest(dispatcher) {
        organizaciones.items.value = listOf(organizacion(3), organizacion(4, "Empresa Demo SpA"))
        contactos.items.value = listOf(contacto(10, organizacionId = 3), contacto(11, organizacionId = 4, nombre = "Pedro Rojas"))
        vm.cargar(3); advanceUntilIdle()
        assertEquals(listOf("Laura Soto"), vm.uiState.value.contactos.map { it.nombre })

        // Al agregar un contacto (por ejemplo, desde su formulario) la sección se actualiza.
        contactos.items.value = contactos.items.value + contacto(12, organizacionId = 3, nombre = "Ana Pérez")
        advanceUntilIdle()
        assertEquals(listOf("Ana Pérez", "Laura Soto"), vm.uiState.value.contactos.map { it.nombre })
    }

    @Test fun recienCreadaAvisaQueFaltanContactos() = runTest(dispatcher) {
        vm.cargar(0); completar(); vm.guardar(); advanceUntilIdle()
        val estado = vm.uiState.value
        assertTrue(estado.contactosCargados)
        assertTrue(estado.contactos.isEmpty())
        assertTrue(estado.aviso!!.contains("agrega al menos un contacto"))
    }

    @Test fun volverDelContactoNoBorraLaOrganizacionRecienCreada() = runTest(dispatcher) {
        vm.cargar(0); completar(); vm.guardar(); advanceUntilIdle()
        val id = vm.uiState.value.id

        // La pantalla vuelve a llamar a cargar(0) al regresar del formulario de contacto.
        vm.cargar(0); advanceUntilIdle()
        assertEquals(id, vm.uiState.value.id)
        assertEquals("  Municipalidad Ficticia  ", vm.uiState.value.nombre)
    }

    @Test fun volverNoDescartaCambiosSinGuardar() = runTest(dispatcher) {
        organizaciones.items.value = listOf(organizacion(3))
        vm.cargar(3); advanceUntilIdle()
        vm.onRubroChange("Educación")
        vm.cargar(3); advanceUntilIdle()
        assertEquals("Educación", vm.uiState.value.rubro)
    }

    @Test fun editarVuelveALaListaYConservaElRegistro() = runTest(dispatcher) {
        organizaciones.items.value = listOf(organizacion(3))
        vm.cargar(3); advanceUntilIdle()
        vm.onNombreChange("Municipalidad Renombrada"); vm.guardar(); advanceUntilIdle()
        assertTrue(vm.uiState.value.guardadoExitosamente)
        assertEquals(listOf("Municipalidad Renombrada"), organizaciones.items.value.map { it.nombre })
    }

    @Test fun dobleToqueGuardaUnaVez() = runTest(dispatcher) {
        vm.cargar(0); completar(); vm.guardar(); vm.guardar(); advanceUntilIdle()
        assertEquals(1, organizaciones.guardados)
    }
}
