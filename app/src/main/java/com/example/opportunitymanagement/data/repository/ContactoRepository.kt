package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.ContactoConOrganizacion
import com.example.opportunitymanagement.data.local.ContactoDao
import com.example.opportunitymanagement.data.local.ContactoEntity
import com.example.opportunitymanagement.data.remote.ContactoApi
import com.example.opportunitymanagement.data.remote.toDto
import com.example.opportunitymanagement.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class ContactoRepository(
    private val dao: ContactoDao,
    private val api: ContactoApi
) {

    // Contactos con el nombre de su organización (JOIN), para la lista.
    val contactos: Flow<List<ContactoConOrganizacion>> = dao.observarTodos()

    fun contactosDeOrganizacion(organizacionId: Long): Flow<List<ContactoEntity>> =
        dao.observarPorOrganizacion(organizacionId)

    suspend fun buscar(id: Long): ContactoEntity? = dao.findById(id)

    /** Crea (id = 0) o actualiza en el servidor y guarda en Room su respuesta. Devuelve el id. */
    suspend fun guardar(item: ContactoEntity): Long {
        val guardado = try {
            if (item.id == 0L) api.crear(item.toDto().copy(id = null))
            else api.actualizar(item.id, item.toDto())
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw IllegalStateException("El contacto ya no existe")
            throw exception
        }
        dao.upsert(guardado.toEntity())
        return checkNotNull(guardado.id) { "El servidor no devolvió el id" }
    }

    /** Las acciones del historial con este contacto quedan sin contacto. */
    suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        when {
            respuesta.isSuccessful -> dao.delete(id)
            respuesta.code() == 404 -> {
                dao.delete(id)
                throw IllegalStateException("El contacto ya no existe")
            }
            else -> throw HttpException(respuesta)
        }
    }
}
