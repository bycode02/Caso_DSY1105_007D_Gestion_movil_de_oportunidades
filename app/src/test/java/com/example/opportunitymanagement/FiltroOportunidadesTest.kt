package com.example.opportunitymanagement

import androidx.lifecycle.viewModelScope
import com.example.opportunitymanagement.data.local.OportunidadConResponsable
import com.example.opportunitymanagement.data.local.OportunidadDao
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.viewmodel.FiltroOportunidades
import com.example.opportunitymanagement.viewmodel.FiltroSeguimiento
import com.example.opportunitymanagement.viewmodel.FiltrosOportunidad
import com.example.opportunitymanagement.viewmodel.ListaViewModel
import com.example.opportunitymanagement.viewmodel.RangoFecha
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FiltroOportunidadesTest {

    private val dia = 24L * 60 * 60 * 1000
    private val hoy = FECHA_EVENTO // 15/03/2027 a medianoche UTC

    private fun item(
        id: Long,
        nombre: String,
        estado: String = "Evento identificado",
        tipo: String = "Feria empresarial",
        prioridad: String = "Alta",
        organizacion: String? = "Municipalidad Ficticia",
        comuna: String? = "Puente Alto",
        region: String? = "Metropolitana",
        responsable: String? = "Ana Pérez",
        evento: String? = "Feria de beneficios",
        fecha: Long? = hoy
    ) = OportunidadConResponsable(
        OportunidadEntity(
            id = id, nombre = nombre, tipo = tipo, descripcion = "d", fuente = "f",
            prioridad = prioridad, potencial = "Alto", estado = estado
        ),
        responsableNombre = responsable,
        eventoNombre = evento,
        organizacionNombre = organizacion,
        eventoFecha = fecha,
        eventoComuna = comuna,
        eventoRegion = region
    )

    private val lista = listOf(
        item(1, "Stand dental", prioridad = "Alta", fecha = hoy + 2 * dia),
        item(2, "Stand veterinario", prioridad = "Media", estado = "Propuesta enviada",
            organizacion = "Empresa Demo SpA", comuna = "Santiago", responsable = "Pedro Rojas",
            evento = "Congreso de bienestar", fecha = hoy + 20 * dia),
        item(3, "Activación pasada", prioridad = "Baja", tipo = "Activación comercial",
            region = "Valparaíso", comuna = "Viña del Mar", fecha = hoy - 3 * dia)
    )

    private fun nombres(f: FiltrosOportunidad) =
        FiltroOportunidades.aplicar(lista, f, hoy).map { it.oportunidad.nombre }

    @Test fun sinFiltrosMuestraTodo() {
        assertEquals(3, nombres(FiltrosOportunidad()).size)
        assertFalse(FiltroOportunidades.hayFiltros(FiltrosOportunidad()))
    }

    @Test fun buscaPorNombreSinImportarMayusculasNiEspacios() {
        assertEquals(listOf("Stand dental"), nombres(FiltrosOportunidad(texto = "  DENTAL ")))
    }

    @Test fun buscaTambienPorNombreDelEvento() {
        assertEquals(listOf("Stand veterinario"), nombres(FiltrosOportunidad(texto = "congreso")))
    }

    @Test fun filtraPorEstadoTipoYPrioridad() {
        assertEquals(listOf("Stand veterinario"), nombres(FiltrosOportunidad(estado = "Propuesta enviada")))
        assertEquals(listOf("Activación pasada"), nombres(FiltrosOportunidad(tipo = "Activación comercial")))
        assertEquals(listOf("Stand veterinario"), nombres(FiltrosOportunidad(prioridad = "Media")))
    }

    @Test fun filtraPorOrganizacionUbicacionYResponsable() {
        assertEquals(listOf("Stand veterinario"), nombres(FiltrosOportunidad(organizacion = "Empresa Demo SpA")))
        assertEquals(listOf("Activación pasada"), nombres(FiltrosOportunidad(comuna = "Viña del Mar")))
        assertEquals(listOf("Activación pasada"), nombres(FiltrosOportunidad(region = "Valparaíso")))
        assertEquals(listOf("Stand veterinario"), nombres(FiltrosOportunidad(responsable = "Pedro Rojas")))
    }

    @Test fun filtraPorFechaDelEvento() {
        assertEquals(listOf("Stand dental"), nombres(FiltrosOportunidad(fecha = RangoFecha.PROXIMOS_7)))
        assertEquals(listOf("Stand dental", "Stand veterinario"), nombres(FiltrosOportunidad(fecha = RangoFecha.PROXIMOS_30)))
        assertEquals(listOf("Activación pasada"), nombres(FiltrosOportunidad(fecha = RangoFecha.PASADOS)))
    }

    @Test fun sinEventoNoEntraEnFiltrosDeFecha() {
        val sinEvento = item(9, "Antigua", evento = null, fecha = null)
        assertFalse(FiltroOportunidades.cumple(sinEvento, FiltrosOportunidad(fecha = RangoFecha.PROXIMOS_30), hoy))
        assertTrue(FiltroOportunidades.cumple(sinEvento, FiltrosOportunidad(), hoy))
    }

    @Test fun filtraPorSeguimientoAtrasadoOPendiente() {
        val atrasado = item(10, "Atrasada").copy(proximoSeguimiento = hoy - dia, proximaAccion = "Llamada")
        val pendiente = item(11, "Pendiente").copy(proximoSeguimiento = hoy, proximaAccion = "Llamada")
        val sinProxima = item(12, "Sin próxima")
        val todas = listOf(atrasado, pendiente, sinProxima)
        fun con(filtro: FiltroSeguimiento) = FiltroOportunidades
            .aplicar(todas, FiltrosOportunidad(seguimiento = filtro), hoy).map { it.oportunidad.nombre }
        assertEquals(listOf("Atrasada"), con(FiltroSeguimiento.ATRASADOS))
        assertEquals(listOf("Pendiente"), con(FiltroSeguimiento.PENDIENTES))
    }

    @Test fun losFiltrosSeCombinan() {
        val f = FiltrosOportunidad(region = "Metropolitana", fecha = RangoFecha.PROXIMOS_30, prioridad = "Media")
        assertEquals(listOf("Stand veterinario"), nombres(f))
        assertTrue(FiltroOportunidades.hayFiltros(f))
    }

    @Test fun opcionesSinRepetirYOrdenadas() {
        assertEquals(listOf("Puente Alto", "Santiago", "Viña del Mar"), FiltroOportunidades.comunas(lista))
        assertEquals(listOf("Empresa Demo SpA", "Municipalidad Ficticia"), FiltroOportunidades.organizaciones(lista))
    }

    // --- Integración con el ViewModel ---

    private val dispatcher = StandardTestDispatcher()
    private var vm: ListaViewModel? = null

    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { vm?.viewModelScope?.cancel(); Dispatchers.resetMain() }

    @Test fun elViewModelFiltraYLimpia() = runTest(dispatcher) {
        val viewModel = ListaViewModel(OportunidadRepository(DaoConEventos(MutableStateFlow(lista))), hoyUtc = { hoy })
            .also { vm = it }
        val recolector = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onBuscar("stand")
        viewModel.onFiltrosChange(viewModel.uiState.value.filtros.copy(prioridad = "Alta"))
        advanceUntilIdle()
        assertEquals(listOf("Stand dental"), viewModel.uiState.value.items.map { it.oportunidad.nombre })
        assertEquals(3, viewModel.uiState.value.total)
        assertEquals("stand", viewModel.uiState.value.filtros.texto)

        viewModel.limpiarFiltros()
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.items.size)
        recolector.cancel()
    }

    private class DaoConEventos(private val items: Flow<List<OportunidadConResponsable>>) : OportunidadDao {
        override fun observarTodas() = items
        override suspend fun buscar(id: Long): OportunidadEntity? = null
        override suspend fun insertar(item: OportunidadEntity): Long = 1
        override suspend fun actualizar(item: OportunidadEntity): Int = 1
        override suspend fun eliminar(id: Long): Int = 1
    }
}
