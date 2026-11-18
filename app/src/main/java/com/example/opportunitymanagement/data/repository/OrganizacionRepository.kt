package com.example.opportunitymanagement.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.opportunitymanagement.data.local.OrganizacionDao
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import kotlinx.coroutines.flow.Flow

/** No se puede eliminar una organización que todavía tiene contactos asociados. */
class OrganizacionConContactosException :
    IllegalStateException("La organización tiene contactos asociados")

class OrganizacionRepository(private val dao: OrganizacionDao) {

    // Se actualiza sola cuando cambia la tabla, igual que la lista de oportunidades.
    val organizaciones: Flow<List<OrganizacionEntity>> = dao.obtenerTodas()

    suspend fun buscar(id: Long): OrganizacionEntity? = dao.encontrarPorId(id)

    /** Inserta si es nueva (id = 0) o actualiza si ya existe. Devuelve el id. */
    suspend fun guardar(item: OrganizacionEntity): Long {
        if (item.id == 0L) return dao.insert(item)
        check(dao.actualizar(item) == 1) { "La organización ya no existe" }
        return item.id
    }

    suspend fun eliminar(id: Long) {
        val borradas = try {
            dao.eliminar(id)
        } catch (exception: SQLiteConstraintException) {
            // La clave foránea de contactos (RESTRICT) impide el borrado.
            throw OrganizacionConContactosException()
        }
        check(borradas == 1) { "La organización ya no existe" }
    }
}
