package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.ContactoConOrganizacion
import com.example.opportunitymanagement.data.local.ContactoDao
import com.example.opportunitymanagement.data.local.ContactoEntity
import kotlinx.coroutines.flow.Flow

class ContactoRepository(private val dao: ContactoDao) {

    // Contactos con el nombre de su organización (JOIN), para la lista.
    val contactos: Flow<List<ContactoConOrganizacion>> = dao.observarTodos()

    fun contactosDeOrganizacion(organizacionId: Long): Flow<List<ContactoEntity>> =
        dao.observarPorOrganizacion(organizacionId)

    suspend fun buscar(id: Long): ContactoEntity? = dao.findById(id)

    /** Inserta si es nuevo (id = 0) o actualiza si ya existe. Devuelve el id. */
    suspend fun guardar(item: ContactoEntity): Long {
        if (item.id == 0L) return dao.insert(item)
        check(dao.updteOrg(item) == 1) { "El contacto ya no existe" }
        return item.id
    }

    suspend fun eliminar(id: Long) {
        check(dao.delete(id) == 1) { "El contacto ya no existe" }
    }
}
