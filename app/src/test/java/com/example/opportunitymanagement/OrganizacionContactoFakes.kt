package com.example.opportunitymanagement

import com.example.opportunitymanagement.data.local.ContactoConOrganizacion
import com.example.opportunitymanagement.data.local.ContactoDao
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.EventoDao
import com.example.opportunitymanagement.data.local.EventoEntity
import com.example.opportunitymanagement.data.local.OrganizacionConContacto
import com.example.opportunitymanagement.data.local.OrganizacionDao
import com.example.opportunitymanagement.data.local.OportunidadConResponsable
import com.example.opportunitymanagement.data.local.OportunidadDao
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.data.local.Role
import com.example.opportunitymanagement.data.local.SeguimientoConDetalle
import com.example.opportunitymanagement.data.local.SeguimientoDao
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import com.example.opportunitymanagement.data.local.SesionDao
import com.example.opportunitymanagement.data.local.SesionEntity
import com.example.opportunitymanagement.data.local.UsuarioEntity
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** DAO de organizaciones en memoria para las pruebas de los ViewModels. */
class FakeOrganizacionDao : OrganizacionDao {
    val items = MutableStateFlow<List<OrganizacionEntity>>(emptyList())
    var guardados = 0
    private var siguienteId = 100L

    override fun obtenerTodas(): Flow<List<OrganizacionEntity>> = items.map { it.sortedBy { o -> o.nombre } }
    // En memoria no hay JOIN con contactos; las pruebas de la lista no lo necesitan.
    override fun observarConContacto(): Flow<List<OrganizacionConContacto>> =
        obtenerTodas().map { lista -> lista.map { OrganizacionConContacto(it, null, 0) } }
    override suspend fun encontrarPorId(id: Long) = items.value.find { it.id == id }
    override suspend fun insert(org: OrganizacionEntity): Long {
        val id = siguienteId++
        guardados++
        items.value = items.value + org.copy(id = id)
        return id
    }
    override suspend fun actualizar(org: OrganizacionEntity): Int {
        if (items.value.none { it.id == org.id }) return 0
        guardados++
        items.value = items.value.map { if (it.id == org.id) org else it }
        return 1
    }
    override suspend fun eliminar(id: Long): Int {
        val antes = items.value.size
        items.value = items.value.filterNot { it.id == id }
        return antes - items.value.size
    }
    override suspend fun upsert(item: OrganizacionEntity) {
        guardados++
        items.value = if (items.value.any { it.id == item.id })
            items.value.map { if (it.id == item.id) item else it }
        else items.value + item
    }
}

/** DAO de contactos en memoria; el JOIN con organizaciones se simula con [organizaciones]. */
class FakeContactoDao(private val organizaciones: FakeOrganizacionDao) : ContactoDao {
    val items = MutableStateFlow<List<ContactoEntity>>(emptyList())
    var guardados = 0
    private var siguienteId = 500L

    override fun getAll(): Flow<List<ContactoEntity>> = items
    override suspend fun findById(id: Long) = items.value.find { it.id == id }
    override suspend fun insert(contact: ContactoEntity): Long {
        val id = siguienteId++
        guardados++
        items.value = items.value + contact.copy(id = id)
        return id
    }
    override suspend fun updteOrg(contact: ContactoEntity): Int {
        if (items.value.none { it.id == contact.id }) return 0
        guardados++
        items.value = items.value.map { if (it.id == contact.id) contact else it }
        return 1
    }
    override suspend fun delete(id: Long): Int {
        val antes = items.value.size
        items.value = items.value.filterNot { it.id == id }
        return antes - items.value.size
    }
    override suspend fun upsert(item: ContactoEntity) {
        guardados++
        items.value = if (items.value.any { it.id == item.id })
            items.value.map { if (it.id == item.id) item else it }
        else items.value + item
    }
    override fun observarPorOrganizacion(organizacionId: Long): Flow<List<ContactoEntity>> =
        items.map { lista -> lista.filter { it.organizacionId == organizacionId }.sortedBy { it.nombre } }
    override fun observarTodos(): Flow<List<ContactoConOrganizacion>> =
        combine(items, organizaciones.items) { contactos, orgs ->
            contactos.mapNotNull { c ->
                orgs.find { it.id == c.organizacionId }?.let { ContactoConOrganizacion(c, it.nombre) }
            }
        }
}

/** DAO de eventos en memoria; el JOIN con organizaciones se simula con [organizaciones]. */
class FakeEventoDao(private val organizaciones: FakeOrganizacionDao) : EventoDao {
    val items = MutableStateFlow<List<EventoEntity>>(emptyList())
    var guardados = 0
    private var siguienteId = 900L

