package com.example.opportunitymanagement.data.repository

import com.example.opportunitymanagement.data.local.SeguimientoConDetalle
import com.example.opportunitymanagement.data.local.SeguimientoDao
import com.example.opportunitymanagement.data.local.SeguimientoEntity
import com.example.opportunitymanagement.data.remote.SeguimientoApi
import com.example.opportunitymanagement.data.remote.toDto
import com.example.opportunitymanagement.data.remote.toEntity
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class SeguimientoRepository(
    private val dao: SeguimientoDao,
    private val api: SeguimientoApi
) {

    /** Historial de la oportunidad, lo más reciente primero. Se actualiza solo. */
    fun historial(oportunidadId: Long): Flow<List<SeguimientoConDetalle>> =
        dao.observarPorOportunidad(oportunidadId)

    /**
     * Registra la acción en el servidor y la guarda en Room. Si viene [nuevoEstado], el servidor
     * también cambia el estado de la oportunidad y agrega el "Cambio de estado" al historial
     * (eso llega a Room con la siguiente sincronización).
     */
    suspend fun registrar(seguimiento: SeguimientoEntity, nuevoEstado: String? = null): Long {
        val registrado = try {
            api.registrar(seguimiento.oportunidadId, seguimiento.toDto(nuevoEstado).copy(id = null))
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw IllegalStateException("La oportunidad ya no existe")
            throw exception
        }
        dao.upsert(registrado.toEntity())
        return checkNotNull(registrado.id) { "El servidor no devolvió el id" }
    }
}
