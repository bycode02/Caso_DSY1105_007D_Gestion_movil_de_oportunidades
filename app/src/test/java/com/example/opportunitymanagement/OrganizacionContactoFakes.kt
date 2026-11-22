package com.example.opportunitymanagement

import com.example.opportunitymanagement.data.local.ContactoConOrganizacion
import com.example.opportunitymanagement.data.local.ContactoDao
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.EventoDao
import com.example.opportunitymanagement.data.local.EventoEntity
import com.example.opportunitymanagement.data.local.OrganizacionConContacto
import com.example.opportunitymanagement.data.local.OrganizacionDao
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
