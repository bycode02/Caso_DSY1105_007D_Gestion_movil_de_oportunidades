package com.example.opportunitymanagement

import com.example.opportunitymanagement.data.remote.ContactoApi
import com.example.opportunitymanagement.data.remote.ContactoDto
import com.example.opportunitymanagement.data.remote.EventoApi
import com.example.opportunitymanagement.data.remote.EventoDto
import com.example.opportunitymanagement.data.remote.OportunidadApi
import com.example.opportunitymanagement.data.remote.OportunidadDto
import com.example.opportunitymanagement.data.remote.OrganizacionApi
import com.example.opportunitymanagement.data.remote.OrganizacionDto
import com.example.opportunitymanagement.data.remote.SeguimientoApi
import com.example.opportunitymanagement.data.remote.SeguimientoDto
import com.example.opportunitymanagement.data.repository.ContactoRepository
import com.example.opportunitymanagement.data.repository.EventoRepository
import com.example.opportunitymanagement.data.repository.OportunidadRepository
import com.example.opportunitymanagement.data.repository.OrganizacionRepository
import com.example.opportunitymanagement.data.repository.SeguimientoRepository
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

/*
 * "Servidores" en memoria para las pruebas de los ViewModels: asignan el id a lo nuevo,
 * devuelven lo que reciben y responden 404 si el registro no existe ([existe]).
 * Lo que responden lo guarda el repository en el DAO falso, igual que en la app.
 */

private fun noEncontrado(): Nothing = throw HttpException(Response.error<Any>(404, "".toResponseBody(null)))

private fun respuestaEliminar(existe: Boolean, codigo: Int): Response<Unit> = when {
    !existe -> Response.error(404, "".toResponseBody(null))
    codigo in 200..299 -> Response.success(Unit)
    else -> Response.error(codigo, "".toResponseBody(null))
}

class FakeOrganizacionApi(
    private var siguienteId: Long = 100,
    private val existe: (Long) -> Boolean = { true }
) : OrganizacionApi {
    val recibidas = mutableListOf<OrganizacionDto>()
    var codigoAlEliminar = 204

    override suspend fun listar() = emptyList<OrganizacionDto>()
    override suspend fun crear(organizacion: OrganizacionDto) =
        organizacion.copy(id = siguienteId++).also { recibidas += it }
    override suspend fun actualizar(id: Long, organizacion: OrganizacionDto): OrganizacionDto {
        if (!existe(id)) noEncontrado()
        return organizacion.copy(id = id).also { recibidas += it }
    }
    override suspend fun eliminar(id: Long) = respuestaEliminar(existe(id), codigoAlEliminar)
}

class FakeContactoApi(
    private var siguienteId: Long = 500,
    private val existe: (Long) -> Boolean = { true }
) : ContactoApi {
    val recibidos = mutableListOf<ContactoDto>()

    override suspend fun listar() = emptyList<ContactoDto>()
    override suspend fun crear(contacto: ContactoDto) = contacto.copy(id = siguienteId++).also { recibidos += it }
    override suspend fun actualizar(id: Long, contacto: ContactoDto): ContactoDto {
        if (!existe(id)) noEncontrado()
        return contacto.copy(id = id).also { recibidos += it }
    }
    override suspend fun eliminar(id: Long) = respuestaEliminar(existe(id), 204)
}

class FakeEventoApi(
    private var siguienteId: Long = 900,
    private val existe: (Long) -> Boolean = { true }
) : EventoApi {
    val recibidos = mutableListOf<EventoDto>()
    var codigoAlEliminar = 204

    override suspend fun listar() = emptyList<EventoDto>()
    override suspend fun crear(evento: EventoDto) = evento.copy(id = siguienteId++).also { recibidos += it }
    override suspend fun actualizar(id: Long, evento: EventoDto): EventoDto {
        if (!existe(id)) noEncontrado()
        return evento.copy(id = id).also { recibidos += it }
    }
    override suspend fun eliminar(id: Long) = respuestaEliminar(existe(id), codigoAlEliminar)
}

class FakeOportunidadApi(
    private var siguienteId: Long = 1,
    private val existe: (Long) -> Boolean = { true }
) : OportunidadApi {
    val recibidas = mutableListOf<OportunidadDto>()
    // Header X-Usuario-Id del último PUT (autor del cambio de estado).
    var usuarioDelCambio: Long? = null
    var eliminaciones = 0

    override suspend fun listar() = emptyList<OportunidadDto>()
    override suspend fun buscar(id: Long): OportunidadDto = error("no se usa")
    override suspend fun crear(oportunidad: OportunidadDto) =
        oportunidad.copy(id = siguienteId++).also { recibidas += it }
    override suspend fun actualizar(id: Long, oportunidad: OportunidadDto, usuarioId: Long?): OportunidadDto {
        if (!existe(id)) noEncontrado()
        usuarioDelCambio = usuarioId
        return oportunidad.copy(id = id).also { recibidas += it }
    }
    override suspend fun eliminar(id: Long): Response<Unit> {
        eliminaciones++
        return respuestaEliminar(existe(id), 204)
    }
}

// Repositories con su DAO falso y un "servidor" que conoce los mismos registros del DAO.
fun repositorio(dao: FakeOrganizacionDao) =
    OrganizacionRepository(dao, FakeOrganizacionApi { id -> dao.items.value.any { it.id == id } })
fun repositorio(dao: FakeContactoDao) =
    ContactoRepository(dao, FakeContactoApi { id -> dao.items.value.any { it.id == id } })
fun repositorio(dao: FakeEventoDao) =
    EventoRepository(dao, FakeEventoApi { id -> dao.items.value.any { it.id == id } })
fun repositorio(dao: FakeOportunidadDao) =
    OportunidadRepository(dao, FakeOportunidadApi { id -> dao.items.value.any { it.id == id } })
fun repositorio(dao: FakeSeguimientoDao, api: FakeSeguimientoApi = FakeSeguimientoApi()) =
    SeguimientoRepository(dao, api)

class FakeSeguimientoApi(private var siguienteId: Long = 1) : SeguimientoApi {
    // Lo que recibió el servidor, con su ruta: (oportunidadId, cuerpo).
    val recibidos = mutableListOf<Pair<Long, SeguimientoDto>>()

    override suspend fun todos() = emptyList<SeguimientoDto>()
    override suspend fun historial(oportunidadId: Long) = emptyList<SeguimientoDto>()
    override suspend fun registrar(oportunidadId: Long, seguimiento: SeguimientoDto): SeguimientoDto {
        recibidos += oportunidadId to seguimiento
        // Como el backend: responde con el id y la oportunidad, sin el campo nuevoEstado.
        return seguimiento.copy(id = siguienteId++, oportunidadId = oportunidadId, nuevoEstado = null)
    }
}
