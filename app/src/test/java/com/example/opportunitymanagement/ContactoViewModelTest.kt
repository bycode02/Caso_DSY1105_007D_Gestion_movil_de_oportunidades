package com.example.opportunitymanagement

import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.viewmodel.ContactoValidator
import com.example.opportunitymanagement.viewmodel.ContactoViewModel
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
class ContactoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var organizaciones: FakeOrganizacionDao
    private lateinit var contactos: FakeContactoDao
    private lateinit var vm: ContactoViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        organizaciones = FakeOrganizacionDao()
        organizaciones.items.value = listOf(organizacion(3), organizacion(4, "Empresa Demo SpA"))
        contactos = FakeContactoDao(organizaciones)
        vm = ContactoViewModel(
            repositorio(contactos),
            repositorio(organizaciones),
            ContactoValidator()
        )
    }

    @After fun cleanup() { vm.viewModelScope.cancel(); Dispatchers.resetMain() }

    private fun completar() {
        vm.onNombreChange("  Laura Soto  ")
        vm.onCargoChange("Encargada")
        vm.onAreaChange("Bienestar")
        vm.onMedioPreferenteChange("Correo")
    }

    @Test fun nuevoDesdeLaOrganizacionLlegaConEllaElegida() = runTest(dispatcher) {
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        assertEquals(3L, vm.uiState.value.organizacionId)
        assertEquals(2, vm.uiState.value.organizaciones.size)
    }

    @Test fun guardaConLaOrganizacionPreseleccionada() = runTest(dispatcher) {
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        completar(); vm.guardar(); advanceUntilIdle()
        val guardado = contactos.items.value.single()
        assertEquals(3L, guardado.organizacionId)
        assertEquals("Laura Soto", guardado.nombre)
        assertTrue(vm.uiState.value.guardadoExitosamente)
    }

    @Test fun sinOrganizacionNoSeGuarda() = runTest(dispatcher) {
        vm.cargar(0); advanceUntilIdle()
        completar(); vm.guardar(); advanceUntilIdle()
        assertEquals(0, contactos.guardados)
        assertNotNull(vm.uiState.value.organizacionError)
    }

    @Test fun elegirOtraOrganizacionLimpiaElError() = runTest(dispatcher) {
        vm.cargar(0); advanceUntilIdle()
        vm.guardar(); advanceUntilIdle()
        vm.onOrganizacionChange(4)
        assertNull(vm.uiState.value.organizacionError)
        assertEquals(4L, vm.uiState.value.organizacionId)
    }

    @Test fun editarUsaLaOrganizacionGuardadaYConservaElRegistro() = runTest(dispatcher) {
        contactos.items.value = listOf(contacto(10, organizacionId = 4))
        vm.cargar(10, organizacionId = 3); advanceUntilIdle()
        assertEquals(4L, vm.uiState.value.organizacionId)

        vm.onCargoChange("Jefa de bienestar"); vm.guardar(); advanceUntilIdle()
        val guardado = contactos.items.value.single()
        assertEquals(10L, guardado.id)
        assertEquals("Jefa de bienestar", guardado.cargo)
    }

    @Test fun volverNoDescartaCambiosSinGuardar() = runTest(dispatcher) {
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        vm.onNombreChange("Laura")
        vm.cargar(0, organizacionId = 3); advanceUntilIdle()
        assertEquals("Laura", vm.uiState.value.nombre)
    }

    @Test fun contactoInexistenteMarcaCargaFallida() = runTest(dispatcher) {
        vm.cargar(99); advanceUntilIdle()
        assertTrue(vm.uiState.value.cargaFallida)
        assertNotNull(vm.uiState.value.mensaje)
    }
}
