package com.example.opportunitymanagement.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.opportunitymanagement.data.local.OrganizacionConContacto
import com.example.opportunitymanagement.data.local.OrganizacionDao
import com.example.opportunitymanagement.data.local.OrganizacionEntity
import com.example.opportunitymanagement.data.remote.OrganizacionApi
import com.example.opportunitymanagement.data.remote.toDto
import com.example.opportunitymanagement.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

/** No se puede eliminar una organización que todavía tiene contactos o eventos asociados. */
class OrganizacionEnUsoException :
    IllegalStateException("La organización tiene contactos o eventos asociados")

class OrganizacionRepository(
    private val dao: OrganizacionDao,
    private val api: OrganizacionApi
) {

    // Se actualiza sola cuando cambia la tabla, igual que la lista de oportunidades.
    val organizaciones: Flow<List<OrganizacionEntity>> = dao.obtenerTodas()

    // Para la lista de organizaciones: incluye el contacto principal y el total de contactos.
    val organizacionesConContacto: Flow<List<OrganizacionConContacto>> = dao.observarConContacto()

    suspend fun buscar(id: Long): OrganizacionEntity? = dao.encontrarPorId(id)

    /** Crea (id = 0) o actualiza en el servidor y guarda en Room su respuesta. Devuelve el id. */
    suspend fun guardar(item: OrganizacionEntity): Long {
        val guardada = try {
            if (item.id == 0L) api.crear(item.toDto().copy(id = null))
            else api.actualizar(item.id, item.toDto())
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw IllegalStateException("La organización ya no existe")
            throw exception
        }
        dao.upsert(guardada.toEntity())
        return checkNotNull(guardada.id) { "El servidor no devolvió el id" }
    }

    /** El servidor responde 409 si la organización tiene contactos o eventos. */
    suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        when {
            respuesta.isSuccessful -> eliminarLocal(id)
            respuesta.code() == 409 -> throw OrganizacionEnUsoException()
            respuesta.code() == 404 -> {
                eliminarLocal(id)
                throw IllegalStateException("La organización ya no existe")
            }
            else -> throw HttpException(respuesta)
        }
    }

    private suspend fun eliminarLocal(id: Long) {
        try {
            dao.eliminar(id)
        } catch (exception: SQLiteConstraintException) {
            // La copia local aún tiene contactos o eventos que el servidor ya no tiene;
            // la próxima sincronización los quita junto con la organización.
        }
    }
}
