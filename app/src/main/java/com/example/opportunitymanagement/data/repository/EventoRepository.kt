package com.example.opportunitymanagement.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.opportunitymanagement.data.local.EventoConOrganizacion
import com.example.opportunitymanagement.data.local.EventoDao
import com.example.opportunitymanagement.data.local.EventoEntity
import com.example.opportunitymanagement.data.remote.EventoApi
import com.example.opportunitymanagement.data.remote.toDto
import com.example.opportunitymanagement.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/** No se puede eliminar un evento que todavía tiene oportunidades asociadas. */
class EventoConOportunidadesException :
    IllegalStateException("El evento tiene oportunidades asociadas")

class EventoRepository(
    private val dao: EventoDao,
    private val api: EventoApi
) {

    // Eventos con el nombre de su organización, del más próximo al más lejano.
    val eventos: Flow<List<EventoConOrganizacion>> = dao.observarTodos()

    fun eventosDeOrganizacion(organizacionId: Long): Flow<List<EventoEntity>> =
        dao.observarPorOrganizacion(organizacionId)

    suspend fun buscar(id: Long): EventoEntity? = dao.buscar(id)

    /** Crea (id = 0) o actualiza en el servidor y guarda en Room su respuesta. Devuelve el id. */
    suspend fun guardar(item: EventoEntity): Long {
        val guardado = try {
            if (item.id == 0L) api.crear(item.toDto().copy(id = null))
            else api.actualizar(item.id, item.toDto())
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw IllegalStateException("El evento ya no existe")
            throw exception
        }
        dao.upsert(guardado.toEntity())
        return checkNotNull(guardado.id) { "El servidor no devolvió el id" }
    }

    /** El servidor responde 409 si el evento tiene oportunidades. */
    suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        when {
            respuesta.isSuccessful -> eliminarLocal(id)
            respuesta.code() == 409 -> throw EventoConOportunidadesException()
            respuesta.code() == 404 -> {
                eliminarLocal(id)
                throw IllegalStateException("El evento ya no existe")
            }
            else -> throw HttpException(respuesta)
        }
    }

    private suspend fun eliminarLocal(id: Long) {
        try {
            dao.eliminar(id)
        } catch (exception: SQLiteConstraintException) {
            // La copia local aún tiene oportunidades que el servidor ya no tiene;
            // la próxima sincronización las quita junto con el evento.
        }
    }
}
