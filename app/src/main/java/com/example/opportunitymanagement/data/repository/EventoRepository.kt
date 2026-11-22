package com.example.opportunitymanagement.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.EventoDao
import com.example.opportunitymanagement.data.local.EventoEntity
import kotlinx.coroutines.flow.Flow

/** No se puede eliminar un evento que todavía tiene oportunidades asociadas. */
class EventoConOportunidadesException :
    IllegalStateException("El evento tiene oportunidades asociadas")

class EventoRepository(private val dao: EventoDao) {

    // Eventos con el nombre de su organización, del más próximo al más lejano.
    val eventos: Flow<List<EventoConOrganizacion>> = dao.observarTodos()

    fun eventosDeOrganizacion(organizacionId: Long): Flow<List<EventoEntity>> =
        dao.observarPorOrganizacion(organizacionId)

    suspend fun buscar(id: Long): EventoEntity? = dao.buscar(id)

    /** Inserta si es nuevo (id = 0) o actualiza si ya existe. Devuelve el id. */
    suspend fun guardar(item: EventoEntity): Long {
        if (item.id == 0L) return dao.insertar(item)
        check(dao.actualizar(item) == 1) { "El evento ya no existe" }
        return item.id
    }

    suspend fun eliminar(id: Long) {
        val borrados = try {
            dao.eliminar(id)
        } catch (exception: SQLiteConstraintException) {
            // La clave foránea de oportunidades (RESTRICT) impide el borrado.
            throw EventoConOportunidadesException()
        }
        check(borrados == 1) { "El evento ya no existe" }
    }
}
