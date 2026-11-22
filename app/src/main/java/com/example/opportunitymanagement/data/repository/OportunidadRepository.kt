package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.OportunidadDao
import com.example.opportunitymanagement.data.local.OportunidadEntity
import com.example.opportunitymanagement.data.remote.OportunidadApi
import com.example.opportunitymanagement.data.remote.toDto
import com.example.opportunitymanagement.data.remote.toEntity
import retrofit2.HttpException

/**
 * Las pantallas leen Room (se actualiza solo); las escrituras van primero al servidor
 * y después se guarda en Room lo que el servidor respondió.
 * La copia completa la trae SincronizacionRepository.
 */
class OportunidadRepository(
    private val dao: OportunidadDao,
    private val api: OportunidadApi
) {
    val oportunidades = dao.observarTodas()

    suspend fun buscar(id: Long) = dao.buscar(id)

    /**
     * Crea (id = 0) o actualiza. El servidor asigna el id al crear y, si cambió el estado,
     * registra el "Cambio de estado" en el historial a nombre de [usuarioId].
     */
    suspend fun guardar(item: OportunidadEntity, usuarioId: Long?): Long {
        val guardada = try {
            if (item.id == 0L) api.crear(item.toDto().copy(id = null))
            else api.actualizar(item.id, item.toDto(), usuarioId)
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw IllegalStateException("La oportunidad ya no existe")
            throw exception
        }
        dao.upsert(guardada.toEntity())
        return checkNotNull(guardada.id) { "El servidor no devolvió el id" }
    }

    /** Su historial se elimina con ella (en el servidor y en Room). */
    suspend fun eliminar(id: Long) {
        val respuesta = api.eliminar(id)
        when {
            respuesta.isSuccessful -> dao.eliminar(id)
            respuesta.code() == 404 -> {
                dao.eliminar(id)
                throw IllegalStateException("La oportunidad ya no existe")
            }
            else -> throw HttpException(respuesta)
        }
    }
}