    override fun observarTodos(): Flow<List<EventoConOrganizacion>> =
        combine(items, organizaciones.items) { eventos, orgs ->
            eventos.sortedWith(compareBy({ it.fechaInicio }, { it.horaInicio })).mapNotNull { e ->
                orgs.find { it.id == e.organizacionId }?.let { EventoConOrganizacion(e, it.nombre) }
            }
        }
    override fun observarPorOrganizacion(organizacionId: Long): Flow<List<EventoEntity>> =
        items.map { lista -> lista.filter { it.organizacionId == organizacionId }.sortedBy { it.fechaInicio } }
    override suspend fun buscar(id: Long) = items.value.find { it.id == id }
    override suspend fun insertar(evento: EventoEntity): Long {
        val id = siguienteId++
        guardados++
        items.value = items.value + evento.copy(id = id)
        return id
    }
    override suspend fun actualizar(evento: EventoEntity): Int {
        if (items.value.none { it.id == evento.id }) return 0
        guardados++
        items.value = items.value.map { if (it.id == evento.id) evento else it }
        return 1
    }
    override suspend fun eliminar(id: Long): Int {
        val antes = items.value.size
        items.value = items.value.filterNot { it.id == id }
        return antes - items.value.size
    }
    override suspend fun upsert(item: EventoEntity) {
        guardados++
        items.value = if (items.value.any { it.id == item.id })
            items.value.map { if (it.id == item.id) item else it }
        else items.value + item
    }
}

// 15/03/2027 a medianoche UTC, como lo entrega el DatePicker.
const val FECHA_EVENTO = 1_805_068_800_000L

fun evento(id: Long, organizacionId: Long, nombre: String = "Feria de beneficios") = EventoEntity(
    id = id, nombre = nombre, tipo = "Feria institucional", fechaInicio = FECHA_EVENTO,
    horaInicio = 9 * 60, horaTermino = 18 * 60, comuna = "Puente Alto", region = "Metropolitana",
    organizacionId = organizacionId, publicoEsperado = "Trabajadores y familias"
)

fun organizacion(id: Long, nombre: String = "Municipalidad Ficticia") = OrganizacionEntity(
    id = id, nombre = nombre, tipo = "Municipalidad", rubro = "Servicios públicos",
    comuna = "Puente Alto", region = "Metropolitana"
)

fun contacto(id: Long, organizacionId: Long, nombre: String = "Laura Soto") = ContactoEntity(
    id = id, nombre = nombre, organizacionId = organizacionId, cargo = "Encargada",
    area = "Bienestar", medioPreferente = "Correo", estado = "Por contactar"
)

/** DAO de oportunidades en memoria (para los ViewModels que la leen o cambian su estado). */
class FakeOportunidadDao : OportunidadDao {
    val items = MutableStateFlow<List<OportunidadEntity>>(emptyList())
    var guardados = 0
    override fun observarTodas(): Flow<List<OportunidadConResponsable>> =
        items.map { lista -> lista.map { OportunidadConResponsable(it, responsableNombre = null) } }
    override suspend fun buscar(id: Long) = items.value.find { it.id == id }
    override suspend fun insertar(item: OportunidadEntity): Long {
        val id = (items.value.maxOfOrNull { it.id } ?: 0) + 1
        items.value = items.value + item.copy(id = id)
        return id
    }
    override suspend fun actualizar(item: OportunidadEntity): Int {
        if (items.value.none { it.id == item.id }) return 0
        items.value = items.value.map { if (it.id == item.id) item else it }
        return 1
    }
    override suspend fun eliminar(id: Long): Int {
        val antes = items.value.size
        items.value = items.value.filterNot { it.id == id }
        return antes - items.value.size
    }
    override suspend fun upsert(item: OportunidadEntity) {
        guardados++
        items.value = if (items.value.any { it.id == item.id })
            items.value.map { if (it.id == item.id) item else it }
        else items.value + item
    }
}

/** DAO de seguimientos en memoria; sin JOIN, los nombres de usuario y contacto quedan en null. */
class FakeSeguimientoDao : SeguimientoDao {
    val items = MutableStateFlow<List<SeguimientoEntity>>(emptyList())
    var guardados = 0
    private var siguienteId = 1L
    override fun observarPorOportunidad(oportunidadId: Long): Flow<List<SeguimientoConDetalle>> =
        items.map { lista ->
            lista.filter { it.oportunidadId == oportunidadId }
                .sortedWith(compareByDescending<SeguimientoEntity> { it.fechaHora }.thenByDescending { it.id })
                .map { SeguimientoConDetalle(it, usuarioNombre = null, contactoNombre = null) }
        }
    override suspend fun insertar(seguimiento: SeguimientoEntity): Long {
        val id = siguienteId++
        items.value = items.value + seguimiento.copy(id = id)
        return id
    }
    override suspend fun upsert(item: SeguimientoEntity) {
        guardados++
        items.value = if (items.value.any { it.id == item.id })
            items.value.map { if (it.id == item.id) item else it }
        else items.value + item
    }
}

/** Sesión en memoria con el usuario [usuarioId]; cerrar() la deja vacía. */
class FakeSesion(usuarioId: Long) : SesionDao {
    val usuario = MutableStateFlow<UsuarioEntity?>(
        UsuarioEntity(usuarioId, "Vendedor", "v@ejemplo.cl", Role.VENDEDOR, true, 0, 0)
    )
    override fun usuarioActual() = usuario
    override suspend fun guardar(sesion: SesionEntity) = error("no se usa")
    override suspend fun cerrar() { usuario.value = null }
}

fun oportunidadDePrueba(id: Long, estado: String = "Contacto realizado", eventoId: Long? = 7) = OportunidadEntity(
    id = id, nombre = "Stand dental", tipo = "Feria empresarial", descripcion = "d", fuente = "f",
    prioridad = "Alta", potencial = "Alto", estado = estado, eventoId = eventoId
)
